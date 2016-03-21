package org.openntf.bildr;

import java.io.Serializable;
import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.NotesException;
import lotus.domino.Session;
import lotus.domino.View;
import lotus.domino.RichTextItem;

import java.io.File;
import java.util.logging.Level;

import com.ibm.xsp.component.UIFileuploadEx.UploadedFile;
import com.ibm.xsp.http.IUploadedFile;

import org.openntf.domino.xsp.XspOpenLogUtil;

public class Profile implements Serializable{

	private static final long serialVersionUID = 1L;
	static DaoBean dao = new DaoBean();
	
	//here all helper properties
	private Boolean newNote;
	private String unid;
	private boolean readOnly;
	public static Boolean debugMode;
	
	//all the document fields...
	private String firstName;
	private String lastName;
	private static String notesName;
	private String title;
	private String email;
	private String camera;
	private String avatar;	
	private String unique;
	
	private UploadedFile uploadedFile;
	
	public Profile() throws NotesException{
		// Constructor...
		XspOpenLogUtil.logEvent(null, this.getClass().getSimpleName().toString() + ". Constructor started.", Level.INFO, null);
		setReadOnly(true);
	}
	
	public void create() throws NotesException{
		AppController.writeToConsole(this.getClass().getSimpleName().toString() + ". create()");
		clear();
		setReadOnly(false);
		newNote = true;		
		Session session = JSFUtil.getSession();
		notesName = JSFUtil.getCurrentUser().getCanonical();
		unique = String.valueOf(session.evaluate("@Unique").get(0));
		AppController.writeToConsole(this.getClass().getSimpleName().toString() + ". create() - Profile created for: " + notesName);
		session.recycle();
	}
	
	public void loadByName(String notesName) throws NotesException{
		AppController.writeToConsole(this.getClass().getSimpleName().toString() + ". loadByName(String notesName): " + notesName);			
		Database dataDB = dao.getDatabase();	
		View dataVW = dataDB.getView("$v-profilesJSON-multi");
		Document doc = dataVW.getDocumentByKey(notesName);
		if (null == doc) {
			XspOpenLogUtil.logError(null, null, this.getClass().getSimpleName().toString() + ". loadByName(String notesName). Document not found. " + notesName, Level.SEVERE, null);
		} else {
			AppController.writeToConsole(this.getClass().getSimpleName().toString() + ". loadByName(String notesName). Document found.");	
			loadValues(doc);
		}
		doc.recycle();
		dataDB.recycle();
	}
	
	public void loadByUnid(String unid) throws NotesException{
		AppController.writeToConsole(this.getClass().getSimpleName().toString() + ". loadByUnid(String unid): " + unid);			
		Database dataDB = dao.getDatabase();
		Document doc = dataDB.getDocumentByUNID(unid);		
		if (null == doc) {
			XspOpenLogUtil.logError(null, null, this.getClass().getSimpleName().toString() + ". loadByUnid(String unid). Document not found. " + unid, Level.SEVERE, null);	
		} else {
			AppController.writeToConsole(this.getClass().getSimpleName().toString() + ". loadByUnid(String unid): Document found.");		
			loadValues(doc);
		}
		doc.recycle();
		dataDB.recycle();
	}
	
	public void loadValues(Document doc) throws NotesException {
		AppController.writeToConsole(this.getClass().getSimpleName().toString() + ". loadValues(Document doc)");		
		firstName = doc.getItemValueString("firstname");
		lastName = doc.getItemValueString("lastname");
		notesName = doc.getItemValueString("notesname");
		title = doc.getItemValueString("title");
		email = doc.getItemValueString("email");
		camera = doc.getItemValueString("camera");
		avatar = doc.getItemValueString("attachments");
		newNote = false;
		unique = doc.getItemValueString("unique");
		unid = doc.getUniversalID();
		AppController.writeToConsole(this.getClass().getSimpleName().toString() + ". loadValues(Document doc). Document loaded.");
	}
	
	public void save() throws NotesException {
		Database dataDB = dao.getDatabase();
		Document doc = null;		
		if (newNote) {
			// True means never been saved
			AppController.writeToConsole(this.getClass().getSimpleName().toString() + ". save(). New document");
			doc = dataDB.createDocument();
			doc.replaceItemValue("form", "$f-profile");
		} else {
			AppController.writeToConsole(this.getClass().getSimpleName().toString() + ". save(). Existing document");	
			doc = dataDB.getDocumentByUNID(unid);			
		}		
		if (uploadedFile != null ) {
			AppController.writeToConsole(this.getClass().getSimpleName().toString() + ". save(). Uploaded file.");		
			//get the uploaded file
	        IUploadedFile iUploadedFile = uploadedFile.getUploadedFile();

	        //get the server file (with a cryptic filename)
	        File serverFile = iUploadedFile.getServerFile();
			if (getDebugMode() == true){
				System.out.println("Profile save() - A file was uploaded... serverFile: "  + serverFile.getName());
			}
	        //get the original filename
	        String fileName = iUploadedFile.getClientFileName();    
	       
	         //File correctedFile = new File( dataDB + File.separator + fileName );
	        AppController.writeToConsole(this.getClass().getSimpleName().toString() + ". save(). Uploaded file. ClientFileName: " + fileName);
	        File correctedFile = new File( serverFile.getParentFile().getAbsolutePath() + File.separator + fileName );	        
	        AppController.writeToConsole(this.getClass().getSimpleName().toString() + ". save(). Uploaded file. correctedFile: " + correctedFile.getName());
	        
	        //rename the file to its original name
	        boolean success = serverFile.renameTo(correctedFile);
	        if (success) {
	        	if (doc.hasItem("files")) {
	        		AppController.writeToConsole(this.getClass().getSimpleName().toString() + ". save(). Uploaded file. Has item files");
	                doc.removeItem("files");
	                doc.save();
	                }
	        	RichTextItem rtFiles = doc.createRichTextItem("files");
	            rtFiles.embedObject(lotus.domino.EmbeddedObject.EMBED_ATTACHMENT, "", correctedFile.getAbsolutePath(), null);
	            doc.save();	
	            rtFiles.recycle();
	        }	        
	        //if we're done: rename it back to the original filename, so it gets cleaned up by the server
            correctedFile.renameTo( iUploadedFile.getServerFile() );
		}		
				
		// Common elements to save
		doc.replaceItemValue("firstname", firstName);
		doc.replaceItemValue("lastname", lastName);
		doc.replaceItemValue("notesname", notesName);
		doc.replaceItemValue("title", title);
		doc.replaceItemValue("email	", email);
		doc.replaceItemValue("camera", camera);
		doc.replaceItemValue("avatar", avatar);
		doc.replaceItemValue("unique", unique);
		doc.computeWithForm(false, false);
		doc.save();		
		
		doc.recycle();
		dataDB.recycle();
		readOnly = true;		
	}
	
	public void clear() {
		if (getDebugMode() == true){
			System.out.println("Profile.java clear()");
		}
		firstName = "";
		lastName = "";
		title = "";
		email = "";
		camera = "";
		avatar = "";
		//unid = null;
	}
	
	public void reset() {
		AppController.writeToConsole(this.getClass().getSimpleName().toString() + ". reset(). ");
	    firstName = null;
		lastName = null;
		notesName = null;
		title = null;
		email = null;
		camera = null;
		avatar = null;
		unique = null;
	}
	
	
	//general getters and setters
	public String getUnid() {
		return unid;
	}	
	public void setUnid(String unid) {
		this.unid = unid;
	}
	
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	
	public String getNotesName() {
		return notesName;
	}
	public void setNotesName(String notesName) {
		this.notesName = notesName;
	}
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	
	public String getCamera() {
		return camera;
	}
	public void setCamera(String camera) {
		this.camera = camera;
	}
	
	public String getAvatar() {
		return avatar;
	}
	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}
	
	public Boolean getNewNote() {
		return newNote;
	}
	public void setNewNote(Boolean newNote) {
		this.newNote = newNote;
	}

	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}

	public boolean isReadOnly() {
		return readOnly;
	}
	
	public Boolean getDebugMode() {
		String configval = Configuration.debugMode;
		return Boolean.valueOf(configval);
	}
	
	public UploadedFile getFileUpload() {
	    return uploadedFile;
	}
	public void setFileUpload( UploadedFile to ) {
	    this.uploadedFile = to;
	}
	
}
