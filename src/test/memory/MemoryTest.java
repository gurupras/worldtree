package test.memory;

import static internal.Helper.*;
import internal.piece.PieceFactory;
import internal.tree.IWorldTree.IMap;
import internal.tree.WorldTreeFactory;

import org.junit.BeforeClass;
import org.junit.Test;

public class MemoryTest {
	private IMap map;
	private WorldTreeFactory factory = null;
	
	@BeforeClass
	public static void setUp() {
		try {
			PieceFactory.initialize(pieceStrings);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

	@Test
	public void sizeTest() {
		factory = new WorldTreeFactory("init.properties", "world.definitions");
		map = factory.newMap("InitTestMap", null);
		map.initRooms();
		map.initRegions();
		map.initTiles();
		
		map.fill();
		write(map);
		
		getMemoryUsage();
		
		System.gc();
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		getMemoryUsage();
	}
	
	private void getMemoryUsage() {
		MemUnit total = new MemUnit(Runtime.getRuntime().totalMemory());
		MemUnit free = new MemUnit(Runtime.getRuntime().freeMemory());
		
		System.out.println("Total Memory  :" + total);
		System.out.println("Free Memory   :" + free);
		System.out.println("Used Memory   :" + total.difference(free));
		System.out.println();
	}
}