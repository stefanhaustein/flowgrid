package org.flowgrid.model;

import org.flowgrid.model.hutn.HutnObject;
import org.flowgrid.model.hutn.HutnSerializer;

public class TutorialData {
  // Stored directly in operation because these fields are part of the signature.
  public int passedWithStars;  // 0 = not passed.
  public double order = 0; // The "order" of the tutorial in the list.  

  // Stored in tutorial JSON
  public int editableStartRow = 3; // inclusive
  public int editableEndRow = 7;  // exclusive
  public int optimalCellCount = 5;
  public int speed = 50;
  public DisabledMap disabledMenus = new DisabledMap();
  
  public void fromJson(HutnObject json) {
    editableStartRow = json.getInt("editableStartRow");
    editableEndRow = json.getInt("editableEndRow");
    optimalCellCount = json.getInt("optimalCellCount");
    speed = json.getInt("speed", 50);
    disabledMenus = new DisabledMap(json.getJsonObject("disabledMenus"));
  }
  
  public void toJson(HutnSerializer json) {
    json.writeLong("editableStartRow", editableStartRow);
    json.writeLong("editableEndRow", editableEndRow);
    json.writeLong("optimalCellCount", optimalCellCount);
    json.writeLong("speed", speed);
    json.startObject("disabledMenus");
    disabledMenus.toJson(json);
    json.endObject();
  }

  public boolean passed() {
    return passedWithStars > 0;
  }
}
