package pdc;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.undo.StateEdit;
import javax.swing.undo.StateEditable;
import java.awt.*;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.stream.Collectors;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Optional;
import java.util.Map.Entry;

import static pdc.Geometry.*;


/**
 * Layer of the map
 *
 * @author Daniel
 *
 */
@SuppressWarnings("serial")
public abstract class MapLayer implements StateEditable, Serializable {
   protected boolean drawingTransparent;
   protected Point start;
   protected Key key;
   protected ArrayList<GeneralPath> pathList;
   public ArrayList<Point> pointList;
   public GeneralPath guiPath;
   public boolean throwAlerts;
   private boolean walling;
   protected Room selectedRoom;
   public ArrayList<Wall> wallList;
   private boolean firstClick;
   private Wall lastWall;
   protected ArrayList<Key> keyList;
   //private UndoableEditSupport undoSupport;
   //private UndoManager undoManager;
   //private StateEdit stateEdit;
   private Point lastPoint;
   private boolean wasFirstClick;
   private ArrayList<Room> candidateRoomsForTransparent;
   public ArrayList<Stair> stairList;
   protected Point playerPosition;
   private Stair firstStair;

   public MapLayer() {

        /* undoSupport = new UndoableEditSupport(this);
        undoManager = new UndoManager();
        undoSupport.addUndoableEditListener(undoManager); */

		this.pathList = new ArrayList<>();
		this.pointList = new ArrayList<>();
		this.keyList = new ArrayList<>();
		wallList = new ArrayList<>();
      throwAlerts = true;
		this.selectedRoom = null;
		firstClick = true;
      candidateRoomsForTransparent = new ArrayList<>();
      stairList = new ArrayList<>();
	}

	public abstract void draw(Graphics g);

   /**
    * Method called to draw opaque walls
    * @param p the point the author clicked on
    */
	public void drawOpaqueWalls(Point p) {

	   if(firstClick){
	      lastPoint = p;
	      firstClick = false;
	      wasFirstClick = true;
      }else{
	      //check that the last 2 points clicked are not the same
         if(!lastPoint.equals(p)) {
         //startStateEdit();
            if(Math.abs(lastPoint.getX()-p.getX()) > Math.abs(lastPoint.getY()-p.getY())) {
               p.setLocation(p.getX(), lastPoint.getY());
            } else {
               p.setLocation(lastPoint.getX(), p.getY());
            }
            lastWall = new Wall(new Line2D.Double(lastPoint, p), WallType.OPAQUE);
            //wallList.add(lastWall);
            addToWallList(lastWall);
            detectRooms();
            //add lastPoint if it hasn't been added yet
            if (wasFirstClick) {
               pointList.add(lastPoint);
               wasFirstClick = false;
            }
            pointList.add(p);
            lastPoint = p;
         }
      }

	}


   /**
    * Adds the specified Wall to the wall list if it is valid, breaking it up into parts if it overlaps any existing walls
    * @param candidateWall the most recent candidate wall
    */
	private void addToWallList(Wall candidateWall){
	   //make an array of candidate Walls in case the original candidate wall needs to be broken up
	   ArrayList<Wall> candidateWalls = new ArrayList<>();
	   candidateWalls.add(candidateWall);
      boolean addCandidateWall = true;
	   for (Wall existingWall : wallList){
	      //handle collinear intersecting walls
         if(existingWall.valueEquals(candidateWall) || existingWall.containsAll(candidateWalls)){
            //lines match or candidate wall is totally contained, don't add candidate wall
            addCandidateWall = false;
            break;
         }
         ArrayList<Wall> candidateWallsToRemove = new ArrayList<>();
         ArrayList<Wall> candidateWallsToAdd = new ArrayList<>();
         for(Wall cWall : candidateWalls){
            if(existingWall.intersectsAndIsCollinearWith(cWall)){
               if(existingWall.containsPoint(cWall.getP1())||existingWall.containsPoint(cWall.getP2())){
                  //cWall overlaps wallInList
                  Optional<Point> sharedEndpoint = cWall.getSharedEndpoint(existingWall);
                  Point2D newCWallEndpoint;
                  Point2D existingCWallEndpoint;
                  if(sharedEndpoint.isPresent()){
                     //cWall and wallInList have a shared endpoint
                     if(cWall.containsWall(existingWall)){
                        //cWall totally contains existingWall
                        if(existingWall.getP1().equals(sharedEndpoint.get())){
                           newCWallEndpoint = existingWall.getP2();
                           candidateWallsToAdd.add(new Wall(existingWall.getP2(), cWall.getP2(), candidateWall.getWallType()));
                        }else{
                           newCWallEndpoint = existingWall.getP1();
                        }

                        if(cWall.getP1().equals(sharedEndpoint.get())){
                           existingCWallEndpoint = cWall.getP2();
                        }else{
                           existingCWallEndpoint = cWall.getP1();
                        }
                        candidateWallsToAdd.add(new Wall(newCWallEndpoint,existingCWallEndpoint,candidateWall.getWallType()));
                     }else {
                        //cWall partially contains existingWall
                        if (cWall.getP1().equals(sharedEndpoint.get())) {
                           candidateWallsToAdd.add(new Wall(sharedEndpoint.get(), cWall.getP2(), candidateWall.getWallType()));
                        } else {
                           candidateWallsToAdd.add(new Wall(sharedEndpoint.get(), cWall.getP1(), candidateWall.getWallType()));
                        }
                     }
                  }else{
                     //an endpoint of cWall is contained by wallInList

                     if(existingWall.containsPoint(cWall.getP1())){
                        existingCWallEndpoint = cWall.getP2();
                     }else{
                        existingCWallEndpoint = cWall.getP1();
                     }
                     if(cWall.containsPoint(existingWall.getP1())) {
                        newCWallEndpoint = existingWall.getP1();
                     }else{
                        newCWallEndpoint = existingWall.getP2();
                     }
                     candidateWallsToAdd.add(new Wall(newCWallEndpoint,existingCWallEndpoint,candidateWall.getWallType()));
                  }
               }else{
                  //cWall totally overlaps wallInList, break cWall into 2 lines
                  if(new Line2D.Double(cWall.getP1(),existingWall.getP1()).contains(existingWall.getP2())){
                     candidateWallsToAdd.add(new Wall(cWall.getP2(),existingWall.getP1(),candidateWall.getWallType()));
                     candidateWallsToAdd.add(new Wall(cWall.getP1(),existingWall.getP2(),candidateWall.getWallType()));
                  }else{
                     candidateWallsToAdd.add(new Wall(cWall.getP1(),existingWall.getP1(),candidateWall.getWallType()));
                     candidateWallsToAdd.add(new Wall(cWall.getP2(),existingWall.getP2(),candidateWall.getWallType()));
                  }
               }
               candidateWallsToRemove.add(cWall);
            }
         }
         candidateWalls.removeAll(candidateWallsToRemove);
         candidateWalls.addAll(candidateWallsToAdd);
      }
      //remove any "Walls" that have identical endpoints
      candidateWalls.removeAll(candidateWalls.stream().filter(wall -> wall.getP1().equals(wall.getP2())).collect(Collectors.toCollection(ArrayList::new)));
	   if(addCandidateWall) {
         wallList.addAll(candidateWalls);
      }
   }

   /**
    * Combines all collinear walls that share an endpoint
    */
   private void combineWalls(){
      //now that there are no overlapping Walls, combine Walls that share an endpoint and are not part of a Room
      boolean combined;
      do {
         ArrayList<Wall> wallsToAdd = new ArrayList<>();
         ArrayList<Wall> wallsToRemove = new ArrayList<>();
         combined = false;
         for (int j = 0; j < wallList.size(); j++) {
            Wall wallA = wallList.get(j);
            if (!wallsToRemove.contains(wallA)
               && !wallA.isPortal()
               && RoomList.getInstance().list.stream().noneMatch(room -> room.walls.contains(wallA))) {
               for (int i = j + 1; i < wallList.size(); i++) {
                  Wall wallB = wallList.get(i);
                  if (!wallsToRemove.contains(wallB)
                     && wallA.getWallType().equals(wallB.getWallType())
                     && RoomList.getInstance().list.stream().noneMatch(room -> room.walls.contains(wallB))) {
                     Optional<Point> sharedEndpoint = wallA.getSharedEndpoint(wallB);
                     if (sharedEndpoint.isPresent() && wallA.intersectsAndIsCollinearWith(wallB)) {
                        Point sharedPoint = sharedEndpoint.get();
                        Point2D newPoint1;
                        Point2D newPoint2;
                        if (wallA.getP1().equals(sharedPoint)) {
                           newPoint1 = wallA.getP2();
                        } else {
                           newPoint1 = wallA.getP1();
                        }

                        if (wallB.getP1().equals(sharedPoint)) {
                           newPoint2 = wallB.getP2();
                        } else {
                           newPoint2 = wallB.getP1();
                        }
                        wallsToAdd.add(new Wall(newPoint1, newPoint2, wallA.getWallType()));
                        wallsToRemove.add(wallA);
                        wallsToRemove.add(wallB);
                        combined = true;
                     }
                  }
               }
            }
         }
         wallList.removeAll(wallsToRemove);
         for (Wall wall : wallsToAdd) {
            addToWallList(wall);
         }
      }while(combined);
   }

   /*
   private void startStateEdit(){
      stateEdit = new StateEdit(MapLayer.this);
      RoomList.getInstance().startStateEdit();
   }

   private void endStateEdit(){
      stateEdit.end();
      RoomList.getInstance().endStateEdit();
      undoManager.addEdit(stateEdit);
   }
   */

   /**
    * Method called to detect rooms from lines drawn on the map
    */
   public void detectRooms(){
      combineWalls();
      breakUpWallsAtIntersections();

      ArrayList<Room> tempRoomList = getRooms();
      //tempRoomList now contains all the valid rooms and only the valid rooms

      //if tempRoomList is bigger than the RoomList, there's a new room in town
      if(tempRoomList.size()>RoomList.getInstance().list.size()) {
         addNewRooms(tempRoomList);
      }

   }

   /**
    * Method to add new rooms to RoomList
    * @param tempRoomList the temporary list of rooms detected after a wall has been drawn
    */
   private void addNewRooms(ArrayList<Room> tempRoomList) {
      //find the new rooms
      ArrayList<Room> newRooms = new ArrayList<>();
      for (Room roomA : tempRoomList) {
         boolean unique = true;
         for (int j = 0; unique && j < RoomList.getInstance().list.size(); j++) {
            Room roomB = RoomList.getInstance().list.get(j);
            if (roomA.walls.size()==roomB.walls.size()&&roomA.walls.containsAll(roomB.walls)) {
               unique = false;
            }
         }
         if (unique) {
            newRooms.add(roomA);
         }
      }
      //check which if any of the new rooms are from splitting a room, handle that if so
      ArrayList<Room> roomsToRemove = new ArrayList<>();
      ArrayList<Room> subRooms = new ArrayList<>();
      for (int i = 0; i < RoomList.getInstance().list.size(); i++) {
         ArrayList<Room> containedRooms = new ArrayList<>();
         Room roomA = RoomList.getInstance().list.get(i);
         for (Room roomB : newRooms) {
            if (roomA.sharesWallAndContains(roomB)) {
               containedRooms.add(roomB);
            }
         }
         if (containedRooms.size() > 0) {
            roomsToRemove.add(roomA);
            boolean first = true;
            for(Room containedRoom : containedRooms) {
               if(first) {
                  subRooms.add(new Room(roomA.ROOMID, roomA.title, roomA.desc, containedRoom.walls));
                  first = false;
               }else{
                  subRooms.add(new Room(roomA.title, roomA.desc, containedRoom.walls));
               }
               newRooms.remove(containedRoom);
            }
         }
      }
      //remove the old rooms, add the subRooms
      RoomList.getInstance().list.removeAll(roomsToRemove);
      RoomList.getInstance().list.addAll(subRooms);
      //if there are still remaining rooms, they're regular new rooms.  Simply add them
      newRooms.forEach(room-> RoomList.getInstance().add(new Room(room.walls)));
   }

   /**
    * Method to find all rooms on the map after a line has been drawn
    * @return a list of all rooms detected on the map
    */
   private ArrayList<Room> getRooms() {
      Graph graph = new Graph(wallList.size());
      wallList.forEach(wall-> graph.addEdge(wall.getP1(),wall.getP2()));
      graph.findCycles();
      ArrayList<Point2D[]> cycles = graph.cycles;
      //convert cycles from vertices to edges
      ArrayList<ArrayList<Wall>> cyclesAsEdges = new ArrayList<>();
      cycles.forEach(cycle->{
         ArrayList<Wall> currentCycle = new ArrayList<>();
         for(int i = 0;i<cycle.length-1;i++){
            Line2D line = new Line2D.Double(cycle[i],cycle[i+1]);
            for(Wall wall : wallList){
               if(wall.representationMatchesLine(line)){
                  currentCycle.add(wall);
               }
            }
         }
         //add edge back to the starting vertex
         Line2D line = new Line2D.Double(cycle[cycle.length-1],cycle[0]);
         for(Wall wall : wallList){
            if(wall.representationMatchesLine(line)){
               currentCycle.add(wall);
            }
         }
         cyclesAsEdges.add(currentCycle);
      });
      //create new rooms from the cycles, put in a temporary array before sorting out non-rooms
      ArrayList<Room> tempRoomList = new ArrayList<>();
      ArrayList<Room> roomsToRemove = new ArrayList<>();
      //add potential rooms to the list, but don't assign them IDs until they're verified to be desired rooms
      cyclesAsEdges.forEach(cycle-> tempRoomList.add(new Room(cycle,false)));

      //remove all rooms that contain other rooms
      for(int i = 0; i<tempRoomList.size()-1;i++){
         for(int j = i+1;j<tempRoomList.size();j++){
            Room roomA = tempRoomList.get(i);
            Room roomB = tempRoomList.get(j);
            if (!roomsToRemove.contains(roomB)&&roomB.sharesWallAndContains(roomA)) {

               roomsToRemove.add(roomB);
            }else if(!roomsToRemove.contains(roomA)&&roomA.sharesWallAndContains(roomB)){
               roomsToRemove.add(roomA);
            }
         }
      }
      tempRoomList.removeAll(roomsToRemove);
      return tempRoomList;
   }

   /**
    * Breaks up walls that intersect into separate line segments so that they are easier to work with
    */
   private void breakUpWallsAtIntersections() {
      HashMap<Wall, ArrayList<Point>> wallsToBreak = new HashMap<>();
      for(int j = 0; j<wallList.size();j++){
         Wall wallA = wallList.get(j);
         for(int i = j+1; i< wallList.size();i++){
            Wall wallB = wallList.get(i);
            if(wallA.intersects(wallB)){
               Optional<Point> intersection = wallA.getIntersectionPoint(wallB);
               intersection.ifPresent(point -> {
                  if ((wallA.getP1().equals(point) || wallA.getP2().equals(point))
                     && !(wallB.getP1().equals(point) || wallB.getP2().equals(point))) {
                     //case 1: wall A intersects at an endpoint, need to break up lastWall and add its new segments
                     if(!wallsToBreak.containsKey(wallB)) {
                        wallsToBreak.put(wallB, new ArrayList<>());
                     }
                     wallsToBreak.get(wallB).add(point);
                  } else if ((wallB.getP1().equals(point) || wallB.getP2().equals(point))
                     && !(wallA.getP1().equals(point) || wallA.getP2().equals(point))) {
                     //case 2: lastWall intersects at an endpoint, need to remove wallA, break it up, and add the new segments
                     if(!wallsToBreak.containsKey(wallA)) {
                        wallsToBreak.put(wallA, new ArrayList<>());
                     }
                     wallsToBreak.get(wallA).add(point);
                  } else if (!(wallB.getP1().equals(point) || wallB.getP2().equals(point))
                     && !(wallA.getP1().equals(point) || wallA.getP2().equals(point))) {
                     //case 3: neither wall intersects at an endpoint, need to break up both walls and add their new segments
                     if(!wallsToBreak.containsKey(wallA)) {
                        wallsToBreak.put(wallA, new ArrayList<>());
                     }
                     wallsToBreak.get(wallA).add(point);

                     if(!wallsToBreak.containsKey(wallB)) {
                        wallsToBreak.put(wallB, new ArrayList<>());
                     }
                     wallsToBreak.get(wallB).add(point);
                  }
               });
            }
         }
      }
      wallsToBreak.forEach((key, value) -> {
         wallList.remove(key);
         breakUpWallAndAddToList(key, value);
      });
      //remove duplicate walls
      wallList = (ArrayList<Wall>) wallList.stream().distinct().collect(Collectors.toList());
   }

   /**
    * Method to break line into multiple line segments at the specified points and add them to the walllist
    * @param points the points at which the wall will be broken up
    * @param wall the wall to be broken up
    */
   private void breakUpWallAndAddToList(Wall wall, ArrayList<Point> points) {
      PointSort ps = new PointSort();
      points.sort(ps);
      Point lastPoint = null;
      ArrayList<Point> wallPoints = new ArrayList<>();
      wallPoints.add(point2DToPoint(wall.getP1()));
      wallPoints.add(point2DToPoint(wall.getP2()));
      wallPoints.sort(ps);
      for(Point point : points){
         if(lastPoint == null){
            lastPoint = point;
            wallList.add(new Wall(wallPoints.get(0),lastPoint,wall.getWallType()));
         }else{
            wallList.add(new Wall(lastPoint,point,wall.getWallType()));
            lastPoint = point;
         }
      }
      //add last segment to complete the wall
      wallList.add(new Wall(lastPoint,wallPoints.get(1),wall.getWallType()));
   }

   /**
    * Places an archway at the specified Point
    * @param point the point at which to place the archway
    */
   public void placeArchway(Point point){
      placePortal(point,WallType.ARCHWAY);
   }

   /**
    * Places a door at the specified Point
    * @param point the point at which to place the door
    */
   public void placeDoor(Point point){
      placePortal(point,WallType.CLOSED_DOOR);
   }

    /**
     * Places a locked door at the specified Point
     * @param point the point at which to place the locked door
     */
    public void placeLockedDoor(Point point){
        placePortal(point,WallType.LOCKED_DOOR);
    }

   /**
    * Method to add portal to map
    * @param point the point that was clicked
    * @param type the type of wall to draw
    */
   private void placePortal(Point point,WallType type){
      ArrayList<Room> roomsToUpdate = new ArrayList<>();
      boolean flag = false;
      int flagerror = 0;
      Wall wallToPlacePortalOn = null;
      for (Wall wall : this.wallList) {
         if (wall.getDistance(point) == 0) {
            wallToPlacePortalOn = wall;
            if (Math.sqrt(((wallToPlacePortalOn.getX2() - wallToPlacePortalOn.getX1()) * (wallToPlacePortalOn.getX2() - wallToPlacePortalOn.getX1())) + ((wallToPlacePortalOn.getY2() - wallToPlacePortalOn.getY1()) * (wallToPlacePortalOn.getY2() - wallToPlacePortalOn.getY1()))) >= 15) {
               if (wallToPlacePortalOn.getX1().equals(wallToPlacePortalOn.getX2())) {
                  if ((Math.abs(point.getY() - wallToPlacePortalOn.getY1()) > 15) && (Math.abs(point.getY() - wallToPlacePortalOn.getY2()) > 15)) {
                     for (Room room : RoomList.getInstance().list) {
                        if (room.walls.contains(wallToPlacePortalOn)) {
                           roomsToUpdate.add(room);
                        }
                     }
                     flag = true;
                     break;
                  } else {
                     flagerror = 1;
                  }
               } else if (wallToPlacePortalOn.getY1().equals(wallToPlacePortalOn.getY2())) {
                  if ((Math.abs(point.getX() - wallToPlacePortalOn.getX1()) > 15) && (Math.abs(point.getX() - wallToPlacePortalOn.getX2()) > 15)) {
                     for (Room room : RoomList.getInstance().list) {
                        if (room.walls.contains(wallToPlacePortalOn)) {
                           roomsToUpdate.add(room);
                        }
                     }
                     flag = true;
                     break;
                  } else {
                     flagerror = 1;
                  }
               } else {
                  flagerror = 1;
               }
            }
         }
      }
      if(flag) {
         boolean canPlacePortal = true;
         CardinalDirection directionForError = null;
         int roomIdForError = 0;
         for(Room room: roomsToUpdate){
            if(room.canPlacePortalOnWall(wallToPlacePortalOn)){
               canPlacePortal = false;
               directionForError = room.getWallDirection(wallToPlacePortalOn);
               roomIdForError = room.ROOMID;
               break;
            }
         }
         if(canPlacePortal) {
            this.wallList.remove(wallToPlacePortalOn);
            Point2D start = wallToPlacePortalOn.getP1();
            Point2D end = wallToPlacePortalOn.getP2();
            Wall newStartWall = new Wall(new Line2D.Double(start, point), WallType.OPAQUE);
            Point2D endDoor;
            if (wallToPlacePortalOn.getX1().equals(wallToPlacePortalOn.getX2())) {
               endDoor = new Point2D.Double(wallToPlacePortalOn.getX2(), point.getY() - 15);
               if (start.getY() < end.getY()) {
                  endDoor = new Point2D.Double(wallToPlacePortalOn.getX2(), point.getY() + 15);
               }
            } else {
               endDoor = new Point2D.Double(point.getX() - 15, wallToPlacePortalOn.getY2());
               if (start.getX() < end.getX()) {
                  endDoor = new Point2D.Double(point.getX() + 15, wallToPlacePortalOn.getY2());
               }
            }
            Wall newEndWall = new Wall(new Line2D.Double(endDoor, end), WallType.OPAQUE);
            Wall portalSeg = new Wall(new Line2D.Double(point, endDoor), type);
            this.wallList.add(newStartWall);
            this.wallList.add(portalSeg);
            this.wallList.add(newEndWall);
            for (Room room : roomsToUpdate) {
               room.walls.remove(wallToPlacePortalOn);
               ArrayList<Wall> newWallList = room.walls;
               newWallList.add(newStartWall);
               newWallList.add(portalSeg);
               newWallList.add(newEndWall);
               room.updatePath(newWallList);
            }
         }else{
            dialog("Room #"+roomIdForError+" already has a "+directionForError.toString()+" portal");
         }
      } else {
         String portalTypeForDialog = "Portal";
         if(type.equals(WallType.CLOSED_DOOR)){
            portalTypeForDialog = "Door";
         }else if(type.equals(WallType.ARCHWAY)){
            portalTypeForDialog = "Archway";
         } else if(type.equals(WallType.LOCKED_DOOR)){
           portalTypeForDialog = "Locked Door";
         }
         if(flagerror ==1) {
            dialog(portalTypeForDialog+" cannot be placed here.");
         } else {
            dialog(portalTypeForDialog+" must be placed on a wall.");
         }
      }
   }

   public void placeKey(Point p) {
      key = new Key(p);
      keyList.add(key);
   }
   /**
    * Method called when drawingTransparent transparent walls
    * @param p the point the author clicked
    * @return true if the author is still drawingTransparent transparent walls
    */
	public boolean drawTransparentWalls(Point p) {
      this.walling = true;
      if (firstClick) {
         firstClick = false;
         //check that the player has clicked on the boundary of a room
         for(int i = 0; i<RoomList.getInstance().list.size();i++){
            Room room = RoomList.getInstance().list.get(i);
            if(room.onBoundary(p)){
               candidateRoomsForTransparent.add(room);
            }
         }
         if(candidateRoomsForTransparent.size()==0){
            walling = false;
         }else {
            lastPoint = p;
         }
      } else {
         if(candidateRoomsForTransparent.size()>0) {
            if(Math.abs(lastPoint.getX()-p.getX()) > Math.abs(lastPoint.getY()-p.getY())) {
               p.setLocation(p.getX(), lastPoint.getY());
            } else{
               p.setLocation(lastPoint.getX(), p.getY());
            }
            boolean secondClickValid=false;
            for(int i = 0;!secondClickValid&&i<candidateRoomsForTransparent.size();i++){
               secondClickValid = candidateRoomsForTransparent.get(i).onBoundary(p);
            }
            if(secondClickValid) {
               StateEdit stateEdit = new StateEdit(MapLayer.this);
               pointList.add(lastPoint);
               pointList.add(p);
               lastWall = new Wall(new Line2D.Double(lastPoint, p), WallType.TRANSPARENT);
               wallList.add(lastWall);
               detectRooms();
               stateEdit.end();
               //undoManager.addEdit(stateEdit);
            }else{
               walling = false;
            }
         }else {
            walling = false;
         }
      }
		return this.walling;
	}


	public abstract MapLayer copy();

	/*public void undo(){
	   if(undoManager.canUndo()){
	      undoManager.undo();
      }
   }*/

	public Room getRoom(Point p) {
		return RoomList.getInstance().getRoom(p);
	}

	public void setSelectedRoom(Room r) {
       this.selectedRoom = r;
	}



    public ArrayList<Wall> getWallList()
    {
       return wallList;
    }

   /**
    * Represents changes of state when the author stops drawing
    */
   public void stopDrawing() {
	   firstClick = true;
	   lastPoint = null;
      wasFirstClick = false;
   }

    /** Abstract method to set whether the MapLayer is for the player mode
    * @param setting the value to give to player mode
    */
   public abstract void setPlayerMode(boolean setting);

   /**
    * Deletes the wall or passageway at p, if there is a wall or passageway at p
    * @param p the point of the wall or passageway to delete
    */
   public void delete(Point p) {
      ArrayList<Wall> wallsToRemove = new ArrayList<>();
      ArrayList<Wall> portalsToRemove = new ArrayList<>();
      //----don't mind me, just checking for staircases to remove first----
      for (Stair s : stairList) {
         Point stairPos = s.getLocation();
         if(Math.abs(stairPos.x - p.x) < 8 && Math.abs(stairPos.y - p.y) < 8) {
            stairList.remove(s.linkedStair);
            stairList.remove(s);
            
            RoomList.getInstance().getRoom(s.linkedStair.getLocation()).removeStairInDirection(s.linkedStair.getDirection());
            RoomList.getInstance().getRoom(s.getLocation()).removeStairInDirection(s.getDirection());
            
            return; // BEWARE THERE'S A RETURN HERE
         }
      }
      //----sorry, i'll get out of your way now----
      for(Wall wall:wallList){
         if(wall.getDistance(p)<4){
            //TODO find a good way to do this.
            // Handle clicking a portal vs a wall. If the author clicks a wall that contains an archway, need to handle
            // deleting that entire wall (check if wall shares intersection with passageway, delete the wall,
            // the passageway, and the wall on the other side of the passageway)
            //if the author clicked on the endpoint of 2 walls, ignore it.  The endpoint of a wall is often shared by
            // 2 walls, so it's ambiguous which wall was clicked.
            if(!wall.hasEndpoint(p)){
               WallType wallType = wall.getWallType();
               if(wallType.equals(WallType.OPAQUE)|| wallType.equals(WallType.TRANSPARENT)){
                  wallsToRemove.add(wall);
                  //remove any portals that are connected to the wall
                  //TODO ask if this is desired
                  /*
                  Point wallP1 = (Point) wall.getP1();
                  Point wallP2 = (Point) wall.getP2();
                  for(Wall portal : wallList){
                     if(portal.getWallType().equals(WallType.ARCHWAY)||portal.getWallType().equals(WallType.DOOR)||){
                        if(portal.hasEndpoint(wallP1)||portal.hasEndpoint(wallP2)){
                           wallsToRemove.add(portal);
                        }
                     }
                  }
                  */
                  ArrayList<Room> roomsToRemove = new ArrayList<>();
                  for(Room room: RoomList.getInstance().list){
                     if(room.walls.contains(wall)){
                        roomsToRemove.add(room);
                     }
                  }
                  RoomList.getInstance().list.removeAll(roomsToRemove);
               }else if(wall.isTraversable()){
                  ArrayList<Room> roomsToAdjust = new ArrayList<>();
                  portalsToRemove.add(wall);
                  for(Room room: RoomList.getInstance().list){
                     if(room.walls.contains(wall)){
                        roomsToAdjust.add(room);
                     }
                  }
                  for(Room room : roomsToAdjust) {
                     for (Wall port : portalsToRemove) {
                        for (Entry<CardinalDirection, Wall> e : room.getPortals().entrySet()) {
                           if (e.getValue() == port) {
                              room.getPortals().remove(e.getKey());
                           }
                        }
                     }
                  }
               }
            }
         }
      }
      for(Wall portal : portalsToRemove){
         portal.setWallType(WallType.OPAQUE);
      }
      if(wallsToRemove.size()>0) {
         wallList.removeAll(wallsToRemove);
         detectRooms();
      }

   }

   public void placeStairs(Point p) {
      CardinalDirection direction;
      if (firstStair == null) {
         direction = CardinalDirection.DOWN;
      } else {
         direction = CardinalDirection.UP;
      }
      Room room = RoomList.getInstance().getRoom(p);
      if(room != null){
         if(!room.hasPortalInDirection(direction)) {
            if(firstStair == null) {
               firstStair = new Stair(p);
               firstStair.setDirection(direction);
               room.addStair(firstStair, direction);
               stairList.add(firstStair);
            }else{
               Stair newStairs = new Stair(p);
               newStairs.setDirection(direction);
               stairList.add(newStairs);
               firstStair.linkWith(newStairs);
               newStairs.linkWith(firstStair);
               room.addStair(newStairs, direction);
               firstStair = null;
            }
         }else{
            if(firstStair!=null){
               room = RoomList.getInstance().getRoom(firstStair.getLocation());
               room.removeStairInDirection(firstStair.getDirection());
               stairList.remove(firstStair);
               firstStair = null;
            }
            dialog("Room already has a "+direction.toString()+" Stair.");
         }
      }else{
         if(firstStair!=null){
            room = RoomList.getInstance().getRoom(firstStair.getLocation());
            room.removeStairInDirection(firstStair.getDirection());
            stairList.remove(firstStair);
            firstStair = null;
         }
         dialog("Stairs must be placed in rooms.");
      }

   }

   /**
    * Custom comparator used to sort points when breaking up walls.
    */
   class PointSort implements Comparator<Point>{

       @Override
       public int compare(Point o1, Point o2) {
          if(o1.getX()!=o2.getX()){
             return (int) (o1.getX() - o2.getX());
          }else{
             return (int) (o1.getY() - o2.getY());
          }
       }
   }

   private void dialog(String message) {
      if(throwAlerts) {
         JOptionPane jop = new JOptionPane(message);
         final JDialog d = jop.createDialog("Error");
         d.setLocation(250, 250);
         d.setVisible(true);
      }
   }

   public abstract void setPlayerPosition(Point position);

   protected void dialogPlayer(String type, String message) {
      if(throwAlerts) {
         JOptionPane jop = new JOptionPane(message);
         final JDialog d = jop.createDialog(type);
         d.setLocation(250, 250);
         d.setVisible(true);
      }
   }
}
