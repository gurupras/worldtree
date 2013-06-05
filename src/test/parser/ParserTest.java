package test.parser;

import static org.junit.Assert.*;

import internal.parser.Parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;

import org.junit.Test;

public class ParserTest {

	@Test
	public void basicIOTest() {
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
			
			StringBuffer command = new StringBuffer();
			while(true) {
				String cmd = in.readLine();
				
				if(cmd.equalsIgnoreCase("quit") || cmd.equalsIgnoreCase("exit"))
					break;
				
				command.append(cmd);
				if(command.toString().contains(";")) {
					Parser parser = new Parser(new StringReader(command.toString()));
					Object o = parser.parse();
					System.out.println(o.toString());
					command.delete(0, command.length());
				}
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

}
