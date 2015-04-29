package edu.arizona.biosemantics.oto2.steps.shared.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonIgnore;

import edu.arizona.biosemantics.common.biology.TaxonGroup;

public class Collection implements Serializable, Comparable<Collection> {

	private int id = -1;
	private String name = "";
	private TaxonGroup taxonGroup;
	private String secret = "";
	private List<Term> terms = new LinkedList<Term>();
	@JsonIgnore
	private Map<Commentable, List<Comment>> comments = new HashMap<Commentable, List<Comment>>();
	@JsonIgnore
	private Map<Colorable, Color> colorizations = new HashMap<Colorable, Color>();	
	private List<Color> colors = new ArrayList<Color>();
	
	public Collection() { }
	
	public Collection(String name, TaxonGroup taxonGroup, String secret, List<Term> terms) {
		this.name = name;
		this.taxonGroup = taxonGroup;
		this.secret = secret;
		this.setTerms(terms);
	}
	
	public Collection(int id, String name, TaxonGroup taxonGroup, String secret, List<Term> terms, 
			Map<Commentable, List<Comment>> comments, Map<Colorable, Color> colorizations, List<Color> colors) {
		super();
		this.id = id;
		this.name = name;
		this.taxonGroup = taxonGroup;
		this.secret = secret;
		this.setTerms(terms);
		this.comments = comments;
		this.colorizations = colorizations;
		this.colors = colors;
	}

	public int getId() {
		return id;
	}
	
	public void setTerms(List<Term> terms) {
		this.terms = terms;
		for(Term term : terms)
			term.setCollectionId(id);
	}

	public boolean hasId() {
		return id != -1;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}

	public List<Term> getTerms() {
		return terms;
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
		Collection other = (Collection) obj;
		if (id != other.id)
			return false;
		return true;
	}

	public TaxonGroup getTaxonGroup() {
		return taxonGroup;
	}

	public void setTaxonGroup(TaxonGroup taxonGroup) {
		this.taxonGroup = taxonGroup;
	}

	@Override
	public int compareTo(Collection o) {
		return this.getId() - o.getId();
	}

	public Color getColorization(Colorable coloralable) {
		return colorizations.get(coloralable);
	}

	public void setColorizations(java.util.Collection<Colorable> coloralables, Color color) {
		for(Colorable coloralable : coloralables)
			setColorization(coloralable, color);
	}
	
	public void setColorization(Colorable coloralable, Color color) {
		colorizations.put(coloralable, color);
	}
	
	public Map<Colorable, Color> getColorizations() {
		return colorizations;
	}
	
	public void setComments(Map<Commentable, List<Comment>> comments) {
		this.comments = comments;
	}

	public void setColorizations(Map<Colorable, Color> colorizations) {
		this.colorizations = colorizations;
	}

	public List<Comment> getComment(Commentable commentable) {
		return comments.get(commentable);
	}
	
	public void addComments(java.util.Collection<Commentable> commentables, Comment comment) {
		for(Commentable commentable : commentables) 
			this.addComment(commentable, comment);
	}
	
	public void addComment(Commentable commentable, Comment comment) {
		if(!comments.containsKey(commentable))
			comments.put(commentable, new LinkedList<Comment>());
		this.comments.get(commentable).add(comment);
	}
	
	public void removeComment(Commentable commentable, Comment comment) {
		if(comments.containsKey(commentable))
			comments.get(commentable).remove(comment);
	}

	public List<Color> getColors() {
		return colors;
	}
	
	public void setColors(List<Color> colors) {
		this.colors = colors;
	}

	public Map<Commentable, List<Comment>> getComments() {
		return comments;
	}

	public boolean hasColorization(Colorable colorable) {
		return this.colorizations.get(colorable) != null;
	}
	
	public boolean hasComments(Commentable commentable) {
		return this.comments.get(commentable) != null && !comments.get(commentable).isEmpty();
	}
	
}
