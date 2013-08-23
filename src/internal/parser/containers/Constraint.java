package internal.parser.containers;

import java.util.ArrayList;
import java.util.Collection;

import internal.Helper.Hierarchy;
import internal.parser.containers.condition.ICondition;
import internal.parser.containers.property.PropertyDef;
import internal.parser.containers.query.IQuery;

/**
 * Container class for storing a constraint <br>
 * CONSTRAINT := ‘FOR’ ‘ALL’ LEVEL QUERY ‘ASSERT’ CONDITION
 * @author guru
 *
 */
public class Constraint extends Statement {
	private Hierarchy level;
	private IQuery query;
	private ICondition condition;
	
	public Constraint(Hierarchy level, IQuery query, ICondition condition) {
		super(StatementType.CONSTRAINT);
		this.level		= level;
		this.query		= query;
		this.condition	= condition;
	}

	@Override
	public String toString() {
		return "FOR ALL " + level + " " + query.toString() + " ASSERT " + condition.toString();
	}
	
	@Override
	public String debugString() {
		StringBuffer result = new StringBuffer("CONSTRAINT(FOR ALL " + level + " ");
		
		result.append(query.debugString());
		result.append(" ASSERT ");
		result.append(condition.debugString());
		result.append(")");
		
		return result.toString();
	}

	/**
	 * Obtain the Level component
	 * @return {@code Hierarchy}
	 */
	public Hierarchy level() {
		return level;
	}

	/**
	 * Obtain the Query component
	 * @return {@code IQuery}
	 */
	public IQuery query() {
		return query;
	}
	
	/**
	 * Obtain the Condition component
	 * @return {@code ICondition}
	 */
	public ICondition condition() {
		return condition;
	}
}
