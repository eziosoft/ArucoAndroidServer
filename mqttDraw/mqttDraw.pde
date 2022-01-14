import mqtt.*;
ArrayList<PVector> mapPoints;
MQTTClient client;

int targetX = 0;
int targetY = 0;

int ROOMBA_SIZE = 300;
int WP_SIZE = 100;

void setup() {
  client = new MQTTClient(this);
  client.connect("mqtt://192.168.0.19", "processing");

  mapPoints = new ArrayList<PVector>();

  size(1000, 800);
  background(0);

  setupChart();
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

  //cam path
  stroke(0, 255, 0, 100);
  beginShape();
  for (PVector p : camPath) {
    vertex(p.x/10, p.y/10);
  }
  endShape();

  //mission
  stroke(255, 255, 0, 100);
  beginShape();
  for (PVector p : mission) {
    vertex(p.x/10, p.y/10);
    circle(p.x/10, p.y/10, WP_SIZE/10);
  }
  endShape();

  //target
  stroke(255, 255, 255, 255);
  circle(targetX, targetY, 10);

  //cam
  stroke(0, 255, 0, 255);
  circle(cam.x/10, cam.y/10, ROOMBA_SIZE/10);
  line(cam.x/10, cam.y/10, (float)(cam.x/10+20*Math.sin(camHeading)), (float)(cam.y/10+20*Math.cos(camHeading)));
  plotHeading(degrees(camHeading));
}

void mousePressed() {
  camPath.clear();
  //mouseX, mouseY
  publishTarget((mouseX-width/2)*10, (mouseY-height/2)*10);
  targetX = mouseX-width/2;
  targetY = mouseY-height/2;
}

void keyPressed() {
  mission.clear();
  println("mission clear");
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

  //println("new message: " + topic + " - " + new String(payload));
}

void connectionLost() {
  println("connection lost");
}
