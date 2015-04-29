package edu.arizona.biosemantics.oto2.steps.shared.model;

import java.io.Serializable;

public class Term implements Serializable, Comparable<Term>, Colorable, Commentable {

	private int id = -1;
	private String term;
	private String originalTerm;
	private String category;
	private boolean removed = false;
	private int collectionId;
	
	public Term() { }

	public Term(int id, String term, String originalTerm, String category, boolean removed, int collectionId) {
		this.id = id;
		this.term = term;
		this.originalTerm = originalTerm;
		this.category = category;
		this.removed = removed;
		this.collectionId = collectionId;
	}
	
	public Term(int id, String term, String category) {
		super();
		this.id = id;
		this.term = term;
		this.originalTerm = term;
		this.category = category;
	}

	public Term(String term, String category) {
		super();
		this.term = term;
		this.originalTerm = term;
		this.category = category;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTerm() {
		return term;
	}

	public void setTerm(String term) {
		this.term = term;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}
	
	public String getOriginalTerm() {
		return originalTerm;
	}

	public boolean hasId() {
		return id != -1;
	}

	public void setOriginalTerm(String originalTerm) {
		this.originalTerm = originalTerm;
	}

	public int getCollectionId() {
		return collectionId;
	}

	public void setCollectionId(int collectionId) {
		this.collectionId = collectionId;
	}

	public boolean hasChangedSpelling() {
		return !term.equals(originalTerm);
	}

	public boolean isRemoved() {
		return removed;
	}

	public void setRemoved(boolean removed) {
		this.removed = removed;
	}

	@Override
	public int compareTo(Term o) {
		return this.getTerm().compareTo(o.getTerm());
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
		Term other = (Term) obj;
		if (id != other.id)
			return false;
		return true;
	}
		
}
