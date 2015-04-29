package edu.arizona.biosemantics.oto2.steps.server.persist.file;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.oto2.steps.server.Configuration;
import edu.arizona.biosemantics.oto2.steps.server.persist.db.OntologyDAO;
import edu.arizona.biosemantics.oto2.steps.shared.model.Collection;
import edu.arizona.biosemantics.oto2.steps.shared.model.Ontology;
import edu.arizona.biosemantics.oto2.steps.shared.model.toontology.OntologyClassSubmission;
import edu.arizona.biosemantics.oto2.steps.shared.model.toontology.OntologySynonymSubmission;
import edu.arizona.biosemantics.oto2.steps.shared.rpc.toontology.ClassExistsException;
import edu.arizona.biosemantics.oto2.steps.shared.rpc.toontology.OntologyFileException;

public class OntologyDAO2 {

	private OntologyDAO ontologyDBDAO;

	public void insertOntology(Collection collection, Ontology ontology) throws OntologyFileException {	
		OntologyManager manager = loadOntologyManager(collection);
		manager.insertOntology(ontology);
		saveOntologyManager(manager);
	}
	
	public void removeOntology(Collection collection, Ontology ontology) {
		OntologyManager manager = loadOntologyManager(collection);
		manager.removeOntology(ontology);
		saveOntologyManager(manager);
	}

	public String insertClassSubmission(Collection collection, OntologyClassSubmission submission) throws OntologyFileException, ClassExistsException {	
		OntologyManager manager = loadOntologyManager(collection);
		String result = manager.insertClassSubmission(submission);
		saveOntologyManager(manager);
		return result;
	}
	
	public void updateClassSubmission(Collection collection, OntologyClassSubmission submission) throws ClassExistsException, OntologyFileException {
		OntologyManager manager = loadOntologyManager(collection);
		//manager.updateClassSubmission(submission);
		saveOntologyManager(manager);
	}
	
	public void removeClassSubmission(Collection collection, OntologyClassSubmission submission) {
		OntologyManager manager = loadOntologyManager(collection);
		//manager.removeClassSubmission(submission);
		saveOntologyManager(manager);
	}
	
	public String insertSynonymSubmission(Collection collection, OntologySynonymSubmission submission) throws OntologyFileException { 
		OntologyManager manager = loadOntologyManager(collection);
		//String result = manager.insertSynonymSubmission(submission);
		saveOntologyManager(manager);
		//return result;
		return null;
	}	

	public void updateSynonymSubmission(Collection collection,  OntologySynonymSubmission submission) throws OntologyFileException {
		OntologyManager manager = loadOntologyManager(collection);
		//manager.updateSynonymSubmission(submission);
		saveOntologyManager(manager);
	}	

	public void removeSynonymSubmission(Collection collection, OntologySynonymSubmission submission) throws OntologyFileException {
		OntologyManager manager = loadOntologyManager(collection);
		//manager.removeSynonymSubmission(submission);
		saveOntologyManager(manager);
	}

	private void saveOntologyManager(OntologyManager manager) {
		File dir = new File(Configuration.collectionOntologyDirectory + File.separator + manager.getCollection().getId());
		dir.mkdirs();
		File file = new File(dir, "ontologyManager.ser");
		try (ObjectOutput output = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)))) {
			output.writeObject(manager);
		} catch (Exception e) {
			log(LogLevel.ERROR, "Serialization of ontology manager failed", e);
		}
	}

	private OntologyManager loadOntologyManager(Collection collection) {
		File dir = new File(Configuration.collectionOntologyDirectory + File.separator + collection.getId());
		dir.mkdirs();
		File file = new File(dir, "ontologyManager.ser");
		try(ObjectInput input = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)))) {
			OntologyManager ontologyManager = (OntologyManager) input.readObject();
			return ontologyManager;
		} catch (IOException | ClassNotFoundException e) {
			log(LogLevel.ERROR, "Deserialization of owlOntologyManager failed. Will instantiate a new one.", e);
			return initialize(collection);
		}
	}
	
	private OntologyManager initialize(Collection collection) {
		OntologyManager manager = new OntologyManager(collection);
		saveOntologyManager(manager);
		return manager;
	}
	
}
