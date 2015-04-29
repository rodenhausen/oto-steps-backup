package edu.arizona.biosemantics.oto2.steps.shared.model.toontology;

import java.io.Serializable;

public class OntologyClassSubmissionStatus implements Serializable, Comparable<OntologyClassSubmissionStatus> {

	private int id = -1;
	private int ontologyClassSubmissionId;
	private Status status;
	private String iri;
	
	public OntologyClassSubmissionStatus() { }
	
	public OntologyClassSubmissionStatus(int ontologyClassSubmissionId,
			Status status, String iri) {
		super();
		this.ontologyClassSubmissionId = ontologyClassSubmissionId;
		this.status = status;
		this.iri = iri;
	}
	
	public OntologyClassSubmissionStatus(int id, int ontologyClassSubmissionId,
			Status status, String iri) {
		super();
		this.id = id;
		this.ontologyClassSubmissionId = ontologyClassSubmissionId;
		this.status = status;
		this.iri = iri;
	}

	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getOntologyClassSubmissionId() {
		return ontologyClassSubmissionId;
	}
	public void setOntologyClassSubmissionId(int ontologyClassSubmissionId) {
		this.ontologyClassSubmissionId = ontologyClassSubmissionId;
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
	public int compareTo(OntologyClassSubmissionStatus o) {
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
		OntologyClassSubmissionStatus other = (OntologyClassSubmissionStatus) obj;
		if (id != other.id)
			return false;
		return true;
	}	
	
}
