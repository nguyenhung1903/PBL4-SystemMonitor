package systemmonitor;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Properties;

import utils.GetMAC;
import utils.MessageReader;
import utils.TrayNotification;
import utils.classes.SystemInfo;
import utils.classes.DiskInfo;
import utils.classes.ProcessInfo;

import static java.lang.System.exit;



public class Client {
    Socket clientSocket;
    private String HOSTNAME;
    private int PORT;
    private int TIMEOUT;
    private int DELAY_SEND;

    private String MALWARE_SERVER;
    private int MALWARE_PORT;

    private boolean isWarning = false;

    TrayNotification tray = new TrayNotification();

    private ScanAV scanAV;

    private void LoadConfig(String fileConfig) {
        Properties config = new Properties();
        try {
            InputStream input = new FileInputStream(new File(fileConfig));
            config.load(input);
            this.HOSTNAME = config.getProperty("HOSTNAME");
            this.PORT = Integer.parseInt(config.getProperty("PORT"));
            this.TIMEOUT = Integer.parseInt(config.getProperty("TIMEOUT"));
            this.DELAY_SEND = Integer.parseInt(config.getProperty("DELAY_SEND"));

            this.MALWARE_SERVER =  config.getProperty("MALWARE_SERVER");
            this.MALWARE_PORT = Integer.parseInt(config.getProperty("MALWARE_PORT"));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private ArrayList<ProcessInfo> getAllProcesses(String command) {
        ArrayList<ProcessInfo> processes = new ArrayList<>();
        try {
            String line;
            Process p = Runtime.getRuntime().exec(command);
            p.onExit();
            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while ((line = input.readLine()) != null) {
                processes.add(new ProcessInfo(line));
            }
            input.close();

            return processes;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private byte[] ArrayList2Byte(ArrayList<ProcessInfo> ps) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        ArrayList<String> result = ProcessInfo.convert2ArrayListString(ps);
        oos.writeObject(result);
        byte[] bytes = bos.toByteArray();
        return bytes;
    }

    private void Run() throws IOException {

        scanAV = new ScanAV(tray, this.MALWARE_SERVER, this.MALWARE_PORT);

        SystemInfo s = new SystemInfo();
        try {
            clientSocket = new Socket(this.HOSTNAME, this.PORT);
            DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream());
            DataInputStream dis = new DataInputStream(clientSocket.getInputStream());

            // MAC
            String IP = dis.readUTF();
            String MAC = GetMAC.GetMACAddress(InetAddress.getByName(IP));
            dos.writeUTF(MAC);
            //

            MessageReader messageReader = new MessageReader(clientSocket, this);
            messageReader.start();

            // OS Name
            dos.writeUTF(s.osName());
            dos.writeUTF(s.getCpuModel());

            if (clientSocket.isConnected()){
                System.out.println("Connected to server (" + clientSocket.getRemoteSocketAddress() + ").");
                tray.displayTray("System Monitor", "Connected to server (" + clientSocket.getRemoteSocketAddress() + ").", TrayNotification.INFO);
            }
            while (clientSocket.isConnected()) {
                ArrayList<ProcessInfo> processes = getAllProcesses("src\\main\\resources\\lib\\getProcessProperties.exe");
                processes.sort((o1, o2) -> {
                    if (o1.getProcessName().compareTo(o2.getProcessName()) == 0) {
                        return o1.getPID().compareTo(o2.getPID());
                    } else {
                        return o1.getProcessName().compareTo(o2.getProcessName());
                    }
                });
                scanAV.updateProcess(processes);
                scanAV.scan();
                // CPU and Mem Load
                dos.writeDouble(s.getCpuLoad());
                dos.writeLong(s.getMemUsage());
                dos.writeLong(s.getTotalMem());

                // =================================
                // .... network traffic
                ArrayList<String> traffics = s.getNetworkTraffic(MAC);
                Double traffic_send = Double.parseDouble(traffics.get(0));
                Double traffic_received = Double.parseDouble(traffics.get(1));
                dos.writeDouble(traffic_send * 1000);
                dos.writeDouble(traffic_received * 1000);

                // Disk =============================
                ArrayList<DiskInfo> diskInfos = s.diskInfo();
                dos.writeInt(diskInfos.size());
                for (DiskInfo d : diskInfos) {
                    dos.writeUTF(d.getPartitionName() + "," + d.getTotalSpace() + "," + d.getFreeSpace());
                }
                // ====================================

                // for (ProcessInfo p : processes) {
                //     System.out.println(p.toString());
                // }

                // Processes
                byte[] bytes = ArrayList2Byte(processes);
                dos.writeInt(bytes.length);
                dos.write(bytes);
                dos.flush();

                dos.writeBoolean(scanAV.getStatus());

                Thread.sleep(DELAY_SEND);
            }

        } catch (SocketException e) {
            System.err.println("==============");
            System.err.println(e.getMessage());
            System.err.println("Connection reset. Reconnecting...");
            tray.displayTray("System Monitor", "Connection reset. Reconnecting...", TrayNotification.INFO);
            try {
                Thread.sleep(TIMEOUT);
                this.Run();
            } catch (InterruptedException e1) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            System.err.println("==============");
            System.err.println(e.getMessage());
            System.err.println("Cannot send data to server. Reconnecting...");
            tray.displayTray("System Monitor", "Cannot send data to server. Reconnecting...", TrayNotification.INFO);
            try {
                Thread.sleep(TIMEOUT);
                this.Run();
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (clientSocket != null) {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void stop() {
        if (!clientSocket.isClosed()) {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("Cannot close socket!");
//                e.printStackTrace();
            }
        }

        exit(0);
    }

    public static void main(String[] args) throws IOException {
        Client app = new Client();
        app.LoadConfig("src\\main\\resources\\config\\config.cfg");
        app.Run();
    }
}