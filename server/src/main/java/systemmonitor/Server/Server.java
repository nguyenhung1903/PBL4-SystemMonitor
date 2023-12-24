package systemmonitor.Server;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Properties;

import javafx.application.Platform;
import systemmonitor.Controllers.OverviewController;

// import utils.ClientHandler;

public class Server extends Thread {
    private String HOSTNAME;
    private int PORT;
    private int BACK_LOG;

    private HashSet<ClientHandler> clients = new HashSet<ClientHandler>();
    private HashSet<String> blacklist = new HashSet<String>();

    private OverviewController overview;

    public Server() {
        LoadServerConfig("src\\main\\resources\\config\\config.cfg");
        LoadBannedList("src\\main\\resources\\config\\blacklist.txt");
    }

    private void LoadServerConfig(String fileConfig) {
        Properties config = new Properties();
        try {
            InputStream input = new FileInputStream(new File(fileConfig));
            config.load(input);
            this.HOSTNAME = config.getProperty("HOSTNAME");
            this.PORT = Integer.parseInt(config.getProperty("PORT"));
            this.BACK_LOG = Integer.parseInt(config.getProperty("BACK_LOG"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void LoadBannedList(String fileBlacklist) {
        try (BufferedReader br = new BufferedReader(new FileReader(fileBlacklist))) {
            String line;
            // Read each line from the file
            while ((line = br.readLine()) != null) {
                blacklist.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setController(OverviewController overview) {
        this.overview = overview;
        overview.setServer(this);
    }

    public void addClient(ClientHandler client) {
        clients.add(client);
        Platform.runLater(() -> {
            // Ensure that overview is not null before calling the method
            if (overview != null) {
                overview.addClient(client.getInetAddress());
            }
        });
    }

    public void disconnectClient(InetAddress inet) {
        for (ClientHandler client : clients) {
            if (client.getInetAddress().equals(inet)) {
                client.disconnect();
                break;
            }
        }
    }

    public void removeClient(ClientHandler client) {
        clients.remove(client);
        Platform.runLater(() -> {
            // Ensure that overview is not null before calling the method
            if (overview != null) {
                overview.removeClient(client.getInetAddress());
            }
        });
    }

    public HashSet<ClientHandler> getClient() {
        return clients;
    }

    public void addToBlackList(String macAddress) {
        blacklist.add(macAddress);
        try {
            FileWriter fw = new FileWriter("src\\main\\resources\\config\\blacklist.txt", true);
            fw.write(macAddress + "\n");
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public HashSet<String> getBlackList() {
        return blacklist;
    }

    @Override
    public void run() {
        InetAddress address = null;
        ServerSocket serverSocket = null;

        try {
            address = InetAddress.getByName(this.HOSTNAME);
        } catch (Exception e) {
            e.printStackTrace();
            // TODO: Catch exception
        }
        try {
            serverSocket = new ServerSocket(this.PORT, this.BACK_LOG, address);
            serverSocket.setReuseAddress(true);
            System.out.println("Server started at " + this.HOSTNAME + ":" + this.PORT);

            while (!serverSocket.isClosed()) {
                try {
                    Socket clientSocket = serverSocket.accept();

                    System.out.println("Client connected: ");

                    // Create a thread to handle the client's request
                    ClientHandler clientHandler = new ClientHandler(clientSocket, this);
//                    clientHandler.setController(overview);
                    clientHandler.start();
                } catch (Exception e) {
                    e.printStackTrace();
                    // TODO: Catch exception
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            // TODO: Catch exception
        } finally {
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    // TODO: Catch exception
                }
            }
        }
    }

    // public static void main(String[] args) throws IOException {
    // Server app = new Server();
    // app.LoadServerConfig("src\\main\\resources\\config\\config.cfg");
    // app.Run();
    // }
}
