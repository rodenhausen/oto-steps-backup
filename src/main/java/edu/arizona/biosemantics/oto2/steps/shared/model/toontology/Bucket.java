package edu.arizona.biosemantics.oto2.steps.shared.model.toontology;

import java.io.Serializable;

public class Bucket implements Serializable, Comparable<Bucket> {

	public static int ID;
	
	private int id = ID++;
	private String name = "";
	
	public Bucket() { }
	
	public Bucket(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String text) {
		this.name = text;
	}
	public int getId() {
		return id;
	}

	@Override
	public int compareTo(Bucket o) {
		return this.getId() - o.getId();
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
		Bucket other = (Bucket) obj;
		if (id != other.id)
			return false;
		return true;
	}
	
	
	
}