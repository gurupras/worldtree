package internal.piece;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * PieceFactory is a singleton factory class that is responsible for returning pieces.
 * @author Guru
 *
 */
public class PieceFactory {
	private static List<Piece> listOfPieces = new ArrayList<Piece>();
	private static PieceFactory instance = null;
	
	protected PieceFactory() {
//		Prevent initialization of PieceFactory.
	}
	
	public PieceFactory(String[] pieceStrings) throws Exception {
		if(instance != null)
			throw new Exception("Multiple initialization of singleton class");
		instance = new PieceFactory();
		instance.initialize(pieceStrings);
	}

	/**
	 * Initialize the list of pieces that this factory class is responsible to generate.
	 * @param list {@code List} containing strings representing the interfaces of all pieces.
	 */
	private void initialize(String[] list) {
		for(String s : list) {
			Piece p = new Piece(s);
			listOfPieces.add(p);
		}
	}

/**
 * Get a piece object that is mapped to the specified set of interfaces
 * @param interfaces {@code String} representing the interfaces required in the Piece
 * @return {@code Piece} representing the given set of interfaces
 * @throws IllegalArgumentException if there is no Piece corresponding to the given set of interfaces.
 */
	private Piece getNewPiece(String interfaces) {
		for(Piece p : listOfPieces) {
			if(p.getBinaryInterfaces().equals(interfaces))
				return p;
			char [] array = interfaces.toUpperCase().toCharArray();
			Arrays.sort(array);
			interfaces = new String(array);
			if(interfaces.equals(p.getText()))
				return p;
		}
		throw new IllegalArgumentException("Invalid interfaces :" + interfaces);
	}
	
	/**
	 * Returns the Piece object corresponding to the specified interfaces.
	 * @param interfaces {@code String} representing the interfaces of the Piece
	 * @return {@code Piece} representing the interfaces.
	 * @throws IllegalArgumentException if the interfaces are invalid or if there is no Piece 
	 * corresponding to the specified interfaces.
	 */
	public static Piece newPiece(String interfaces) {
		return instance.getNewPiece(interfaces);
	}

	/**
	 * To be used only for debugging purposes. Completely random is of no use.
	 * @return {@code IPiece} a random {@code Piece}
	 */
	public static IPiece randomPiece() {
		return listOfPieces.get((new Random()).nextInt(listOfPieces.size()));
	}

	/**
	 * Accepts a set of interfaces to use or avoid using and returns a random {@code IPiece} 
	 * from the set of valid pieces.<br>
	 * Invalid interfaces are to be prefixed with a '!' symbol.
	 * @param interfaces {@code String} representing set of valid/invalid interfaces.
	 * @return {@code IPiece} conforming to the specified conditions.
	 */
	public static IPiece randomPiece(String interfaces) throws IllegalStateException {
		String permittedInterfaces = "DLRU";
		String originalString = interfaces;
		while(true) {
			if(interfaces.contains("!")) {
				int pos = interfaces.indexOf('!');
				char invalidInterface = interfaces.charAt(pos + 1);
				interfaces = interfaces.replaceFirst("!.", "");
				
				permittedInterfaces.replace("" + invalidInterface, "");
			}
			else
				break;
		}
		
//		We've removed all the invalid interfaces. Now to remove ones that are not specified..
		for(char c : permittedInterfaces.toCharArray()) {
			if(!interfaces.contains("" + c))
				permittedInterfaces = permittedInterfaces.replace("" + c, "");
		}
		
		/**
		 * Now we have the list of permitted interfaces!
		 * Create a sublist from listOfPieces and select a random IPiece from it.
		 */
		
		List<IPiece> subList = new ArrayList<IPiece>();
		for(IPiece piece : listOfPieces) {
			if(permittedInterfaces.contains(piece.getText()))
				subList.add(piece);
		}
		try {
			return subList.get(new Random().nextInt(subList.size()));
		} catch(IllegalArgumentException e) {
			System.err.println("Original list : " + originalString + "\n");
			e.printStackTrace();
			throw new IllegalStateException("All interfaces have been banned!");
		} 
	}
	/**
	 * The Piece class is used to define every type of Piece that is valid.
	 * These pieces occupy Cells.
	 * @author guru
	 *
	 */
	private class Piece extends TileInterface implements IPiece {
		
		private String visual;
		
		private Piece(String interfaces) {
			super(interfaces);
			if(interfaces.equals(""))
				this.visual = "+------+\n|      |\n|      |\n|      |\n+------+\n";
			else
				initializeVisual();
		}

		/**
		 * Read the file 'pieces.txt' to initialize a visual representation for this piece.
		 */
		private void initializeVisual() {
			visual = "";
			BufferedReader in = null;
			try {
				ArrayList<TileInterfaceType> interfaceList 	= getValidInterfaces();
				in = new BufferedReader(new FileReader(new File("pieces.txt")));
				String line;
				while( (line = in.readLine()) != null) {
					if(line.matches("[A-Z]+")) {
						ArrayList<TileInterfaceType> tokenList		= new ArrayList<TileInterfaceType>();
						char[] tokens = line.toCharArray();
						for(char c : tokens) {
							tokenList.add(TileInterfaceType.convert("" + c));
						}
						if(interfaceList.containsAll(tokenList) && tokenList.containsAll(interfaceList)) {
//							Valid visual
							while( (line = in.readLine()) != null) {
								if(line.matches("(-x)*"))
									break;
								else
									visual += line + "\n";
							}
						}
					}
					
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if(in != null) {
					try {
						in.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}

		/**
		 * Get the list of valid neighbouring pieces with regard to a particular interface of this Piece.
		 * @param it {@code InterfaceType} specifying the interface of this Piece that is to be used
		 * @return {@code ArrayList<Piece>} containing list of valid pieces.
		 */
		@Override
		public ArrayList<IPiece> getValidNeighbourPieces(TileInterfaceType it) {
			ArrayList<IPiece> returnList = new ArrayList<IPiece>();
			TileInterfaceType complementaryInterface = it.getComplementaryInterface();
			assert(this.hasInterface(it));
			for(Piece p : listOfPieces) {
				if(p.hasInterface(complementaryInterface))
					returnList.add(p);
			}
			return returnList;
		}
		
		/**
		 * Returns a visual representation of this Piece.
		 * @return {@code String} containing visual representation of this Piece.
		 */
		@Override
		public String toString() {
			return visual;
		}

		/**
		 * Returns a {@code String} containing the textual representation of this Piece.
		 * @return {@code String} containing the interfaces as defined for this Piece.
		 */
		@Override
		public String getText() {
			return super.toString();
		}
	}
}
