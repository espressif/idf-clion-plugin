# ESP-IDF CLion Plugin
ESP-IDF CLion Plugin

The source for the initial start of the plugin was taken from [this](https://github.com/daniel-sullivan/clion-embedded-esp32/) project.

## Configuration and Installation

Please download the release zip file from releases section and unzip it.
Make sure not to unzip the internal Zip file in the archive.

Start the CLion.

* Click on Plugins from the side window<br>
![clion_setup_1.png](docs%2Freadme_images%2Finstall%2Fclion_setup_1.png)

* Next click on the settings wheel on the top and select `Install Plugins from Disk...`<br>
![clion_setup_2.png](docs%2Freadme_images%2Finstall%2Fclion_setup_2.png)

* From the popup window select the zip you extracted and click Ok<br>
![clion_setup_3.png](docs%2Freadme_images%2Finstall%2Fclion_setup_3.png)

* After the installation you will be show the plugin installed, but you will have to restart the IDE. So click on the Restart IDE button<br>
![clion_setup_4.png](docs%2Freadme_images%2Finstall%2Fclion_setup_4.png)

At this stage you have installed the plugin but since the plugin is in the beta versions, there are some configurations that are required to be done manually for IDF.

## Setting up the plugin

* You need to download and install the IDF Tools. You can download and install them from the [IDF Page](https://github.com/espressif/esp-idf/releases). Once you have downloaded and installed the tools you can continue on the next step.
* Now Load an example project from the idf folder. For this document we will be using hello_world example.
* Once you have selected and opened the projec you will be given with the following project configuration wizard. Please remove all the existing toolchains from here.<br>
![clion_plugin_setup.png](docs%2Freadme_images%2Fsetup%2Fclion_plugin_setup.png)
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


The added feature to debug using the OpenOCD is the one that will be most useful.
To Use that follow the steps below.

* From `Run` menu click on `Edit Configurations` and click on `+` from the opened window and select `ESP Debug Configuration`.
![clion_plugin_setup_8.png](docs%2Freadme_images%2Fsetup%2Fclion_plugin_setup_8.png)
* Make sure to select the `Executable` and `Target` from the dropdown. Also `Select Board` according to your board.
![clion_plugin_setup_9.png](docs%2Freadme_images%2Fsetup%2Fclion_plugin_setup_9.png)
After you have selected the board and configured other things accordingly click Ok
* Now you can click on debug or press `Shift+F9` (Default Shortcut to debug on Windows) making sure that the debug configuration you created is selected in the plugin.
When your breakpoint hits you will have the debug view with all the available features in CLion. You will be able to view Threads, stacktrace and also have access to the GDB Console.
![clion_plugin_setup_10.png](docs%2Freadme_images%2Fsetup%2Fclion_plugin_setup_10.png)


