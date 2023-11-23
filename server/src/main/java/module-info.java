module systemmonitor {
    requires javafx.controls;
    requires javafx.fxml;
    requires redis.clients.jedis;

    opens systemmonitor to javafx.fxml;
    opens systemmonitor.Controllers to javafx.fxml;

    exports systemmonitor;
}
