package com.ofamilymedia.trumpet.classes;

import java.util.HashMap;

public class Colors {
	
	private static HashMap<String, Gradient> gradients = new HashMap<String, Gradient>();
	static
	{
		gradients.put("red", new Gradient(0xffffece7,0xffffdbd3));
		gradients.put("orange", new Gradient(0xfffff4e7,0xffffead3));
		gradients.put("yellow", new Gradient(0xffffffe7,0xffffffd3));
		gradients.put("green", new Gradient(0xffecffe7,0xffdbffd3));
		gradients.put("teal", new Gradient(0xffe7ffff,0xffd3ffff));
		gradients.put("blue", new Gradient(0xffe7f0ff,0xffd3e3ff));
		gradients.put("purple", new Gradient(0xfff6e7ff,0xffeed3ff));
		gradients.put("pink", new Gradient(0xffffe7f6,0xffffd3ee));
		gradients.put("white", new Gradient(0xffffffff,0xfff4f4f4));
		gradients.put("ads", new Gradient(0xffddffd0, 0xFFc2ffad));
	};
	
	public static Gradient getGradient(String index) {
		
		if(gradients.containsKey(index)) {
			
			return gradients.get(index);
			
		}
		
		return null;
	}
	
	public static class Gradient implements java.io.Serializable {
		private static final long serialVersionUID = -8613249120645108055L;
		public int start = 0;
		public int end = 0;
		
		public Gradient(int s, int e) {
			start = s;
			end = e;
		}
		
	    public String toString() {
	        return "Colors.Gradient{" +
	                ", start=" + start +
	                ", end=" + end +
	                '}';
	    }
	}
}
