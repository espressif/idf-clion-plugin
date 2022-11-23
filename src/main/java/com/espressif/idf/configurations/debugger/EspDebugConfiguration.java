package com.espressif.idf.configurations.debugger;

import com.intellij.execution.Executor;
import com.intellij.execution.configurations.CommandLineState;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.RuntimeConfigurationException;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import com.jetbrains.cidr.cpp.execution.CMakeAppRunConfiguration;
import com.jetbrains.cidr.cpp.execution.gdbserver.DownloadType;
import com.jetbrains.cidr.cpp.execution.gdbserver.Utils;
import com.jetbrains.cidr.cpp.toolchains.CPPToolchains;
import com.jetbrains.cidr.execution.CidrCommandLineState;
import com.jetbrains.cidr.execution.CidrExecutableDataHolder;
import org.apache.commons.lang.StringUtils;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.espressif.idf.configurations.debugger.IEspDebuggerConfigurationParams.*;

public class EspDebugConfiguration extends CMakeAppRunConfiguration implements CidrExecutableDataHolder
{
	private static final Logger LOG = Logger.getInstance(EspDebugConfiguration.class);

	private int gdbPort = DEFAULT_GDB_PORT;
	private int telnetPort = DEFAULT_TELNET_PORT;
	private String boardConfigFile;
	private String offset = DEFAULT_PROGRAM_OFFSET;
	private boolean haltOnReset = DEFAULT_HAR;
	private boolean flushRegs = DEFAULT_FLUSH_REGS;
	private boolean initialBreak = DEFAULT_BREAK_FUNCTION;
	private String initialBreakName = DEFAULT_BREAK_FUNCTION_NAME;
	private String espTarget = DEFAULT_ESP_TARGET;
	private String flashVoltage = DEFAULT_FLASH_VOLTAGE;
	private DownloadType downloadType = DownloadType.ALWAYS;
	private IEspDebuggerConfigurationParams.ResetType resetType = DEFAULT_RESET;

	public EspDebugConfiguration(Project project, ConfigurationFactory factory, String name)
	{
		super(project, factory, name);
	}

	@Override
	public @Nullable CommandLineState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment env)
	{
		//TODO check if the toolchain is present use that or create a new
		//		Optional<CPPToolchains.Toolchain> optionalToolchain = CPPToolchains.getInstance().getToolchains().stream().findFirst();
		//		CPPToolchains.Toolchain toolchain = optionalToolchain.isPresent() ? optionalToolchain.get() : createToolChain();
		CPPToolchains.Toolchain toolchain = CPPToolchains.getInstance().getDefaultToolchain();
		EspDebugLauncher espDebugLauncher = new EspDebugLauncher(getProject(), toolchain, this);
		return new CidrCommandLineState(env, espDebugLauncher);
	}

	/**
	 * @return
	 */
	private CPPToolchains.Toolchain createToolChain()
	{
		//TODO creation of the toolchain can be done here as well. needs implementation
		CPPToolchains.Toolchain defaultToolChain = CPPToolchains.getInstance().getDefaultToolchain();
		return null;
	}

	@Override
	public void writeExternal(@NotNull Element parentElement) throws WriteExternalException
	{
		try
		{
			super.writeExternal(parentElement);
			Element element = new Element(TAG_OPENOCD);
			parentElement.addContent(element);
			element.setAttribute(ATTR_GDB_PORT, String.valueOf(gdbPort));
			element.setAttribute(ATTR_TELNET_PORT, String.valueOf(telnetPort));
			if (StringUtils.isNotEmpty(boardConfigFile))
				element.setAttribute(ATTR_BOARD_CONFIG, boardConfigFile);
			element.setAttribute(ATTR_RESET_TYPE, resetType.command);
			element.setAttribute(ATTR_DOWNLOAD_TYPE, downloadType.name());
			element.setAttribute(ATTR_HAR, String.valueOf(haltOnReset));
			element.setAttribute(ATTR_FLUSH_REGS, String.valueOf(flushRegs));
			element.setAttribute(ATTR_BREAK_FUNCTION, String.valueOf(initialBreak));
			element.setAttribute(ATTR_BREAK_FUNCTION_NAME, initialBreakName);
			element.setAttribute(ATTR_ESP_TARGET, espTarget);
			element.setAttribute(ATTR_ESP_FLASH_VOLTAGE, flashVoltage);
		}
		catch (Exception e)
		{
			LOG.error("Error Writing: ", e);
		}
	}

	@Override
	public void readExternal(@NotNull Element parentElement) throws InvalidDataException
	{
		try
		{
			super.readExternal(parentElement);
			Element element = parentElement.getChild(TAG_OPENOCD);
			boardConfigFile = element.getAttributeValue(ATTR_BOARD_CONFIG);
			gdbPort = Integer.parseInt(element.getAttributeValue(ATTR_GDB_PORT));
			telnetPort = Integer.parseInt(element.getAttributeValue(ATTR_TELNET_PORT));
			resetType = ResetType.valueOf(element.getAttributeValue(ATTR_RESET_TYPE));
			haltOnReset = Boolean.parseBoolean(element.getAttributeValue(ATTR_HAR));
			flushRegs = Boolean.parseBoolean(element.getAttributeValue(ATTR_FLUSH_REGS));
			initialBreak = element.getAttributeValue(ATTR_BREAK_FUNCTION) != null ? Boolean.parseBoolean(
					element.getAttributeValue(ATTR_BREAK_FUNCTION)) : DEFAULT_BREAK_FUNCTION;
			downloadType = DownloadType.valueOf(element.getAttributeValue(ATTR_DOWNLOAD_TYPE));
			initialBreakName = element.getAttributeValue(ATTR_BREAK_FUNCTION_NAME, DEFAULT_BREAK_FUNCTION_NAME);
			espTarget = element.getAttributeValue(ATTR_ESP_TARGET);
			flashVoltage = element.getAttributeValue(ATTR_ESP_FLASH_VOLTAGE);
		}
		catch (Exception e)
		{
			LOG.error("Error Reading: ", e);
		}
	}

	@Override
	public void checkConfiguration() throws RuntimeConfigurationException
	{
		super.checkConfiguration();
		Utils.checkPort(gdbPort);
		Utils.checkPort(telnetPort);
		if (gdbPort == telnetPort)
		{
			throw new RuntimeConfigurationException("GDB and Telnet port cannot be same");
		}
		else if (StringUtils.isEmpty(boardConfigFile))
		{
			throw new RuntimeConfigurationException("Board configuration not defined");
		}
	}

	public int getGdbPort()
	{
		return gdbPort;
	}

	public void setGdbPort(int gdbPort)
	{
		this.gdbPort = gdbPort;
	}

	public int getTelnetPort()
	{
		return telnetPort;
	}

	public void setTelnetPort(int telnetPort)
	{
		this.telnetPort = telnetPort;
	}

	public String getBoardConfigFile()
	{
		return boardConfigFile;
	}

	public void setBoardConfigFile(String boardConfigFile)
	{
		this.boardConfigFile = boardConfigFile;
	}
	public DownloadType getDownloadType()
	{
		return downloadType;
	}

	public void setDownloadType(DownloadType downloadType)
	{
		this.downloadType = downloadType;
	}

	public String getOffset()
	{
		return offset;
	}

	public void setOffset(String offset)
	{
		this.offset = offset;
	}

	public boolean getHAR()
	{
		return haltOnReset;
	}

	public void setHAR(boolean haltOnReset)
	{
		this.haltOnReset = haltOnReset;
	}

	public boolean getFlushRegs()
	{
		return flushRegs;
	}

	public void setFlushRegs(boolean flushRegs)
	{
		this.flushRegs = flushRegs;
	}

	public boolean getInitialBreak()
	{
		return initialBreak;
	}

	public void setInitialBreak(boolean initialBreak)
	{
		this.initialBreak = initialBreak;
	}

	public String getInitialBreakName()
	{
		return initialBreakName;
	}

	public void setInitialBreakName(String initialBreakName)
	{
		this.initialBreakName = initialBreakName;
	}

	public String getEspTarget()
	{
		return espTarget;
	}

	public void setEspTarget(String espTarget)
	{
		this.espTarget = espTarget;
	}

	public String getFlashVoltage()
	{
		return flashVoltage;
	}

	public void setFlashVoltage(String flashVoltage)
	{
		this.flashVoltage = flashVoltage;
	}
}
