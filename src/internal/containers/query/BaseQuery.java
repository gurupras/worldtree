package internal.containers.query;

import internal.containers.Statement;
import internal.containers.StatementType;
import internal.containers.condition.ICondition;
import internal.containers.pattern.IPattern;

public class BaseQuery extends Statement implements IQuery {
	private IPattern pattern;
	private ICondition condition;
	
	public BaseQuery(IPattern pattern, ICondition condition) {
		super(StatementType.QUERY);
		this.pattern	= pattern;
		this.condition	= condition;
	}
	
	@Override
	public String toString() {
		StringBuffer result = new StringBuffer(pattern.toString());
		if(condition != null)
			result.append(" WHERE " + condition.toString());
		
		return result.toString();
	}

	@Override
	public String debugString() {
		StringBuffer result = new StringBuffer("QUERY(" + pattern.debugString());
		
		if(condition != null)
			result.append(" WHERE " + condition.debugString() + ")");
		
		return result.toString();
	}

	@Override
	public IPattern pattern() {
		return pattern;
	}

	@Override
	public ICondition condition() {
		return condition;
	}

	@Override
	public IQuery subQuery() {
		return null;
	}
}
