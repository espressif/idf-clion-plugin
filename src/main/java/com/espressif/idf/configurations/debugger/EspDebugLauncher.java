package com.espressif.idf.configurations.debugger;

import com.espressif.idf.configurations.debugger.openocd.OpenOcdComponent;
import com.espressif.idf.configurations.debugger.openocd.OpenOcdSettingsState;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.CommandLineState;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.filters.Filter;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.ui.ExecutionConsole;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.ThrowableComputable;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugSession;
import com.jetbrains.cidr.cpp.execution.CMakeAppRunConfiguration;
import com.jetbrains.cidr.cpp.execution.debugger.backend.CLionGDBDriverConfiguration;
import com.jetbrains.cidr.cpp.execution.gdbserver.DownloadType;
import com.jetbrains.cidr.cpp.toolchains.CPPDebugger;
import com.jetbrains.cidr.cpp.toolchains.CPPToolchains;
import com.jetbrains.cidr.execution.CidrLauncher;
import com.jetbrains.cidr.execution.debugger.CidrDebuggerPathManager;
import com.jetbrains.cidr.execution.debugger.backend.DebuggerCommandException;
import com.jetbrains.cidr.execution.debugger.backend.DebuggerDriver;
import com.jetbrains.cidr.execution.debugger.remote.CidrRemoteDebugParameters;
import com.jetbrains.cidr.execution.debugger.remote.CidrRemoteGDBDebugProcess;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class EspDebugLauncher extends CidrLauncher
{
	private static final Logger LOG = Logger.getInstance(EspDebugLauncher.class);
	private static final Key<AnAction> RESTART_KEY = Key.create(EspDebugLauncher.class.getName() + "#restartAction");
	private final Project project;
	private final CPPToolchains.Toolchain toolchain;
	private final EspDebugConfiguration espDebugConfiguration;
	private OpenOcdComponent openOcdComponent;

	public EspDebugLauncher(Project project, CPPToolchains.Toolchain toolchain,
			EspDebugConfiguration espDebugConfiguration)
	{
		super();
		this.project = project;
		this.toolchain = toolchain;
		this.espDebugConfiguration = espDebugConfiguration;
		openOcdComponent = new OpenOcdComponent();
		toolchain = CPPToolchains.getInstance().getDefaultToolchain();
	}

	@Override
	protected ProcessHandler createProcess(@NotNull CommandLineState commandLineState) throws ExecutionException
	{
		File runFile = findRunFile(commandLineState);
		findOpenOcdAction(project).stopOpenOcd();
		String targetProfileName = commandLineState.getExecutionTarget().getDisplayName();
		try
		{
			GeneralCommandLine commandLine = OpenOcdComponent.createOcdCommandLine(espDebugConfiguration, runFile,
					"reset", true, targetProfileName);
			OSProcessHandler osProcessHandler = new OSProcessHandler(commandLine);
			osProcessHandler.addProcessListener(new ProcessAdapter()
			{
				@Override
				public void processTerminated(@NotNull ProcessEvent event)
				{
					super.processTerminated(event);
					if (event.getExitCode() == 0)
					{
						Informational.showSuccessfulDownloadNotification(project);
					}
					else
					{
						Informational.showFailedDownloadNotification(project);
					}
				}
			});
			return osProcessHandler;
		}
		catch (ConfigurationException e)
		{
			LOG.error(e);
			Informational.showPluginError(project, e);
			throw new ExecutionException(e);
		}
	}

	@Override
	protected @NotNull XDebugProcess createDebugProcess(@NotNull CommandLineState commandLineState,
			@NotNull XDebugSession xDebugSession) throws ExecutionException
	{
		OpenOcdSettingsState ocdSettingsState = project.getComponent(OpenOcdSettingsState.class);

		CidrRemoteDebugParameters remoteDebugParameters = new CidrRemoteDebugParameters();
		remoteDebugParameters.setSymbolFile(findRunFile(commandLineState).getAbsolutePath());
		remoteDebugParameters.setRemoteCommand("tcp:localhost:" + espDebugConfiguration.getGdbPort());

		if (toolchain == null)
			throw new ExecutionException("Project toolchain is not defined. Please define it in project Settings");

		CPPToolchains.Toolchain toolchainCopy = toolchain.copy();
		File gdbFile = null;
		if (ocdSettingsState.shippedGdb)
		{
			gdbFile = CidrDebuggerPathManager.getBundledGDBBinary();
			CPPDebugger cppDebugger = CPPDebugger.create(CPPDebugger.Kind.CUSTOM_GDB, gdbFile.getAbsolutePath());
			toolchainCopy.setDebugger(cppDebugger);
		}

		CLionGDBDriverConfiguration gdbDriverConfiguration = new CLionGDBDriverConfiguration(project, toolchainCopy);
		xDebugSession.stop();
		CidrRemoteGDBDebugProcess debugProcess = new CidrRemoteGDBDebugProcess(gdbDriverConfiguration,
				remoteDebugParameters, xDebugSession, commandLineState.getConsoleBuilder(), project1 -> new Filter[0]);
		debugProcess.getProcessHandler().addProcessListener(new ProcessAdapter()
		{
			@Override
			public void processWillTerminate(@NotNull ProcessEvent event, boolean willBeDestroyed)
			{
				super.processWillTerminate(event, willBeDestroyed);
				findOpenOcdAction(project).stopOpenOcd();
			}
		});
		debugProcess.getProcessHandler().putUserData(RESTART_KEY,
				new AnAction("Reset", "MCU Reset", IconLoader.findIcon("reset.png", EspDebugLauncher.class))
				{
					@Override
					public void actionPerformed(@NotNull AnActionEvent e)
					{
						XDebugSession session = debugProcess.getSession();
						session.pause();
						debugProcess.postCommand(debuggerDriver -> {
							try
							{
								ProgressManager.getInstance().runProcess(() -> {
									while (debuggerDriver.getState() != DebuggerDriver.TargetState.SUSPENDED)
										Thread.yield();
								}, null);
								debuggerDriver.executeInterpreterCommand("monitor reset init");
								session.resume();
							}
							catch (DebuggerCommandException debuggerCommandException)
							{
								LOG.error(debuggerCommandException);
								Informational.showFailedDownloadNotification(project);
							}
						});
					}
				});

		new Thread(() -> {
			while (!debugProcess.getCurrentStateMessage().equals("Connected"))
			{
				try
				{
					Thread.sleep(100);
				}
				catch (InterruptedException e)
				{
					LOG.error(e);
				}
			}

			// Connected so perform initialization
			XDebugSession session = debugProcess.getSession();

			if (espDebugConfiguration.getHAR())
			{
				session.pause();
				debugProcess.postCommand(debuggerDriver -> {
					try
					{
						ProgressManager.getInstance().runProcess(() -> {
							while (debuggerDriver.getState() != DebuggerDriver.TargetState.SUSPENDED)
								Thread.yield();
						}, null);

						// Determine which commands need to be run
						if (espDebugConfiguration.getFlushRegs())
						{
							debuggerDriver.executeInterpreterCommand("flushregs");
						}

						if (espDebugConfiguration.getInitialBreak() && StringUtils.isNotEmpty(
								espDebugConfiguration.getInitialBreakName()))
						{
							debuggerDriver.executeInterpreterCommand("thb " + espDebugConfiguration.getInitialBreakName());
						}

						session.resume();
					}
					catch (DebuggerCommandException exception)
					{
						LOG.error(exception);
					}
				});

			}
		}).start();

		return debugProcess;
	}

	@Override
	public @NotNull XDebugProcess startDebugProcess(@NotNull CommandLineState commandLineState,
			@NotNull XDebugSession xDebugSession) throws ExecutionException
	{
		File runFile = null;
		if (espDebugConfiguration.getDownloadType() != DownloadType.NONE)
		{
			runFile = findRunFile(commandLineState);
			if (espDebugConfiguration.getDownloadType() == DownloadType.UPDATED_ONLY && OpenOcdComponent.isLatestUploaded(
					runFile))
			{
				runFile = null;
			}
		}

		try
		{
			xDebugSession.stop();
			openOcdComponent = findOpenOcdAction(project);
			openOcdComponent.stopOpenOcd();
			String targetProfileName = commandLineState.getExecutionTarget().getDisplayName();
			Future<OpenOcdComponent.STATUS> downloadResult = openOcdComponent.startOpenOcd(espDebugConfiguration,
					runFile, targetProfileName);

			ProgressManager progressManager = ProgressManager.getInstance();
			ThrowableComputable<OpenOcdComponent.STATUS, ExecutionException> process = () -> {
				try
				{
					progressManager.getProgressIndicator().setIndeterminate(true);
					while (true)
					{
						try
						{
							return downloadResult.get(500, TimeUnit.MILLISECONDS);
						}
						catch (TimeoutException ignored)
						{
							ProgressManager.checkCanceled();
						}
					}
				}
				catch (InterruptedException |
						java.util.concurrent.ExecutionException e)
				{
					throw new ExecutionException(e);
				}
			};

			String progressTitle = runFile == null ? "Start OpenOCD" : "Firmware Download";
			OpenOcdComponent.STATUS downloadStatus = progressManager.runProcessWithProgressSynchronously(process,
					progressTitle, true, project);
			if (downloadStatus == OpenOcdComponent.STATUS.FLASH_ERROR)
			{
				downloadResult.cancel(true);
				throw new ExecutionException("OpenOCD cancelled");
			}
			return super.startDebugProcess(commandLineState, xDebugSession);
		}
		catch (ConfigurationException e)
		{
			Informational.showPluginError(project, e);
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void collectAdditionalActions(@NotNull CommandLineState state, @NotNull ProcessHandler processHandler,
			@NotNull ExecutionConsole console, @NotNull List<? super AnAction> actions) throws ExecutionException
	{
		super.collectAdditionalActions(state, processHandler, console, actions);
		AnAction restart = processHandler.getUserData(RESTART_KEY);
		if (restart != null) actions.add(restart);
	}

	private @NotNull File findRunFile(CommandLineState commandLineState) throws ExecutionException
	{
		String targetProfileName = commandLineState.getExecutionTarget().getDisplayName();
		CMakeAppRunConfiguration.BuildAndRunConfigurations runConfiguration = espDebugConfiguration.getBuildAndRunConfigurations(
				targetProfileName);
		if (runConfiguration == null)
		{
			throw new ExecutionException("Target is not defined");
		}
		File runFile = runConfiguration.getRunFile(project);
		if (runFile == null)
		{
			throw new ExecutionException("Run File is not defined for " + runConfiguration);
		}
		if (!runFile.exists() || !runFile.isFile())
		{
			throw new ExecutionException("Invalid run file " + runFile.getAbsolutePath());
		}
		return runFile;
	}

	@Override
	public @NotNull Project getProject()
	{
		return project;
	}

	private OpenOcdComponent findOpenOcdAction(Project project)
	{
		return project.getComponent(OpenOcdComponent.class);
	}
}
