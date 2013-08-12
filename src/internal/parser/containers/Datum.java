package internal.parser.containers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class Datum {
	Object data;
	DatumType type;
	
	protected Datum() {
//		Stops empty constructor initialization
	}
	
	private Datum(Integer data) {
		this.data 	= data;
		this.type 	= DatumType.INT;
	}
	
	private Datum(Float data) {
		this.data	= data;
		this.type	= DatumType.FLOAT;
	}
	
	private Datum(String data) {
		this.data	= data;
		this.type	= DatumType.STRING;
	}
	
	private Datum(Boolean data) {
		this.data	= data;
		this.type	= DatumType.BOOL;
	}
	
	public Object data() {
		return data;
	}
	
	public DatumType type() {
		return type;
	}
	
	public Datum toInt() {
		Datum datum = null;
		
		if(type.equals(Datum.Int.class)) {
			datum	= new Datum.Int(new Integer((Integer) this.data));
		}
		
		else if(type.equals(Datum.Flt.class)) {
			datum	= new Datum.Int(new Integer((Integer.parseInt(Float.toString((Float) this.data)))));
		}
		
		else if(type.equals(Datum.Str.class)) {
			datum	= new Datum.Int(new Integer(Integer.parseInt((String) this.data)));
		}
		datum.type	= DatumType.INT;
		return datum;
	}
	
	public Datum toFlt() {
		Datum datum = null;
		
		if(type.equals(Datum.Int.class)) {
			datum	= new Datum.Flt(new Float((Integer) this.data));
		}
		
		else if(type.equals(Datum.Flt.class)) {
			datum	= new Datum.Flt(new Float((Float) this.data));
		}
		
		else if(type.equals(Datum.Str.class)) {
			datum	= new Datum.Flt(new Float(Float.parseFloat((String) this.data)));
		}
		datum.type	= DatumType.FLOAT;
		return datum;
	}
	
	public Datum toStr() {
		Datum datum = null;
		
		if(type.equals(Datum.Int.class)) {
			datum	= new Datum.Str(Integer.toString((Integer)this.data));
		}
		
		else if(type.equals(Datum.Flt.class)) {
			datum	= new Datum.Str(Float.toString((Float) this.data));
		}
		
		else if(type.equals(Datum.Str.class)) {
			datum	= new Datum.Str(new String((String) this.data));
		}
		
		datum.type	= DatumType.STRING;
		
		return datum;
	}
	
	
	
	
	
	public static class Int extends Datum {
		public Int(Integer data) {
			super(data);
		}
		
		@Override
		public String toString() {
			return Integer.toString((Integer) data);
		}

		@Override
		public List<Datum> split(int size) {
			List<Datum> result = new ArrayList<Datum>();
			
			int availableQty = (Integer) data;
			while(result.size() < size) {
				if(result.size() == size - 1) {
//					Last element..add the remaining
					result.add(new Datum.Int(availableQty));
				}
				else {
					int qty = 0 + ((int) (Math.random() * (availableQty + 1)));
					result.add(new Datum.Int(qty));
					availableQty -= qty;
				}
			}
			
//			TODO: Remove this
			int sum = 0;
			for(Datum d : result) {
				sum += (Integer) d.data;
			}
			assert sum == (Integer) data : "Datum.Int.allocate failed to allocate accurately!\n"
					+ "Total allocated :" + sum + "\n"
					+ "Available       :" + data + "\n";
			return result;
		}
	}
	
	
	
	public static class Flt extends Datum {
		public Flt(Float data) {
			super(data);
		}
		
		@Override
		public String toString() {
			return Float.toString((Float)data);
		}

		@Override
		public List<Datum> split(int size) {
			List<Datum> result = new ArrayList<Datum>();
			
			float availableQty = (Float) data;
			while(result.size() < size) {
				float qty = 0 + ((float) (Math.random() * (availableQty + 1)));
				result.add(new Datum.Flt(qty));
				availableQty -= qty;
			}
			
//			TODO: Remove this
			float sum = 0;
			for(Datum d : result) {
				sum += (Float) d.data;
			}
			assert sum == (Integer) data : "Datum.Int.allocate failed to allocate accurately!\n"
					+ "Total allocated :" + sum + "\n"
					+ "Available       :" + data + "\n";
			return result;
		}
	}
	
	
	
	public static class Str extends Datum {
		public Str(String data) {
			super(data);
		}
		
		@Override
		public String toString() {
			return (String) data;
		}

		@Override
		public List<Datum> split(int size) {
			throw new IllegalStateException("Cannot allocate value of type " + this.getClass().getName());
		}
	}
	
	
	
	public static class Bool extends Datum {
		public Bool(Boolean data) {
			super(data);
		}
		
		@Override
		public String toString() {
			return Boolean.toString((Boolean) data);
		}

		@Override
		public List<Datum> split(int size) {
			// TODO Auto-generated method stub
			return null;
		}
	}

	public abstract List<Datum> split(int size);
	
	public Datum add(Datum datum) {
		assert (type.equals(DatumType.INT) || type.equals(DatumType.FLOAT)) : "Cannot add Datum types " + this.type + " , " + datum.type;
		
		int data 			= (Integer) this.data;
		switch(datum.type) {
		case BOOL:
			break;
		case FLOAT:
			return new Datum.Flt(data + (Float) datum.data);
		case INT:
			return new Datum.Int(data + (Integer) datum.data);
		case STRING:
			break;
		default:
			break;
		}
		throw new IllegalArgumentException("Cannot add Datum types " + this.type + " , " + datum.type);
	}
	public Datum subtract(Datum datum) {
		assert (type.equals(DatumType.INT) || type.equals(DatumType.FLOAT)) : "Cannot add Datum types " + this.type + " , " + datum.type;
		
		int data 			= (Integer) this.data;
		switch(datum.type) {
		case BOOL:
			break;
		case FLOAT:
			return new Datum.Flt(data - (Float) datum.data);
		case INT:
			return new Datum.Int(data - (Integer) datum.data);
		case STRING:
			break;
		default:
			break;
		}
		throw new IllegalArgumentException("Cannot subtract Datum types " + this.type + " , " + datum.type);
	}
	public Datum multiply(Datum datum) {
		assert (type.equals(DatumType.INT) || type.equals(DatumType.FLOAT)) : "Cannot add Datum types " + this.type + " , " + datum.type;
		
		int data 			= (Integer) this.data;
		switch(datum.type) {
		case BOOL:
			break;
		case FLOAT:
			return new Datum.Flt(data * (Float) datum.data);
		case INT:
			return new Datum.Int(data * (Integer) datum.data);
		case STRING:
			break;
		default:
			break;
		}
		throw new IllegalArgumentException("Cannot add Datum types " + this.type + " , " + datum.type);
	}
	public Datum divide(Datum datum) {
		assert (type.equals(DatumType.INT) || type.equals(DatumType.FLOAT)) : "Cannot add Datum types " + this.type + " , " + datum.type;
		
		int data 			= (Integer) this.data;
		switch(datum.type) {
		case BOOL:
			break;
		case FLOAT:
			return new Datum.Flt(data / (Float) datum.data);
		case INT:
			return new Datum.Int(data / (Integer) datum.data);
		case STRING:
			break;
		default:
			break;
		}
		throw new IllegalArgumentException("Cannot add Datum types " + this.type + " , " + datum.type);
	}
	
	
	public enum DatumType {
		INT(Datum.Int.class),
		FLOAT(Datum.Flt.class),
		STRING(Datum.Str.class),
		BOOL(Datum.Bool.class),
		;
		
		private Class<?> clazz;
		
		private DatumType(Class<?> clazz) {
			this.clazz = clazz;
		}
		
		protected static DatumType parse(Class<?> clazz) {
			for(DatumType dt : values()) {
				if(dt.clazz.equals(clazz))
					return dt;
			}
			throw new IllegalArgumentException(clazz.getName() + " is not a valid DatumType!");
		}
	}
}
