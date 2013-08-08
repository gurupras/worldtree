package internal.parser.containers.property;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

import internal.parser.containers.Datum;
import internal.parser.containers.Statement;
import internal.parser.containers.IContainer;
import internal.parser.containers.StatementType;
import internal.parser.containers.condition.ICondition;
import internal.parser.containers.expr.AggExpr;
import internal.parser.containers.expr.IExpr;
import internal.parser.containers.query.IQuery;

/**
 * Container class for property definitions <br>
 * <pre>
 * PROPERTYDEF := 
 *           ‘DEFINE’ LEVEL REF `.’ property ‘AS’ f(REF, REF, …) ‘IN’ QUERY
 *         | ‘DEFINE’ LEVEL REF `.’ property ‘AS’ ‘AGGREGATE’ f(REF, REF, …) ‘IN’ QUERY
 *         | ‘DEFINE’ LEVEL REF `.’ property ‘AS’ ‘RANDOM’ RANDOMSPEC ‘WHERE’ CONDITION
 *         | ‘INHERIT’ LEVEL REF `.’ property ‘FROM’ ‘PARENT’</pre>
 * @author guru
 *
 */
public class PropertyDef extends Statement {
	private String level, parent;
	private AggExpr aggExpr;
	private Property property;
	private ICondition condition;
	private IExpr expr;
	private IQuery query;
	private RandomSpec randomSpec;
	private Type type;

	public PropertyDef(Type type, String level, Property property, AggExpr aggExpr, RandomSpec random, 
			String parent, IExpr expr, ICondition condition, IQuery query) {
		super(StatementType.PROPERTYDEF);
		this.type			= type;
		this.level			= level;
		this.property		= property;
		this.expr			= expr;
		this.condition		= condition;
		this.aggExpr		= aggExpr;
		this.randomSpec		= random;
		this.parent			= parent;
		this.query			= query;
	}
	
	public PropertyDef(String level, Property property, IExpr expr, ICondition condition, IQuery query) {
		this(Type.BASIC, level, property, null, null, null, expr, condition, query);
	}
	public PropertyDef(String level, Property property, AggExpr aggExpr, IQuery query) {
		this(Type.AGGREGATE, level, property, aggExpr, null, null, null, null, query);
	}
	
	public PropertyDef(String level, Property property, RandomSpec random, ICondition condition) {
		this(Type.RANDOM, level, property, null, random, null, null, condition, null);
	}
	
	public PropertyDef(String level, Property property, String parent) {
		this(Type.INHERIT, level, property, null, null, parent, null, null, null);
	}
	
	public String level() {
		return level;
	}
	
	public String parent() {
		return parent;
	}
	
	public AggExpr aggregateExpression() {
		return aggExpr;
	}
	
	public Property property() {
		return property;
	}
	
	public ICondition condition() {
		return condition;
	}
	
	public IExpr expression() {
		return expr;
	}
	
	public IQuery query() {
		return query;
	}
	
	public RandomSpec randomspec() {
		return randomSpec;
	}
	
	public Type type() {
		return type;
	}
	
	@Override
	public String debugString() {
		StringBuffer result = null;
		switch(type) {
		case AGGREGATE:
			result = new StringBuffer("PROPERTYDEF(DEFINE " + level + " ");
			result.append(property.debugString() + " AS AGGREGATE " + 
					aggExpr.debugString() + " IN " + query.debugString() + ")");
			break;
		case BASIC:
			result = new StringBuffer("PROPERTYDEF(DEFINE " + level + " " + property.debugString() + " AS ");
			if(condition != null)
				result.append(condition.debugString());
			else
				result.append(expr.debugString());
			result.append(" IN " + query.debugString() + ")");
			break;
		case INHERIT:
			result = new StringBuffer("PROPERTYDEF(INHERIT " + level + " ");
			result.append(property.debugString() + " FROM " + parent + ")");
			break;
		case RANDOM:
			result = new StringBuffer("PROPERTYDEF(DEFINE " + level + " ");
			result.append(property.debugString() + " AS " + randomSpec.debugString());
			if(condition != null)
				result.append(" WHERE " + condition.debugString()); 
			result.append(")");
			break;
		}
		
		return result.toString();
	}
	
	@Override
	public String toString() {
		StringBuffer result = new StringBuffer();
		switch(type) {
		case AGGREGATE:
			result.append("DEFINE " + level + " " + property + " AS AGGREGATE " + aggExpr + " IN " + query);
			break;
		case BASIC:
			result.append("DEFINE " + level + " " + property + " AS " + "(" + condition + ") IN " + query);
			break;
		case INHERIT:
			result.append("INHERIT " + level + " " + property + " FROM " + parent);
			break;
		case RANDOM:
			result.append("DEFINE " + level + " " + property + " AS " + randomSpec);
			if(condition != null)
				result.append(" WHERE " + condition);
			break;
		}
		result.append(")");
		return result.toString();
	}
	
	public enum Type {
		AGGREGATE,
		RANDOM,
		BASIC,
		INHERIT,
		;
	}
	
	public static class RandomSpec implements IContainer {
		String dataType;
		Datum low, high;
		
		public RandomSpec(String dataType, Datum low, Datum high) {
			this.dataType	= dataType;
			this.low		= low;
			this.high		= high;
		}
		
		public String dataType() {
			return dataType;
		}
		
		public Datum low() {
			return low;
		}
		
		public Datum high() {
			return high;
		}
		
		@Override
		public String debugString() {
			return "RANDOMSPEC(UNIFORM " + dataType + " FROM " + low + " TO " + high + ")";	
		}
		
		@Override
		public String toString() {
			return "UNIFORM " + dataType + " FROM " + low + " TO " + high;
		}
	}
}
