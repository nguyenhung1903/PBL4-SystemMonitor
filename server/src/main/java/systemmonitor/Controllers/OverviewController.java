package systemmonitor.Controllers;

import java.awt.*;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import systemmonitor.App;
import systemmonitor.Server.Server;
import systemmonitor.Utilities.DataAccess;
import systemmonitor.Utilities.TrayNotification;

public class OverviewController {

    private Server server;

    private HashSet<String> clientnameHasNotiList = new HashSet<String>();
    // List of Client's InetAddresses
//    private ArrayList<InetAddress> clients;
    // List of client's panes (client's pane is a titled pane)
    private ObservableList<TitledPane> clientPanes = FXCollections.observableArrayList();
    // Details stages of clients - a stage popup when double click on a client's
    // pane
    private ObservableList<Stage> openingStages = FXCollections.observableArrayList();
    // timestep to reload information of client
    private double timestep = 1;
    // Redis connector
    private final DataAccess dataAccess;

    @FXML
    private ScrollPane scrollPane;
    @FXML
    private AnchorPane anchorScrollPane;
    @FXML
    public Button restartBtn;
    @FXML
    public Button stopBtn;

    private final double gap = 50; // distance between two client's panes

    private final TrayNotification tray = new TrayNotification();
    // Constructor
    public OverviewController() {
//        this.clients = new ArrayList<>();
        dataAccess = new DataAccess();
    }

    public void initialize() {
        AttachButtonEvents();

        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(timestep),
                event -> updateClientPane()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    public void setServer(Server server) {
        this.server = server;
    }

    // A new client connects to server
//    public void addClient(InetAddress address) {
////        this.clients.add(address);
//        addClientPane(address.getHostName());
//    }

    // Dynamically add client's panes
    public void addClient(InetAddress inet) {
        TitledPane newTitledPane = new TitledPane();
        newTitledPane.setText(inet.getHostName());

        AnchorPane contentPane = new AnchorPane();
        contentPane.setPrefSize(220, 180);
//        contentPane.setManaged(false);

        // Customize the content of the TitledPane

        // The components:
        Label ipLabel = new Label("IP Address:");
        Text ipText = new Text();
        Label macLabel = new Label("MAC Address:");
        Text macText = new Text();
        Label osLabel = new Label("OS:");
        Text osText = new Text();
        Separator separator = new Separator();
        Label ramLabel = new Label("RAM:");
        Label cpuLabel = new Label("CPU:");
        ProgressBar ramProgressBar = new ProgressBar();
        ProgressBar cpuProgressBar = new ProgressBar();
        Label statusLabel = new Label("Status:");
        Text statusText = new Text("status");


        statusText.setStyle("-fx-fill: #00ff00; -fx-font-weight: bold;");

        // Set size and layout for components:
        ipLabel.setLayoutX(14.0);
        ipLabel.setLayoutY(14.0);
        ipText.setLayoutX(106);
        ipText.setLayoutY(27);

        macLabel.setLayoutX(14.0);
        macLabel.setLayoutY(32.0);
        macText.setLayoutX(106);
        macText.setLayoutY(45);

        osLabel.setLayoutX(14.0);
        osLabel.setLayoutY(50.0);
        osText.setLayoutX(106);
        osText.setLayoutY(63);

        ramLabel.setLayoutX(14.0);
        ramLabel.setLayoutY(90.0);

        cpuLabel.setLayoutX(14.0);
        cpuLabel.setLayoutY(118.0);

        ramProgressBar.setLayoutX(53.0);
        ramProgressBar.setLayoutY(90.0);
        ramProgressBar.setPrefHeight(18.0);
        ramProgressBar.setPrefWidth(130);

        cpuProgressBar.setLayoutX(53.0);
        cpuProgressBar.setLayoutY(116.0);
        cpuProgressBar.setPrefHeight(18.0);
        cpuProgressBar.setPrefWidth(130);

        statusLabel.setLayoutX(14.0);
        statusLabel.setLayoutY(146.0);
        statusText.setLayoutX(60);
        statusText.setLayoutY(159);

        separator.setLayoutY(75.0);
        separator.setPrefHeight(0.0);
        separator.setPrefWidth(220.0);

        contentPane.getChildren().addAll(ipLabel, macLabel, osLabel, ramLabel, cpuLabel,
                ramProgressBar, cpuProgressBar, statusLabel, separator, ipText, macText, osText, statusText);

        // Context menu for new title pane
        ContextMenu contextMenu = new ContextMenu();
        MenuItem openItem = new MenuItem("Open");
        MenuItem disconnectItem = new MenuItem("Disconnect");
        MenuItem banItem = new MenuItem("Ban");
        // Add event: click on open item
        openItem.setOnAction((event) -> {
            openDetails(inet.getHostName());
        });
        // Add event: click on disconnect item
        disconnectItem.setOnAction((event) -> {
            server.disconnectClient(inet);
        });
        // Add event: click on ban item
        banItem.setOnAction((event) -> {
            server.addToBlackList(dataAccess.getMAC(inet.getHostName()));
            server.disconnectClient(inet);
        });
        contextMenu.getItems().addAll(openItem, disconnectItem, banItem);

        // Add event: double-click on a TitledPane
        newTitledPane.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
                    if (mouseEvent.getClickCount() == 2) {
                        openDetails(inet.getHostName());
                    }
                }
            }
        });

        newTitledPane.setContextMenu(contextMenu);
        newTitledPane.setContent(contentPane);

        // Set the position for the new TitledPane
        // Calculate the position based on the number of existing panes.
        double xc, yc;
        if (clientPanes.size() % 2 == 0) {
            xc = 14;
            yc = 14 + (180 + gap) * (clientPanes.size()) / 2;
        } else {
            xc = 14 + (200 + gap);
            yc = 14 + (180 + gap) * (int) ((clientPanes.size()) / 2);
        }

        newTitledPane.setLayoutX(xc);
        newTitledPane.setLayoutY(yc);

        // Add the new TitledPane to the existing AnchorPane
        clientPanes.add(newTitledPane);
        anchorScrollPane.getChildren().add(newTitledPane);
    }

    // Pop up details stage (details.fxml form)
    private void openDetails(String clientName) {
        FXMLLoader fxmlLoader = new FXMLLoader(
                App.class.getResource("details" + ".fxml"));
        Stage stage = new Stage();
        try {
            Parent parent = fxmlLoader.load();
            DetailsController dc = fxmlLoader.getController();
            dc.setDL(clientName, dataAccess);
            dc.start();

            stage.setScene(new Scene(parent));
            stage.getIcons().add(new Image("file:src/main/resources/assets/imgs/client.png"));
            stage.setTitle(clientName);
            stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent t) {
                    openingStages.remove(stage);
                }
            });
            openingStages.add(stage);
            stage.setResizable(false);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void AttachButtonEvents() {
        // Attach event for restart button
        restartBtn.setOnAction((event) -> {
            if (server.getState() == Thread.State.TERMINATED) {
                clientnameHasNotiList = new HashSet<>();
                server = new Server(tray);
                server.setController(this);
                server.start();
                if (stopBtn.isDisable())
                    stopBtn.setDisable(false);
            } else {
                server.stopServer();
                try {
                    server.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                server = new Server(tray);
                server.setController(this);
                server.start();
            }
        });

        // Attach event for stop button
        stopBtn.setOnAction((event) -> {
            if (!server.isInterrupted()) {
                server.stopServer();
                try {
                    server.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                stopBtn.setDisable(true);
            }
        });
    }

    // Update panes (client's information) after a step of time
    private void updateClientPane() {
        if (clientPanes.isEmpty())
            return;

        for (TitledPane titledPane : clientPanes) {
            String clientName = titledPane.getText();
            if (titledPane.getContent() instanceof AnchorPane) {
                AnchorPane container = (AnchorPane) titledPane.getContent();
                ProgressBar ramProgressBar = (ProgressBar) container.getChildren().get(5);
                ProgressBar cpuProgressBar = (ProgressBar) container.getChildren().get(6);
                try {
                    ramProgressBar.setProgress(
                            (double) dataAccess.getCurrentMemoryUsage(clientName) / dataAccess.getTotalMem(clientName));
                    cpuProgressBar.setProgress(dataAccess.getCurrentCpuUsage(clientName) / 100);
                } catch (NumberFormatException e) {
                    ramProgressBar.setProgress(0);
                    cpuProgressBar.setProgress(0);
                }

                Text ipText = (Text) container.getChildren().get(9);
                Text macText = (Text) container.getChildren().get(10);
                Text osText = (Text) container.getChildren().get(11);
                Text statusText = (Text) container.getChildren().get(12);
                ipText.setText(dataAccess.getIP(clientName));
                macText.setText(dataAccess.getMAC(clientName));
                String ostext = dataAccess.getOSName(clientName);
                if (ostext.length() > 17) {
                    osText.setText(ostext.substring(0,17) + "...");
                } else {
                    osText.setText(ostext);
                }

                if (dataAccess.getStatus(clientName) != null) {
                    if (dataAccess.getStatus(clientName)) {
                        statusText.setStyle("-fx-fill: #006400; -fx-font-weight: bold");
                        statusText.setText("SAFE");
                        if (clientnameHasNotiList.contains(clientName)) { clientnameHasNotiList.remove(clientName); }

                    } else {
                        if (!clientnameHasNotiList.contains(clientName)) {
                            tray.displayTray(clientName + ": Warning", clientName + " is in danger!", TrayNotification.WARNING);
                            clientnameHasNotiList.add(clientName);
                        }
                        statusText.setStyle("-fx-fill: #af0505; -fx-font-weight: bold");
                        statusText.setText("UNSAFE");
                    }
                } else {
                    statusText.setStyle("-fx-fill: #ffdb00; -fx-font-weight: italic");
                    statusText.setText("UNKNOWN");
                }

            }
        }
    }

    // A client is disconnected from server
    public void removeClient(InetAddress address) {
//        this.clients.remove(address);
        removeClientPane(address.getHostName());
        removeClientDetailsStage(address.getHostName());
    }

    // Remove the client's pane
    private void removeClientPane(String clientName) {
        for (TitledPane titledPane : clientPanes) {
            if (titledPane.getText().equals(clientName)) {
                anchorScrollPane.getChildren().remove(titledPane);
                clientPanes.remove(titledPane);
                break;
            }
        }
        relocationPanes();
    }

    // Relocation the others
    private void relocationPanes() {

        for (int i = 0; i < clientPanes.size(); i++) {
            double xc, yc;
            if (i % 2 == 0) {
                xc = 14;
                yc = 14 + (180 + gap) * i / 2;
            } else {
                xc = 14 + (200 + gap);
                yc = 14 + (180 + gap) * (int) (i / 2);
            }
            clientPanes.get(i).setLayoutX(xc);
            clientPanes.get(i).setLayoutY(yc);
        }
    }

    // Close the stage which belongs to that client (if it is opening)
    private void removeClientDetailsStage(String clientName) {
        for (Stage stage : openingStages) {
            if (stage.getTitle().equals(clientName)) {
                stage.close();
                openingStages.remove(stage);
                break;
            }
        }
    }
}
