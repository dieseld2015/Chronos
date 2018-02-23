import java.awt.Point;
import java.awt.geom.GeneralPath;
import java.util.Iterator;

public class Player {
	private static final int GRIDDISTANCE = Constants.GRIDDISTANCE;
	private Point position;
	private boolean placed, playing, placing;
	private GeneralPath currentRoom;
	private MapLayer mapLayer;
	private String representation;
	
	public Player(MapLayer mapLayer){
		placed = false;
		playing = false;
		placing = false;
		this.mapLayer = mapLayer;
		representation = "P";
	}
	
	public void place(Point pos) {
		position = pos;
		position.setLocation(Math.round(position.x / GRIDDISTANCE) * GRIDDISTANCE + 2,
				Math.round(position.y / GRIDDISTANCE) * GRIDDISTANCE - 2);
		placed = true;
		placing = false;
		Iterator<GeneralPath> itr = mapLayer.pathList.iterator();
		while (itr.hasNext()) {
			GeneralPath curr = itr.next();
			if (curr.contains(position)) {
				currentRoom = curr;
			}
		}
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
	public void startPlacing() {
		placing = true;
	}
	public void stopPlacing() {
		placing = false;
	}
	
	public Point getPosition() {
		return position;
	}
	
	public void goUp() {
		if (playing) {
			position.move(position.x, position.y - GRIDDISTANCE);
			if (collides()) {
				position.move(position.x, position.y + GRIDDISTANCE);
			}
		}
	}

	public void goDown() {
		if (playing) {
			position.move(position.x, position.y + GRIDDISTANCE);
			if (collides()) {
				position.move(position.x, position.y - GRIDDISTANCE);
			}
		}
	}

	public void goLeft() {
		if (playing) {
			position.move(position.x - GRIDDISTANCE, position.y);
			if (collides()) {
				position.move(position.x + GRIDDISTANCE, position.y);
			}
		}
	}

	public void goRight() {
		if (playing) {
			position.move(position.x + GRIDDISTANCE, position.y);
			if (collides()) {
				position.move(position.x - GRIDDISTANCE, position.y);
			}
		}
	}
	
	private boolean collides() {
		boolean outside = true;
		Iterator<GeneralPath> itr = mapLayer.pathList.iterator();
		while (itr.hasNext() && outside) {
			GeneralPath path = itr.next();
			if (path.equals(currentRoom) &&
					path.contains(position.x, position.y - 2) &&
						path.contains(position.x + GRIDDISTANCE - 4, position.y - GRIDDISTANCE + 2) &&
							path.contains(position.x + GRIDDISTANCE - 4, position.y - 2) &&
								path.contains(position.x, position.y - GRIDDISTANCE + 2))
									outside = false;
		}
		return outside;
	}

	public void rePlace() {
		Iterator<GeneralPath> itr = mapLayer.pathList.iterator();
		while (itr.hasNext()) {
			GeneralPath curr = itr.next();
			if (curr.contains(position)) {
				currentRoom = curr;
			}
		}
	}
	
}