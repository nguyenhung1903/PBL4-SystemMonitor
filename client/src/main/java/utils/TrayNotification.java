package utils;

import java.awt.*;
import java.awt.TrayIcon.MessageType;


public class TrayNotification {
    public static MessageType ERROR = MessageType.ERROR;
    public static MessageType WARNING = MessageType.WARNING;
    public static MessageType INFO = MessageType.INFO;
    public static MessageType NONE = MessageType.NONE;

    //Obtain only one instance of the SystemTray object
    SystemTray tray = SystemTray.getSystemTray();

    TrayIcon trayIcon;

    public static Integer time = 3000;
    public static void main(String[] args) {
        new TrayNotification().displayTray("Hello, World", "notification demo", MessageType.INFO);
    }

    public TrayNotification(){
        //If the icon is a file
        Image image = Toolkit.getDefaultToolkit().createImage("src/main/resources/images/icon.png");
        //Alternative (if the icon is on the classpath):
        //Image image = Toolkit.getDefaultToolkit().createImage(getClass().getResource("icon.png"));

        trayIcon = new TrayIcon(image, "Tray Demo");

        //Let the system resize the image if needed
        trayIcon.setImageAutoSize(true);
        //Set tooltip text for the tray icon
        trayIcon.setToolTip("System Monitor");
        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            throw new RuntimeException(e);
        }
    }

    public void displayTray(String caption, String text, MessageType msgType) {
        trayIcon.displayMessage(caption, text, msgType);
    }
}
