package internal.parser.resolve.constraint;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import internal.Helper.Hierarchy;
import internal.parser.TokenCmpOp;
import internal.parser.containers.Constraint;
import internal.parser.containers.Datum;
import internal.parser.containers.condition.ICondition;
import internal.parser.containers.expr.IExpr;
import internal.parser.containers.property.Property;
import internal.parser.containers.property.PropertyDef;
import internal.parser.containers.query.IQuery;
import internal.parser.resolve.Column;
import internal.parser.resolve.Result;
import internal.parser.resolve.query.QueryResolutionEngine;
import internal.tree.IWorldTree;
import internal.tree.IWorldTree.IMap;
import development.com.collection.range.Range;
import development.com.collection.range.RangeSet;

public class BasicSolver implements IConstraintSolver {

	private Datum evaluate(IWorldTree node, PropertyDef definition) {
		Collection<Datum> values = new ArrayList<Datum>();
		Property property = definition.property();
		
		Column column = null;
		String columnName = null;
		switch(definition.type()) {
		case AGGREGATE:
			columnName = definition.aggregateExpression().expr().reference().toString();
			break;
		case BASIC:
			break;
		case INHERIT:
			break;
		case RANDOM:
			return node.properties().get(property);
		}
		Result result = QueryResolutionEngine.evaluate(node, definition.query());
		column = result.get(columnName);
		for(IWorldTree child : column) {
			Datum value = child.properties().get(property);
			if(value != null)
				values.add(value);
		}
		
		Datum propertyValue = null;
		for(Datum value : values) {
			switch(definition.type()) {
			case AGGREGATE:
				switch(definition.aggregateExpression().type()) {
				case COUNT:
					if(propertyValue == null)
						propertyValue = new Datum.Int(0);
					propertyValue = propertyValue.add(new Datum.Int(1));
					break;
				case MAX:
					if(propertyValue == null)
						propertyValue = value.clone();
					if(value.compareTo(propertyValue, TokenCmpOp.GT) == 0)
						propertyValue = value.clone();
					break;
				case MIN:
					if(propertyValue == null)
						propertyValue = value.clone();
					if(value.compareTo(propertyValue, TokenCmpOp.LT) == 0)
						propertyValue = value.clone();
					break;
				case SUM:
					if(propertyValue == null)
						propertyValue = value.clone();
					else
						propertyValue = propertyValue.add(value);
					break;
				default:
					break;
				}
				break;
			case BASIC:
				break;
			case INHERIT:
				break;
			case RANDOM:
				break;
			default:
				break;
			
			}
		}
		return propertyValue;
	}
	
	private boolean satisfies(IWorldTree node, ICondition condition, PropertyDef definition) {
		if(condition == null)
			return true;
		ICondition subCondition = condition.subCondition();
		
		boolean result = false;
		switch(condition.type()) {
		case BASIC:
			Datum propertyValue = evaluate(node, definition);
			if(condition.property().equals(definition.property())) {
				Datum conditionValue = condition.value();
				switch(condition.operator()) {
				case EQ:
					if(propertyValue.compareTo(conditionValue, TokenCmpOp.EQ) == 0)
						result = true;
					break;
				case GE:
					if(propertyValue.compareTo(conditionValue, TokenCmpOp.GE) == 0)
						result = true;
					break;
				case GT:
					if(propertyValue.compareTo(conditionValue, TokenCmpOp.GT) == 0)
						result = true;
					break;
				case LE:
					if(propertyValue.compareTo(conditionValue, TokenCmpOp.LE) == 0)
						result = true;
					break;
				case LT:
					if(propertyValue.compareTo(conditionValue, TokenCmpOp.LT) == 0)
						result = true;
					break;
				case NOTEQ:
					if(propertyValue.compareTo(conditionValue, TokenCmpOp.NOTEQ) == 0)
						result = true;
					break;
				}
			}
			break;
		case BOOLEAN:
			break;
		case COMPLEX:
			break;
		}
		
		if(subCondition != null) {
			switch(condition.unionType()) {
			case AND:
				return result & satisfies(node, subCondition, definition);
			case OR:
//				TODO: Not sure how to handle or
				break;
			}
		}
		return result;
	}
	
	@Override
	public void pushDownConstraints(IWorldTree node) {
		sortDefinitions(node);
		List<IWorldTree> nodes = new ArrayList<IWorldTree>();
		IMap map = ((IMap) node.root());	//FIXME: Hack
		nodes.add(map);
		nodes.addAll(map.getNodesByLevel(Hierarchy.Room));
		nodes.addAll(map.getNodesByLevel(Hierarchy.Region));
		nodes.addAll(map.getNodesByLevel(Hierarchy.Tile));
		
		List<IWorldTree> nodesCopy = new ArrayList<IWorldTree>(nodes);
		IWorldTree currentNode = null;
		while(true) {
			boolean satisfied = true;
			iterativePushDown(node);
			while(nodesCopy.size() > 0) {
				currentNode = nodesCopy.get(0);
				for(Constraint constraint : node.constraints()) {
					if(constraint.level().equals(Hierarchy.parse(currentNode.getClass()))) {
						Result result = QueryResolutionEngine.evaluate(currentNode, constraint);
						if(result.get(constraint.query().pattern().lhs().toString()).contains(currentNode)) {
							Property property = constraint.condition().property();
							PropertyDef definition = null;
							for(PropertyDef def : currentNode.root().definitions()) {
								if(def.level().equals(Hierarchy.parse(currentNode.getClass())) && def.property().equals(property)) {
									definition = def;
									break;
								}
							}
							satisfied &= satisfies(currentNode, constraint.condition(), definition);
						}
					}
				}
				if(!satisfied) {
					nodesCopy.clear();
					nodesCopy.addAll(nodes);
					break;
				}
				else
					nodesCopy.remove(0);
			}
			if(satisfied)
				break;
		}
	}
	
	private void iterativePushDown(IWorldTree node) {
//		FIXME:	Currently assumes that properties need to be materialized only at the lowest level..all other levels are aggregates
//		TODO:	Need to handle dependent properties in the right order
		if(node.children() != null) {
			for(IWorldTree child : node.children())
				iterativePushDown(child);
//				TODO: Enable query parsing on constraint.query()
//				Result result = QueryResolutionEngine.evaluate(node, constraint.query());
//				Column column = result.get(constraint.query().pattern().lhs().toString());
//				if(column.contains(node)) {
//				}
		}
		else {
			Collection<PropertyDef> definitions = node.definitions();
			if(definitions == null)
				return;
			for(PropertyDef definition : definitions) {
				if(!definition.level().equals(Hierarchy.parse(node.getClass())))
					continue;
				Property property = definition.property();
				Range range = null;
				Datum value = null;
				
				switch(definition.type()) {
				case BASIC:
					IQuery query = definition.query();
					Result result = QueryResolutionEngine.evaluate(node, query);
					Column column = result.get(definition.reference().toString());
					if(column.contains(node)) {
						IExpr expr = definition.expression();
						ICondition condition = definition.condition();
						if(expr != null)
							value = expr.evaluate(node, result);
						else if(condition != null)
							value = condition.evaluate(node, result);
					}
					break;
				case INHERIT:
					break;
				case RANDOM:
					range = definition.randomspec().range();
					value = range.generateRandom();
					break;
				default:
					break;
				}
				if(value != null) {
					node.properties().put(property, value);
					updateParent(node, property);
				}
			}
		}
	}

	private void updateParent(IWorldTree node, Property property) {
		IWorldTree parent = node.parent();
		PropertyDef definition = null;
		if(node.parent() == null)
			return;
		
		for(PropertyDef def : node.definitions()) {
			if(def.level().equals(Hierarchy.parse(parent.getClass())) && def.property().equals(property)) {
				definition = def;
				break;
			}
		}
		if(definition != null) {
			Datum value = evaluate(parent, definition);
			parent.properties().put(property, value);
			updateParent(parent, property);
		}
		else {
//			Parent has no definition of this property..ignore updating parent
		}
	}
	
	@Override
	public RangeSet getBounds(IWorldTree node, PropertyDef definition) {
		return null;
	}

	@Override
	public void initializeBounds(IWorldTree node) {
	}

	@Override
	public RangeSet processBounds(IWorldTree node, Constraint constraint) {
		return null;
	}
	
	
	private void sortDefinitions(IWorldTree root) {
		Collection<PropertyDef> definitions = root.definitions();
		
//		We now find dependencies among property definitions
		HashMap<Hierarchy, HashMap<Property, PropertyDef>> propertyDefMap = 
				new HashMap<Hierarchy, HashMap<Property, PropertyDef>>();
		HashMap<Hierarchy, HashMap<Property, Collection<Property>>> propertyDependencyMap = 
				new HashMap<Hierarchy, HashMap<Property, Collection<Property>>>();
		
		for(Hierarchy level : Hierarchy.values()) {
			HashMap<Property, Collection<Property>> levelDependencyMap = new HashMap<Property, Collection<Property>>();
			HashMap<Property, PropertyDef> levelPropertyDefMap = new HashMap<Property, PropertyDef>();
			for(PropertyDef definition : definitions) {
				if(!definition.level().equals(level))
					continue;
				Property baseProperty = definition.property();
				levelPropertyDefMap.put(baseProperty, definition);
				Set<Property> dependencies = new HashSet<Property>();	//TODO: Figure out whether we care about the order here
				switch(definition.type()) {
				case AGGREGATE:
//					TODO: Figure out whether we're only looking at lower levels when we're aggregating..if not, we need some code here
					break;
				case BASIC:
					if(definition.expression() != null) {
						IExpr expr = definition.expression();
						while(expr != null) {
							if(expr.property() != null)
								dependencies.add(expr.property());
							expr = expr.subExpr();
						}
					}
					else if(definition.condition() != null) {
						ICondition condition = definition.condition();
						while(condition != null) {
							if(condition.property() != null)
								dependencies.add(condition.property());
							condition = condition.subCondition();
						}
					}
					break;
				case INHERIT:
//					TODO: Need to implement this
					break;
				case RANDOM:
					break;
				default:
					throw new IllegalStateException("Unimplemented PropertyDef type " + definition.type());
				}
//				Handle the query
				IQuery query = definition.query();
				if(query != null) {
					while(query != null) {
						if(query.level().equals(definition.level())) {
							ICondition queryCondition = query.condition();
							while(queryCondition != null) {
								if(queryCondition.property() != null)
									dependencies.add(queryCondition.property());
								queryCondition = queryCondition.subCondition();
							}
						}
						query = query.subQuery();
					}
				}
				
//				We now have all the dependencies
				levelDependencyMap.put(baseProperty, dependencies);
			}
			propertyDefMap.put(level, levelPropertyDefMap);
			propertyDependencyMap.put(level, levelDependencyMap);
		}
		List<PropertyDef> orderedDefinitions = new ArrayList<PropertyDef>();
		for(Hierarchy level : Hierarchy.values()) {
			List<Property> levelOrderedProperties = new ArrayList<Property>();
			resolvePropertyDependencies(propertyDependencyMap.get(level), null, null, levelOrderedProperties);
//			We now have the order in which definitions need to be instantiated
			for(Property property: levelOrderedProperties) {
				HashMap<Property, PropertyDef> localDefMap = propertyDefMap.get(level);
				orderedDefinitions.add(localDefMap.get(property));
			}
		}
//		At this point, we have an ordered list of definitions
		assert(definitions.size() == orderedDefinitions.size() && definitions.containsAll(orderedDefinitions)) : 
			"definitions and orderedDefinitions are not equivalent!\n";
		root.setDefinitions(orderedDefinitions);
	}
	
	private void resolvePropertyDependencies(HashMap<Property, Collection<Property>> dependencyMap, Property property, 
			Collection<Property> visited, List<Property> orderedProperties) {
		if(property == null) {
//			First call..iterate over map and start solving
			for(java.util.Map.Entry<Property, Collection<Property>> entry : dependencyMap.entrySet()) {
				List<Property> newVisited = new ArrayList<Property>();
				Property baseProperty = entry.getKey();
				newVisited.add(baseProperty);
				resolvePropertyDependencies(dependencyMap, baseProperty, newVisited, orderedProperties);
			}
		}
		else {
			if(dependencyMap.containsKey(property)) {
				for(Property dependency : dependencyMap.get(property)) {
					if(visited.contains(dependency)) {
//						Fatal! We have a circular dependency...
//						TODO: Add some nice visual showing the circular dependency
						throw new IllegalStateException("Circular dependency detected on property '" + property + "'\n");
					}
					visited.add(dependency);
					resolvePropertyDependencies(dependencyMap, dependency, visited, orderedProperties);
				}
			}
			if(!orderedProperties.contains(property))
				orderedProperties.add(property);
		}
	}
	
	
}
