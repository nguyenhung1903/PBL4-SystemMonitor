package utils;

import systemmonitor.Client;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

public class MessageReader extends Thread {
    private final Socket socket;
    private final Client client;

    public MessageReader(Socket socket, Client client) {
        this.socket = socket;
        this.client = client;
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
                    client.stop();
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("Cannot read message from server!");
        }
    }
}
