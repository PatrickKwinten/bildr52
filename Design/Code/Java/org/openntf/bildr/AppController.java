package org.openntf.bildr;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Level;

import javax.faces.context.FacesContext;

import com.ibm.commons.util.io.json.JsonJavaObject;
import com.ibm.xsp.designer.context.XSPContext;
import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.NotesException;

import lotus.domino.View;

import org.openntf.domino.xsp.XspOpenLogUtil;

import lotus.domino.Name;

public class AppController implements Serializable{

	private static final long serialVersionUID = 1L;
	
	private static final String startpage = getProperty("startpage");
	
	private ArrayList<JsonJavaObject> profiles;
	
	//The DaoBean contains information where the data database is located e.g. on premise or on bluemix
	private static DaoBean dao = new DaoBean();

	public AppController() throws NotesException{
		// Constructor...
		XspOpenLogUtil.logEvent(null, this.getClass().getSimpleName().toString() + ". Constructor started. Debug = " + getDebugMode() + ", user = " + JSFUtil.getCurrentUser().getCanonical(), Level.INFO, null);
	}	
	
	
	public Boolean isRegistered() throws NotesException {
		writeToConsole(this.getClass().getSimpleName().toString() + ". isRegistered()");		
		boolean profile = false;		
		Name name = JSFUtil.getCurrentUser();
		if (name.getCommon()=="Anonymous"){
			writeToConsole(this.getClass().getSimpleName().toString() + ". isRegistered() - Anonymous user");				
			profile = false;
		}
		else{
			writeToConsole(this.getClass().getSimpleName().toString() + ". isRegistered() - Welcome " + name.getCommon());			
			Database dataDB = dao.getDatabase(); 
			View profileView;
			try {
				profileView = dataDB.getView("$v-profiles");
				profileView.refresh();
				Document doc = profileView.getDocumentByKey(name.getCanonical());
				if (null == doc){
					writeToConsole( this.getClass().getSimpleName().toString() + ". isRegistered(). " + name.getCommon() + " is not yet registered.");					
					profile = false;
				}
				else{
					writeToConsole(this.getClass().getSimpleName().toString() + ". isRegistered() - " + name.getCommon() + " is registered");					
					profile = true;
					doc.recycle();
				}			
				profileView.recycle();
			} catch (NotesException e) {
				XspOpenLogUtil.logError(null, e, this.getClass().getSimpleName().toString() + ". isRegistered()", Level.SEVERE, null);
			}
			dataDB.recycle();
		}		
		return profile;
	}
	
	public String getObjectType(String id) throws NotesException{
		writeToConsole(this.getClass().getSimpleName().toString() + ". getObjectType(id). Id = " + id);
		String ObjType = null;
		if (id == null){
			ObjType = "unknown";
		}
		else{
			Database dataDB = dao.getDatabase();
			Document objDoc;
			try{
				objDoc = dataDB.getDocumentByUNID(id);
				String ObjTypeTmp = objDoc.getItemValueString("form");
				writeToConsole(this.getClass().getSimpleName().toString() + ". getObjectType(id). Form = " + ObjTypeTmp);
				if (ObjTypeTmp.equals("$f-profile")) {
					ObjType = "profile";					
				}
				else if(ObjTypeTmp.equals("$f-picture")){
					ObjType = "picture";
				}
				else if(ObjTypeTmp.equals("$f-album")){
					ObjType = "album";
				}
				else{
					ObjType = "notavailable";
				}
				
			} catch (NotesException e) {
				// TODO Auto-generated catch block
				XspOpenLogUtil.logError(null, e, this.getClass().getSimpleName().toString() + ". getObjectType(String id)", Level.SEVERE, null);
				//e.printStackTrace();
			}	
		}		
		return ObjType;		
	}
	
	public String getProfileID() throws NotesException{
		writeToConsole(this.getClass().getSimpleName().toString() + ". getProfileID()");
		String id = null;		
		Name name = JSFUtil.getCurrentUser();
		if (name.getCommon()=="Anonymous"){
			writeToConsole(this.getClass().getSimpleName().toString() + ". getProfileID() - Anonymous user");
			id = null;
		}
		else{
			Database dataDB = dao.getDatabase();
			View profileView;
			try {
				profileView = dataDB.getView("$v-profiles");
				Document doc = profileView.getDocumentByKey(name.getCanonical());
				if (null == doc){
					id = "";
				}
				else{
					id = doc.getUniversalID();
					doc.recycle();
				}			
				profileView.recycle();
			} catch (NotesException e) {
				XspOpenLogUtil.logError(null, e, this.getClass().getSimpleName().toString() + ". getProfileID()", Level.SEVERE, null);
			}	
			dataDB.recycle();
		}
		
		return id;
	}
	
	public static Boolean hasConfiguration() throws NotesException{
		writeToConsole("AppController. hasConfiguration()");
		boolean configuration = false;		
		Database dataDB = dao.getDatabase(); 
		View prefView;
		try {
			prefView = dataDB.getView("$v-preferences");
			Document doc = prefView.getFirstDocument();
			if (null == doc){
				configuration = false;
			}
			else{
				configuration = true;
				doc.recycle();
			}			
			prefView.recycle();
		} catch (NotesException e) {
			// TODO Auto-generated catch block
			XspOpenLogUtil.logError(null, e, "AppController. hasConfiguration()", Level.SEVERE, null);
			//e.printStackTrace();
		}	
		dataDB.recycle();
		return configuration;
	}
	
	public static String getConfigurationID(){
		writeToConsole("AppController. getConfigurationID()");		
		String id = null;
		Database dataDB = dao.getDatabase(); 
		View prefView;
		try {
			prefView = dataDB.getView("$v-preferences");
			Document doc = prefView.getFirstDocument();
			if (null == doc){
				id = "";
			}
			else{
				id = doc.getUniversalID();
				doc.recycle();
			}			
			prefView.recycle();
			dataDB.recycle();
		} catch (NotesException e) {
			// TODO Auto-generated catch block
			XspOpenLogUtil.logError(null, e, "AppController. getConfigurationID()", Level.SEVERE, null);
			//e.printStackTrace();
		}		
		return id;
	}
	
//	@Deprecated
//	public static void writeToLog(String msg, String severity){
//		if (getDebugMode() == true){
//			OpenLogUtil.logEvent(null, msg, Level.parse(severity) , null);
//		}
//	}
	public static void writeToConsole(String msg){
		if (getDebugMode() == true){
			System.out.println(msg);
		}
	}
	
	public ArrayList<JsonJavaObject> getProfiles() {
		return profiles;
	}
	
	/**
	 * getting the XSP Contect and redirect
	 */
	public void redirectToStartPage() {
		try {
			this.getXSPContext().redirectToPage(startpage);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public XSPContext getXSPContext() {
		return XSPContext.getXSPContext(getFacesContext());
	}

	public FacesContext getFacesContext() {
		return FacesContext.getCurrentInstance();
	}
	
	public static Boolean getDebugMode() {
		String configval = Configuration.debugMode;
		return Boolean.valueOf(configval);
	}
	
	private static String getProperty(String prop) {
		try {
			return getApplicationProperties().getProperty(prop);
		} catch (Throwable t) {
			XspOpenLogUtil.logError(t);
			return null;
		}
	}
	
	private static Properties getApplicationProperties() throws IOException {
		InputStream input = FacesContext.getCurrentInstance().getExternalContext().getResourceAsStream("application.properties");
		Properties props = new Properties();
		props.load(input);
		return props;
	}
	
}
