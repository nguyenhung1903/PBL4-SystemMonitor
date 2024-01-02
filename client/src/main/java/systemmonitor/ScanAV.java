package systemmonitor;

import utils.TrayNotification;
import utils.classes.ProcessInfo;
import utils.classes.ScanRecord;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Queue;


class SCAN implements Runnable{
    private ScanAV scanAV;
    private ProcessInfo process;

    public SCAN(ScanAV scanAV, ProcessInfo process){
        this.scanAV = scanAV;
        this.process = process;
    }

    private void sendFile(String path, DataOutputStream dataOutputStream)
            throws Exception
    {
        int bytes = 0;
        // Open the File where he located in your pc
        File file = new File(path);
        FileInputStream fileInputStream = new FileInputStream(file);

        dataOutputStream.writeUTF(file.getName());

        dataOutputStream.writeLong(file.length());
        // Here we  break file into chunks
        byte[] buffer = new byte[4 * 1024];
        while ((bytes = fileInputStream.read(buffer))
                != -1) {
            // Send the file to Server Socket
            dataOutputStream.write(buffer, 0, bytes);
            dataOutputStream.flush();
        }
        // close the file here
        fileInputStream.close();
    }

    private String getResultFromMalwareServer(){
        try {
            // Scan malware
            Socket malwareServer = new Socket(this.scanAV.getMwServer(), this.scanAV.getMwPort());
            DataOutputStream dos = new DataOutputStream(malwareServer.getOutputStream());
            DataInputStream dis = new DataInputStream(malwareServer.getInputStream());
            sendFile(process.getProcessPath(), dos);
            dos.flush();
            String result = dis.readUTF();
            malwareServer.close();
            return result;
        } catch (Exception e) {
            System.out.println("Cannot connect to malware server!"  + e.getMessage());
            return "OK";
        }
    }
    @Override
    public void run() {
        System.out.println("Scanning " + process.getProcessName());
        String result = getResultFromMalwareServer();
        System.out.println("Result: " + process.getProcessName() + " - " + result);
        if (!result.equals("OK")) {
            scanAV.tray.displayTray("["+ process.getProcessName() +"] Warning", "This process (PID:" + process.getPID()
                    + "- path: " + process.getProcessPath() + "is " + result, TrayNotification.ERROR);
        }
        this.scanAV.updateResult(new ScanRecord(process.getProcessPath(), process.getProcessName(), result));
    }
}


public class ScanAV {
    private final Queue<String> qPID;
    private HashSet<ScanRecord> results;
    private HashSet<ProcessInfo> processes;
    public TrayNotification tray;
    private String MALWARE_SERVER;
    private int MALWARE_PORT;
    public ScanAV(TrayNotification tray, String mwServer, int mwPort){
        this.qPID = new java.util.LinkedList<>();
        this.results = new HashSet<>();
        this.processes = new HashSet<>();
        this.tray = tray;
        this.MALWARE_SERVER = mwServer;
        this.MALWARE_PORT = mwPort;
    }

    public String getMwServer(){
        return this.MALWARE_SERVER;
    }

    public Integer getMwPort(){
        return this.MALWARE_PORT;
    }

    public void updateResult(ScanRecord result){
        results.add(result);
    }
    public HashSet<ScanRecord> getResults(){
        return results;
    }

    public void updateProcess(ArrayList<ProcessInfo> ps){
        HashSet<ProcessInfo> temp = new HashSet<>();
        for (ProcessInfo process : ps) {
            if (processes.stream().filter(p -> p.getProcessPath().equals(process.getProcessPath())).count() == 0) {
                temp.add(process);
                qPID.add(process.getPID());
            }
       }
        processes.addAll(temp);
    }

    public void scan(){
        while(!qPID.isEmpty()){
            String PID = qPID.poll();
            for (ProcessInfo process : processes) {
                if(process.getPID().equals(PID)) {
                    Thread sc = new Thread(new SCAN(this, process));
                    sc.start();
                }
            }
        }
    }

    public Boolean getStatus(){
        for (ScanRecord result : results) {
            if(!result.getResult().equals("OK")){
                return false;
            }
        }
        return true;
    }
}
