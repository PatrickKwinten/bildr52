package org.openntf.bildr;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;

import org.openntf.domino.xsp.XspOpenLogUtil;
import com.ibm.xsp.extlib.util.*;

import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.NotesException;
import lotus.domino.Session;
import lotus.domino.View;

public class Configuration implements Serializable{
	
	private static final long serialVersionUID = 1L;
	//The DaoBean contains information where the data database is located e.g. on premise or on bluemix
	static DaoBean dao = new DaoBean();
	
	//here all helper properties
	private Boolean newNote;
	private String unid;
	private boolean readOnly;
	public static String debugMode;
	//here all document fields...
	private String title;
	private String titleDisplay;
	private String legal;
	private String legalDisplay;
	private String searchDisplay;
	private Integer rowsDefault;
	private Integer rowsLatest;
	private Integer rowsSearch;
	private Integer rowsCarrousel;
	private String carrouselDisplay;
	private String unique;	
	private String notesName;
	private Date updated;	
	private List<String> extensions = new ArrayList<String>();	
	private List<String> category = new ArrayList<String>();
	
	private String categories;
	private String runtimes;
	private Integer maxsize;
	
	private Boolean search;	

	public Configuration(){
		// Constructor...
		XspOpenLogUtil.logEvent(null, this.getClass().getSimpleName().toString() + ". Constructor started.", Level.INFO, null);
		try {
			if (AppController.hasConfiguration()){
				String configID = AppController.getConfigurationID();
				AppController.writeToConsole(this.getClass().getSimpleName().toString() + ". Configuration() - ID:" + configID);
				this.loadByUnid(configID);
			}
			else{
				this.create();
				String newTitle = "Bildr";
				this.setTitle(newTitle);
				this.save();
			}
		} catch (NotesException e) {
			XspOpenLogUtil.logError(null, e, this.getClass().getSimpleName().toString() , Level.SEVERE, null);
		}
	}
	
	public void create() throws NotesException{
		AppController.writeToConsole(this.getClass().getSimpleName().toString() + ". create()");
		clear();
		newNote = true;		
		Session session = JSFUtil.getSession();
		rowsDefault = 20;
		rowsLatest = 10;
		rowsSearch = 20;
		rowsCarrousel = 5;
		unique = String.valueOf(session.evaluate("@Unique").get(0));
		ArrayList<String> extensions = new ArrayList<String>();
		extensions.add("jpg");
		search= false;
		maxsize=1000;
		runtimes = "html5";
		categories = "";
		titleDisplay = "true";
	}
	
	public void loadByKey(String key) throws NotesException {
		AppController.writeToConsole(this.getClass().getSimpleName().toString() + ". loadByKey(String key) : " + key);
		Database dataDB = dao.getDatabase();
		View view = dataDB.getView("byKey");
		Document doc = view.getDocumentByKey(key);		
		if (null == doc) {
			XspOpenLogUtil.logEvent(null, this.getClass().getSimpleName().toString() + ". loadByKey(). Document not found.", Level.INFO, null);
		} else {
			loadValues(doc);
		}	
		
		doc.recycle();
		view.recycle();	
		dataDB.recycle();
	}
	
	public void loadByUnid(String unid) throws NotesException{
		AppController.writeToConsole(this.getClass().getSimpleName().toString() + ". loadByUnid(String)");
		Database dataDB = dao.getDatabase();
		Document doc = dataDB.getDocumentByUNID(unid);		
		if (null == doc) {
			XspOpenLogUtil.logEvent(null, this.getClass().getSimpleName().toString() + ". loadByUnid(String). Document not found: " + unid, Level.INFO, null);
		} else {
			AppController.writeToConsole(this.getClass().getSimpleName().toString() + ". loadByUnid(String). Doc found.");
			loadValues(doc);
		}
		doc.recycle();
		dataDB.recycle();
	}
	
	public void loadValues(Document doc) throws NotesException {
		AppController.writeToConsole(this.getClass().getSimpleName().toString() + ". loadValues(Document doc).");
		debugMode = doc.getItemValueString("debug");
		title = doc.getItemValueString("title");
		titleDisplay = doc.getItemValueString("title_display");
		legal = doc.getItemValueString("legal");
		legalDisplay = doc.getItemValueString("legal_display");
		searchDisplay = doc.getItemValueString("search_display");
		rowsDefault = doc.getItemValueInteger("rows_default");
		rowsLatest = doc.getItemValueInteger("rows_latest");
		rowsSearch = doc.getItemValueInteger("rows_search");
		rowsCarrousel = doc.getItemValueInteger("rows_carrousel");
		carrouselDisplay = doc.getItemValueString("carrousel_display");
		newNote = false;
		unique = doc.getItemValueString("unique");
		unid = doc.getUniversalID();		
		notesName = doc.getItemValueString("$UpdatedBy");
		updated = doc.getLastModified().toJavaDate();		
		setExtensions(doc.getItemValue("extensions"));
		setCategory(doc.getItemValue("category"));
		String tmp = doc.getItemValueString("search");
		if (tmp =="1"){
			search = true;
		}
		else{
			search = false;
		}
		AppController.writeToConsole(this.getClass().getSimpleName().toString() + ". loadValues(Document doc). Doc loaded");
	}
	
	public void save() throws NotesException {
		AppController.writeToConsole(this.getClass().getSimpleName().toString() + ". save()");
		Database dataDB = dao.getDatabase();		
		Document doc = null;		
		if (newNote) {
			// True means never been saved
			AppController.writeToConsole(this.getClass().getSimpleName().toString() + ". save(). New document");
			doc = dataDB.createDocument();
			doc.replaceItemValue("form", "$f-preferences");
		} else {
			AppController.writeToConsole(this.getClass().getSimpleName().toString() + ". save(). Existing document");
			doc = dataDB.getDocumentByUNID(unid);			
		}
		// Common elements to save
		doc.replaceItemValue("debug", debugMode);
		doc.replaceItemValue("title", title);
		doc.replaceItemValue("title_display", titleDisplay);
		doc.replaceItemValue("legal", legal);
		doc.replaceItemValue("legal_display", legalDisplay);
		doc.replaceItemValue("search_display", searchDisplay);
		doc.replaceItemValue("rows_default", rowsDefault);
		doc.replaceItemValue("rows_latest", rowsLatest);
		doc.replaceItemValue("rows_search", rowsSearch);
		doc.replaceItemValue("rows_carrousel", rowsCarrousel);		
		doc.replaceItemValue("carrousel_display", carrouselDisplay);
		doc.replaceItemValue("unique", unique);
		doc.replaceItemValue("extensions", new Vector(getExtensions()));		
		doc.replaceItemValue("category", new Vector(getCategory()));
		if (search == true){
			doc.replaceItemValue("search", "1");			
		}		
		doc.computeWithForm(false, false);
		doc.save();
		
		doc.recycle();
		dataDB.recycle();
	}
	
	public void clear() {
		AppController.writeToConsole(this.getClass().getSimpleName().toString() + ". clear()");
		debugMode = "";
		title = "";
		titleDisplay = "";
		legal = "";
		legalDisplay = "";
		searchDisplay = "";
		rowsDefault = 20;
		rowsLatest = 10;
		rowsSearch = 20;
		rowsCarrousel = 5;
		carrouselDisplay = "";
		extensions.clear();		
		category.clear();
		search = false;
	}
		
	
	//general getters and setters
	public String getDebugMode() {
		return debugMode;
	}
	public void setDebugMode(String debugMode) {
		this.debugMode = debugMode;
	}
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getTitleDisplay() {
		return titleDisplay;
	}
	public void setTitleDisplay(String titleDisplay) {
		this.titleDisplay = titleDisplay;
	}
	
	public String getLegal() {
		return legal;
	}
	public void setLegal(String legal) {
		this.legal = legal;
	}
	
	public String getLegalDisplay() {
		return legalDisplay;
	}
	public void setLegalDisplay(String legalDisplay) {
		this.legalDisplay = legalDisplay;
	}
	
	public String getSearchDisplay() {
		return searchDisplay;
	}
	public void setSearchDisplay(String searchDisplay) {
		this.searchDisplay = searchDisplay;
	}
	
	public Integer getRowsDefault(){
		return rowsDefault;
	}
	public void setRowsDefault(Integer rowsDefault){
		this.rowsDefault = rowsDefault;
	}	
	
	public Integer getRowsLatest(){
		return rowsLatest;
	}
	public void setRowsLatest(Integer rowsLatest){
		this.rowsLatest = rowsLatest;
	}
	
	public Integer getRowsSearch(){
		return rowsSearch;
	}
	public void setRowsSearch(Integer rowsSearch){
		this.rowsSearch = rowsSearch;
	}
	
	public Integer getRowsCarrousel(){
		return rowsCarrousel;
	}
	public void setRowsCarrousel(Integer rowsCarrousel){
		this.rowsCarrousel = rowsCarrousel;
	}
	
	public String getCarrouselDisplay() {
		return carrouselDisplay;
	}
	public void setCarrouselDisplay(String carrouselDisplay) {
		this.carrouselDisplay = carrouselDisplay;
	}
	
	public String getUnid() {
		return unid;
	}
	public void setUnid(String unid) {
		this.unid = unid;
	}
	
	public String getUnique() {
		return unique;
	}
	public void setUnique(String unique) {
		this.unique = unique;
	}
	
	public Boolean getNewNote() {
		return newNote;
	}
	public void setNewNote(Boolean newNote) {
		this.newNote = newNote;
	}
	
	public boolean isReadOnly() {
		return readOnly;
	}
	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}
	
	public static String isDebugMode(){
		return debugMode;		
	}	
	
	public Boolean getSearch() {
		return search;
	}

	public void setSearch(Boolean search) {
		this.search = search;
	}
	
	public String getNotesName() {
		return notesName;
	}
	public void setNotesName(String notesName) {
		this.notesName = notesName;
	}
	
	public void setUpdated(Date updated) {
		this.updated = updated;
	}
	public Date getUpdated() {
		return updated;
	}
	
	public void setCategory(List<String> category) {
	    this.category = category;
	}
	public List<String> getCategory() {
	    return category;
	}
	
	public void setExtensions(List<String> extensions) {
	    this.extensions = extensions;
	}
	public List<String> getExtensions() {
	    return extensions;
	}

}
