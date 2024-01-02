package utils.classes;

public class ScanRecord {
    private String ProcessPath;
    private String processName;
    private String result;

    public ScanRecord(String processPath, String processName, String result){
        this.ProcessPath = processPath;
        this.processName = processName;
        this.result = result;
    }

    public String getProcessPath(){
        return ProcessPath;
    }
    public String getProcessName(){
        return processName;
    }

    public String getResult(){
        return result;
    }
}
