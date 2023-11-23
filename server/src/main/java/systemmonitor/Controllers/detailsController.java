package systemmonitor.Controllers;

import java.io.IOException;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.util.Duration;
import systemmonitor.Utilities.DataAccess;

public class detailsController {
    private DataAccess dataAccess;
    private String clientName;

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
    private AreaChart<String, Number> ethernetChart;

    private double memtimeIndex = 1;
    private double memTimestep = 0.5;
    private int memSample = 100; // <= 100

    private double cputimeIndex = 1;
    private double cpuTimestep = 0.5;
    private int cpuSample = 100; // <= 100

    public void setDL(String clientName, DataAccess dataAccess) {
        this.dataAccess = dataAccess;
        this.clientName = clientName;
    }

    public void start() throws IOException, InterruptedException {
        initializeGeneral();
        initializeMemChart();
        initializeCpuChart();
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
        memoryChart.setTitle("MEMORY");
        memoryChart.setLegendVisible(false);

        NumberAxis yAxis = (NumberAxis) memoryChart.getYAxis();
        yAxis.setAutoRanging(false);
        yAxis.setLowerBound(0);
        yAxis.setUpperBound(16000); // TODO: Max memory of client's ram
        yAxis.setTickUnit(1000);

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
}
