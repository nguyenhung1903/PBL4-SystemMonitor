package utils.classes;

public class DiskInfo {
    private String PartitionName;
    private Long TotalSpace;
    private Long FreeSpace;
    private Long UsageSpace;

    public DiskInfo(String partitionName, Long TotalSpace, Long FreeSpace) {
        this.PartitionName = partitionName;
        this.TotalSpace = TotalSpace;
        this.FreeSpace = FreeSpace;
        this.UsageSpace = this.TotalSpace - this.FreeSpace;
    }

    public String getPartitionName() {
        return PartitionName;
    }

    public Long getTotalSpace() {
        return TotalSpace;
    }

    public Long getFreeSpace() {
        return FreeSpace;
    }

    public Long getUsageSpace() {
        return UsageSpace;
    }

    @Override
    public String toString() {
        return "DiskInfo{" +
                "PartitionName='" + PartitionName + '\'' +
                ", TotalSpace=" + TotalSpace +
                ", FreeSpace=" + FreeSpace +
                ", UsageSpace=" + UsageSpace +
                '}';
    }
}
