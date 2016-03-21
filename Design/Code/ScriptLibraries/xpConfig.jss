var BILDR_VERSION = 40;

function initBase(){	
	var prefHashMap:java.util.HashMap = new java.util.HashMap();
	//Session Settings
	var currentUser = session.getEffectiveUserName();
	
	//Workaround here to re-init the session vars if another user has logged in
	if ( !sessionScope.initConfig ) {
		//PREPARATION		
		var db = sessionAsSigner.getDatabase(database.getServer(), database.getFilePath());		//open as signer
				
		var con = facesContext.getExternalContext();		
		var request = con.getRequest();
		//var specialDomain =  docPrefs.getItemValueString("Tx_PrefDomain");
		
		var url:XSPUrl = new XSPUrl(database.getHttpURL());
				
		var server =  url.getHost();
		
			
		var protocolFull = request.getProtocol();
		var protocolOnly = protocolFull.split("/")[0]; 
		var port = (request.getServerPort().equals(80)) ? "" : ":" + request.getServerPort();
		
			
		//PREPARING USER RELATED PROPERTIES
		var roles = context.getUser().getRoles();
		sessionScope.put("prefUserName",currentUser);
		sessionScope.put("prefUserRoles",roles);
		sessionScope.put("prefUserAdmin",roles.contains("[admin]"));
		sessionScope.put("prefUserDebug",roles.contains("[debug]"));
		sessionScope.put("prefUserSessionAuth", @Contains(facesContext.getExternalContext().getRequest().getHeader("Cookie"), "DomAuthSessId") === 1 ? true : false); //dnt
		if(@ClientType() == "Notes"){
			sessionScope.put("prefUserSessionID",facesContext.getExternalContext().getRequest().getSession().getId());
		}
		

		
		//CLEAR UPLOAD RELATED SCOPE VARIABLES
		var iter:java.util.Iterator = sessionScope.keySet().iterator();
		while (iter.hasNext()) {
			var key = iter.next();
			if (key.toString().indexOf("upload") == 0) {	
				sessionScope.remove(key);
			}
		}
			
		

				
					
		//UPLOAD PROPERTIES
		//sessionScope.put("prefIMGExtensions", docPrefs.getItemValue("Tx_ImageExtensions") );		
		sessionScope.put("prefIMGExtensions",upload.getString('filetypes'));
		//sessionScope.put("prefIMGExtensionsDesc", docPrefs.getItemValueString("Tx_ImageExtensionsDescription") );	
		sessionScope.put("prefIMGExtensionsDesc",upload.getString('filedescription'));
		//sessionScope.put("prefUploadRuntimes", docPrefs.getItemValue("Tx_PrefRuntimes").join(",") );
		sessionScope.put("prefUploadRuntimes",upload.getString('runtimes'));
		//sessionScope.put("prefUploadMaxSize", docPrefs.getItemValueInteger("Nb_MaxImageSize") );
		sessionScope.put("prefUploadMaxSize",upload.getString('maxsize'));
				
		//UPLAOD RESIZING PROPERTIES
		/*
		sessionScope.put("resizeConfig-Smaller", {
			mode : getResizeDimension(docPrefs, "imp_resizemode_sm"),
			x : getResizeDimension( docPrefs, "imp_sm_x_res"),
			y : getResizeDimension( docPrefs, "imp_sm_y_res"),
			maxValue : getResizeDimension( docPrefs, "imp_maxvalue_sm"),
			perc : getResizeDimension( docPrefs, "imp_percentvalue_sm")
		});
		sessionScope.put("resizeConfig-Thumbnail", {
			mode : getResizeDimension(docPrefs, "imp_resizemode_tn"),
			x : getResizeDimension( docPrefs, "imp_tn_x_res"),
			y : getResizeDimension( docPrefs, "imp_tn_y_res"),
			maxValue : getResizeDimension( docPrefs, "imp_maxvalue_tn"),
			perc : getResizeDimension( docPrefs, "imp_percentvalue_tn")
		});
		*/
		
		sessionScope.put("resizeConfig-Smaller", {
			mode : @Integer(upload.getString('imp_resizemode_sm')),
			x : @Integer(upload.getString('imp_sm_x_res')),
			y : @Integer(upload.getString('imp_sm_y_res')),
			maxValue : @Integer(upload.getString('imp_maxvalue_sm')),
			perc : upload.getString('imp_percentvalue_sm')
		});
		sessionScope.put("resizeConfig-Thumbnail", {
			mode : @Integer(upload.getString('imp_resizemode_tn')),
			x : @Integer(upload.getString('imp_tn_x_res')),
			y : @Integer(upload.getString('imp_tn_y_res')),
			maxValue : @Integer(upload.getString('imp_maxvalue_tn')),
			perc : upload.getString('imp_percentvalue_tn')
		});
		sessionScope.put("initConfig", true);
	}
}

function sendError(message) {
	sessionScope.put("errorMessage",message);
	context.redirectToPage("error");
}

function getResizeDimension(doc:NotesDocument, field:String) {
	var result = 0;	
	var item:NotesItem = doc.getFirstItem(field);
	if (item == null){return 0;}
	var itemVal = item.getValues();
	if (itemVal==null){return 0;} 	
	itemVal = itemVal.elementAt(0);	
	switch(item.getType()){
		case 1280:
			if (itemVal.toString()!= "") {result = @Integer(itemVal.toString());}
			break;			
		case 768:
			result =  @Integer(itemVal);
			break; 
	}
	return result;
}

/*
 * Returns the URL to the profile photo of a specific author
 * Results are cached in the sessionScope
 */

Bildr = {
	deletePicture : function(picNoteId) {
		try {
			var docPic = database.getDocumentByID( picNoteId );
			var picId = docPic.getItemValueString("Tx_id");
			var db:NotesDatabase = sessionAsSigner.getDatabase( database.getServer(), database.getFilePath() );
			var dcAlbums:NotesDocumentCollection = db.search( "Form=\"$f-album\" & @IsMember(\"" + picId + "\"; Tx_pictureIds)" );
			var docAlbum:NotesDocument = dcAlbums.getFirstDocument();
			while (docAlbum != null) {
				var picIds = @Trim(@Replace(docAlbum.getItemValue("Tx_pictureIds"), picId, ""));
				docAlbum.replaceItemValue("Tx_pictureIds", picIds);
				docAlbum.save();
				docAlbum = dcAlbums.getNextDocument(docAlbum);
			}		
			docPic.removePermanently(true);
		}catch(e) {
			print("Bildr.deletePicture: " + e.toString() );
		}
	}		
}
