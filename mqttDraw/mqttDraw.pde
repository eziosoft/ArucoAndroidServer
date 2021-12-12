import mqtt.*;
ArrayList<PVector> mapPoints;
MQTTClient client;

void setup() {
  client = new MQTTClient(this);
  client.connect("mqtt://192.168.0.19", "processing");

  mapPoints = new ArrayList<PVector>();

  size(640, 360);
  background(102);
}

void draw() {
  stroke(255);
  if (mousePressed == true) {
    line(mouseX, mouseY, pmouseX, pmouseY);
  }


  noFill();
  strokeJoin(ROUND);
  stroke(0, 0, 255, 100);
  strokeWeight(10);

  beginShape();
  for (PVector p : mapPoints) {
    vertex(p.x, p.y);
  }
  endShape();
}

void keyPressed() {
  client.publish("/hello", "world");
}

void clientConnected() {
  println("client connected");

  client.subscribe("map");
}

void messageReceived(String topic, byte[] payload) {
  //if (topic=="map") {
    loadMap( new String(payload));
  //}
  println("new message: " + topic + " - " + new String(payload));
}

void connectionLost() {
  println("connection lost");
}


void loadMap(String json) {
  println("load map");
  JSONObject jsonObject = parseJSONObject(json);

  var points = jsonObject.getJSONArray("points");


  for (int i = 0; i < points.size(); i++) {
    // Get each object in the array
    JSONObject point = points.getJSONObject(i);

    // Get x,y from position
    int x = point.getInt("x");
    int y = point.getInt("y");

    mapPoints.add(new PVector(x*10, y*10));
  }
}
