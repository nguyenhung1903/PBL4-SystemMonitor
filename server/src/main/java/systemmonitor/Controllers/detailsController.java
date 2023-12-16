package systemmonitor.Controllers;

import java.io.IOException;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.text.Text;
import javafx.util.Duration;
import systemmonitor.Utilities.DataAccess;

public class detailsController {
    private DataAccess dataAccess;
    private String clientName;

    @FXML
    private TitledPane grandPane;

    // GENERAL TAB
    @FXML
    private TextField tfPCname;
    @FXML
    private TextField tfIP;
    @FXML
    private TextField tfMAC;
    @FXML
    private TextField tfOS;
    @FXML
    private TextField tfCPUModel;
    @FXML
    private TextField tfTotalDisk;
    @FXML
    private TextField tfTotalMem;

    // PERFORMANCE TAB
    @FXML
    private AreaChart<String, Number> memoryChart;
    @FXML
    private Text totalmemTxt;
    @FXML
    private Text inusememTxt;
    @FXML
    private AreaChart<String, Number> cpuChart;
    @FXML
    private Text utilizationTxt;
    @FXML
    private Text cpuspeedTxt;
    @FXML
    private AreaChart<String, Number> ethernetChart;
    @FXML
    private Text sendTxt;
    @FXML
    private Text receivedTxt;

    private double memtimeIndex = 1;
    private double memTimestep = 2;
    private int memSample = 100; // <= 100

    private double cputimeIndex = 1;
    private double cpuTimestep = 2;
    private int cpuSample = 100; // <= 100

    private double trafficTimeIndex = 1;
    private double trafficTimestep = 2;
    private int trafficSample = 100; // <= 100

    public void setDL(String clientName, DataAccess dataAccess) {
        this.dataAccess = dataAccess;
        this.clientName = clientName;
        this.grandPane.setText(clientName);
    }

    public void start() throws IOException, InterruptedException {
        initializeGeneral();
        initializeMemChart();
        initializeCpuChart();
        initializeTrafficChart();
    }

    private void initializeGeneral() {
        tfPCname.setText(clientName);
        tfIP.setText(dataAccess.getIP(clientName));
        tfMAC.setText(dataAccess.getMAC(clientName));
        tfOS.setText(dataAccess.getOSName(clientName));
        tfCPUModel.setText(dataAccess.getCPUModel(clientName));
        tfTotalDisk.setText(Long.toString(dataAccess.getTotalStorage(clientName)));
        tfTotalMem.setText(Long.toString(dataAccess.getTotalMem(clientName)));
    }

    private void initializeMemChart() throws IOException, InterruptedException {

        memtimeIndex = -memSample * memTimestep;

        memoryChart.setTitle("MEMORY");
        memoryChart.setLegendVisible(false);

        Long totalMem = dataAccess.getTotalMem(clientName);

        NumberAxis yAxis = (NumberAxis) memoryChart.getYAxis();
        yAxis.setAutoRanging(false);
        yAxis.setLowerBound(0);
        yAxis.setUpperBound(totalMem);
        yAxis.setTickUnit(500);

        XYChart.Series<String, Number> memDataSeries = new XYChart.Series<>();
        memDataSeries.setName("Memory usage (MB)");

        // // Add data points to the series
        memDataSeries.getData().clear();
        memoryChart.getData().clear();

        for (int i = 0; i < memSample - 1; i++) {
            memDataSeries.getData()
                    .add(new XYChart.Data<String, Number>(Double.toString(memtimeIndex += memTimestep), 0));
        }

        Long mem = dataAccess.getCurrentMemoryUsage(clientName);

        totalmemTxt.setText(Long.toString(totalMem));
        inusememTxt.setText(Long.toString(mem));

        memDataSeries.getData()
                .add(new XYChart.Data<String, Number>(Double.toString(memtimeIndex += memTimestep), mem));
        memoryChart.getData().add(memDataSeries);

        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(memTimestep),
                event -> updatememChartData()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void updatememChartData() {
        // Update the chart data
        XYChart.Series<String, Number> memDataSeries = memoryChart.getData().get(0);

        Long mem = dataAccess.getCurrentMemoryUsage(clientName);

        inusememTxt.setText(Long.toString(mem));

        if (memDataSeries.getData().size() > memSample)
            memDataSeries.getData().remove(0);
        memDataSeries.getData()
                .add(new XYChart.Data<String, Number>(Double.toString(memtimeIndex += memTimestep), mem));
    }

    private void initializeCpuChart() throws IOException, InterruptedException {

        cputimeIndex = -cpuSample * cpuTimestep;

        cpuChart.setTitle("CPU");
        cpuChart.setLegendVisible(false);

        NumberAxis yAxis = (NumberAxis) cpuChart.getYAxis();
        yAxis.setAutoRanging(false);
        yAxis.setLowerBound(0);
        yAxis.setUpperBound(100);

        XYChart.Series<String, Number> cpuDataSeries = new XYChart.Series<>();
        cpuDataSeries.setName("CPU (%)");

        // Add data points to the series
        cpuDataSeries.getData().clear();
        cpuChart.getData().clear();

        for (int i = 0; i < cpuSample - 1; i++) {
            cpuDataSeries.getData()
                    .add(new XYChart.Data<String, Number>(Double.toString(cputimeIndex += cpuTimestep), 0));
        }

        Double cpu = dataAccess.getCurrentCpuUsage(clientName);

        utilizationTxt.setText(String.format("%.2f", cpu));
        cpuDataSeries.getData()
                .add(new XYChart.Data<String, Number>(Double.toString(cputimeIndex += cpuTimestep), cpu));
        cpuChart.getData().add(cpuDataSeries);

        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(cpuTimestep),
                event -> updatecpuChartData()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void updatecpuChartData() {
        // Update the chart data
        XYChart.Series<String, Number> cpuDataSeries = cpuChart.getData().get(0);

        Double cpu = dataAccess.getCurrentCpuUsage(clientName);

        utilizationTxt.setText(String.format("%.2f", cpu));

        if (cpuDataSeries.getData().size() > cpuSample)
            cpuDataSeries.getData().remove(0);
        cpuDataSeries.getData()
                .add(new XYChart.Data<String, Number>(Double.toString(cputimeIndex += cpuTimestep), cpu));
    }

    private void initializeTrafficChart() throws IOException, InterruptedException {

        trafficTimeIndex = -trafficSample * trafficTimestep;

        ethernetChart.setTitle("TRAFFIC");
        ethernetChart.setLegendVisible(true);

        NumberAxis yAxis = (NumberAxis) ethernetChart.getYAxis();
        yAxis.setAutoRanging(true);

        XYChart.Series<String, Number> sendSeries = new XYChart.Series<>();
        sendSeries.setName("Send Traffic (Kbps)");

        XYChart.Series<String, Number> receivedSeries = new XYChart.Series<>();
        receivedSeries.setName("Received Traffic (Kbps)");

        for (int i = 0; i < trafficSample - 1; i++) {
            sendSeries.getData()
                    .add(new XYChart.Data<String, Number>(Double.toString(trafficTimeIndex), 0));
            receivedSeries.getData()
                    .add(new XYChart.Data<String, Number>(Double.toString(trafficTimeIndex), 0));
            trafficTimeIndex += trafficTimestep;
        }

        ethernetChart.getData().addAll(sendSeries, receivedSeries);

        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(trafficTimestep),
                event -> updateTrafficChartData(sendSeries, receivedSeries)));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void updateTrafficChartData(XYChart.Series<String, Number> sendSeries,
            XYChart.Series<String, Number> receivedSeries) {
        // Update the chart data for send traffic
        Double currentSendTraffic = dataAccess.getCurrentTrafficSend(clientName);
        sendTxt.setText(String.format("%.2f", currentSendTraffic));

        if (sendSeries.getData().size() > trafficSample) {
            sendSeries.getData().remove(0);
        }
        sendSeries.getData().add(new XYChart.Data<>(Double.toString(trafficTimeIndex), currentSendTraffic));

        // Update the chart data for received traffic
        Double currentReceivedTraffic = dataAccess.getCurrentTrafficReceived(clientName);
        receivedTxt.setText(String.format("%.2f", currentReceivedTraffic));

        if (receivedSeries.getData().size() > trafficSample) {
            receivedSeries.getData().remove(0);
        }
        receivedSeries.getData().add(new XYChart.Data<>(Double.toString(trafficTimeIndex), currentReceivedTraffic));

        trafficTimeIndex += trafficTimestep;
    }
}
