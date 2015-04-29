package edu.arizona.biosemantics.oto2.steps.server.persist.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import edu.arizona.biosemantics.common.biology.TaxonGroup;
import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.oto2.steps.server.persist.db.Query.QueryException;
import edu.arizona.biosemantics.oto2.steps.shared.model.Collection;
import edu.arizona.biosemantics.oto2.steps.shared.model.Ontology;

public class OntologyDAO {
	
	public OntologyDAO() {} 
	
	public Ontology get(int id) throws QueryException  {
		Ontology ontology = null;
		try(Query query = new Query("SELECT * FROM otosteps_ontology WHERE id = ?")) {
			query.setParameter(1, id);
			ResultSet result = query.execute();
			while(result.next()) {
				ontology = createOntology(result);
			}
		} catch(QueryException | SQLException e) {
			log(LogLevel.ERROR, "Query Exception", e);
			throw new QueryException(e);
		}
		return ontology;
	}
	
	private Ontology createOntology(ResultSet result) throws QueryException, SQLException {
		int id = result.getInt("id");
		String externalId = result.getString("iri");
		String name = result.getString("name");
		String prefix = result.getString("acronym");
		String browseURL = result.getString("browse_url");
		int collectionId = result.getInt("collection");

		Set<TaxonGroup> taxonGroups = getTaxonGroups(id);
		return new Ontology(id, externalId, name, prefix, taxonGroups, browseURL, collectionId);
	}

	private Set<TaxonGroup> getTaxonGroups(int id) throws QueryException {
		Set<TaxonGroup> taxonGroups = new HashSet<TaxonGroup>();
		try(Query query = new Query("SELECT taxongroup FROM otosteps_ontology_taxongroup WHERE ontology = ?")) {
			query.setParameter(1, id);
			ResultSet result = query.execute();
			while(result.next()) {
				TaxonGroup taxonGroup = null;
				try {
					taxonGroup = TaxonGroup.valueOf(result.getString("taxongroup"));
					taxonGroups.add(taxonGroup);
				} catch(IllegalArgumentException e) {}
			}
		} catch(QueryException | SQLException e) {
			log(LogLevel.ERROR, "Query Exception", e);
			throw new QueryException(e);
		}
		return taxonGroups;
	}

	public Ontology insert(Ontology ontology) throws QueryException  {
		if(!ontology.hasId()) {
			try(Query insert = new Query("INSERT INTO `otosteps_ontology` (`iri`, `name`, `acronym`, "
					+ "`browse_url`, `collection`) VALUES(?, ?, ?, ?, ?)")) {
				insert.setParameter(1, ontology.getIri());
				insert.setParameter(2, ontology.getName());
				insert.setParameter(3, ontology.getAcronym());
				insert.setParameter(4, ontology.getBrowseURL());
				insert.setParameter(5, ontology.getCollectionId());
				insert.execute();
				
				ResultSet generatedKeys = insert.getGeneratedKeys();
				generatedKeys.next();
				int id = generatedKeys.getInt(1);
				ontology.setId(id);
				
				addTaxonGroups(ontology);
			} catch(QueryException | SQLException e) {
				log(LogLevel.ERROR, "Query Exception", e);
				throw new QueryException(e);
			}
		}
		return ontology;
	}
	
	private void addTaxonGroups(Ontology ontology) throws QueryException {
		for(TaxonGroup taxonGroup : ontology.getTaxonGroups()) {
			try(Query query = new Query("INSERT INTO `otosteps_ontology_taxongroup` (`ontology`, `taxongroup`) "
					+ "VALUES(?, ?)")) {
				query.setParameter(1, ontology.getId());
				query.setParameter(2, taxonGroup.toString());
				query.execute();
			} catch(QueryException e) {
				log(LogLevel.ERROR, "Query Exception", e);
				throw e;
			}
		}
	}

	public void update(Ontology ontology) throws QueryException  {		
		try(Query query = new Query("UPDATE otosteps_ontology SET iri = ?, name = ?, acronym = ?, "
				+ "browse_url = ?, collection = ? WHERE id = ?")) {
			query.setParameter(1, ontology.getIri());
			query.setParameter(2, ontology.getName());
			query.setParameter(3, ontology.getAcronym());
			query.setParameter(4, ontology.getBrowseURL());
			query.setParameter(5, ontology.getCollectionId());
			query.setParameter(6, ontology.getId());
			query.execute();
			
			ensureTaxonGroups(ontology);
		} catch(QueryException e) {
			log(LogLevel.ERROR, "Query Exception", e);
			throw e;
		}
	}
	
	private void ensureTaxonGroups(Ontology ontology) throws QueryException {
		Set<TaxonGroup> currentTaxonGroups = this.getTaxonGroups(ontology.getId());
		Set<TaxonGroup> toRemoveTaxonGroups = new HashSet<TaxonGroup>(currentTaxonGroups);
		toRemoveTaxonGroups.removeAll(ontology.getTaxonGroups());
		Set<TaxonGroup> toAddTaxonGroups = new HashSet<TaxonGroup>(ontology.getTaxonGroups());
		toAddTaxonGroups.removeAll(currentTaxonGroups);
		
		for(TaxonGroup taxonGroup : toRemoveTaxonGroups) {
			try(Query query = new Query("DELETE FROM otosteps_ontology WHERE id = ? AND taxongroup = ?")) {
				query.setParameter(1, ontology.getId());
				query.setParameter(2, taxonGroup.toString());
			} catch(QueryException e) {
				log(LogLevel.ERROR, "Query Exception", e);
				throw e;
			}
		}
		for(TaxonGroup taxonGroup : toAddTaxonGroups) {
			try(Query query = new Query("INSERT INTO `otosteps_ontology_taxongroup` (`ontology`, `taxongroup`) "
					+ "VALUES(?, ?)")) {
				query.setParameter(1, ontology.getId());
				query.setParameter(2, taxonGroup.toString());
				query.execute();
			} catch(QueryException e) {
				log(LogLevel.ERROR, "Query Exception", e);
				throw e;
			}
		}
	}

	public void remove(Ontology ontology) throws QueryException  {
		try(Query query = new Query("DELETE FROM otosteps_ontology WHERE id = ?")) {
			query.setParameter(1, ontology.getId());
			query.execute();
			
			removeTaxonGroups(ontology);
		} catch(QueryException e) {
			log(LogLevel.ERROR, "Query Exception", e);
			throw e;
		}
	}

	private void removeTaxonGroups(Ontology ontology) throws QueryException {
		try(Query query = new Query("DELETE FROM otosteps_ontology_taxongroup WHERE ontology = ?")) {
			query.setParameter(1, ontology.getId());
			query.execute();
			
		} catch(QueryException e) {
			log(LogLevel.ERROR, "Query Exception", e);
			throw e;
		}
	}

	public List<Ontology> getOntologiesForCollection(Collection collection) throws QueryException {
		List<Ontology> ontologies = new LinkedList<Ontology>();
		try(Query query = new Query("SELECT id FROM otosteps_ontology WHERE collection = ? OR collection = -1")) {
			query.setParameter(1, collection.getId());
			ResultSet result = query.execute();
			while(result.next()) {
				int id = result.getInt(1);
				Ontology ontology = get(id);
				if(ontology != null)
					ontologies.add(ontology);
			}
		} catch(QueryException | SQLException e) {
			log(LogLevel.ERROR, "Query Exception", e);
			throw new QueryException(e);
		}
		return ontologies;
	}

	public List<Ontology> getRelevantOntologiesForCollection(Collection collection) throws QueryException {
		List<Ontology> collectionOntologies = this.getOntologiesForCollection(collection);
		List<Ontology> relevantOntologies = new LinkedList<Ontology>();
		for(Ontology collectionOntology : collectionOntologies) 
			if(collectionOntology.getTaxonGroups().contains(collection.getTaxonGroup()))
				relevantOntologies.add(collectionOntology);
		return relevantOntologies;
	}
	
	public List<Ontology> getPermanentOntologies() throws QueryException {
		List<Ontology> ontologies = new LinkedList<Ontology>();
		try(Query query = new Query("SELECT id FROM otosteps_ontology WHERE collection = -1")) {
			ResultSet result = query.execute();
			while(result.next()) {
				int id = result.getInt(1);
				Ontology ontology = get(id);
				if(ontology != null)
					ontologies.add(ontology);
			}
		} catch(QueryException | SQLException e) {
			log(LogLevel.ERROR, "Query Exception", e);
			throw new QueryException(e);
		}
		return ontologies;
	}
	
}
