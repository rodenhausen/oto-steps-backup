package edu.arizona.biosemantics.oto2.steps.shared.model.toontology;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import edu.arizona.biosemantics.oto2.steps.shared.model.Colorable;
import edu.arizona.biosemantics.oto2.steps.shared.model.Commentable;
import edu.arizona.biosemantics.oto2.steps.shared.model.Ontology;
import edu.arizona.biosemantics.oto2.steps.shared.model.Term;

public class OntologyClassSubmission implements Serializable, Colorable, Commentable, 
		EntityQualityClass, OntologySubmission, Comparable<OntologyClassSubmission> {
	
	private int id = -1;
	private Term term;
	private String submissionTerm = "";
	private Ontology ontology;
	private String classIRI = "";
	private String superclassIRI = "";
	private String definition = "";
	private String synonyms = "";
	private String source = "";
	private String sampleSentence = "";
	private String partOfIRI = "";
	private boolean entity;
	private boolean quality;
	private String user;
	private List<OntologyClassSubmissionStatus> submissionStatuses = new LinkedList<OntologyClassSubmissionStatus>();
	
	public OntologyClassSubmission() { }
	
	public OntologyClassSubmission(int id, Term term, String submissionTerm, Ontology ontology, String classIRI,
			String superclassIRI, String definition, String synonyms, String source, String sampleSentence, 
			String partOfIRI, boolean entity, boolean quality, String user, List<OntologyClassSubmissionStatus> submissionStatuses) { 
		this.id = id;
		this.term = term;
		this.submissionTerm = submissionTerm == null ? "" : submissionTerm;
		this.ontology = ontology;
		this.classIRI = classIRI == null ? "" : classIRI;
		this.superclassIRI = superclassIRI == null ? "" : superclassIRI;
		this.definition = definition == null ? "" : definition;
		this.synonyms = synonyms == null ? "" : synonyms;
		this.source = source == null ? "" : source;
		this.sampleSentence = sampleSentence == null ? "" : sampleSentence;
		this.partOfIRI = partOfIRI == null ? "" : partOfIRI;
		this.entity = entity;
		this.quality = quality;
		this.user = user;
		this.submissionStatuses = submissionStatuses;
	}
	
	public OntologyClassSubmission(Term term, String submissionTerm, Ontology ontology, String classIRI,
			String superclassIRI, String definition, String synonyms, String source, String sampleSentence, 
			String partOfIRI, boolean entity, boolean quality, String user) { 
		this.term = term;
		this.submissionTerm = submissionTerm == null ? "" : submissionTerm;
		this.ontology = ontology;
		this.classIRI = classIRI == null ? "" : classIRI;
		this.superclassIRI = superclassIRI == null ? "" : superclassIRI;
		this.definition = definition == null ? "" : definition;
		this.synonyms = synonyms == null ? "" : synonyms;
		this.source = source == null ? "" : source;
		this.sampleSentence = sampleSentence == null ? "" : sampleSentence;
		this.partOfIRI = partOfIRI == null ? "" : partOfIRI;
		this.entity = entity;
		this.quality = quality;
		this.user = user;
	}

	public boolean hasId() {
		return id != -1;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Term getTerm() {
		return term;
	}

	public void setTerm(Term term) {
		this.term = term;
	}

	public String getSubmissionTerm() {
		return submissionTerm;
	}

	public void setSubmissionTerm(String submissionTerm) {
		this.submissionTerm = submissionTerm;
	}

	public Ontology getOntology() {
		return ontology;
	}

	public void setOntology(Ontology ontology) {
		this.ontology = ontology;
	}

	public String getSuperclassIRI() {
		return superclassIRI;
	}

	public void setSuperclassIRI(String superclassIRI) {
		this.superclassIRI = superclassIRI;
	}

	public String getDefinition() {
		return definition;
	}

	public void setDefinition(String definition) {
		this.definition = definition;
	}

	public String getSynonyms() {
		return synonyms;
	}

	public void setSynonyms(String synonyms) {
		this.synonyms = synonyms;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getSampleSentence() {
		return sampleSentence;
	}

	public void setSampleSentence(String sampleSentence) {
		this.sampleSentence = sampleSentence;
	}

	public String getPartOfIRI() {
		return partOfIRI;
	}

	public void setPartOfIRI(String partOfIRI) {
		this.partOfIRI = partOfIRI;
	}

	public boolean isEntity() {
		return entity;
	}

	public void setEntity(boolean entity) {
		this.entity = entity;
	}

	public boolean isQuality() {
		return quality;
	}

	public void setQuality(boolean quality) {
		this.quality = quality;
	}

	public List<OntologyClassSubmissionStatus> getSubmissionStatuses() {
		return submissionStatuses;
	}

	public void setSubmissionStatuses(
			List<OntologyClassSubmissionStatus> submissionStatuses) {
		this.submissionStatuses = submissionStatuses;
	}

	public String getClassIRI() {
		return classIRI;
	}
	
	public boolean hasClassIRI() {
		return classIRI != null && !classIRI.trim().isEmpty();
	}

	public void setClassIRI(String classIRI) {
		this.classIRI = classIRI;
	}

	public boolean hasSampleSentence() {
		return this.sampleSentence != null && !sampleSentence.trim().isEmpty();
	}

	public boolean hasSource() {
		return this.source != null && !source.trim().isEmpty();
	}

	public boolean hasSynonyms() {
		return this.synonyms != null && !this.synonyms.trim().isEmpty();
	}

	public boolean hasPartOfIRI() {
		return this.partOfIRI != null && !this.partOfIRI.trim().isEmpty();
	}

	public boolean hasSuperclassIRI() {
		return this.superclassIRI != null && !this.superclassIRI.trim().isEmpty();
	}

	@Override
	public int compareTo(OntologyClassSubmission o) {
		return this.getId() - o.getId();
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
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
		OntologyClassSubmission other = (OntologyClassSubmission) obj;
		if (id != other.id)
			return false;
		return true;
	}	
	
	
	

}
