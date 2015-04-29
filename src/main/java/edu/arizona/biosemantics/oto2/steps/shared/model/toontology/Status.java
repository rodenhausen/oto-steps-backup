package edu.arizona.biosemantics.oto2.steps.shared.model.toontology;

import java.io.Serializable;

public class Status implements Serializable, Comparable<Status> {

	private int id = -1;
	private String name;
	
	public Status() { }
	
	public Status(int id, String name) {
		super();
		this.id = id;
		this.name = name;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public boolean hasId() {
		return id != -1;
	}

	@Override
	public int compareTo(Status o) {
		return this.name.compareTo(o.getName());
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
		Status other = (Status) obj;
		if (id != other.id)
			return false;
		return true;
	}
		
}
