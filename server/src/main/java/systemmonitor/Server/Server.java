package systemmonitor.Server;

import java.io.*;
import java.net.*;
import java.util.Properties;

import javafx.application.Platform;
import systemmonitor.Controllers.overviewController;

// import utils.ClientHandler;

public class Server extends Thread {
    private String HOSTNAME;
    private int PORT;
    private int BACK_LOG;

    private overviewController overview;

    public Server() {
        LoadServerConfig("src\\main\\resources\\config\\config.cfg");
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

    public void setController(overviewController overview) {
        this.overview = overview;
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
                    // System.out.println("Client connected: " +
                    // clientSocket.getInetAddress().getHostName());
                    System.out.println("Client connected: ");

                    Platform.runLater(() -> {
                        // Ensure that overview is not null before calling the method
                        if (overview != null) {
                            overview.addClient(clientSocket.getInetAddress());
                        }
                    });
                    // Create a thread to handle the client's request
                    ClientHandler clientHandler = new ClientHandler(clientSocket);
                    clientHandler.setController(overview);
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
