package edu.arizona.biosemantics.oto2.steps.shared.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import edu.arizona.biosemantics.common.biology.TaxonGroup;

public class Ontology implements Serializable, Comparable<Ontology> {

	private int id = -1;
	private String iri;
	private String name;
	private String acronym;
	private Set<TaxonGroup> taxonGroups = new HashSet<TaxonGroup>();
	private String browseURL;
	private int collectionId = -1;
	
	public Ontology() { }
	
	public Ontology(int id, String iri, String name, String acronym,
			Set<TaxonGroup> taxonGroups, String browseURL, int collectionId) {
		super();
		this.id = id;
		this.iri = iri;
		this.name = name;
		this.acronym = acronym;
		this.taxonGroups = taxonGroups;
		this.browseURL = browseURL;
		this.collectionId = collectionId;
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

	public String getIri() {
		return iri;
	}

	public void setIri(String iri) {
		this.iri = iri;
	}

	public String getAcronym() {
		return acronym;
	}

	public void setAcronym(String acronym) {
		this.acronym = acronym;
	}

	public Set<TaxonGroup> getTaxonGroups() {
		return taxonGroups;
	}

	public void setTaxonGroups(Set<TaxonGroup> taxonGroups) {
		this.taxonGroups = taxonGroups;
	}

	public int getCollectionId() {
		return collectionId;
	}

	public void setCollectionId(int collectionId) {
		this.collectionId = collectionId;
	}
	
	public boolean hasCollectionId() {
		return collectionId != -1;
	}
	
	public boolean hasId() {
		return id != -1;
	}
	
	public void setBrowseURL(String browseURL) {
		this.browseURL = browseURL;
	}

	public String getBrowseURL() {
		return browseURL;
	}

	@Override
	public int compareTo(Ontology o) {
		return this.getName().compareTo(o.getName());
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
		Ontology other = (Ontology) obj;
		if (id != other.id)
			return false;
		return true;
	}
		
}
