package wat.shop.utils;

import java.util.Comparator;
import java.util.HashMap;

import hla.rti1516e.encoding.EncoderFactory;

public class Interaction{
	
	private String interactionName;
	private HashMap<String, byte[]> parameters = new HashMap<String, byte[]>();
	double time;
	
	public Interaction(String name) {
		this.interactionName = name;
	}
	
	public String getName() {
		return interactionName;
	}
	
	public void addParameter(String name, byte[] bytes) {
		if(!parameters.containsKey(name)) {
			parameters.put(name, bytes);
		}
	}
	
	public int getParamSize() {
		return parameters.size();
	}
	
	public byte[] getParameter(String name) {
		if(parameters.containsKey(name)) {
			return parameters.get(name);
		}
		return null;
	}
	
	public void setTime(double time) {
		this.time = time;
	}
	
	public double getTime() {
		return this.time;
	}
	
	public int getParameterInt(EncoderFactory factory, String name) {
		byte[] data = parameters.get(name);
		if(data != null) {
			return Encoder.decodeInt(factory, data);
		}
		return Integer.MAX_VALUE;
	}
	
	public double getParameterDouble(EncoderFactory factory, String name) {
		byte[] data = parameters.get(name);
		if(data != null) {
			return Encoder.decodeDouble(factory, data);
		}
		return Double.MAX_VALUE;
	}

	public static class TimeComparator implements Comparator<Interaction> {

		@Override
		public int compare(Interaction o1, Interaction o2) {
			if(o1 == o2) {
				return 0;
			}
			else if(o1.getTime() > o2.getTime()) 
				return 1;
			else
				return -1;
		}
		
	}
	
}
