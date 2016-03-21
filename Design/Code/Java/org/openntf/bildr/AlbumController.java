package org.openntf.bildr;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Vector;
import java.util.logging.Level;

import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.NotesException;
import lotus.domino.View;
import lotus.domino.ViewEntry;
import lotus.domino.ViewEntryCollection;
import lotus.domino.ViewNavigator;

import org.openntf.domino.xsp.XspOpenLogUtil;

import com.ibm.commons.util.io.json.JsonException;
import com.ibm.commons.util.io.json.JsonJavaFactory;
import com.ibm.commons.util.io.json.JsonJavaObject;
import com.ibm.commons.util.io.json.JsonParser;

public class AlbumController implements Serializable{
	private static final long serialVersionUID = 1L;
	private static DaoBean dao = new DaoBean();	
	private Album album;
	
	public AlbumController() throws NotesException{
		XspOpenLogUtil.logEvent(null, null, this.getClass().getSimpleName().toString() + ". Constructor started.", Level.INFO, null);		
		this.album = new Album();	
		String unid = JSFUtil.getParameter( "unid" );	
	}
	

	
	
	
public ArrayList<JsonJavaObject> searchJSONObjects(String searchKey, Integer resultLimit) throws NotesException {
	AppController.writeToConsole(this.getClass().getSimpleName().toString() + ". searchJSONObjects(...)");
		ArrayList<JsonJavaObject> JSONObjects = new ArrayList<JsonJavaObject>();
		
		Boolean isFiltered;
		ViewNavigator navigator;
		ViewEntryCollection vec;
		
		Integer limit = (resultLimit == null) ? 100000 : resultLimit;
		
		JsonJavaFactory factory = JsonJavaFactory.instanceEx;
		
		Database dataDB = dao.getDatabase();
		String ViewName = "$v-albumsJSON-flat";	
		View luView = dataDB.getView(ViewName);
		luView.setAutoUpdate(false);
		
		//Do we have some Key to search? --> Build the query and filter the view
		if (searchKey == null || searchKey.equals("")) {
			isFiltered = false; //--> Use ViewNavigator
		} else {
			//Build the query
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

		//use ViewEntryCollection to loop
		vec = luView.getAllEntries();		
		ViewEntry entry = isFiltered ? vec.getFirstEntry(): navigator.getFirstDocument();		
		Integer counter = 0;		
		while ((entry != null) && (counter < limit)) {
		   
			/*View: 
			 * 1. Column: SortKey
			 * 2. Column: JSON String: (z.b. {"docUNID": "FAE110220E57ECE7C12578A700375101","name": "John Doe","job": "Office Manager","pictureURL": "https://dev1.quintessens.com/directory.nsf/0/FAE110220E57ECE7C12578A700375101/$FILE/PortalPicture.jpg"})
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


	
	public Boolean getDebugMode() {
		String configval = Configuration.debugMode;
		return Boolean.valueOf(configval);
	}	

	public Album getAlbum() {
		return this.album;
	}
	
}
