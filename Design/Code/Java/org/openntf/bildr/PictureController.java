package org.openntf.bildr;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.Vector;
import java.util.Map.Entry;
import java.util.logging.Level;

import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.NotesException;
import lotus.domino.View;
import lotus.domino.ViewEntry;
import lotus.domino.ViewEntryCollection;
import lotus.domino.ViewNavigator;

import com.ibm.commons.util.io.json.JsonException;
import com.ibm.commons.util.io.json.JsonJavaFactory;
import com.ibm.commons.util.io.json.JsonJavaObject;
import com.ibm.commons.util.io.json.JsonParser;

import org.openntf.domino.xsp.XspOpenLogUtil;

/**
 * @author Patrick Kwinten
 *
 */

public class PictureController implements Serializable{	
	
	private static final long serialVersionUID = 1L;
	private static DaoBean dao = new DaoBean();	
	private Picture picture;

	public PictureController(){
		// Constructor...
		XspOpenLogUtil.logEvent(null, this.getClass().getSimpleName().toString() + ". Constructor started.", Level.INFO, null);			
		this.picture = new Picture();		
		String unid = JSFUtil.getParameter( "unid" );		
		if( unid != null ) {
			// Try to load values from Notes Document
			this.picture.setUnid( unid );
		}		
	}
	
	public void loadByUnid(String unid) throws NotesException{
		AppController.writeToConsole(this.getClass().getSimpleName().toString() + ". loadByUnid(unid). Unid = " + unid);
		picture = new Picture();
		picture.loadByUnid(unid);
	}
	
	
	public void loadValues(Document doc) throws NotesException {
		AppController.writeToConsole(this.getClass().getSimpleName().toString() + ". loadValues(Document doc)");
		Picture picture = new Picture();
		picture.setSubject(doc.getItemValueString("Photo_Title"));
		picture.setDescription(doc.getItemValueString("PhotoDescription"));
		picture.setCreator(doc.getItemValueString("Au_Author"));
		picture.setThumb(doc.getItemValueString("Photo_ThumbFilename"));
		picture.setSmall(doc.getItemValueString("Photo_SmallFilename"));
		picture.setOriginal(doc.getItemValueString("Photo_OriginalFilename"));
		picture.setCreated(doc.getCreated().toJavaDate());
		picture.setUnid(doc.getUniversalID());
		picture.setCarousel(doc.getItemValueString("Photo_Carousel"));		
		AppController.writeToConsole(this.getClass().getSimpleName().toString() + ". loadValues(Document) - Doc loaded");
	}
	
	
	
	public ArrayList<JsonJavaObject> loadPictures() throws NotesException {
		AppController.writeToConsole(this.getClass().getSimpleName().toString() + ". loadPictures()");
		ArrayList<JsonJavaObject> pictures = new ArrayList<JsonJavaObject>();
		//get Info about Environment
		Database dataDB = dao.getDatabase();
		String ViewName = "$v-picturesJSON-flat";		
		if (!(dataDB==null)) {
			if ((dataDB.isOpen())) {
				AppController.writeToConsole(this.getClass().getSimpleName().toString() + ". loadPictures(). DB found.");
				
			}
			else{
				XspOpenLogUtil.logError(null, null, this.getClass().getSimpleName().toString() + " loadPictures(). DB not found", Level.SEVERE, null);
			}
		}				
		try {
			pictures = JSFUtil.loadJSONObjects(dataDB.getServer(), dataDB.getFilePath(), ViewName, "", 1);
		} catch (NotesException e) {
			XspOpenLogUtil.logError(null, e, this.getClass().getSimpleName().toString() + " loadPictures().", Level.SEVERE, null);
		}
		dataDB.recycle();
		AppController.writeToConsole(this.getClass().getSimpleName().toString() + ". loadPictures(). Array size = " + pictures.size());				
		return pictures;		
	}
	
	public ArrayList<JsonJavaObject> loadPictures(String parentID) throws NotesException {
		AppController.writeToConsole(this.getClass().getSimpleName().toString() + ". loadPictures(String parentID)" + parentID);
		ArrayList<JsonJavaObject> pictures = new ArrayList<JsonJavaObject>();
		Database dataDB = dao.getDatabase();
		String ViewName = "$v-picturesJSON-multi";		
		if (!(dataDB==null)) {
			if ((dataDB.isOpen())) {
				AppController.writeToConsole(this.getClass().getSimpleName().toString() + ". loadPictures(). DB found");
				
			}
			else{
				XspOpenLogUtil.logError(null, null, this.getClass().getSimpleName().toString() + ". loadPictures(). DB not found.", Level.SEVERE, null);		
			}
		}				
		try {
			pictures = JSFUtil.loadJSONObjects(dataDB.getServer(), dataDB.getFilePath(), ViewName, parentID, 2);
		} catch (NotesException e) {
			XspOpenLogUtil.logError(null, e, this.getClass().getSimpleName().toString() + ". loadPictures().", Level.SEVERE, null);
		}
		dataDB.recycle();
		AppController.writeToConsole(this.getClass().getSimpleName().toString() + ". loadPictures(). Array size = " + pictures.size());
		return pictures;
	}
	
	
	//not sure this method is in use...
	public ArrayList<JsonJavaObject> loadPicturesWithComments() throws NotesException {
		AppController.writeToConsole(this.getClass().getSimpleName().toString() + ". loadPicturesWithComments()");
		ArrayList<JsonJavaObject> pictures = new ArrayList<JsonJavaObject>();
		Database dataDB = dao.getDatabase();
		String ViewName = "$v-picturesJSON-flat";
		if (!(dataDB==null)) {
			if ((dataDB.isOpen())) {
				AppController.writeToConsole(this.getClass().getSimpleName().toString() + ". loadPicturesWithComments(). DB found");
			}
			else{
				XspOpenLogUtil.logError(null, null, this.getClass().getSimpleName().toString() + ". loadPicturesWithComments(). DB not found.", Level.SEVERE, null);
			}
		}				
		try {
			pictures = JSFUtil.loadJSONObjects(dataDB.getServer(), dataDB.getFilePath(), ViewName, "", 1);
		} catch (NotesException e) {
			XspOpenLogUtil.logError(null, e, this.getClass().getSimpleName().toString() + ". loadPicturesWithComments().", Level.SEVERE, null);
		}
		finally {
			//not sure what to do but we will do it anyway
		}
		dataDB.recycle();
		AppController.writeToConsole(this.getClass().getSimpleName().toString() + ". loadPicturesWithComments(). Array size = " + pictures.size());
		return pictures;
	}
	
	public Set<Entry<String, Integer>> categoriesMap(String viewName) throws NotesException{
		AppController.writeToConsole(this.getClass().getSimpleName().toString() + ". Set<Entry<String, Integer>>." );		
		HashMap<String, Integer> categories = new HashMap<String, Integer>();
		Database dataDB = dao.getDatabase();	
		ViewNavigator nav = dataDB.getView(viewName).createViewNav();	
		ViewEntry entry = nav.getFirst();	
		while (entry != null && !entry.isTotal()){
			categories.put(entry.getColumnValues().firstElement().toString(), entry.getChildCount());
			ViewEntry tmpentry = nav.getNextSibling(entry);
			entry.recycle();
			entry = tmpentry;
		}	
		nav.recycle();
		return categories.entrySet();	
	}
	
	
	
	public ArrayList<JsonJavaObject> searchJSONObjects(String Key, Integer resultLimit) throws NotesException {
		AppController.writeToConsole(this.getClass().getSimpleName().toString() + ". searchJSONObjects(String Key, Integer resultLimit)");
		ArrayList<JsonJavaObject> JSONObjects = new ArrayList<JsonJavaObject>();		
		Boolean isFiltered;
		ViewNavigator navigator;
		ViewEntryCollection vec;		
		Integer limit = (resultLimit == null) ? 100000 : resultLimit;
		JsonJavaFactory factory = JsonJavaFactory.instanceEx;
		
		Database db = dao.getDatabase();
		String ViewName = "$v-picturesJSON-flat";	
		View vw = db.getView(ViewName);
		vw.setAutoUpdate(false);
		
		//Do we have some Key to search? --> Build the query and filter the view
		if (Key == null || Key.equals("")) {
			isFiltered = false; //--> Use ViewNavigator
		} else {
			//Build the query
			String query = "";
			query = query + Key;
			@SuppressWarnings("unused")
			int count = vw.FTSearch(query); //--> View is filtered now --> Use ViewEntryCollection
			isFiltered = true;
		}

	
		//use a ViewNavigator to loop
		//optimization found: http://www.everythingaboutit.eu/2012/04/peformance-trick-beim-durchlesen-von.html
		navigator = vw.createViewNav();
		navigator.setBufferMaxEntries(1024);
		//VN_ENTRYOPT_NOCOUNTDATA we are not interested in the number of children, we can go a little faster
		navigator.setEntryOptions(ViewNavigator.VN_ENTRYOPT_NOCOUNTDATA);

		//use ViewEntryCollection to loop
		vec = vw.getAllEntries();
		
		ViewEntry entry = isFiltered ? vec.getFirstEntry(): navigator.getFirstDocument();
		
		Integer counter = 0;
		
		while ((entry != null) && (counter < limit)) {
		   
			/*View: 
			 * 1. Column: SortKey
			 * 2. Column: JSON String: (e.g. {"docUNID": "FAE110220E57ECE7C12578A700375101","name": "John Doe","job": "Office Manager","pictureURL": "https://dev1.quintessens.com/directory.nsf/0/FAE110220E57ECE7C12578A700375101/$FILE/portrait.jpg"})
			*/
			
			Vector<?> columnValues = entry.getColumnValues();
			String colJson = String.valueOf(columnValues.get(1));
			
			try {
				JsonJavaObject json = (JsonJavaObject) JsonParser.fromJson(factory, colJson);
				if (json != null) {
					JSONObjects.add(json);
				}
			} catch (JsonException e) {
				XspOpenLogUtil.logError(null, e, this.getClass().getSimpleName().toString() + "searchJsonObjects", Level.SEVERE, null);
			}				
		   ViewEntry tempEntry = entry;
		   entry = isFiltered ? vec.getNextEntry(): navigator.getNextDocument();
		   tempEntry.recycle();
		   
		   counter++;
		} 		
		vw.recycle();
		db.recycle();		
		return JSONObjects;
	}
	
	
	/**
	 * Getters and Setters
	 *
	 */
	
	public Boolean getDebugMode() {
		String configval = Configuration.debugMode;
		return Boolean.valueOf(configval);
	}	
	public Picture getPicture() {
		return this.picture;
	}
	
}
