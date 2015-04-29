package edu.arizona.biosemantics.oto2.steps.shared.model.toontology;

import java.io.Serializable;

public class OntologySynonymSubmissionStatus implements Serializable, Comparable<OntologySynonymSubmissionStatus> {

	private int id = -1;
	private int ontologySynonymSubmissionId;
	private Status status;
	private String iri;
	
	public OntologySynonymSubmissionStatus() { }
	
	public OntologySynonymSubmissionStatus(int ontologySynonymSubmissionId,
			Status status, String iri) {
		super();
		this.ontologySynonymSubmissionId = ontologySynonymSubmissionId;
		this.status = status;
		this.iri = iri;
	}
	
	public OntologySynonymSubmissionStatus(int id, int ontologySynonymSubmissionId,
			Status status, String iri) {
		super();
		this.id = id;
		this.ontologySynonymSubmissionId = ontologySynonymSubmissionId;
		this.status = status;
		this.iri = iri;
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getOntologySynonymSubmissionId() {
		return ontologySynonymSubmissionId;
	}
	public void setOntologySynonymSubmissionId(int ontologySynonymSubmissionId) {
		this.ontologySynonymSubmissionId = ontologySynonymSubmissionId;
	}
	public Status getStatus() {
		return status;
	}
	public void setStatus(Status status) {
		this.status = status;
	}
	
	public String getIri() {
		return iri;
	}

	public void setIri(String iri) {
		this.iri = iri;
	}

	public boolean hasId() {
		return id != -1;
	}

	@Override
	public int compareTo(OntologySynonymSubmissionStatus o) {
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
		OntologySynonymSubmissionStatus other = (OntologySynonymSubmissionStatus) obj;
		if (id != other.id)
			return false;
		return true;
	}
	
}
