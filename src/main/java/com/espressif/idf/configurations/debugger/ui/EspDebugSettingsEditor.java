package com.espressif.idf.configurations.debugger.ui;

import com.espressif.idf.configurations.debugger.EspDebugConfiguration;
import com.espressif.idf.configurations.debugger.IEspDebuggerConfigurationParams;
import com.espressif.idf.configurations.debugger.openocd.OpenOcdSettingsState;
import com.espressif.idf.configurations.debugger.util.EspConfigParser;
import com.intellij.execution.ui.CommonProgramParametersPanel;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.fields.ExtendableTextField;
import com.intellij.ui.components.fields.IntegerField;
import com.intellij.util.ui.GridBag;
import com.jetbrains.cidr.cpp.execution.CMakeAppRunConfiguration;
import com.jetbrains.cidr.cpp.execution.CMakeAppRunConfigurationSettingsEditor;
import com.jetbrains.cidr.cpp.execution.CMakeBuildConfigurationHelper;
import com.jetbrains.cidr.cpp.execution.gdbserver.DownloadType;
import org.jdesktop.swingx.JXRadioGroup;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONArray;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.util.List;
import java.util.Map;

public class EspDebugSettingsEditor extends CMakeAppRunConfigurationSettingsEditor
{
	public static final String SHARE_OPENOCD_SCRIPTS_ESP_CONFIG_JSON = "/share/openocd/scripts/esp-config.json";
	private IntegerField gdbPort;
	private IntegerField telnetPort;
	private JCheckBox harCheck;
	private JCheckBox flushRegsCheck;
	private JCheckBox initialBreakpointCheck;
	private ExtendableTextField initialBreakpointName;
	private String openocdHome;
	private JXRadioGroup<DownloadType> downloadGroup;
	private JComboBox<String> targetSelector;
	private JComboBox<String> flashVoltage;
	private JComboBox<String> boardSelector;
	private Map<String, JSONArray> boardConfigsMap;

	public EspDebugSettingsEditor(@NotNull Project project,
			@NotNull CMakeBuildConfigurationHelper cMakeBuildConfigurationHelper)
	{
		super(project, cMakeBuildConfigurationHelper);
		openocdHome = project.getComponent(OpenOcdSettingsState.class).openOcdHome;
	}

	@Override
	protected void resetEditorFrom(@NotNull CMakeAppRunConfiguration cMakeAppRunConfiguration)
	{
		super.resetEditorFrom(cMakeAppRunConfiguration);

		EspDebugConfiguration espDebugConfiguration = (EspDebugConfiguration) cMakeAppRunConfiguration;
		openocdHome = espDebugConfiguration.getProject().getComponent(OpenOcdSettingsState.class).openOcdHome;

		gdbPort.setText(String.valueOf(espDebugConfiguration.getGdbPort()));
		telnetPort.setText(String.valueOf(espDebugConfiguration.getTelnetPort()));
		downloadGroup.setSelectedValue(espDebugConfiguration.getDownloadType());

		harCheck.setSelected(espDebugConfiguration.getHAR());
		flushRegsCheck.setSelected(espDebugConfiguration.getFlushRegs());
		initialBreakpointCheck.setSelected(espDebugConfiguration.getInitialBreak());
		initialBreakpointName.setText(espDebugConfiguration.getInitialBreakName());
		targetSelector.setSelectedItem(espDebugConfiguration.getEspTarget());
		boardSelector.setSelectedItem(espDebugConfiguration.getBoardConfigFile());
	}

	@Override
	protected void applyEditorTo(@NotNull CMakeAppRunConfiguration cMakeAppRunConfiguration)
			throws ConfigurationException
	{
		super.applyEditorTo(cMakeAppRunConfiguration);
		EspDebugConfiguration espDebugConfiguration = (EspDebugConfiguration) cMakeAppRunConfiguration;

		gdbPort.validateContent();
		telnetPort.validateContent();
		espDebugConfiguration.setGdbPort(gdbPort.getValue());
		espDebugConfiguration.setTelnetPort(telnetPort.getValue());
		espDebugConfiguration.setDownloadType(downloadGroup.getSelectedValue());

		espDebugConfiguration.setHAR(harCheck.isSelected());
		espDebugConfiguration.setFlushRegs(flushRegsCheck.isSelected());
		espDebugConfiguration.setInitialBreak(initialBreakpointCheck.isSelected());
		espDebugConfiguration.setInitialBreakName(initialBreakpointName.getText());
		espDebugConfiguration.setEspTarget(targetSelector.getSelectedItem().toString());
		for (String config : (String[]) boardConfigsMap.get(boardSelector.getSelectedItem()).toArray(new String[0]))
		{
			espDebugConfiguration.setBoardConfigFile(config);
		}
		espDebugConfiguration.setFlashVoltage(flashVoltage.getSelectedItem().toString());
	}

	@Override
	protected void createEditorInner(JPanel panel, GridBag gridBag)
	{
		super.createEditorInner(panel, gridBag);
		EspConfigParser espConfigParser = new EspConfigParser(
				getOpenocdHome().concat(SHARE_OPENOCD_SCRIPTS_ESP_CONFIG_JSON));
		for (Component component : panel.getComponents())
		{
			if (component instanceof CommonProgramParametersPanel)
			{
				component.setVisible(false);//todo get rid of this hack
			}
		}

		panel.add(new JLabel("Select Flash Voltage:"), gridBag.nextLine().next());
		flashVoltage = new JComboBox<>();
		addFlashVoltages(flashVoltage, getOpenocdHome());
		panel.add(flashVoltage, gridBag.next().coverLine());

		panel.add(new JLabel("Select Target:"), gridBag.nextLine().next());
		targetSelector = new JComboBox<>();
		addTargetBoards(targetSelector, getOpenocdHome());
		panel.add(targetSelector, gridBag.next().coverLine());
		targetSelector.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				String selectedTarget = targetSelector.getItemAt(targetSelector.getSelectedIndex());
				boardConfigsMap = espConfigParser.getBoardsConfigs(selectedTarget);
				boardSelector.removeAllItems();
				boardConfigsMap.keySet().forEach(boardSelector::addItem);
				boardSelector.setSelectedItem(boardSelector.getItemAt(0));
			}
		});

		boardConfigsMap = espConfigParser.getBoardsConfigs(targetSelector.getSelectedItem().toString());
		panel.add(new JLabel("Select Board:"), gridBag.nextLine().next());
		boardSelector = new JComboBox<>();
		boardConfigsMap.keySet().forEach(boardSelector::addItem);
		panel.add(boardSelector, gridBag.next().coverLine());

		JPanel portsPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));

		gdbPort = addPortInput(portsPanel, "GDB port", IEspDebuggerConfigurationParams.DEFAULT_GDB_PORT);
		portsPanel.add(Box.createHorizontalStrut(10));

		telnetPort = addPortInput(portsPanel, "Telnet port", IEspDebuggerConfigurationParams.DEFAULT_TELNET_PORT);

		panel.add(portsPanel, gridBag.nextLine().next().coverLine());

		panel.add(createDownloadSelector(), gridBag.nextLine().coverLine());

		panel.add(createGDBSettingsSelector(), gridBag.nextLine().coverLine());
	}

	private void addFlashVoltages(JComboBox<String> flashVoltage, String openocdHome)
	{
		String boardsFile = openocdHome + SHARE_OPENOCD_SCRIPTS_ESP_CONFIG_JSON;
		EspConfigParser espConfigParser = new EspConfigParser(boardsFile);
		List<String> flashVoltages = espConfigParser.getEspFlashVoltages();
		flashVoltages.forEach(flashVoltage::addItem);
	}

	private void addTargetBoards(JComboBox<String> targetSelector, String openocdHome)
	{
		String boardsFile = openocdHome + SHARE_OPENOCD_SCRIPTS_ESP_CONFIG_JSON;
		EspConfigParser espConfigParser = new EspConfigParser(boardsFile);
		List<String> targets = espConfigParser.getEspTargets();
		targets.forEach(targetSelector::addItem);
	}

	@NotNull
	private JPanel createDownloadSelector()
	{
		JPanel downloadPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
		GridLayout downloadGrid = new GridLayout(2, 2);
		downloadPanel.setLayout(downloadGrid);
		downloadPanel.add(new JLabel("Flashing Options:"));
		downloadGroup = new JXRadioGroup<>(DownloadType.values());
		downloadPanel.add(downloadGroup);

		return downloadPanel;
	}

	private JPanel createGDBSettingsSelector()
	{
		JPanel settingsPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
		GridLayout settingsGrid = new GridLayout(2, 4);
		settingsPanel.setLayout(settingsGrid);

		flushRegsCheck = new JCheckBox("Flush registers", IEspDebuggerConfigurationParams.DEFAULT_FLUSH_REGS);
		settingsPanel.add(flushRegsCheck);

		harCheck = new JCheckBox("Halt after reset", IEspDebuggerConfigurationParams.DEFAULT_HAR);
		harCheck.addItemListener(e -> {
			if (e.getStateChange() == ItemEvent.SELECTED)
			{
				initialBreakpointCheck.setVisible(true);
				if (initialBreakpointCheck.isSelected())
					initialBreakpointName.setVisible(true);
			}
			else
			{
				initialBreakpointCheck.setVisible(false);
				initialBreakpointName.setVisible(false);
			}
		});
		settingsPanel.add(harCheck);

		initialBreakpointCheck = new JCheckBox("Break on function",
				IEspDebuggerConfigurationParams.DEFAULT_BREAK_FUNCTION);
		initialBreakpointCheck.addItemListener(e -> {
			if (e.getStateChange() == ItemEvent.SELECTED)
			{
				initialBreakpointName.setVisible(true);
			}
			else
			{
				initialBreakpointName.setVisible(false);
			}
		});
		settingsPanel.add(initialBreakpointCheck);

		initialBreakpointName = new ExtendableTextField(IEspDebuggerConfigurationParams.DEFAULT_BREAK_FUNCTION_NAME);
		settingsPanel.add(initialBreakpointName);

		return settingsPanel;
	}

	private IntegerField addPortInput(JPanel portsPanel, String label, int defaultValue)
	{
		portsPanel.add(new JLabel(label + ": "));
		IntegerField field = new IntegerField(label, 1024, 65535);
		field.setDefaultValue(defaultValue);
		field.setColumns(5);
		portsPanel.add(field);
		return field;
	}

	private ExtendableTextField addOffsetInput(String defaultValue)
	{
		ExtendableTextField field = new ExtendableTextField(defaultValue);
		field.setColumns(5);
		return field;
	}

	private String getOpenocdHome()
	{
		return openocdHome;
	}

}
