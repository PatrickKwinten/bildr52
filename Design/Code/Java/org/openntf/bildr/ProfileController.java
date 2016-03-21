package org.openntf.bildr;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Vector;
import java.util.logging.Level;

import com.ibm.commons.util.io.json.JsonException;
import com.ibm.commons.util.io.json.JsonJavaFactory;
import com.ibm.commons.util.io.json.JsonJavaObject;
import com.ibm.commons.util.io.json.JsonParser;

import lotus.domino.Database;
import lotus.domino.NotesException;
import lotus.domino.View;
import lotus.domino.ViewEntry;
import lotus.domino.ViewEntryCollection;
import lotus.domino.ViewNavigator;

import org.openntf.domino.xsp.XspOpenLogUtil;

/**
 * @author Patrick Kwinten
 *
 */
public class ProfileController implements Serializable{

	private static final long serialVersionUID = 1L;
	public static DaoBean dao = new DaoBean();	
	private Profile profile;
	private JsonJavaObject data;
	
	public ProfileController() throws NotesException{
		// Constructor...
		XspOpenLogUtil.logEvent(null, null, this.getClass().getSimpleName().toString() + ". Constructor started.", Level.INFO, null);		
		this.profile = new Profile();		
		String unid = JSFUtil.getParameter( "unid" );		
		if( unid != null ) {
			// Try to load values from Notes Document
			this.profile.setUnid( unid );
		}		
	}
	
	public ArrayList<JsonJavaObject> loadProfiles() throws NotesException {
		AppController.writeToConsole("AppController.java loadProfiles()");
		ArrayList<JsonJavaObject> profiles = new ArrayList<JsonJavaObject>();
		Database dataDB = dao.getDatabase();
		String ViewName = "$v-profilesJSON-flat";
		if (!(dataDB==null)) {
			if ((dataDB.isOpen())) {
				AppController.writeToConsole("AppController.java loadProfiles(). DB found: ");
			}
			else{
				XspOpenLogUtil.logError(null, null, this.getClass().getSimpleName().toString() + ". loadProfiles(). DB not found.", Level.SEVERE, null);
			}
		}				
		try {
			profiles = JSFUtil.loadJSONObjects(dataDB.getServer(), dataDB.getFilePath(), ViewName, "", 1);
		} catch (NotesException e) {
			XspOpenLogUtil.logError(null, e, this.getClass().getSimpleName().toString() + ". loadProfiles().", Level.SEVERE, null);
		}
		dataDB.recycle();
		AppController.writeToConsole("AppController.java loadProfiles(). arrays size = " + profiles.size());		
		return profiles;
	}
	
	
	public ArrayList<JsonJavaObject> searchJSONObjects(String searchKey, Integer resultLimit) throws NotesException {
		ArrayList<JsonJavaObject> JSONObjects = new ArrayList<JsonJavaObject>();
		Boolean isFiltered;
		ViewNavigator navigator;
		ViewEntryCollection vec;
		Integer limit = (resultLimit == null) ? 100000 : resultLimit;
		JsonJavaFactory factory = JsonJavaFactory.instanceEx;
		Database dataDB = dao.getDatabase();
		String ViewName = "$v-profilesJSON-flat";	
		View luView = dataDB.getView(ViewName);
		luView.setAutoUpdate(false);
		
		//Do we have some Key to search? --> Build the query and filter the view
		if (searchKey == null || searchKey.equals("")) {
			isFiltered = false; //--> Use ViewNavigator
		} else {
			String query = "";
			query = query + searchKey;
			@SuppressWarnings("unused")
			int count = luView.FTSearch(query); //--> View is filtered now --> Use ViewEntryCollection
			isFiltered = true;
		}	
		//use a ViewNavigator to loop
		//optimization found: http://www.everythingaboutit.eu/2012/04/peformance-trick-beim-durchlesen-von.html
		navigator = luView.createViewNav();
		navigator.setBufferMaxEntries(1024);
		//VN_ENTRYOPT_NOCOUNTDATA we are not interested in the number of children, we can go a little faster
		navigator.setEntryOptions(ViewNavigator.VN_ENTRYOPT_NOCOUNTDATA);
		vec = luView.getAllEntries();		
		ViewEntry entry = isFiltered ? vec.getFirstEntry(): navigator.getFirstDocument();		
		Integer counter = 0;		
		while ((entry != null) && (counter < limit)) {
		   
			/*View: 
			 * 1. Column: SortKey
			 * 2. Column: JSON String: (z.b. {"docUNID": "FAE110220E57ECE7C12578A700375101","name": "John Doe","job": "Office Manager","pictureURL": "http://dev1.quintessens.com/directory.nsf/0/FAE110220E57ECE7C12578A700375101/$FILE/PortalPicture.jpg"})
			*/
			
			Vector<?> columnValues = entry.getColumnValues();
			String colJson = String.valueOf(columnValues.get(1));			
			try {
				JsonJavaObject json = (JsonJavaObject) JsonParser.fromJson(factory, colJson);
				if (json != null) {
					JSONObjects.add(json);
				}				
			} catch (JsonException e) {
				XspOpenLogUtil.logError(null, e, this.getClass().getSimpleName().toString() + ". searchJSONObjects()", Level.SEVERE, null);
				
			}				
		   ViewEntry tempEntry = entry;
		   entry = isFiltered ? vec.getNextEntry(): navigator.getNextDocument();
		   tempEntry.recycle();		   
		   counter++;
		} 
		luView.recycle();
		dataDB.recycle();		
		return JSONObjects;
	}
	
	
	public String loadProfilePicURL(String id) throws NotesException{		
		AppController.writeToConsole("Profile.java loadProfilePicURL() id = " + id);	
		String url = new String();
		Database dataDB = dao.getDatabase();
		String ViewName = "$v-profilesJSON-flat";
		if (!(dataDB==null)) {
			if ((dataDB.isOpen())) {
				AppController.writeToConsole("Profile.java loadProfilePicURL(id). DB found");	
			}
			else{
				XspOpenLogUtil.logError(null, null, this.getClass().getSimpleName().toString() + ". loadProfilePicURL(id). DB not found.", Level.SEVERE, null);
			}
		}		
		this.data = JSFUtil.loadJSONObject(dataDB.getServer(), dataDB.getFilePath(), ViewName, id, 1);
		String filename = this.data.getJsonProperty("avatar").toString();
		if (filename.equals("") || filename == null || filename.equals("profileNoPhoto.png")){
			url = "profileNoPhoto.png";			
		}
		else{
			String path = dao.getFilepath();
			AppController.writeToConsole("Profile.java loadProfilePicURL(id)......." + path);	
			String docID = this.data.getJsonProperty("docUNID").toString();
			url =  "../" + path.toLowerCase() + "/0/" + docID + "/$FILE/" + filename;
			
		}		
		return url;
	}
	
	
	public ArrayList<JsonJavaObject> loadAlbums() throws NotesException {
		AppController.writeToConsole("Profile.java loadAlbums()");	
		ArrayList<JsonJavaObject> albums = new ArrayList<JsonJavaObject>();
		Database dataDB = dao.getDatabase();
		String ViewName = "$v-albumsJSON-flat";		
		if (!(dataDB==null)) {
			if ((dataDB.isOpen())) {
				AppController.writeToConsole("Profile.java loadAlbums()  DB found");	
			}
			else{
				XspOpenLogUtil.logError(null, null, this.getClass().getSimpleName().toString() + ". loadAlbums(). DB not found.", Level.SEVERE, null);
			}
		}				
		try {
			albums = JSFUtil.loadJSONObjects(dataDB.getServer(), dataDB.getFilePath(), ViewName, "", 1);
		} catch (NotesException e) {
			XspOpenLogUtil.logError(null, e, this.getClass().getSimpleName().toString() + ". loadAlbums().", Level.SEVERE, null);
		}
		dataDB.recycle();
		AppController.writeToConsole("Profile.java loadAlbums(). arrays size = " + albums.size());	
		return albums;
	}
	
	
	public ArrayList<JsonJavaObject> loadAlbums(String parentID) throws NotesException {
		AppController.writeToConsole("Profile.java loadAlbums(String)" + parentID);	
		ArrayList<JsonJavaObject> albums = new ArrayList<JsonJavaObject>();
		Database dataDB = dao.getDatabase();
		String ViewName = "$v-albumsJSON-multi";
		if (!(dataDB==null)) {
			if ((dataDB.isOpen())) {
				AppController.writeToConsole("Profile.java loadAlbums(). DB found");	
			}
			else{
				XspOpenLogUtil.logError(null, null, this.getClass().getSimpleName().toString() + ". loadAlbums(String parentID). DB not found.", Level.SEVERE, null);
			}
		}				
		try {
			albums = JSFUtil.loadJSONObjects(dataDB.getServer(), dataDB.getFilePath(), ViewName, parentID, 2);
		} catch (NotesException e) {
			XspOpenLogUtil.logError(null, e, this.getClass().getSimpleName().toString() + ". loadAlbums(String parentID).", Level.SEVERE, null);
		}
		dataDB.recycle();
		AppController.writeToConsole("Profile.java loadAlbums(). arrays size = " + albums.size());
		return albums;
	}
	
	
}
