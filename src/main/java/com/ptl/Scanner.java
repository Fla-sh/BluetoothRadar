package com.ptl;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

public class Scanner implements Runnable {
    private final String[] gain_sudo_command = {"/bin/bash", "-c", "echo \"instrukcja\" | sudo -S apt-get"};

    private final String[] scan_command = {"/bin/bash", "-c", "sudo /usr/bin/btmgmt find"};
    private final String result_file_name = "result.csv";
    private HashMap<String, ArrayList<Integer>> found_devices;
    private ArrayList<String> scan_result;

    public Scanner(){
        found_devices = new HashMap<>();
        scan_result = new ArrayList<>();
    }

    @Override
    public void run() {
        while(true) {
            this.gainSudo();
            this.scan();
            this.analyseScan();
            this.evaluateAvarnge();
            this.saveResult();
        }
    }

    private void gainSudo(){
        ProcessBuilder processBuilder = new ProcessBuilder().command(gain_sudo_command);
        try {
            Process process = processBuilder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void registerDevice(int rssi, String addres){
        if(found_devices.containsKey(addres)){
            found_devices.get(addres).add(rssi);
        }
        else{
            ArrayList<Integer> list = new ArrayList<>();
            list.add(rssi);
            found_devices.put(addres, list);
        }
    }

    private void scan(){
        ArrayList<String> response = new ArrayList<>();
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command(scan_command);
        try {
            Process process = processBuilder.start();
            InputStreamReader inputStreamReader = new InputStreamReader(process.getInputStream());
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = null;
            while((line = reader.readLine()) != null){
                response.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        scan_result = response;
    }

    private void analyseScan(){
        for (String line: scan_result) {
            String words[] = line.split(" ");
            Integer rssi = 0;
            String address = null;
            for(int i = 0; i < words.length; i++){
                if(words[i].equals("dev_found:")){
                    address = words[i + 1];
                }
                else if(words[i].equals("rssi")){
                    rssi = Integer.valueOf(words[i + 1]);
                }
            }
            if(rssi != 0 && address != null) registerDevice(rssi, address);
        }
    }

    private void evaluateAvarnge(){
        for(String key: found_devices.keySet()){
            Integer sum = 0;
            for(Integer value: found_devices.get(key)) sum += value;
            Integer avg = sum / found_devices.get(key).size();
            ArrayList<Integer> list = new ArrayList<>();
            list.add(avg);
            found_devices.put(key, list);
        }
    }

    private void saveResult(){
        FileWriter fileWriter = null;
        BufferedWriter bufferedWriter = null;
        try {
            fileWriter = new FileWriter(result_file_name);
            bufferedWriter = new BufferedWriter(fileWriter);

            for(String key: found_devices.keySet()){
                String line = key + "," + found_devices.get(key).get(0) + "\n";
                System.out.println(line);
                bufferedWriter.write(line);

            }
            bufferedWriter.write(new SimpleDateFormat("E dd-MM-yyyy   HH:mm:ss").format(new Date()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {

                try {
                    bufferedWriter.close();
                    fileWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

        }
    }
}
