package test.ui;

import static org.junit.Assert.*;

import internal.tree.IWorldTree;
import internal.tree.IWorldTree.IMap;
import internal.tree.IWorldTree.IRegion;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;

import org.junit.Test;

public class UIDebugEngine {
	
	@Test
	public static void movementTest(IMap map) {
		BufferedReader in = null;
		
		try {
			in = new BufferedReader(new InputStreamReader(System.in));
			String command;
			map.initialize();	//Map initialized
			map.initialize();	//Rooms initialized
			IRegion child = (IRegion)((IWorldTree) map.children().toArray()[0]).children().toArray()[0];	//Region0
			while(true) {
				command = in.readLine();
				UIDebugParser testParser = new UIDebugParser(new StringReader(command));
				try {
					testParser.parse(child);
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		} finally {
			if(in != null) {
				try {
					in.close();
				} catch (IOException e) {
					System.err.println(e.getMessage());
					e.printStackTrace();
				}
			}
		}
	}
	
	public static void write(IWorldTree object) {
		BufferedWriter out = null;
		try {
			out = new BufferedWriter(new FileWriter(new File("output/output.txt")));
			out.write(object.toString());
		} catch(IOException e) {
			e.printStackTrace();
		} finally {
			if(out != null) {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
