# solar-charger-data-logger
This application helps in logging and monitoring of data received from Solar Chargers on a given IP and network using Modbus TCP/IP protocol.

This application consists of two main parts.
1. Logger Application It is a JavaSE based console application that can be setup to work as a background service on any OS. This application connects with configured solar chargers using Modbus TCP/IP protocol to pull information of predefined registers and log those in CSV and MySQL.
2. Monitor Application This application has been built using JavaFX. This application connects with MySQL to pull information logged by Logger application and display it in a grid format. This application also keeps refreshing the displayed information after a given interval. An export option has also been provided to export data within a given date range for any specific device.

To run and test Monitor Application.
1. Update device connection settings in application.properties file.
2. You can use this windows based MODBus simulator (https://sourceforge.net/projects/easymodbustcpserver/) if you don't have actual device.
3. Open this application in IntelliJ Idea and use "Run" functionality.

Here is full technology stack of complete application:
* JavaSE
* JavaFX
* Modbus TCP/IP protocol implementation by using EasyModbus client (http://easymodbustcp.net/en/).
* MySQL
* File IO

**Note: This git repository only contain limited version of Logger Application for demo only. Other functionality has been removed for copy righted purposes.**

Please contact me via any of below given options to get complete source code of Logger application with any customizations.
* LinkedIn (https://www.linkedin.com/in/zeeshanelahi/)
* Upwork (https://www.upwork.com/fl/zeeshanelahi83)
* Stack Overflow (https://stackoverflow.com/users/2492524/zeeshan-elahi)

**Implementation Details:**

Main.java - It is main file of this application and include complete functionality of connecting with device and pulling information.

Completed version of application also contain logic to initialize ReaderThreads of application for all Solar Chargers that we need to monitor.

As per my understanding application code is very much self explanatory. But, if you still have any confusion. Please add your comment with any further query.