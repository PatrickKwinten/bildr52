package org.openntf.bildr;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;

import lotus.domino.Session;
import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.NotesException;

import org.openntf.domino.xsp.XspOpenLogUtil;

public class Picture implements Serializable{

	private static final long serialVersionUID = 1L;
	
	private String unid;
	private String subject;
	private String description;
	private String thumb;
	private String small;
	private String original;
	private String creator;
	private String carousel;
	private Date created;
	private Boolean owner;
	
	private static DaoBean dao = new DaoBean();	
	
	public Picture(){
		// Constructor...
		XspOpenLogUtil.logEvent(null, this.getClass().getSimpleName().toString() + ". Constructor started. Debug = " + getDebugMode(), Level.INFO, null);		
	}
	
	public void reset() {
		AppController.writeToConsole(this.getClass().getSimpleName().toString() + ". reset()");	
		subject = null;
		description = null;
		thumb = null;
		small = null;
		original = null;
		creator = null;
		unid = null;
		carousel = null;
		setOwner(true);
	}
	
	
	public void loadByUnid(String unid) throws NotesException{
		AppController.writeToConsole(this.getClass().getSimpleName().toString() + ". loadByUnid(String unid) : " + unid);	
		Database dataDB = dao.getDatabase();
		Document doc = dataDB.getDocumentByUNID(unid);		
		if (null == doc) {
			XspOpenLogUtil.logError(null, null, this.getClass().getSimpleName().toString() + ". loadByUnid(String unid). Document not found", Level.SEVERE, null);			
		} else {
			AppController.writeToConsole(this.getClass().getSimpleName().toString() + ". loadByUnid(String) - Document found");			
			loadValues(doc);
		}
		doc.recycle();
		dataDB.recycle();
	}
	
	public void loadValues(Document doc) throws NotesException {
		AppController.writeToConsole(this.getClass().getSimpleName().toString() + ". loadValues(Document)");	
		subject = doc.getItemValueString("Photo_Title");
		description = doc.getItemValueString("PhotoDescription");
		creator = doc.getItemValueString("Au_Author");
		thumb = doc.getItemValueString("Photo_ThumbFilename");
		small = doc.getItemValueString("Photo_SmallFilename");
		original = doc.getItemValueString("Photo_OriginalFilename");
		created = doc.getCreated().toJavaDate();
		unid = doc.getUniversalID();		
		carousel = doc.getItemValueString("Photo_Carousel");
		AppController.writeToConsole(this.getClass().getSimpleName().toString() + ". loadValues(Document) - Document loaded");	
	}
	
	public Boolean isAuthor(String name) throws NotesException{
		AppController.writeToConsole(this.getClass().getSimpleName().toString() + ". isAuthor(name) : " + name);	
		Session session = JSFUtil.getSession();
		String uName = session.getEffectiveUserName().toString().toLowerCase();
		String cName = name.toLowerCase();
		if (uName.equals(cName)){
			return true;
		}
		else{
			return false;
		}
	}	
	
	public ArrayList<String> loadCategories() throws NotesException {		
		return null;
	}
	

	//general getters and setters	

	public Boolean getDebugMode() {
		String configval = Configuration.debugMode;
		return Boolean.valueOf(configval);
	}
	
	public String getUnid() {
		return unid;
	}
	public void setUnid(String unid) {
		this.unid = unid;
	}

	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getThumb() {
		return thumb;
	}
	public void setThumb(String thumb) {
		this.thumb = thumb;
	}
	
	public String getSmall() {
		return small;
	}
	public void setSmall(String small) {
		this.small = small;
	}
	
	public String getOriginal() {
		return original;
	}
	public void setOriginal(String original) {
		this.original = original;
	}

	public String getCreator() {
		return creator;
	}
	public void setCreator(String creator) {
		this.creator = creator;
	}
	
	public String getCarousel() {
		return carousel;
	}
	public void setCarousel(String carousel) {
		this.carousel = carousel;
	}
	
	public void setCreated(Date created) {
		this.created = created;
	}
	public Date getCreated() {
		return created;
	}
	
	public void setOwner(Boolean owner) {
		this.owner = owner;
	}
	public Boolean getOwner() {
		return owner;
	}
	
}
