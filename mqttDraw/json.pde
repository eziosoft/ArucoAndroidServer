//{"id":1003,"position3d":{"x":-1430.7463328251413,"y":-186.7221197073276,"z":2789.7509968199747},"rotation":{"x":2.8831558545404157,"y":0.009334305301308632,"z":0.2653751758920899}}

PVector cam = new PVector(0, 0);
float camHeading = 0;
ArrayList<PVector> camPath = new ArrayList<PVector>();


void loadCam(String json) {
  JSONObject camObject = parseJSONObject(json);
  var position3d=camObject.getJSONObject("position3d");
  var rotation=camObject.getJSONObject("rotation");
  cam.set(position3d.getInt("x"), position3d.getInt("y"));
  camHeading = rotation.getFloat("z");
  camPath.add(cam.copy());
}

void loadMap(String json) {
  mapPoints.clear();
  println("load map");
  JSONObject jsonObject = parseJSONObject(json);

  var points = jsonObject.getJSONArray("points");


  for (int i = 0; i < points.size(); i++) {
    // Get each object in the array
    JSONObject point = points.getJSONObject(i);


    // Get x,y from position
    int x = point.getInt("x");
    int y = point.getInt("y");

    PVector pv = new PVector(x, y);

    println(pv.toString());
    mapPoints.add(pv);
  }
}

void publishTarget(float x, float y)
{
  var json = new JSONObject();
  json.setFloat("x", x);
  json.setFloat("y", y);
  client.publish("target", json.toString());
}
