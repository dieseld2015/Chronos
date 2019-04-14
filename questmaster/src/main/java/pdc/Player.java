package pdc;

import java.awt.*;
import java.util.ArrayList;
import java.io.Serializable;
import java.util.Iterator;

/*
 * encapsulates player's avatar data
 */
@SuppressWarnings("serial")
public class Player implements Serializable {
	private static final int GRIDDISTANCE = Constants.GRIDDISTANCE;
	private Point position;
	private boolean placed, playing, placing;
	private Room currentRoom;
	private MapLayer mapLayer;
	private ArrayList<Key> keysOwned;
	private String representation;
	private final int XOFFSET = 2;
	private final int YOFFSET = 4;
	private final double COLLISION_MARGIN = 13.01;
	private Key key;
	
	public Player(MapLayer mapLayer){
		placed = false;
		playing = false;
		placing = false;
		keysOwned = new ArrayList<>();
		this.mapLayer = mapLayer;

		representation = "\u00B6";
	}
	
	public void place(Point pos) {
		position = pos;
		position.setLocation(Math.round(position.x / GRIDDISTANCE) * GRIDDISTANCE + XOFFSET,
				Math.round(position.y / GRIDDISTANCE) * GRIDDISTANCE - YOFFSET);
		placed = true;
		placing = false;
		rePlace();
	}
	
	public String getRepresentation() {
		return representation;
	}
		
	public boolean isPlaced() {
		return placed;
	}
	public boolean isPlaying() {
		return playing;
	}
	public boolean isPlacing() {
		return placing;
	}
	public void startPlaying() {
		playing = true;
	}
	public void stopPlaying() {
		playing = false;
	}
	public void startPlacing() {
		placing = true;
	}
	public void stopPlacing() {
		placing = false;
	}
	public boolean hasKey(){return keysOwned.size()>0;}

	public Point getPosition() {
		return position;
	}

	public void lockDoor() {
	    if(keysOwned.size()>0) {
            ArrayList<Point> directions = new ArrayList<>();
            directions.add(new Point(position.x, position.y - GRIDDISTANCE));
            directions.add(new Point(position.x, position.y + GRIDDISTANCE));
            directions.add(new Point(position.x - GRIDDISTANCE, position.y));
            directions.add(new Point(position.x + GRIDDISTANCE, position.y));
            for (Point d : directions) {
                double closestCollision = Double.MAX_VALUE;
                double closestNonCollision = Double.MAX_VALUE;
                //TODO would like to not iterate through all walls. CurrentRoom currently does not store archways
                for (Wall w : mapLayer.wallList) {
                    // System.out.println("Wall WallType: " + w.getWallType());
                    double distance = w.getDistance(d);
                    if (w.getWallType() == WallType.LOCKDOOR) {
                        if (distance < closestCollision) {
                            closestCollision = distance;
                            if (closestCollision < closestNonCollision && closestCollision < COLLISION_MARGIN) {
                                w.setType(WallType.OPENLOCKDOOR);
                                mapLayer.dialogPlayer("Door","UNLOCKED!");
                                break;
                            }
                        }
                    } else if (w.getWallType() == WallType.OPENLOCKDOOR) {
                        if (distance < closestCollision) {
                            closestCollision = distance;
                            if (closestCollision < closestNonCollision && closestCollision < COLLISION_MARGIN) {
                                w.setType(WallType.LOCKDOOR);
                                mapLayer.dialogPlayer("Door","LOCKED!");
                                break;
                            }
                        }
                    }

                }
            }
        }
        else
        {
            mapLayer.dialogPlayer("Error","You do not have any keys.");
        }

    }

	public void goUp() {
		if (playing && !collides(new Point(position.x, position.y - GRIDDISTANCE))) {
			position.move(position.x, position.y - GRIDDISTANCE);
		}
      positionDebug();
   }

   private void positionDebug() {
      System.out.println("Player position: " + position);
      ArrayList<Room> rl = RoomList.getInstance().list;
      for (Room room : rl) {
         System.out.println("Room#" + room.ROOMID + "contains player: " + room.contains(position));
      }
   }

   public void goDown() {
		if (playing && !collides(new Point(position.x, position.y + GRIDDISTANCE))) {
			position.move(position.x, position.y + GRIDDISTANCE);
		}
      positionDebug();
   }

	public void goLeft() {
		if (playing && !collides(new Point(position.x - GRIDDISTANCE, position.y))) {
			position.move(position.x - GRIDDISTANCE, position.y);
		}
      positionDebug();
   }

	public void goRight() {
		if (playing && !collides(new Point(position.x + GRIDDISTANCE, position.y))) {
			position.move(position.x + GRIDDISTANCE, position.y);
		}
      positionDebug();
   }

   public void pickUpKey()
   {
       boolean keyPresent = false;
       for(Key K :mapLayer.keyList)
       {
           if(Math.abs(K.getPosition().getX() - position.getX()) < 5 &&Math.abs(K.getPosition().getY() - position.getY())<5)
           {
               mapLayer.keyList.remove(K);
               keysOwned.add(K);
               mapLayer.dialogPlayer("Inventory", "Key Added.");
               keyPresent = true;
               break;
           }
       }
       if(!keyPresent)
       {
           mapLayer.dialogPlayer("Inventory", "No Key Here.");
       }

   }

   public void dropKey()
   {
        Point p  = new Point();
        p.setLocation(position.getX(), position.getY());
       if(keysOwned.size()>0) {
           mapLayer.placeKey(p);
           keysOwned.remove(0);
           mapLayer.dialogPlayer("Inventory", "Key Dropped.");
       }
       else
       {
           mapLayer.dialogPlayer("Inventory", "No Key to Drop.");
       }
   }
	
	/**
	 * Returns true if there is no collision at point p, false otherwise
	 * @param p the point to move to
	 */
	private boolean collides(Point p){
		double closestCollision = Double.MAX_VALUE;
		double closestNonCollision = Double.MAX_VALUE;
		//TODO would like to not iterate through all walls. CurrentRoom currently does not store archways
		for (Wall w : mapLayer.wallList) {
			// System.out.println("Wall WallType: " + w.getWallType());
			double distance = w.getDistance(p);
			if (w.getWallType() == WallType.OPAQUE||w.getWallType() == WallType.LOCKDOOR) {
				if (distance < closestCollision) {
					closestCollision = distance;
				}
			}
			else if (w.getWallType() == WallType.CLOSEDDOOR) {
            if (distance < closestCollision) {
               closestCollision = distance;
               if (closestCollision < closestNonCollision && closestCollision < COLLISION_MARGIN) {
                  w.setType(WallType.OPENDOOR);
               }
            }
         }
         else if (distance < closestNonCollision) {
				closestNonCollision = distance;
			}
		}
		System.out.println("Closest collision: " + closestCollision);
		System.out.println("Closest non-collision: " + closestNonCollision);
		return closestCollision < closestNonCollision && closestCollision < COLLISION_MARGIN;
	}

	public void rePlace() {
		Iterator<Room> itr = RoomList.getInstance().iterator();
		while (itr.hasNext()) {
			Room curr = itr.next();
			if (curr.contains(position)) {
				currentRoom = curr;
			}
		}
	}
	
	public void draw(Graphics g) {
		if (placed) {
			Graphics2D g2d = (Graphics2D) g;
			g2d.setColor(Color.BLACK);
			g2d.drawString(representation, position.x, position.y);
			placing = false;
		}
	}

	public String getRoomName() {
		if (RoomList.getInstance().getRoom(position) == null) {
			return "~CANT GET ROOM NAME~";
		}
		return RoomList.getInstance().getRoom(position).title;
		// return currentRoom.title;
	}

	public String getRoomDesc() {
		if (RoomList.getInstance().getRoom(position) == null) {
			return "~CANT GET ROOM DESCRIPTION~";
		}
		return RoomList.getInstance().getRoom(position).desc;
		// return currentRoom.desc;
	}
}
