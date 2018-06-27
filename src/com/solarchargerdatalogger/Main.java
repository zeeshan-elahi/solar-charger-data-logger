package com.solarchargerdatalogger;

import java.io.*;
import java.net.ConnectException;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.*;

import de.re.easymodbus.modbusclient.ModbusClient;
import org.apache.log4j.Logger;

/**
 * Created by Zeeshan on 13/10/2017.
 * This file is just to demonstrated usage of easymodbus modbus client. Most of the other custom functionality have been omitted.
 * Please go http://easymodbustcp.net/en/ for more details about tis library.
 */
public class Main {

    final static Logger logger = Logger.getLogger(Main.class);

    public static void main(String[] args) throws Exception {
        logger.info("Starting Solar Charger Data Logger Service");
        logger.info("Please make sure you are in same network for device to connect or you have started Modbus Server Simulator.");

        //Create Logs Directory
        File logsDir = new File("logs");

        // if the directory does not exist, create it
        if (!logsDir.exists()) {

            try{
                logsDir.mkdir();
            }
            catch(SecurityException se){
                se.printStackTrace();
            }
        }

        InputStream fileInput = new FileInputStream("./application.properties");
        Properties properties = new Properties();
        properties.load(fileInput);
        fileInput.close();

        //Read deviceCount property to get number of device to connect
        int deviceCount = Integer.parseInt(properties.getProperty("deviceCount"));
        logger.info("Device count: " + deviceCount);

        for(int device = 1; device <= deviceCount; device++){

            int intervalForService =  Integer.parseInt(properties.getProperty("device." + device + ".intervalForService"));

            //Push values to Map to pass to thread.
            Map<String, String> devicePropertiesMap = new TreeMap<>();
            devicePropertiesMap.put("device.number", String.valueOf(device));
            devicePropertiesMap.put("device.name", properties.getProperty("device." + device + ".name"));
            devicePropertiesMap.put("device.IP", properties.getProperty("device." + device + ".IP"));
            devicePropertiesMap.put("device.port", properties.getProperty("device." + device + ".port"));
            devicePropertiesMap.put("device.intervalForService", properties.getProperty("device." + device + ".intervalForService"));
            devicePropertiesMap.put("device.deviceStatus", properties.getProperty("device." + device + ".deviceStatus"));
            devicePropertiesMap.put("device.powerControl", properties.getProperty("device." + device + ".powerControl"));

            if(properties.getProperty("device." + device + ".deviceStatus").equals("1")) {

                //Create a new ModbusClient Object
                ModbusClient modbusClient = new ModbusClient();
                //modbusClient.setConnectionTimeout( (Integer.parseInt(this.deviceMap.get("device.intervalForService")) - 1) * 1000);
                modbusClient.setConnectionTimeout(2000);
                boolean isConnectionClosed = false;

                try {

                    //First create directory for Device
                    String dirPath = "logs/Device" + String.format("%02d", device);
                    File deviceLogsDir = new File(dirPath);

                    // if the directory does not exist, create it
                    if (!deviceLogsDir.exists()) {

                        try{
                            deviceLogsDir.mkdir();
                        }
                        catch(SecurityException se){
                            logger.error(se);
                        }
                    }

                    //We will create new log file everyday.
                    String fileName = new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + ".csv";
                    String filePath = dirPath + "/" + fileName;
                    File file = new File(filePath);

                    //Create the file
                    if (!file.exists()){
                        file.createNewFile();
                        logger.info(filePath + " log file is created!");
                        file = null;

                    }else{
                        //logger.info(filePath + " already exists.");
                    }

                    logger.info("Connecting with device: IP - " + properties.getProperty("device." + device + ".IP") + " Port - " + properties.getProperty("device." + device + ".port"));
                    modbusClient.Connect(properties.getProperty("device." + device + ".IP"), Integer.parseInt(properties.getProperty("device." + device + ".port")));

                    if(modbusClient.isConnected()) {

                        logger.info("Reading Holding Registers for Device #" + device);

                        //ReadHoldingRegiters accept two parameters. i.e.
                        //Parameter one should be address of register from here we need to start reading
                        //Parmeter two should be number of registers to read
                        int[] inputRegisters4101 = modbusClient.ReadHoldingRegisters(4103, 32);
                        logger.info(Arrays.toString(inputRegisters4101));

                        logger.info("Closing connection with device: IP - " + properties.getProperty("device." + device + ".IP") + " Port - " + properties.getProperty("device." + device + ".IP"));
                        modbusClient.Disconnect();
                        isConnectionClosed = true;

                        //As we have stored/copied values of registers starting from 4103 in int array inputRegisters4101
                        //We can use those values as it or convert as per requirement.
                        logger.info("Device #" + device
                                + " -> Registers: " + Arrays.toString(inputRegisters4101));

                        //Convert to String and write to file
                        String registersValues = Arrays.toString(inputRegisters4101);
                        String currentDateTimeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                        StringBuilder stringToWrite = new StringBuilder();
                        stringToWrite.append(currentDateTimeStamp + ", " + registersValues);
                        logger.info("Writing information of Device #" + device + " -> " + stringToWrite.toString());

                        // Assume default encoding.
                        FileWriter fileWriter =
                                new FileWriter(filePath, true);

                        // Always wrap FileWriter in BufferedWriter.
                        BufferedWriter bufferedWriter =
                                new BufferedWriter(fileWriter);

                        // Note that write() does not automatically
                        // append a newline character.
                        bufferedWriter.write(stringToWrite.toString());
                        bufferedWriter.newLine();
                        stringToWrite = null;

                        // Always close files.
                        bufferedWriter.close();
                        fileWriter.close();
                    }

                    modbusClient = null;

                }catch (ConnectException c){
                    logger.error("Unable to connect with device " + properties.getProperty("device." + device + ".name"), c);

                }catch (SocketException s){
                    logger.error("Unable to read registers from device " + properties.getProperty("device." + device + ".name"), s);

                }catch(Exception ex) {
                    logger.error("Reader throw an exception.", ex);
                }finally {
                    if(!isConnectionClosed){
                        try {
                            logger.info("Closing connection with device: IP - " + properties.getProperty("device." + device + ".IP") + " Port - " + properties.getProperty("device." + device + ".IP"));
                            modbusClient.Disconnect();
                            modbusClient = null;
                        } catch (IOException e) {
                            logger.error("Unable to close connection with device " + properties.getProperty("device." + device + ".name"), e);
                        }
                    }
                }

            }else{
                logger.error("Unable to read registers from device " + properties.getProperty("device." + device + ".name"));
            }
        }

    }

}