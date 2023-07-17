# ESP-IDF CLion Plugin
ESP-IDF CLion Plugin for ESP-IDF CMake based projects (4.x and above).

The source for the initial start of the plugin was taken from [this](https://github.com/daniel-sullivan/clion-embedded-esp32/) project.

# Table Of Contents

* [ Configuration and Installation ](#config_install) <br>
* [ Building Flashing and Serial Monitor ](#build_flash_monitor)<br>
* [ OpenOCD Debugging ](#openocdDebugging)<br>
* [ Creating New Project ](#creatingNewProject)<br>

<a name="config_install"></a>
## Configuration and Installation

Please download the release zip file from releases section and unzip it.
Make sure not to unzip the internal Zip file in the archive.

Start the CLion.

It is recommended to first configure the CLion using some example project

* Click on Plugins from the side window<br>
![clion_setup_1.png](docs%2Freadme_images%2Finstall%2Fclion_setup_1.png)

* Next click on the settings wheel on the top and select `Install Plugins from Disk...`<br>
![clion_setup_2.png](docs%2Freadme_images%2Finstall%2Fclion_setup_2.png)

* From the popup window select the zip you extracted and click Ok<br>
![clion_setup_3.png](docs%2Freadme_images%2Finstall%2Fclion_setup_3.png)

* After the installation you will be show the plugin installed, but you will have to restart the IDE. So click on the Restart IDE button<br>
![clion_setup_4.png](docs%2Freadme_images%2Finstall%2Fclion_setup_4.png)

**At this stage you have installed the plugin but since the plugin is in the beta versions, there are some configurations that are required to be done manually for IDF.**


* You need to download and install the IDF Tools. You can download and install them from the [IDF Page](https://github.com/espressif/esp-idf/releases). Once you have downloaded and installed the tools you can continue on the next step.
* Now Load an example project from the idf folder. For this document we will be using hello_world example.
* Once you have selected and opened the project you will be given with the following project configuration wizard. Please remove all the existing toolchains from here.<br>
![clion_plugin_setup_1.png](docs%2Freadme_images%2Fsetup%2Fclion_plugin_setup_1.png)
* After that click on `+` Add and select system from the dropdown<br>
![clion_plugin_setup_2.png](docs%2Freadme_images%2Fsetup%2Fclion_plugin_setup_2.png)
* Next step please configure the Paths. The tools are usually installed under the `.espressif` directory in the user home folder.
You can find the tools for the board you are working from there for this example we are using esp32 and the image below shows the paths for those tools.
Next once the paths are configured Click `Next`.<br>
![clion_plugin_setup_3.png](docs%2Freadme_images%2Fsetup%2Fclion_plugin_setup_3.png) 
* Now in the next window configure the IDF_PATH and the Virtual Python env path as shown below in CMake options, replace path according to your idf installation<br>
![clion_plugin_setup_4.png](docs%2Freadme_images%2Fsetup%2Fclion_plugin_setup_4.png)
* Also make sure that in the env variables you add the following shown below. In the bottom section note that in the path variable we have added the bin directory for the xtensa-esp32-elf bin directory from the espressif tools installation directory.<br> 
![clion_plugin_setup_5.png](docs%2Freadme_images%2Fsetup%2Fclion_plugin_setup_5.png)
* Next Press `Ctrl+Alt+S` to open the settings or you can go to settings from the File Menu. Open the settings for Python Interpreter. <br>
![clion_plugin_setup_6.png](docs%2Freadme_images%2Fsetup%2Fclion_plugin_setup_6.png)
Click on `...` and select `python.exe` from the `.espressif/python_env/VERSION_OF_IDF_YOU_INSTALLED_PYTHON_VERSION_ON_YOUR_SYSTEM/Scripts` directory in user home and click Ok<br>
* If all goes well you will be able to see these launch configurations that you can use to build and deploy the application.<br>
![clion_plugin_setup_7.png](docs%2Freadme_images%2Fsetup%2Fclion_plugin_setup_7.png)
<br><br><br>
**These are the basic configurations that are required since the plugin is in beta version at the moment**

<a name="build_flash_monitor"></a>
## Building, Flashing, Serial Monitor
### Building
To build the project you can select app from the launch configuration and click build.
### Flashing
To flash the application you can select flash option from launch configuration and click `Run` button.
To make sure that you are flashing on the correct port you can modify the env variables in flash configuration. 
To do that click on the arrow next to flash configuration from dropdown and click Edit.

![flash_edit.png](docs%2Freadme_images%2Fflash_edit.png) <br>
From the next screen you can add an environment variable as shown below for the port.
![flash_port.png](docs%2Freadme_images%2Fflash_port.png)
### Serial Monitor
The serial monitor for this version is still a work in progress. You cannot run it from the monitor option in 
launch configurations at the moment. Although you can try to use the terminal in the CLion 
to run `idf.py monitor command` but make sure that your CLion is configured to use the default `build` directory since
the default build directory in the CLion is different.

<a name="openocdDebugging"></a>
## OpenOCD Debugging
`ESP Debug Configuration` is the newly added feature to support debugging using the OpenOCD.
Please follow the below steps to configure.

* From the `Run` menu, click on `Edit Configurations` and click on `+` from the opened window and select `ESP Debug Configuration`.
![clion_plugin_setup_8.png](docs%2Freadme_images%2Fsetup%2Fclion_plugin_setup_8.png)
* Make sure to select the `Executable` and `Target` from the dropdown. Also `Select Board` according to your board.
![clion_plugin_setup_9.png](docs%2Freadme_images%2Fsetup%2Fclion_plugin_setup_9.png)
Once you have selected the board and configured other settings, click OK.
* To start debugging, click the `Debug` button or press `Shift+F9` (the default shortcut for debugging on Windows). Make sure that the debug configuration you created is selected in the plugin. When your breakpoint is hit, you will see the debug view with all the available features in CLion. You can view threads, stacktraces, and access the GDB console.

![clion_plugin_setup_10.png](docs%2Freadme_images%2Fsetup%2Fclion_plugin_setup_10.png)

<a name="creatingNewProject"></a>
## Creating New Project

Open CLion and Click on New Project

* After selecting a name for the project click Create.
* Once you are into the project please repeat the CMake configuration steps from the [ Configuration and Installation ](#config_install) section.
* Create a directory called `main` in the project root and add a file `main.c/main.cpp` there. Also create a `CMakeList.txt` in this directory
```cmake
set(SOURCES "main.c")

idf_component_register(SRCS ${SOURCES} INCLUDE_DIRS "")

```

* Now add another CMakeList.txt in the project root
```cmake
cmake_minimum_required(VERSION 3.24)

include($ENV{IDF_PATH}/tools/cmake/project.cmake)
set(CMAKE_C_COMPILER C:/Users/your_user/.espressif/tools/xtensa-esp32-elf/esp-2022r1-11.2.0/xtensa-esp32-elf/bin/xtensa-esp32-elf-gcc.exe)
set(CMAKE_ASM_COMPILER C:/Users/your_user/.espressif/tools/xtensa-esp32-elf/esp-2022r1-11.2.0/xtensa-esp32-elf/bin/xtensa-esp32-elf-g++.exe)

project(esp_idf_demo)
include_directories("src")
```

* To export the environment variables for your development platform, open a CLion terminal and navigate to the `esp-idf` home directory. Then, run the appropriate export script:
  - Windows: `export.ps1`
  - macOS and Linux: `export.sh`
* Now from the terminal you can use `idf.py` commands.
* Set the target using the terminal in CLion `idf.py set-target esp32`
* You can build and flash your project using the `idf.py` commands. If the configurations are correct, you will also see the launch configurations in the dropdown menu. These launch configurations can be used to build, flash, and debug your project. For more information, see  [ Building Flashing and Serial Monitor ](#build_flash_monitor) section.