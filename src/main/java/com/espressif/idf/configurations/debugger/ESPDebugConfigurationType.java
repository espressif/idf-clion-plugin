package com.espressif.idf.configurations.debugger;

import com.espressif.idf.configurations.debugger.ui.EspDebugSettingsEditor;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NotNullLazyValue;
import com.jetbrains.cidr.cpp.execution.CMakeAppRunConfiguration;
import com.jetbrains.cidr.cpp.execution.CMakeRunConfigurationType;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class ESPDebugConfigurationType extends CMakeRunConfigurationType
{
	public static final String ID = "com.espressif.idf.configurations.debugger.ESPDebugConfigurationType";
	public static final String DEFAULT_FACTORY_ID = "com.espressif.idf.configurations.debugger.ESPDebugConfigurationFactory";
	public static final String DISPLAY_NAME = "ESP Debug Configuration";
	public static final String DESCRIPTION = "ESP Debug Configuration to debug via JTAG/OpenOCD";
	public static NotNullLazyValue<Icon> LAZY_ICON = NotNullLazyValue.volatileLazy(()-> AllIcons.Actions.StartDebugger);
	private final ConfigurationFactory factory;

	public ESPDebugConfigurationType()
	{
		super(ID, DEFAULT_FACTORY_ID, DISPLAY_NAME, DESCRIPTION, LAZY_ICON);
		factory = this.getFactory();
	}

	@Override
	public SettingsEditor<? extends CMakeAppRunConfiguration> createEditor(@NotNull Project project)
	{
		return new EspDebugSettingsEditor(project, getHelper(project));
	}

	@Override
	protected @NotNull CMakeAppRunConfiguration createRunConfiguration(@NotNull Project project,
			@NotNull ConfigurationFactory configurationFactory)
	{
		return new EspDebugConfiguration(project, factory, "");
	}
}
