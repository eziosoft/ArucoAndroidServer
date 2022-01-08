import controlP5.*;

ControlP5 cp5;

Chart myChart;

void setupChart() {
  cp5 = new ControlP5(this);
  cp5.printPublicMethodsFor(Chart.class);
  myChart = cp5.addChart("heading")
               .setPosition(-500, -400)
               .setSize(500, 100)
               .setRange(-200, 200)
               .setView(Chart.LINE); // use Chart.LINE, Chart.PIE, Chart.AREA, Chart.BAR_CENTERED
               

  myChart.getColor().setBackground(color(255, 100));
  myChart.setStrokeWeight(1.5);

  myChart.addDataSet("heading");
  myChart.setColors("heading", color(255,0,255),color(255,0,0));
  myChart.setData("heading", new float[400]);
}

void plotHeading(float heading)
{
  myChart.unshift("heading", heading);
}
