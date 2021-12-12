import mqtt.*;
ArrayList<PVector> mapPoints;
PVector cam = new PVector(0, 0);
int camHeading = 0;
ArrayList<PVector> camPath = new ArrayList<PVector>();
MQTTClient client;

void setup() {
  client = new MQTTClient(this);
  client.connect("mqtt://192.168.0.19", "processing");

  mapPoints = new ArrayList<PVector>();

  size(1000, 800);
  background(0);
}

void draw() {
  background(0);
  translate(width/2, height/2);
  stroke(255);
  //if (mousePressed == true) {
  //  line(mouseX, mouseY, pmouseX, pmouseY);
  //}


  noFill();
  strokeJoin(ROUND);
  stroke(230, 0, 0, 100);
  strokeWeight(2);

  //map
  beginShape();
  for (PVector p : mapPoints) {
    vertex(p.x/10, p.y/10);
  }
  endShape();

  //cam
  stroke(0, 255, 0, 100);
  circle(cam.x/10, cam.y/10, 10);
  line(cam.x/10, cam.y/10,(float)(cam.x/10+10*Math.sin(camHeading)), (float)(cam.y/10+10*Math.cos(camHeading)));
  

  //cam path
  beginShape();
  for (PVector p : camPath) {
    vertex(p.x/10, p.y/10);
  }
  endShape();
}

void clientConnected() {
  println("client connected");

  client.subscribe("map");
  client.subscribe("cam");
}

void messageReceived(String topic, byte[] payload) {
  if (topic.equals("map")) {
    loadMap( new String(payload));
  }

  if (topic.equals("cam")) {
    loadCam( new String(payload));
  }

  println("new message: " + topic + " - " + new String(payload));
}

void connectionLost() {
  println("connection lost");
}
