package internal.piece;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * PieceFactory is a singleton factory class that is responsible for returning pieces.
 * @author Guru
 *
 */
public class PieceFactory implements Serializable {
	private static final long serialVersionUID = -5816554261706965026L;
	
	private static PieceFactory instance	= null;
	private static Map<Piece, String> pieceMap;
	protected PieceFactory() {
//		Prevent initialization of PieceFactory.
	}
	
	/**
	 * Public method to initialize the {@code PieceFactory}
	 * @param pieceStrings {@code String[]} containing the pieces to initialize
	 * @throws Exception on multiple initialization
	 */
	public static void initialize(String[] pieceStrings) throws Exception {
		if(instance != null)
			System.err.println("Warn: Multiple initialization of singleton class");
		instance = new PieceFactory(pieceStrings);
	}

	private PieceFactory(String[] list) {
		pieceMap = new HashMap<Piece, String>(17);
		initPieces(list);
	}
	
	/**
	 * Re-initialize this {@code PieceFactory} with different pieces
	 * @param pieceStrings {@code String[]} containing strings of pieces
	 * @throws IllegalStateException if {@code PieceFactory} was never initialized
	 */
	public static void reInit(String[] pieceStrings) throws IllegalStateException {
		if(instance == null)
			throw new IllegalStateException("PieceFactory has not been initialized!");
		
		instance.initPieces(pieceStrings);
	}
	
	private void initPieces(String[] list) {
		List<Piece> pieces = new ArrayList<Piece>();
		for(String s : list) {
			Piece p = new Piece(s);
			pieces.add(p);
		}
		initializeVisuals(pieces);
	}
	
	/**
	 * Read the file 'pieces.txt' to initialize a visual representation for this piece.
	 */
	private void initializeVisuals(List<Piece> pieces) {
		StringBuffer visual = new StringBuffer();
		for(Piece p : pieces) {
			visual.delete(0, visual.length());
			BufferedReader in = null;
			try {
				List<TileInterfaceType> interfaceList   = p.getValidInterfaces();
				in = new BufferedReader(new FileReader(new File("pieces.txt")));
				String line;
				while( (line = in.readLine()) != null) {
					if(line.matches("[A-Z]+")) {
						ArrayList<TileInterfaceType> tokenList          = new ArrayList<TileInterfaceType>();
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
									visual.append(line + "\n");
							}
						}
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			pieceMap.put(p, visual.toString());
		}
//		FIXME: Automatically add empty Piece
		pieceMap.put(new Piece(""), "+----------+\n|          |\n|          |\n|          |\n|          |\n|          |\n+----------+\n");
	}

	
	

/**
 * Get a piece object that is mapped to the specified set of interfaces
 * @param interfaces {@code String} representing the interfaces required in the Piece
 * @return {@code Piece} representing the given set of interfaces
 * @throws IllegalArgumentException if there is no Piece corresponding to the given set of interfaces.
 */
	private Piece getNewPiece(String interfaces) {
		for(Piece p : pieceMap.keySet()) {
			if(p.getBinaryInterfaces().equals(interfaces))
				return p;
			char [] array = interfaces.toUpperCase().toCharArray();
			Arrays.sort(array);
			interfaces = String.valueOf(array);
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
	public static IPiece newPiece(String interfaces) {
		return instance.getNewPiece(interfaces);
	}

	/**
	 * To be used only for debugging purposes. Completely random is of no use.
	 * @return {@code IPiece} a random {@code Piece}
	 */
	public static IPiece randomPiece() {
		List<Piece> pieces = new ArrayList<Piece>(pieceMap.keySet());
		return pieces.get((new Random()).nextInt(pieces.size()));
	}

	/**
	 * Accepts a set of interfaces to use or avoid using and returns a random {@code IPiece} 
	 * from the set of valid pieces.<br>
	 * Invalid interfaces are to be prefixed with a '!' symbol.
	 * @param interfaces {@code String} representing set of valid/invalid interfaces.
	 * @return {@code IPiece} conforming to the specified conditions.
	 */
	
	public static IPiece randomPiece(Map<String, String> interfaceMap) throws IllegalStateException {
		String mandatoryInterfaces  = interfaceMap.get("mandatoryInterfaces");
		String invalidInterfaces	= interfaceMap.get("invalidInterfaces");
		
//		Empty piece case
		if(invalidInterfaces.equals("DLRU")) {
			for(Piece p : pieceMap.keySet()) {
				if(p.getText().equals(""))
					return p;
			}
		}

		List<IPiece> subList = new ArrayList<IPiece>();
//		First, we create a sublist containing all pieces that satisfy mandatory interface list
		for(Piece p : pieceMap.keySet()) {
			if(p.getText().equals(""))
				continue;
			if(p.getText().contains(mandatoryInterfaces)) {
				subList.add(p);
//				Now test to see if Piece contains invalid interface
				for(char c : invalidInterfaces.toCharArray()) {
					if(p.getText().contains("" + c)) {
						subList.remove(p);
						break;
					}
				}
			}
		}

		/**
		 * Now we have the list of permitted Piece(s)!
		 */		
		try {
			return subList.get(new Random().nextInt(subList.size()));
		} catch(IllegalArgumentException e) {
			System.err.println("Mandatory list : " + mandatoryInterfaces + "\n" +
					           "Invalid list   : " + invalidInterfaces);
			e.printStackTrace();
			throw new IllegalStateException("subList of Piece(s) is empty!");
		} 
	}
	
	/**
	 * The Piece class is used to define every type of Piece that is valid.
	 * These pieces occupy Cells.
	 * @author guru
	 *
	 */
	private class Piece extends TileInterface implements IPiece, Serializable {
		private static final long serialVersionUID = 8109699128257127720L;
		
		private Piece(String interfaces) {
			super(interfaces);
		}

		@Override
		public ArrayList<IPiece> getValidNeighbourPieces(TileInterfaceType it) {
			ArrayList<IPiece> returnList = new ArrayList<IPiece>();
			TileInterfaceType complementaryInterface = it.getComplementaryInterface();
			assert(this.hasInterface(it));
			for(Piece p : pieceMap.keySet()) {
				if(p.hasInterface(complementaryInterface))
					returnList.add(p);
			}
			return returnList;
		}
		
		@Override
		public String toString() {
			String s = pieceMap.get(this);
			return s;
		}

		@Override
		public String getText() {
			return super.getText();
		}
	}
}
