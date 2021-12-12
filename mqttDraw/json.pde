void loadCam(String json) {
  JSONObject camObject = parseJSONObject(json);
  cam.set(camObject.getInt("x"), camObject.getInt("y"));
  camHeading = camObject.getFloat("heading");
  camPath.add(cam.copy());
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

    PVector pv = new PVector(x, y);

    println(pv.toString());
    mapPoints.add(pv);
  }
}
