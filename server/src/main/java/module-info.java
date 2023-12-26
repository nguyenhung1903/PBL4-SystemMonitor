module systemmonitor {
    requires javafx.controls;
    requires javafx.fxml;
    requires redis.clients.jedis;
    requires java.datatransfer;
    requires java.desktop;

    opens systemmonitor to javafx.fxml;
    opens systemmonitor.Controllers to javafx.fxml;
    opens systemmonitor.Utilities.Classes to javafx.fxml, javafx.base;

    exports systemmonitor;
}
