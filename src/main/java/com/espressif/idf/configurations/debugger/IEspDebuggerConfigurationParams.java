package com.espressif.idf.configurations.debugger;

public interface IEspDebuggerConfigurationParams
{
	int DEFAULT_GDB_PORT = 3333;
	int DEFAULT_TELNET_PORT = 4444;
	String DEFAULT_PROGRAM_OFFSET = "0x1000";
	boolean DEFAULT_HAR = true;
	boolean DEFAULT_FLUSH_REGS = true;
	boolean DEFAULT_BREAK_FUNCTION = true;
	String DEFAULT_BREAK_FUNCTION_NAME = "app_main";
	String DEFAULT_ESP_TARGET = "esp32";
	String DEFAULT_FLASH_VOLTAGE = "default";
	String ATTR_ESP_TARGET = "esp_target";
	String ATTR_ESP_FLASH_VOLTAGE = "esp_flash_voltage";
	String ATTR_GDB_PORT = "gdb_port";
	String ATTR_TELNET_PORT = "telnet_port";
	String ATTR_BOARD_CONFIG = "board_config";
	String ATTR_DOWNLOAD_TYPE = "download_type";
	String ATTR_HAR = "halt_on_reset";
	String ATTR_FLUSH_REGS = "flush_regs";
	String ATTR_BREAK_FUNCTION = "break";
	String ATTR_BREAK_FUNCTION_NAME = "break_function";
	String ATTR_RESET_TYPE = "reset_type";
	ResetType DEFAULT_RESET = ResetType.INIT;

	String TAG_OPENOCD = "openocd";


	enum ResetType
	{
		RUN("init;reset run;"),
		INIT("init;reset init;"),
		HALT("init;reset halt"),
		NONE("");

		final String command;

		ResetType(String command){
			this.command = command;
		}

		@Override
		public String toString()
		{
			return super.toString();
		}
	}
}
