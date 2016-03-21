package org.openntf.bildr;

import java.io.Serializable;
import java.util.logging.Level;

import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.NotesException;
import lotus.domino.Session;
import org.openntf.domino.xsp.XspOpenLogUtil;

public class Comment implements Serializable{
	private static final long serialVersionUID = 1L;
	//The DaoBean contains information where the data database is located e.g. on premise or on bluemix
	private static DaoBean dao = new DaoBean();

	private String unid;
	private String subject;
	private String creator;
	private String comment;
	private String unique;
	private Boolean newNote;
	
	public Comment(){
		// Constructor...
		XspOpenLogUtil.logEvent(null, this.getClass().getSimpleName().toString() + ". Constructor started. ", Level.INFO, null);	
	}
	
	
	public void create() throws NotesException{
		AppController.writeToConsole(this.getClass().getSimpleName().toString() + ". create()");	
		clear();
		newNote = true;		
		Session session = JSFUtil.getSession();		
		creator = JSFUtil.getCurrentUser().getCanonical();
		unique = String.valueOf(session.evaluate("@Unique").get(0));
	}
	
	public void loadValues(Document doc) throws NotesException {			
		AppController.writeToConsole(this.getClass().getSimpleName().toString() + ". loadValues(Document)");	
		subject = doc.getItemValueString("subject");
		comment = doc.getItemValueString("comment");
		creator = doc.getItemValueString("creator");
		newNote = false;
		unique = doc.getItemValueString("unique");
		unid = doc.getUniversalID();
		AppController.writeToConsole(this.getClass().getSimpleName().toString() + ". loadValues(Document) - Doc loaded.");	
	}
	
	public void save()throws NotesException {
		AppController.writeToConsole(this.getClass().getSimpleName().toString() + ". save()");
		Database dataDB = dao.getDatabase();
		Document doc = null;		
		if (newNote) {
			// True means never been saved
			AppController.writeToConsole(this.getClass().getSimpleName().toString() + ". save(). New document");	
			doc = dataDB.createDocument();
			doc.replaceItemValue("form", "$f-comment");
		} else {
			AppController.writeToConsole(this.getClass().getSimpleName().toString() + ". save(). Existing document");	
			doc = dataDB.getDocumentByUNID(unid);			
		}
		doc.replaceItemValue("subject", subject);
		doc.replaceItemValue("creator", creator);
		doc.replaceItemValue("comment", comment);
		doc.replaceItemValue("unique", unique);
		
		doc.computeWithForm(false, false);
		doc.save();
		doc.recycle();
		dataDB.recycle();
	}
	
	public void save(String parentID) throws NotesException {
		AppController.writeToConsole(this.getClass().getSimpleName().toString() + ". save(String parentID): " + parentID);	
		Database dataDB = dao.getDatabase();
		Document doc = null;  // PlaceHolder
		
		if (newNote) {
			// True means never been saved
			AppController.writeToConsole(this.getClass().getSimpleName().toString() + ". save(). New document.");
			doc = dataDB.createDocument();
			doc.replaceItemValue("form", "$f-comment");
		} else {
			AppController.writeToConsole(this.getClass().getSimpleName().toString() + ". save(). Existing document");
			doc = dataDB.getDocumentByUNID(unid);			
		}
		doc.replaceItemValue("parentID", parentID);
		doc.replaceItemValue("subject", subject);
		doc.replaceItemValue("creator", creator);
		doc.replaceItemValue("comment", comment);
		doc.replaceItemValue("unique", unique);
		
		doc.computeWithForm(false, false);
		doc.save();
		
		doc.recycle();
		dataDB.recycle();
	}
	
	public void clear() {
		subject = "";
		comment = "";
	}
	
	
	//general getters and setters
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getCreator() {
		return creator;
	}
	public void setCreator(String creator) {
		this.creator = creator;
	}

	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getUnique() {
		return unique;
	}
	public void setUnique(String unique) {
		this.unique = unique;
	}
	
	public Boolean getDebugMode() {
		String configval = Configuration.debugMode;
		return Boolean.valueOf(configval);
	}
	
}
