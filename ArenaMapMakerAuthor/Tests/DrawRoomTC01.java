import static org.junit.jupiter.api.Assertions.*;

import java.awt.Point;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import civ.CIV;
import pdc.RoomList;

/**
 * This is the tests for Use Case #1 Drawing rooms on map and display to author
 * 
 * @author Daniel
 *
 */
class DrawRoomTC01 {
	private CIV civ;

	@BeforeEach
	public void setup() {
		civ = new CIV();
	}

	@AfterEach
	public void tearDown() {
		civ = null;
		RoomList.reset();
	}

	// TC01-1 User clicks points on map without pressing draw button
	@Test
	void test01_1() throws Throwable {
		civ.mousePressed(new Point(), false, true);
		civ.mousePressed(new Point(200, 100), true, true);
		assertEquals(true, civ.getRoomList().isEmpty());
		assertEquals(true, civ.getRoomList().isEmpty());
		assertEquals(false, civ.map.isCreating());
		assertEquals(true, civ.map.mapLayer.pointList.isEmpty());
	}

	// TC01-2 User clicks opaque wall option, then clicks two points on map to draw
	// a line
	@Test
	void test01_2() throws Throwable {
		civ.outlining();
		civ.mousePressed(new Point(), false, true);
		civ.mousePressed(new Point(200, 100), false, true);
		assertEquals(true, civ.map.isCreating());
		assertEquals(true, civ.getRoomList().isEmpty());
		assertEquals(true, civ.map.mapLayer.pointList.contains(new Point()));
		assertEquals(true, civ.map.mapLayer.pointList.contains(new Point(195, 105)));
	}

	// TC01-3 User clicks opaque wall option, then clicks two
	// points on map to draw a line while holding alt
	@Test
	void test01_3() throws Throwable {
		civ.outlining();
		civ.mousePressed(new Point(), true, true);
		civ.mousePressed(new Point(87, 95), true, true);
		assertEquals(true, civ.getRoomList().isEmpty());
		assertEquals(true, civ.map.isCreating());
		assertEquals(true, civ.map.mapLayer.pointList.contains(new Point()));
		assertEquals(true, civ.map.mapLayer.pointList.contains(new Point(87, 95)));
	}

	// TC01-4 User completes a room shape by clicking on starting point for a second
	// time
	@Test
	void test01_4() throws Throwable {
		civ.outlining();
		civ.mousePressed(new Point(), true, true);
		civ.mousePressed(new Point(87, 95), true, true);
		civ.mousePressed(new Point(300, 90), false, true);
		civ.mousePressed(new Point(), false, true);
		civ.mousePressed(new Point(800, 700), true, true);

		assertEquals(1, civ.getRoomList().size());
		assertEquals(false, civ.map.isCreating());
		assertEquals(true, civ.map.mapLayer.pointList.contains(new Point()));
		assertEquals(true, civ.map.mapLayer.pointList.contains(new Point(87, 95)));
		assertEquals(true, civ.map.mapLayer.pointList.contains(new Point(300, 90)));
		assertEquals(true, civ.map.mapLayer.pointList.contains(new Point()));
		assertEquals(false, civ.map.mapLayer.pointList.contains(new Point(800, 700)));
	}

	// TC01-5 User clicks the four corners to make a square 4x4 room
	@Test
	void test01_5() throws Throwable {
		civ.outlining();
		civ.mousePressed(new Point(15, 15), false, true);
		civ.mousePressed(new Point(15, 75), false, true);
		civ.mousePressed(new Point(75, 75), false, true);
		civ.mousePressed(new Point(75, 15), false, true);
		civ.mousePressed(new Point(15, 15), false, true);
		civ.mousePressed(new Point(300, 90), false, true);

		assertEquals(1, civ.getRoomList().size());
		assertEquals(false, civ.map.isCreating());
		assertEquals(true, civ.map.mapLayer.pointList.contains(new Point(15, 15)));
		assertEquals(true, civ.map.mapLayer.pointList.contains(new Point(15, 75)));
		assertEquals(true, civ.map.mapLayer.pointList.contains(new Point(75, 15)));
		assertEquals(false, civ.map.mapLayer.pointList.contains(new Point(300, 90)));
	}

	// TC01-6 User clicks the four corners to make small room (1x1)
	@Test
	void test01_6() throws Throwable {
		civ.outlining();
		civ.mousePressed(new Point(15, 15), false, true);
		civ.mousePressed(new Point(15, 30), false, true);
		civ.mousePressed(new Point(30, 30), false, true);
		civ.mousePressed(new Point(30, 15), false, true);
		civ.mousePressed(new Point(15, 15), false, true);
		civ.mousePressed(new Point(300, 90), false, true);

		assertEquals(1, civ.getRoomList().size());
		assertEquals(false, civ.map.isCreating());
		assertEquals(true, civ.map.mapLayer.pointList.contains(new Point(15, 15)));
		assertEquals(true, civ.map.mapLayer.pointList.contains(new Point(30, 30)));
		assertEquals(true, civ.map.mapLayer.pointList.contains(new Point(30, 15)));
		assertEquals(false, civ.map.mapLayer.pointList.contains(new Point(300, 90)));
	}

	// TC01-7 User clicks the four corners of the map to make largest room possible
	@Test
	void test01_7() throws Throwable {
		civ.outlining();
		civ.mousePressed(new Point(Integer.MIN_VALUE, Integer.MIN_VALUE), true, true);
		civ.mousePressed(new Point(Integer.MIN_VALUE, Integer.MAX_VALUE), true, true);
		civ.mousePressed(new Point(Integer.MAX_VALUE, Integer.MAX_VALUE), true, true);
		civ.mousePressed(new Point(Integer.MAX_VALUE, Integer.MIN_VALUE), true, true);
		civ.mousePressed(new Point(Integer.MIN_VALUE, Integer.MIN_VALUE), true, true);
		civ.mousePressed(new Point(300, 90), false, true);

		assertEquals(1, civ.getRoomList().size());
		assertEquals(false, civ.map.isCreating());
		assertEquals(true, civ.map.mapLayer.pointList.contains(new Point(Integer.MIN_VALUE, Integer.MIN_VALUE)));
		assertEquals(true, civ.map.mapLayer.pointList.contains(new Point(Integer.MAX_VALUE, Integer.MAX_VALUE)));
		assertEquals(true, civ.map.mapLayer.pointList.contains(new Point(Integer.MIN_VALUE, Integer.MAX_VALUE)));
		assertEquals(false, civ.map.mapLayer.pointList.contains(new Point(300, 90)));
	}

	// TC01-8 User clicks the corners to make an L-shaped room
	@Test
	void test01_8() throws Throwable {
		// TODO
	}

	// TC01-9 User clicks the corners to make a +-shaped room
	@Test
	void test01_9() throws Throwable {
		// TODO
	}

	// TC01-10 User clicks the corners to make a triangular room
	@Test
	void test01_10() throws Throwable {
		// TODO
	}
}
