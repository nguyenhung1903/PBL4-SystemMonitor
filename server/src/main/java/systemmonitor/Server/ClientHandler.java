package systemmonitor.Server;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

import systemmonitor.Utilities.DataAccess;
import systemmonitor.Utilities.Classes.DiskInfo;
import systemmonitor.Utilities.Classes.ProcessInfo;
import systemmonitor.Utilities.TrayNotification;

import java.io.File;
import java.util.concurrent.TimeoutException;

public class ClientHandler extends Thread {
    private final Socket clientSocket;
    private final Server server;
    private String MAC = null;
    private String OSName = null;
    private String CPUModel = null;

//    private OverviewController overview;

    DataAccess dataAccess;

    public ClientHandler(Socket socket, Server server) throws SocketException {
        this.clientSocket = socket;
        clientSocket.setSoTimeout(5000);
        this.server = server;
        this.dataAccess = new DataAccess();
    }

//    public void setController(OverviewController overview) {
//        this.overview = overview;
//        overview.setClientHandler(this);
//    }

    public InetAddress getInetAddress() {
        return clientSocket.getInetAddress();
    }

    @Override
    public void run() {
        try {
            receiveStaticInfo();
            receiveDynamicInfo();
            // receiveObject();
            // receiveFile();
        } catch (SocketTimeoutException e) {
            System.err.println("Take too long for new information.");
            new TrayNotification().displayTray("Client disconnected!", "Client " + clientSocket.getInetAddress() + " disconnected!", TrayNotification.INFO);
            if (!clientSocket.isClosed()) {
                disconnect();
            }
        } catch (SocketException e) {
            System.err.println("Client disconnected!");
            new TrayNotification().displayTray("Client disconnected!", "Client " + clientSocket.getInetAddress() + " disconnected!", TrayNotification.INFO);
            if (!clientSocket.isClosed()) {
                server.writeLog("Client " + clientSocket.getInetAddress() + ": " + e.getMessage());
                disconnect();
            }
        } catch (IOException e) {
            System.err.println("Cannot receive data from client!");
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

    private String GetOSName() {
        try {
            DataInputStream dis = new DataInputStream(clientSocket.getInputStream());
            String OSName = dis.readUTF();
            return OSName;
        } catch (Exception e) {
            System.out.println("The system can't get the CPU Model!");
            return "";
        }
    }

    private String GetCPUModel() {
        try {
            DataInputStream dis = new DataInputStream(clientSocket.getInputStream());
            String GPUModel = dis.readUTF();
            return GPUModel;
        } catch (Exception e) {
            System.err.println("The system can't get the CPU Model!");
            return "";
        }
    }

    private String GetMACAddress() {
        try {
            DataInputStream dis = new DataInputStream(clientSocket.getInputStream());
            DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream());
            String IP = clientSocket.getInetAddress().getHostAddress();
            dos.writeUTF(IP);
            String MAC = dis.readUTF();
            return MAC;
        } catch (Exception e) {
            System.err.println("The system can't get the MAC address!");
            return "";
        }
    }

    private ArrayList<String> Bytes2ArrayList(byte[] b) {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(b);
            ObjectInputStream ois = new ObjectInputStream(bais);
            ArrayList<String> data = (ArrayList<String>) ois.readObject();
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void receiveStaticInfo() {
        this.MAC = GetMACAddress();

        if (server.getBlackList().contains(MAC)) {
            System.out.println("Client " + clientSocket.getInetAddress().getHostName() + " is in blacklist!");
            new TrayNotification().displayTray("Client " + clientSocket.getInetAddress().getHostName() + " is in blacklist!", "Client " + clientSocket.getInetAddress().getHostName() + " is in blacklist!", TrayNotification.INFO);
            sendDisconnectMessage("banned");
            disconnect();
            return;
        } else {
            server.addClient(this);
        }

        this.OSName = GetOSName();
        this.CPUModel = GetCPUModel();

        String clientName = clientSocket.getInetAddress().getHostName();

        // Store data in redis database
        dataAccess.setIP(clientName, clientSocket.getInetAddress().getHostAddress());
        dataAccess.setMAC(clientName, MAC);
        dataAccess.setOSName(clientName, OSName);
        dataAccess.setCPUModel(clientName, CPUModel);
    }

    private void receiveDynamicInfo() throws IOException {
        while (this.clientSocket.isConnected()) {
            DataInputStream dis = new DataInputStream(clientSocket.getInputStream());

            Double CPULoad = dis.readDouble();
            Long MemUsage = dis.readLong();
            Long TotalMem = dis.readLong();

            // ==========================
            double traffic_send = dis.readDouble();
            double traffic_received = dis.readDouble();
            // ==========================

            int diskLen = dis.readInt();
            ArrayList<DiskInfo> diskInfos = new ArrayList<>();
            for (int i = 0; i < diskLen; i++) {
                String[] d = dis.readUTF().split(",");
                diskInfos.add(new DiskInfo(d[0], Long.parseLong(d[1]), Long.parseLong(d[2])));
            }

            int length = dis.readInt();
            byte[] data = new byte[length];
            if (length > 0) {
                dis.readFully(data, 0, length);
            }

            ArrayList<ProcessInfo> processes = ProcessInfo.convert2ArrayListProcessInfo(Bytes2ArrayList(data));

//            for (ProcessInfo p: processes){
//                System.out.println(p.toString());
//            }

            boolean isWarning = dis.readBoolean();

            System.out.println("=========");
            System.out.println("OS: " + OSName + "\nCPU Model: " + CPUModel);
            System.out.println("CPU Load: " + CPULoad);
            System.out.println("Mem: " + MemUsage + "/" + TotalMem + "MB");
            System.out.printf("Traffic send: %.5f\n", traffic_send);
            System.out.printf("Traffic received: %.5f\n", traffic_received);
            System.out.println("Disks: ");

            long TotalStorage = 0;
            for (DiskInfo d : diskInfos) {
                TotalStorage += d.getTotalSpace();
                System.out.println(d.getPartitionName() + " # Disk Space: " + d.getUsageSpace() + "/" + d.getTotalSpace() + "MB");
            }

            System.out.println("MAC: " + MAC + ": " + processes.size() + ": " + isWarning);

            String clientName = clientSocket.getInetAddress().getHostName();

            dataAccess.addCpuUsage(clientName, CPULoad);
            dataAccess.addMemUsage(clientName, MemUsage);
            dataAccess.setTotalMem(clientName, TotalMem);
            dataAccess.setTotalStorage(clientName, TotalStorage);
            dataAccess.addTrafficReceived(clientName, traffic_received);
            dataAccess.addTrafficSend(clientName, traffic_send);
            dataAccess.setProcessList(clientName, processes);
            dataAccess.setStatus(clientName, isWarning);

            System.out.println("=========");
        }
    }

    private void receiveObject() throws Exception {

        while (this.clientSocket.isConnected()) {
            DataInputStream dis = new DataInputStream(clientSocket.getInputStream());

            Double CPULoad = dis.readDouble();
            Long MemUsage = dis.readLong();
            Long TotalMem = dis.readLong();

            int diskLen = dis.readInt();
            ArrayList<DiskInfo> diskInfos = new ArrayList<>();
            for (int i = 0; i < diskLen; i++) {
                String[] d = dis.readUTF().split(",");
                diskInfos.add(new DiskInfo(d[0], Long.parseLong(d[1]), Long.parseLong(d[2])));
            }

            int length = dis.readInt();
            byte[] data = new byte[length];
            if (length > 0) {
                dis.readFully(data, 0, length);
            }

            ArrayList<String> processes = Bytes2ArrayList(data);

            System.out.println("=========");
            System.out.println("OS: " + OSName + "\nCPU Model: " + CPUModel);
            System.out.println("CPU Load: " + CPULoad);
            System.out.println("Mem: " + MemUsage + "/" + TotalMem + "MB");
            System.out.println("Disks: ");
            for (DiskInfo d : diskInfos) {
                System.out.println(d.getPartitionName() + " # Disk Space: " + d.getUsageSpace() + "/" + d.getTotalSpace() + "MB");
            }

            System.out.println("MAC: " + MAC + ": " + processes.size());

            String clientName = clientSocket.getInetAddress().getHostName();

            dataAccess.addCpuUsage(clientName, CPULoad);
            dataAccess.addMemUsage(clientName, MemUsage);

            System.out.println("=========");
        }

    }

    private void receiveFile() {
        try {
            // Input stream to receive data from the client
            InputStream inputStream = clientSocket.getInputStream();
            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
            DataInputStream dataInputStream = new DataInputStream(bufferedInputStream);

            // Read the file name and size from the client
            // dataInputStream.readUTF();

            String fileName = "filename.txt";
            dataInputStream.readLong();

            System.out.println("Receiving file: " + fileName);

            // Output stream to save the received file
            String filePath = "downloads\\" + fileName;

            // Check extention of file
            String fileExtention = filePath.substring(filePath.lastIndexOf('.') + 1);
            // then write file to server's disk
            if (fileExtention.equals("txt"))
                writeTextfile(filePath, dataInputStream);
            else
                writeBinaryfile(filePath, dataInputStream);

            // Close socket

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendDisconnectMessage(String message) {
        try {
            DataOutputStream dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());
            dataOutputStream.writeUTF(message);
        } catch (IOException e) {
            System.err.println("Cannot send disconnect message to client!");
            e.printStackTrace();
        }
    }

    private void writeTextfile(String filePath, DataInputStream dataInputStream) {
        try {
            InputStreamReader in = new InputStreamReader(dataInputStream);
            BufferedReader reader = new BufferedReader(in);
            FileWriter fileWriter = new FileWriter(new File(filePath));
            String line;
            while ((line = reader.readLine()) != null) {
                fileWriter.write(line);
                fileWriter.write("\n");
            }

            // System.out.println("File received successfully.\nSize: " + totalBytesReceived
            // + " bytes");

            // Close streams
            fileWriter.close();
            in.close();
            dataInputStream.close();
        }

        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeBinaryfile(String filePath, DataInputStream dataInputStream) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(filePath);
            byte[] buffer = new byte[1024];
            int bytesRead;
            long totalBytesReceived = 0;

            while ((bytesRead = dataInputStream.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, bytesRead);
                totalBytesReceived += bytesRead;
                // fileSize -= bytesRead;
            }

            System.out.println("File received successfully.\nSize: " + totalBytesReceived + " bytes");

            // Close streams
            fileOutputStream.close();
            dataInputStream.close();
        }

        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        try {
            this.interrupt();
            clientSocket.close();
            server.removeClient(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
