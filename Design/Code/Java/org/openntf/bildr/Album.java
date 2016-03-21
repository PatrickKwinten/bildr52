package org.openntf.bildr;

import java.io.Serializable;
import java.util.Date;
import java.util.Vector;
import java.util.logging.Level;

import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.NotesException;
import lotus.domino.Session;
import lotus.domino.View;
import com.ibm.commons.util.io.json.JsonJavaObject;

import org.openntf.domino.xsp.XspOpenLogUtil;

public class Album implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	//The DaoBean contains information where the data database is located e.g. on premise or on bluemix
	static DaoBean dao = new DaoBean();
	
	//here all helper properties
	private Boolean newNote;
	private String unid;
	private boolean readOnly;
	private JsonJavaObject data;
	//here all document fields...
	
	private String subject;
	private static String notesName;//creator
	private Date created;
	private String description;
	private Vector pictures;
	
	
	public Album() throws NotesException{
		// Constructor...
		XspOpenLogUtil.logEvent(null, null, this.getClass().getSimpleName().toString() + ". Constructor started.", Level.INFO, null);
		setReadOnly(true);
	}
	
	public void create() throws NotesException{
		AppController.writeToConsole(this.getClass().getSimpleName().toString() + ". create()");
		clear();
		setReadOnly(false);
		newNote = true;		
		Session session = JSFUtil.getSession();
		notesName = JSFUtil.getCurrentUser().getCanonical();
		AppController.writeToConsole(this.getClass().getSimpleName().toString() + ". create(). User = " + notesName);
		session.recycle();
	}
	
	public void clear() {
		System.out.println("Album.java clear()");
		subject = "";
		description = "";
	}
	public void reset() {
		AppController.writeToConsole(this.getClass().getSimpleName().toString() + ". reset()");
		subject = null;
		description = null;
	}
	
	public void remove(String unid) throws NotesException{
		AppController.writeToConsole(this.getClass().getSimpleName().toString() + ". remove(String unid) : " + unid);
		Database dataDB = dao.getDatabase();
		Document doc = dataDB.getDocumentByUNID(unid);
		if (null == doc) {
			XspOpenLogUtil.logError(null, null, this.getClass().getSimpleName().toString() + ". remove(). Document not found.", Level.INFO, null);
		} else {
			doc.removePermanently(true);
		}
		doc.recycle();
		dataDB.recycle();
	}
	
	
	public void loadByName(String notesName) throws NotesException{
		AppController.writeToConsole(this.getClass().getSimpleName().toString() + ". loadByName(String notesName) : " + notesName);
		Database dataDB = dao.getDatabase();
		View dataVW = dataDB.getView("$v-albumsJSON-multi");
		Document doc = dataVW.getDocumentByKey(notesName);
		if (null == doc) {
			XspOpenLogUtil.logError(null, null, this.getClass().getSimpleName().toString() + ". loadByName(String notesName). Document not found, severityType, doc)",Level.INFO,null);
		} else {
			AppController.writeToConsole(this.getClass().getSimpleName().toString() + ". loadByName(String notesName). Document found. ");
			loadValues(doc);
		}
		doc.recycle();
		dataDB.recycle();
	}
	
	public void loadValues(Document doc) throws NotesException {
		AppController.writeToConsole(this.getClass().getSimpleName().toString() + ". loadValues(Document doc)");
		subject = doc.getItemValueString("Tx_name");		
		notesName = doc.getItemValueString("Au_Author");
		description = doc.getItemValueString("Tx_Description");
		created = doc.getCreated().toJavaDate();
		newNote = false;
		unid = doc.getUniversalID();
		setPictures(doc.getItemValue("Tx_pictureIds"));//"Tx_pictureIds"
		AppController.writeToConsole(this.getClass().getSimpleName().toString() + ". loadValues(Document doc). Document loaded.");
	}
	
	public void save() throws NotesException {
		AppController.writeToConsole(this.getClass().getSimpleName().toString() + ". save()");
		Database dataDB = dao.getDatabase();
		Document doc = null;  // PlaceHolder		
		if (newNote) {
			AppController.writeToConsole(this.getClass().getSimpleName().toString() + ". save(). New document.");
			doc = dataDB.createDocument();
			doc.replaceItemValue("form", "$f-album");
		} else {
			AppController.writeToConsole(this.getClass().getSimpleName().toString() + ". save(). Existing document.");
			doc = dataDB.getDocumentByUNID(unid);			
		}
		doc.replaceItemValue("Tx_name", subject);
		doc.replaceItemValue("Tx_Description", description);
		doc.computeWithForm(false, false);
		doc.save();		
		doc.recycle();
		dataDB.recycle();
		readOnly = true;		
	}
	
	public void loadByUnid(String unid) throws NotesException{
		AppController.writeToConsole(this.getClass().getSimpleName().toString() + ". loadByUnid(String unid) : " + unid);
		Database dataDB = dao.getDatabase();
		Document doc = dataDB.getDocumentByUNID(unid);		
		if (null == doc) {
			XspOpenLogUtil.logError(null, null, this.getClass().getSimpleName().toString() + ". loadByUnid(String unid). Document not found.", Level.INFO, null);
		} else {
			AppController.writeToConsole(this.getClass().getSimpleName().toString() + ". loadByUnid(String unid). Document loaded");
			loadValues(doc);
		}
		doc.recycle();
		dataDB.recycle();
	}
	
	public Boolean isAuthor(String name) throws NotesException{
		AppController.writeToConsole(this.getClass().getSimpleName().toString() + ". isAuthor(String name): " + name);
		Session session = JSFUtil.getSession();
		String uName = session.getEffectiveUserName().toString().toLowerCase();
		String cName = name.toLowerCase();
		if (uName.equals(cName)){
			AppController.writeToConsole(this.getClass().getSimpleName().toString() + ". isAuthor(String name): true" );
			return true;
		}
		else{
			AppController.writeToConsole(this.getClass().getSimpleName().toString() + ". isAuthor(String name): false" );
			return false;
		}
	}
	
	
	
	
	//general getters and setters
	public String getUnid() {
		return unid;
	}
	
	public String getNotesName() {
		return notesName;
	}
	public void setNotesName(String notesName) {
		this.notesName = notesName;
	}
	
	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}
	public boolean isReadOnly() {
		return readOnly;
	}
	
	public Boolean getNewNote() {
		return newNote;
	}
	public void setNewNote(Boolean newNote) {
		this.newNote = newNote;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}
	public String getSubject() {
		return subject;
	}


	public void setCreated(Date created) {
		this.created = created;
	}
	public Date getCreated() {
		return created;
	}


	public void setDescription(String description) {
		this.description = description;
	}
	public String getDescription() {
		return description;
	}

	public void setPictures(Vector pictures) {
		this.pictures = pictures;
	}

	public Vector getPictures() {
		return pictures;
	}
	
	public Boolean getDebugMode() {
		String configval = Configuration.debugMode;
		return Boolean.valueOf(configval);
	}	





}
