package systemmonitor.Server;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Properties;

import javafx.application.Platform;
import systemmonitor.Controllers.OverviewController;
import systemmonitor.Utilities.TrayNotification;

// import utils.ClientHandler;

public class Server extends Thread {
    private String HOSTNAME;
    private int PORT;
    private int BACK_LOG;
    private ServerSocket serverSocket;

    private HashSet<ClientHandler> clients = new HashSet<ClientHandler>();
    private HashSet<String> blacklist = new HashSet<String>();

    private OverviewController overview;

    private TrayNotification tray;

    public Server(TrayNotification tray) {
        this.tray = tray;
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
                client.sendDisconnectMessage("disconnected");
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
        serverSocket = null;

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

                    tray.displayTray("New client connected", "Client connected: " + clientSocket.getInetAddress(), TrayNotification.INFO);

                    // Create a thread to handle the client's request
                    ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                    clientHandler.start();
                } catch (SocketException e) {
                    System.err.println("Cannot accept client!");
//                    e.printStackTrace();
                }
            }

        } catch (IOException e) {
            System.err.println("Cannot start server!");
//            e.printStackTrace();
        }
    }

    public void stopServer() {
        for (ClientHandler client : clients) {
            client.disconnect();
        }

        if (!serverSocket.isClosed()) {
            try {
                this.interrupt();
                serverSocket.close();
            } catch (IOException e) {
                System.err.println("Cannot close socket!");
            }
        }
    }
    // public static void main(String[] args) throws IOException {
    // Server app = new Server();
    // app.LoadServerConfig("src\\main\\resources\\config\\config.cfg");
    // app.Run();
    // }
}
