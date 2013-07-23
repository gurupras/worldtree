package internal.parser.containers.expr;

import internal.parser.containers.IContainer;

public class AggExpr implements IContainer {
	private AggType type;
	private IExpr expr;
	
	public AggExpr(AggType type, IExpr expr) {
		this.type	= type;
		this.expr	= expr;
	}
	
	@Override
	public String debugString() {
		StringBuffer returnString = new StringBuffer("AGGTYPE(");
		if(type.equals(AggType.COUNT)) {
			assert expr == null : type + " with expr :" + expr + " ?\n";
			returnString.append(type + "())");
		}
		else {
			returnString.append(" " + type);
			returnString.append(" " + expr.debugString());
		}
		return returnString.toString();
	}
	
	@Override
	public String toString() {
		if(type.equals(AggType.COUNT)) {
			assert expr == null : type + " with expr :" + expr + " ?\n";
			return type + "()";
		}
		
		StringBuffer returnString = new StringBuffer(type.toString());
		if(expr != null)
			returnString.append(" " + expr);
		else
			returnString.append(" COUNT");
		
		return returnString.toString();
	}
	
	
	public enum AggType {
		SUM		("SUM"),
		MAX		("MAX"),
		MIN		("MIN"),
		COUNT	("COUNT"),
		;
		
		private String type;
		
		private AggType(String type) {
			this.type = type;
		}
		
		public static AggType parse(String type) {
			for(AggType t : values()) {
				if(t.toString().equalsIgnoreCase(type))
					return t;
			}
			return null;
		}
		
		@Override
		public String toString() {
			return type;
		}
	}

	
}
