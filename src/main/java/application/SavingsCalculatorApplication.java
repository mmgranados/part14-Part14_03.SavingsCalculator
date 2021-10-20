package application;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.text.DecimalFormat;
import java.time.Month;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SavingsCalculatorApplication extends Application {

    @Override
    public void start(Stage window) {
        BorderPane layout = new BorderPane();

//        // set the titles for the axes
//        xAxis.setLabel("Months");
//        yAxis.setLabel("Relative support (%)");

        // Data series for total savings with and without interest
        XYChart.Series noInterest = new XYChart.Series();
        XYChart.Series withInterest = new XYChart.Series();

        // Layout for slider1 section
        BorderPane slider1 = new BorderPane();
        // Creates a slider
        Slider sliderMonthlySavings = new Slider(25, 250, 25);
        sliderMonthlySavings.setShowTickMarks(true);
        sliderMonthlySavings.setShowTickLabels(true);
        sliderMonthlySavings.setMajorTickUnit(25);
        sliderMonthlySavings.setBlockIncrement(25);
        Label labelMonthlySavings = new Label("Monthly savings");
        Label valueMonthlySavings = new Label("Value:");
        slider1.setLeft(labelMonthlySavings);    // Label
        slider1.setCenter(sliderMonthlySavings);    // slider itself
        slider1.setRight(valueMonthlySavings);   // value of the slider


        // Layout for slider2 section
        BorderPane slider2 = new BorderPane();
        // Creates a slider
        Slider sliderInterestRate = new Slider(0, 10, 0);
        sliderInterestRate.setShowTickMarks(true);
        sliderInterestRate.setShowTickLabels(true);
        Label labelInterestRate = new Label("Yearly interest rate");    // Slider title
        Label valueInterestRate = new Label("0");    // Slider value in text
        slider2.setLeft(labelInterestRate);    // Label
        slider2.setCenter(sliderInterestRate);    // slider itself
        slider2.setRight(valueInterestRate);   // value of the slider
        sliderInterestRate.setMajorTickUnit(2);
        sliderInterestRate.setBlockIncrement(1);


        // Make a layout of ui for the sliders section
        VBox sliders = new VBox();
        sliders.getChildren().add(slider1);
        sliders.getChildren().add(slider2);

        NumberAxis xAxis = new NumberAxis(0, 30, 1);
        NumberAxis yAxis = new NumberAxis();

        // arraylist that contains data computed for savings after month X
        // 30 months
        ArrayList<Double> totalSavingsOnMonthX = new ArrayList<>();

        // create the line chart. The values of the chart are given as numbers
        // and it uses the axes we created earlier
        LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Relative support in the years 1968-2008");

        for (int years = 0; years < 31; years++) {
            noInterest.getData().add(new XYChart.Data(years, (12 * years * sliderMonthlySavings.getValue())));
        }

        // make the data series of savings with interest rate
        for (int years = 0; years < 31; years++) {
            withInterest.getData().add(new XYChart.Data(years,
                    sumOfCashFlows(sliderMonthlySavings.getValue()*12, sliderInterestRate.getValue()/100 , years)));
        }


        sliderMonthlySavings.valueProperty().addListener(
                new ChangeListener<Number>() {

                    public void changed(ObservableValue<? extends Number >
                                                observable, Number oldValue, Number newValue)
                    {
                        DecimalFormat strNewValue = new DecimalFormat("#.00");
                        valueMonthlySavings.setText(strNewValue.format(newValue));

                        // Section makes the chart update non-compounding savings when there are changes in monthly cash flow
                        XYChart.Series newNoInterest = new XYChart.Series();

                        for (int years = 0; years < 31; years++) {
                            newNoInterest.getData().add(new XYChart.Data(years, (12 * years * sliderMonthlySavings.getValue())));
                        }

                        // make the data series of savings with no interest rate
                        noInterest.getData().setAll(newNoInterest.getData());

                        XYChart.Series newWithInterest = new XYChart.Series();

                        // Section makes the chart update the compounding savings when there are changes in monthly cash flow
                        for (int years = 0; years < 31; years++) {
                            newWithInterest.getData().add(new XYChart.Data(years,
                                    sumOfCashFlows(sliderMonthlySavings.getValue()*12, sliderInterestRate.getValue()/100 , years)));
                        }

                        withInterest.getData().setAll(newWithInterest.getData());

                        System.out.println(getLineChartValues(lineChart).toString());
                    }


                });

        // Listener for changes in value of slider value
        sliderInterestRate.valueProperty().addListener(
                new ChangeListener<Number>() {

                    public void changed(ObservableValue<? extends Number >
                                                observable, Number oldValue, Number newValue)
                    {
                        DecimalFormat strNewValue = new DecimalFormat("#.00");
                        valueInterestRate.setText(strNewValue.format(newValue));

                        XYChart.Series newWithInterest = new XYChart.Series();

                        // Section makes the chart update the compounding savings when there are changes in interest rate
                        for (int years = 0; years < 31; years++) {
                            newWithInterest.getData().add(new XYChart.Data(years,
                                    sumOfCashFlows(sliderMonthlySavings.getValue()*12, sliderInterestRate.getValue()/100 , years)));
                        }

                        withInterest.getData().setAll(newWithInterest.getData());

                        System.out.println(getLineChartValues(lineChart).toString());
                    }
                });


        // Relative position of elements in BorderPane or main layout
        layout.setCenter(lineChart);
        layout.setTop(sliders);

        lineChart.getData().add(noInterest);
        lineChart.getData().add(withInterest);

        Scene view = new Scene(layout, 640, 480);
        window.setScene(view);
        window.show();

    }

    public static double sumOfCashFlows(double cashFlow, double interest, int duration) {
        ArrayList<Double> cash = new ArrayList<>();
        for (int year = 1; year < duration + 1; year++) {
            double futureVal = cashFlow * Math.pow(1+interest, 1+duration-year); // cashFLow * (1+intRate) ^ duration
            cash.add(futureVal);

        }

        Double totalCash = cash.stream()
                .mapToDouble(d -> d)
                .sum();

        return totalCash;
    }

    private List<Map<Integer, Double>> getLineChartValues(LineChart lineChart) {
        List<Map<Integer, Double>> valuesOfChartLines = new ArrayList<>();

        for (int i = 0; i < lineChart.getData().size(); i++) {

            XYChart.Series data = (XYChart.Series) lineChart.getData().get(i);
            List<XYChart.Data> dataPoints = new ArrayList<>();
            data.getData().stream().forEach(d -> dataPoints.add(XYChart.Data.class.cast(d)));

            Map<Integer, Double> lineValues = new HashMap<>();
            for (XYChart.Data point : dataPoints) {
                int x = (int) point.getXValue();
                double y = 0;

                try {
                    y = (double) point.getYValue();
                } catch (Throwable t) {
                    try {
                        y = (int) point.getYValue();
                    } catch (Throwable t2) {
                    }
                }

                lineValues.put(x, y);
            }

            valuesOfChartLines.add(lineValues);
        }

        return valuesOfChartLines;
    }

    public static void main(String[] args) {
        System.out.println("Hello world!");
        launch(SavingsCalculatorApplication.class);
    }

}
