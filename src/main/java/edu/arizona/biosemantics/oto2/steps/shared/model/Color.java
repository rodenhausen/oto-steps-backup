package edu.arizona.biosemantics.oto2.steps.shared.model;

import java.io.Serializable;

public class Color implements Serializable, Comparable<Color> {

	private static int ID = 0;
	
	private int id = ID++;
	private String hex;
	private String use;
	
	public Color() { }
	
	public Color(String hex, String use) {
		super();
		this.hex = hex;
		this.use = use;
	}
	public String getHex() {
		return hex;
	}
	public void setHex(String hex) {
		this.hex = hex;
	}
	public String getUse() {
		return use;
	}
	public void setUse(String use) {
		this.use = use;
	}
	
	@Override
	public String toString() {
		return hex + ":" + use;
	}

	public int getId() {
		return id;
	}

	@Override
	public int compareTo(Color o) {
		return this.hex.compareTo(o.hex);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Color other = (Color) obj;
		if (id != other.id)
			return false;
		return true;
	}	
	
	

}
