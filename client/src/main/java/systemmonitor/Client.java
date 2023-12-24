package systemmonitor;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Properties;

import utils.Sender;
import utils.SystemInfo;
import utils.classes.DiskInfo;
import utils.classes.ProcessInfo;

class GetMAC {
    public static String GetMACAddress(InetAddress ip) {
        try {
            NetworkInterface network = NetworkInterface.getByInetAddress(ip);
            byte[] mac = network.getHardwareAddress();
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < mac.length; i++) {
                builder.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
            }

            return builder.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}

public class Client {
    private String HOSTNAME;
    private int PORT;

    private int TIMEOUT;
    private int DELAY_SEND;

    private boolean isWarning = false;

    private void LoadConfig(String fileConfig) {
        Properties config = new Properties();
        try {
            InputStream input = new FileInputStream(new File(fileConfig));
            config.load(input);
            this.HOSTNAME = config.getProperty("HOSTNAME");
            this.PORT = Integer.parseInt(config.getProperty("PORT"));
            this.TIMEOUT = Integer.parseInt(config.getProperty("TIMEOUT"));
            this.DELAY_SEND = Integer.parseInt(config.getProperty("DELAY_SEND"));

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
        ArrayList <String> result = ProcessInfo.convert2ArrayListString(ps);
        oos.writeObject(result);
        byte[] bytes = bos.toByteArray();
        return bytes;
    }

    private void Run() throws IOException {
        SystemInfo s = new SystemInfo();
        Socket clientSocket = null;
        try {
            clientSocket = new Socket(this.HOSTNAME, this.PORT);
            DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream());
            DataInputStream dis = new DataInputStream(clientSocket.getInputStream());

            // MAC
            String IP = dis.readUTF();
            String MAC = GetMAC.GetMACAddress(InetAddress.getByName(IP));
            dos.writeUTF(MAC);
            //

            // OS Name
            dos.writeUTF(s.osName());
            dos.writeUTF(s.getCpuModel());

            if (clientSocket.isConnected())
                System.out.println("Connected to server (" + clientSocket.getRemoteSocketAddress() + ").");

            while (clientSocket.isConnected()) {
                ArrayList<ProcessInfo> processes = getAllProcesses("src\\main\\resources\\lib\\getProcessProperties.exe");
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

                
                dos.writeBoolean(isWarning);

                Thread.sleep(DELAY_SEND);
            }
        }

        catch (SocketException e) {
            System.err.println("Connection reset. Reconnecting...");
            try {
                Thread.sleep(TIMEOUT);
                this.Run();
            } catch (InterruptedException e1) {
                e.printStackTrace();
            }
        }

        catch (IOException e) {
            try {
                System.err.println("==============");
                 System.err.println(e.getMessage());
                System.err.println("Connection Time out. Reconnecting...");
                Thread.sleep(TIMEOUT);
                this.Run();
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
        }

        catch (InterruptedException e) {
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

    public static void main(String[] args) throws IOException {
        Client app = new Client();
        app.LoadConfig("src\\main\\resources\\config\\config.cfg");
        app.Run();
    }
}