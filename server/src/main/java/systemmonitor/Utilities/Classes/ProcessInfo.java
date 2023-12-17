package systemmonitor.Utilities.Classes;

import java.io.Serializable;
import java.util.ArrayList;

public class ProcessInfo implements Serializable {
    private String PID;
    private String ProcessName;
    private String ProcessPath;

    public ProcessInfo(String PID, String ProcessName, String ProcessPath) {
        this.PID = PID;
        this.ProcessName = ProcessName;
        this.ProcessPath = ProcessPath;
    }

    public ProcessInfo(String p){
        String[] parts = p.split(",");
        this.PID = parts[0];
        this.ProcessName = parts[1];
        this.ProcessPath = parts[2];
    }

    @Override
    public String toString() {
//        return "ProcessInfo{" +
//                "PID='" + PID + '\'' +
//                ", ProcessName='" + ProcessName + '\'' +
//                ", ProcessPath='" + ProcessPath + '\'' +
//                '}';
//    }
        return PID + "," + ProcessName + "," + ProcessPath;
    }
    public static ArrayList<String> convert2ArrayListString(ArrayList<ProcessInfo> processes){
        ArrayList<String> result = new ArrayList<>();
        for (ProcessInfo process : processes) {
            result.add(process.getPID() + "," + process.getProcessName() + "," + process.getProcessPath());
        }
        return result;
    }

    public static ArrayList<ProcessInfo> convert2ArrayListProcessInfo(ArrayList<String> processes){
        ArrayList<ProcessInfo> result = new ArrayList<>();
        for (String process : processes) {
            result.add(new ProcessInfo(process));
        }
        return result;
    }

    public String getPID() {
        return PID;
    }

    public String getProcessName() {
        return ProcessName;
    }

    public String getProcessPath() {
        return ProcessPath;
    }

    public void setPID(String PID) {
        this.PID = PID;
    }

    public void setProcessName(String ProcessName) {
        this.ProcessName = ProcessName;
    }

    public void setProcessPath(String ProcessPath) {
        this.ProcessPath = ProcessPath;
    }
}
