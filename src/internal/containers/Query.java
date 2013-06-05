package internal.containers;

import internal.containers.condition.ICondition;
import internal.containers.pattern.Pattern;

public class Query implements IContainer {
	private Pattern pattern;
	private ICondition condition;
	
	public Query(Pattern pattern, ICondition condition) {
		this.pattern	= pattern;
		this.condition	= condition;
	}
	
	
	@Override
	public String toString() {
		return pattern.toString() + " WHERE " + condition.toString();
	}
	
	@Override
	public String debugString() {
		return "QUERY(" + pattern.debugString() + " WHERE " + condition.debugString() + ")";
	}
}