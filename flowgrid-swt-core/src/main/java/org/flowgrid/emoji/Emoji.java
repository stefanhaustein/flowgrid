package org.flowgrid.emoji;

import java.util.Iterator;
import java.util.TreeMap;
import java.util.TreeSet;

public class Emoji implements Comparable<Emoji> {
/**
 * Provides access to a subset of the emojis available on Android by some properties
 * (such as color).
 */

  public enum Property {
    ANIMAL,
    BLUE, BIRD, BOAT, BROWN,
    CIRCLE, 
    SWEETS, 
    EMERGENCY_VEHICLE,
    FOOD, FRUIT, FLOWER, FISH, FLYING,
    GREEN,
    HEART,
    INSECT,
    ISSUES,
    MAMMAL,
    MOTORIZED,
 //   ORANGE,  Removed because too hard to distinguish from red or yellow in some cases.
    PLANT, PURPLE,
    RAIL_VEHICLE, RED, REPTILE, ROAD_VEHICLE,
    SQUARE, SWIMMING_OR_FLOATING,
    TRIANGLE,
    VEHICLE, 
    YELLOW;
    
    public String toString() {
      StringBuilder sb = new StringBuilder();
      for (String s: name().split("_")) {
        if(sb.length() > 0) {
          sb.append(' ');
        }
        sb.append(s.charAt(0) + s.substring(1).toLowerCase());
      }
      return sb.toString();
    }
    
    public long mask() {
      return 1L << ordinal();
    }
  }

  
  private static final long ANIMAL = Property.ANIMAL.mask();
  private static final long BIRD = ANIMAL | Property.BIRD.mask();
  private static final long BLUE = Property.BLUE.mask();
  private static final long BOAT = Property.BOAT.mask() | Property.VEHICLE.mask() | Property.SWIMMING_OR_FLOATING.mask();
  private static final long BROWN = Property.BROWN.mask();
  private static final long CIRCLE = Property.CIRCLE.mask();
  private static final long SWEETS = (Property.FOOD.mask()) | 
                                       Property.SWEETS.mask();
  private static final long EMERGENCY_VEHICLE = Property.ROAD_VEHICLE.mask() | Property.VEHICLE.mask() | Property.MOTORIZED.mask();
  private static final long FISH = ANIMAL | Property.FISH.mask() | Property.SWIMMING_OR_FLOATING.mask();
  private static final long FLOWER = Property.FLOWER.mask() | Property.PLANT.mask();
  private static final long FLYING = Property.FLYING.mask();
  private static final long FOOD = Property.FOOD.mask();
  private static final long FRUIT = Property.FOOD.mask() | Property.PLANT.mask() | Property.FRUIT.mask();
  private static final long GREEN = Property.GREEN.mask();
  private static final long HEART = Property.HEART.mask();
  private static final long INSECT = ANIMAL | Property.INSECT.mask();
  private static final long ISSUES = Property.ISSUES.mask();
  private static final long MAMMAL = ANIMAL | Property.MAMMAL.mask();
  private static final long MOTORIZED = Property.VEHICLE.mask() | Property.MOTORIZED.mask();
//  private static final long ORANGE = Property.ORANGE.mask();
  private static final long PLANT = Property.PLANT.mask();
  private static final long PURPLE = Property.PURPLE.mask();
  private static final long REPTILE = ANIMAL | Property.REPTILE.mask();
  private static final long RAIL_VEHICLE = Property.RAIL_VEHICLE.mask();
  private static final long RED = Property.RED.mask();
  private static final long ROAD_VEHICLE = Property.VEHICLE.mask() | Property.ROAD_VEHICLE.mask();
  private static final long SQUARE = Property.SQUARE.mask();
  private static final long SWIMMING = Property.SWIMMING_OR_FLOATING.mask();
  private static final long TRIANGLE = Property.TRIANGLE.mask();
  private static final long VEHICLE = Property.VEHICLE.mask();
  private static final long YELLOW = Property.YELLOW.mask();

public enum Category implements Iterable<Property> {
  ANIMAL(Property.ANIMAL, Property.BIRD, Property.INSECT, Property.MAMMAL,
      Property.FISH, Property.REPTILE),
  COLOR(Property.BLUE, Property.BROWN, Property.GREEN, Property.RED, Property.PURPLE, Property.YELLOW),
  SHAPE(Property.CIRCLE, Property.HEART, Property.TRIANGLE, Property.SQUARE),
  FOOD(Property.FOOD, Property.SWEETS, Property.FRUIT),
  PLANT(Property.PLANT, Property.FRUIT, Property.FLOWER),
  VEHICLE(Property.VEHICLE, Property.BOAT, Property.EMERGENCY_VEHICLE,
      Property.RAIL_VEHICLE, Property.ROAD_VEHICLE, Property.MOTORIZED),
  OTHER(Property.SWIMMING_OR_FLOATING, Property.FLYING);

  private TreeSet<Property> propertySet = new TreeSet<Property>();
  Category(Property... properties) {
    for (Property p: properties) {
      propertySet.add(p);
    }
  }

  @Override
  public Iterator<Property> iterator() {
    return propertySet.iterator();
  }

  public boolean contains(Property property) {
    return propertySet.contains(property);
  }

  public boolean contains(Emoji emoji) {
    for (Property p: this) {
      if (emoji.is(p)) {
        return true;
      }
    }
    return false;
  }
}


public final int codepoint;
public final long properties;
public final String name;

public static TreeMap<Integer,Emoji> map = new TreeMap<Integer,Emoji>();

private static final void add(int codepoint, long properties, String name) {
  map.put(codepoint, new Emoji(codepoint, properties, name));
}
  
  static {
    add(0x1f300, 0, "cyclone");
    
    add(0x1f315, YELLOW | CIRCLE, "full moon symbol");
    
    add(0x1f330, PLANT | BROWN | FOOD, "chestnut");
    add(0x1f331, GREEN | PLANT, "seedling");
    add(0x1f332, GREEN | PLANT, "evergreen tree");
    add(0x1f333, GREEN | PLANT, "deciduous tree");
    add(0x1f334, GREEN | PLANT, "palm tree");
    add(0x1f335, GREEN | PLANT, "cactus");
    
    add(0x1f337, FLOWER, "tulip");
    add(0x1f338, FLOWER | PURPLE, "cherry blossom");
    add(0x1f339, FLOWER | RED, "rose");
    add(0x1f33a, FLOWER, "hibiscus");
    add(0x1f33b, FLOWER | ISSUES, "sunflower");  // looks like sun
    add(0x1f33c, FLOWER, "blossom");
    add(0x1f33d, PLANT | YELLOW | FOOD, "ear of maize");  
    add(0x1f33e, PLANT | FOOD | ISSUES, "ear of rice");  // Food but hard to recognize
    add(0x1f33f, PLANT, "herb");
    add(0x1f340, PLANT | GREEN, "four leaf clover");
    add(0x1f341, PLANT | RED, "maple leaf");
    add(0x1f342, PLANT | ISSUES, "fallen leaf");
    add(0x1f343, PLANT | GREEN | ISSUES, "leaf fluttering in the wind");
    add(0x1f344, 0, "mushroom");    
    add(0x1f345, RED | PLANT | FOOD, "tomato");
    add(0x1f346, FRUIT | PURPLE, "aubergine");
    add(0x1f347, FRUIT | PURPLE, "grapes");
    add(0x1f348, FRUIT,  "Melon");
    add(0x1f349, FRUIT,  "Watermelon");
    add(0x1f34a, FRUIT,  "Tangerine");
    add(0x1f34b, YELLOW | FRUIT, "lemon");
    add(0x1f34c, YELLOW | FRUIT,  "Banana");
    add(0x1f34d, FOOD | FRUIT,  "Pineapple");
    add(0x1f34e, RED | FRUIT,  "Red apple");
    add(0x1f34f, GREEN | FRUIT,  "Green apple");
    add(0x1f350, GREEN | FRUIT, "pear");
    add(0x1f351, FRUIT,  "Peach");
    add(0x1f352, RED | FRUIT,  "Cherries");
    add(0x1f353, RED | FRUIT,  "Strawberry");
    add(0x1f354, FOOD, "hamburger");
    add(0x1f355, FOOD, "slice of pizza");
    add(0x1f356, FOOD, "Meat on bone");
    add(0x1f357, FOOD, "Poultry leg");
    add(0x1f358, FOOD | ISSUES, "Rice cracker");
    add(0x1f359, FOOD | ISSUES, "Rice ball");
    add(0x1f35a, FOOD, "cooked rice");
    add(0x1f35b, FOOD, "curry and rice");
    add(0x1f35c, FOOD, "streaming bowl");
    add(0x1f35d, FOOD, "spaghetti");
    add(0x1f35e, FOOD, "bread");
    add(0x1f35f, FOOD, "french fries");
    add(0x1f360, FOOD | ISSUES, "roasted sweet potato");  // hard to recognize
    add(0x1f361, FOOD | ISSUES, "dango");
    add(0x1f362, FOOD | ISSUES, "oden");
    add(0x1f363, FOOD | ISSUES, "sushi");
    add(0x1f364, FOOD | ISSUES, "fried shrimp");
    add(0x1f365, FOOD | ISSUES, "fish cake with swirl design");
    add(0x1f366, SWEETS, "soft ice cream");
    add(0x1f367, SWEETS, "shaved ice cream");
    add(0x1f368, SWEETS, "ice cream");
    add(0x1f369, SWEETS, "doghnut");
    add(0x1f36a, SWEETS, "cookie");
    add(0x1f36b, SWEETS, "chocolate bar");
    add(0x1f36c, SWEETS, "candy");
    add(0x1f36d, SWEETS, "lollipop");
    add(0x1f36e, SWEETS, "custard");
    add(0x1f36f, SWEETS, "honey pot");
    add(0x1f370, SWEETS, "shortcake");
    add(0x1f371, FOOD, "bento box");
    add(0x1f372, FOOD, "pot of food");
    add(0x1f373, FOOD, "cooking");

    add(0x1f382, SWEETS, "birthday cake");
    add(0x1f383, PLANT | ISSUES, "jack-o-lantern");
    add(0x1f384, PLANT | GREEN | ISSUES, "christmas tree");
    
    add(0x1f400, MAMMAL, "rat");
    add(0x1f401, MAMMAL, "mouse");
    add(0x1f402, MAMMAL | BROWN, "ox");
    add(0x1f403, MAMMAL, "water buffalo");
    add(0x1f404, MAMMAL, "cow");
    add(0x1f405, MAMMAL, "tiger");
    add(0x1f406, MAMMAL, "leopart");
    add(0x1f407, MAMMAL, "rabbit");
    add(0x1f408, MAMMAL, "cat");
    add(0x1f409, 0, "dragon");
    add(0x1f40a, REPTILE | GREEN, "crocodile");
    add(0x1f40b, MAMMAL | SWIMMING, "whale");
    add(0x1f40c, ANIMAL, "snail");
    add(0x1f40d, ANIMAL, "snake");
    add(0x1f40e, MAMMAL, "horse");
    add(0x1f40f, MAMMAL, "ram");
    add(0x1f410, MAMMAL, "goat");
    add(0x1f411, MAMMAL, "sheep");
    add(0x1f412, MAMMAL | BROWN, "monkey");
    add(0x1f413, BIRD, "rooster");
    add(0x1f414, BIRD, "chicken");
    add(0x1f415, MAMMAL, "dog");
    add(0x1f416, MAMMAL, "pig");
    add(0x1f417, MAMMAL | BROWN, "boar");
    add(0x1f418, MAMMAL, "elephant");
    add(0x1f419, ANIMAL | SWIMMING, "octopus");
    add(0x1f41a, 0, "spiral shell");
    add(0x1f41b, ANIMAL, "bug");
    add(0x1f41c, INSECT, "ant");
    add(0x1f41d, INSECT | FLYING, "honeybee");
    add(0x1f41e, INSECT | FLYING, "lady beetle");
    add(0x1f41f, FISH, "fish");
    add(0x1f420, FISH, "tropical fish");
    add(0x1f421, FISH, "blowfish");
    add(0x1f422, REPTILE | GREEN, "turtle");
    add(0x1f423, BIRD, "hatching chick");
    add(0x1f424, BIRD | YELLOW, "baby chick");
    add(0x1f425, BIRD | YELLOW, "front facing baby chick");
    add(0x1f426, BIRD | FLYING, "bird");
    add(0x1f427, BIRD, "penguin");
    add(0x1f428, MAMMAL, "koala");
    add(0x1f429, MAMMAL, "poodle");
    add(0x1f42a, MAMMAL, "dromedary camel");
    add(0x1f42b, MAMMAL, "bactrian camel");
    add(0x1f42c, MAMMAL | SWIMMING, "dolphin");
    add(0x1f42d, MAMMAL, "mouse face");
    add(0x1f42e, MAMMAL, "cow face");
    add(0x1f42f, MAMMAL, "tiger face");
    add(0x1f430, MAMMAL, "rabbit face");
    add(0x1f431, MAMMAL, "cat face");
//    add(0x1f432, GREEN, "dragon face");
    add(0x1f433, MAMMAL | SWIMMING, "spouting whale");
    add(0x1f434, MAMMAL, "horse face");
    add(0x1f435, MAMMAL | BROWN, "monkey face");
    add(0x1f436, MAMMAL, "dog face");
    add(0x1f437, MAMMAL, "pig face");
    add(0x1f438, REPTILE | GREEN, "frog face");
    add(0x1f439, MAMMAL, "hamster face");
    add(0x1f43a, MAMMAL, "wolf face");
    add(0x1f43b, MAMMAL | BROWN, "bear face");
    add(0x1f43c, MAMMAL, "panda face");
    
    add(0x1f498, HEART, "heart with arrow");
    add(0x1f499, HEART | BLUE, "blue heart");
    add(0x1f49a, HEART | GREEN, "green heart");
    add(0x1f49b, HEART | YELLOW, "yellow heart");
    add(0x1f49c, HEART | PURPLE, "purple heart");
    
    add(0x1f4a1, 0, "electric light bulb");
    
    add(0x1f4a3, 0, "bomb");
    add(0x1f4a4, 0, "sleeping symbol");
    add(0x1f4a5, 0, "collision symbol");
    
    add(0x1f4d0, TRIANGLE, "triangular ruler");
    
    add(0x1f4d7, GREEN, "green book");
    add(0x1f4d8, BLUE, "blue book");
    add(0x1f4d9, 0, "orange book");  // Matches red on android
    
    add(0x1f534, RED | CIRCLE, "large red circle");
    add(0x1f535, BLUE | CIRCLE, "large blue circle");
    add(0x1f536, SQUARE, "large orange diamond");  // Not marked as orange because it matches yellow on Android
    add(0x1f537, SQUARE, "large blue diamond"); // not blue on android
//    add(0x1f539, BLUE | SQUARE, "small blue diamond");  not blue on android
    add(0x1f53a, RED | TRIANGLE, "up-pointing red triangle");
    add(0x1f53b, RED | TRIANGLE, "down-pointing red triangle");
    
    add(0x1f550, 0, "clock face one oclock");
    
    add(0x1f680, VEHICLE | MOTORIZED | FLYING, "rocket");
    add(0x1f681, VEHICLE | MOTORIZED | FLYING, "helicopter");
    add(0x1f682, RAIL_VEHICLE| MOTORIZED, "steam locomotive");
    add(0x1f683, RAIL_VEHICLE | ISSUES, "railway car");
    add(0x1f684, RAIL_VEHICLE | MOTORIZED, "high-speed train");
    add(0x1f685, RAIL_VEHICLE | MOTORIZED, "high-speed train with bullet nose");
    add(0x1f686, RAIL_VEHICLE | MOTORIZED | ISSUES, "train");  // hard to distinguish from bus
    add(0x1f687, RAIL_VEHICLE | MOTORIZED, "metro");
    add(0x1f688, RAIL_VEHICLE | MOTORIZED, "light rail");
    add(0x1f689, 0, "station");
    add(0x1f68a, RAIL_VEHICLE | MOTORIZED, "tram");
    add(0x1f68b, RAIL_VEHICLE | MOTORIZED, "tram car");
    add(0x1f68c, ROAD_VEHICLE | MOTORIZED, "bus");
    add(0x1f68d, ROAD_VEHICLE | MOTORIZED | ISSUES, "oncoming bus"); // hard to distinguish from train
    add(0x1f68e, ROAD_VEHICLE | MOTORIZED | ISSUES, "trolleybus");  // hard to distinguish from rail
    add(0x1f68f, 0, "bus stop");
    add(0x1f690, ROAD_VEHICLE | MOTORIZED, "minibus");
    add(0x1f691, EMERGENCY_VEHICLE, "ambulance");
    add(0x1f692, EMERGENCY_VEHICLE | RED, "fire engine");
    add(0x1f693, EMERGENCY_VEHICLE, "police car");
    add(0x1f694, EMERGENCY_VEHICLE, "oncoming police car");
    add(0x1f695, ROAD_VEHICLE | MOTORIZED, "taxi");
    add(0x1f696, ROAD_VEHICLE | MOTORIZED, "oncoming taxi");
    add(0x1f697, ROAD_VEHICLE | MOTORIZED, "automobile");
    add(0x1f698, ROAD_VEHICLE | MOTORIZED, "oncoming automobile");
    add(0x1f699, ROAD_VEHICLE | MOTORIZED, "recreational vehicle");
    add(0x1f69a, ROAD_VEHICLE | MOTORIZED, "delivery truck");
    add(0x1f69b, ROAD_VEHICLE | MOTORIZED, "articulared lorry");
    add(0x1f69c, ROAD_VEHICLE | MOTORIZED, "tractor");
    add(0x1f69d, RAIL_VEHICLE | MOTORIZED, "monorail");
    add(0x1f69e, RAIL_VEHICLE | MOTORIZED, "mountain railway");
    add(0x1f69f, RAIL_VEHICLE | MOTORIZED, "suspension railway");
    add(0x1f6a0, VEHICLE | ISSUES, "mountain cableway");  // The motor is in the base station
    add(0x1f6a1, VEHICLE | ISSUES, "aerial tramway");     // The motor is in the base station
    add(0x1f6a2, BOAT | MOTORIZED, "ship");
    add(0x1f6a3, BOAT, "rowboat");
    add(0x1f6a4, BOAT | MOTORIZED, "speedboat");
    
    add(0x1f6b2, ROAD_VEHICLE, "bicylce");
  }
  
  public Emoji(int codepoint, long properties, String name) {
    this.codepoint = codepoint;
    this.name = name;
    this.properties = properties;
  }
  
  public boolean is(Property expected) {
    return (properties & expected.mask()) == expected.mask();
  }

  public static boolean isEmoji(String s) {
	int i = 0;
	while (i < s.length()) {
      int codepoint = Character.codePointAt(s, i);
      if (codepoint < 0x1f300 || codepoint > 0x1f6ff) {
    	  return false;
      }
	  i += codepoint >= 0x010000 ? 2 : 1;
	}
	return true;
  }
  
  /**
   * Returns whether any of the characters in s has the expected properties.
   */
  public static boolean contains(String s, Property expected) {
    if (s == null || s.length() == 0) {
      return false;
    }
    int i = 0;
    while (i < s.length()) {
      int codepoint = Character.codePointAt(s, i);
      Emoji e = map.get(codepoint);
      if (e == null) {
        return false;
      }
      if (!e.is(expected)) {
        return false;
      }
      i += codepoint >= 0x010000 ? 2 : 1;
    }
    return true;
  }

  @Override
  public int compareTo(Emoji another) {
    return codepoint - another.codepoint;
  }
}
