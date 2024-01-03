package utils;

import systemmonitor.Client;
import utils.classes.ProcessInfo;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;

public class MessageReader extends Thread {
    private final Socket socket;
    private final Client client;

    public MessageReader(Socket socket, Client client) {
        this.socket = socket;
        this.client = client;
    }

    private void DisconnectInterface(String MAC, String Status) throws IOException {
        MAC = MAC.replace("-", "");
        System.out.println(MAC);
        Process p = Runtime.getRuntime().exec("src/main/resources/lib/setNetworkInterfaceStatus.exe " + MAC + " " + Status);

        p.onExit();
        String line;
        BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
        while ((line = input.readLine()) != null) {
            System.out.println(line);
        }

        input.close();

    }

    @Override
    public void run() {
        try {
            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
            while (socket.isConnected()) {
                // Read message from server
                String message = dataInputStream.readUTF();
                if (message.equals("disconnected") || message.equals("banned")) {
                    System.err.println("You are " + message + " from server!");
                    DisconnectInterface(GetMAC.GetMACAddress(socket.getInetAddress()), "disable");
                    client.stop();
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("Cannot read message from server!");
        }
    }
}
