import xpUtils;

/*
 * function to embed a uploaded file in a document
 * 
 * con (ExternalContext)		= external servlet context object, can be retrieved using facesContext.getExternalContext()
 * strFileData (string)			= the name of the request parameter that holds the uploaded file
 * docTarget (NotesDocument)	= document in which the uploaded file should be embedded 
 */
function embedUploadedFile( con, strFileData, docTarget:NotesDocument, strFileName ) {
	var imageResizer = null;
	var thumbnailURL = "";
	var correctedFile = null;	
	try {
		//get a handle an the uploaded file
		var request:com.sun.faces.context.MyHttpServletRequestWrapper = con.getRequest();
		var map:java.util.Map = request.getParameterMap();
	 	var fileData:com.ibm.xsp.http.UploadedFile = map.get( strFileData );
	 	if (strFileName == null || strFileName == "") {
	 		strFileName = fileData.getClientFileName();
	 	}	 	
	 	
	 	//get uploaded file
	 	var tempFile:java.io.File = fileData.getServerFile();		//the uploaded file with a cryptic name
	 	
	 	
	 	
	 		
	 	
	 	//check file size
	 	var uploadMaxSize = upload.getString('maxsize'); 
		if ( uploadMaxSize > 0) {
			var fileSize = (tempFile.length() / 1024);		//file size in KB
			if ( fileSize > uploadMaxSize) {
				throw("filesize (" + Math.round(fileSize) + " KB) exceeds maximum filesize (" + uploadMaxSize + " KB)");
			}
		}	 	
	 	if (docTarget.getItemValueString("Photo_Title").equals("") ) {
	 		docTarget.replaceItemValue("Photo_Title", strFileName);
	 	}	 	
	 	
	 	//embed original file in target document
	 	var rtFiles:NotesRichTextItem;	 		
	 	strItemNameOriginal = "Photo_Original";
	 	if (docTarget.hasItem(strItemNameOriginal) ) {
	 		rtFiles = docTarget.getFirstItem(strItemNameOriginal);
	 	} else {
	 		rtFiles = docTarget.createRichTextItem(strItemNameOriginal);
	 	}	 	 	
	 	correctedFile = new java.io.File( tempFile.getParentFile().getAbsolutePath() + java.io.File.separator + strFileName );
	 	
	 	//rename the file on the OS so we can embed it with its correct name
	 	var success = tempFile.renameTo(correctedFile);	
	 	rtFiles.embedObject(lotus.domino.local.EmbeddedObject.EMBED_ATTACHMENT, "", correctedFile.getAbsolutePath(), null);
	 	docTarget.replaceItemValue(strItemNameOriginal + "Filename", strFileName );
	 	docTarget.replaceItemValue( "Photo_OriginalFilesize", correctedFile.length() );
		
	 	//resize sources
		var path = correctedFile.getParentFile().getAbsolutePath() + java.io.File.separator;
		imageResizer = resizeImage(path, correctedFile.getName(), "SM_" + correctedFile.getName(),  sessionScope.get("resizeConfig-Smaller")); 
		docTarget.replaceItemValue("Photo_Original_Height", imageResizer.getSourceImageHeight() );
		docTarget.replaceItemValue("Photo_Original_Width", imageResizer.getSourceImageWidth() );
		
		//create and embed medium size image
		//if (sessionScope.get("prefUserDebug")) { print ("- resizing for medium"); }
		strItemNameOriginal = "Photo_Smaller";
		if (docTarget.hasItem(strItemNameOriginal) ) {
	 		rtFiles = docTarget.getFirstItem(strItemNameOriginal);
	 	} else {
	 		rtFiles = docTarget.createRichTextItem(strItemNameOriginal);
	 	}		
		rtFiles.embedObject(lotus.domino.local.EmbeddedObject.EMBED_ATTACHMENT, "", path + imageResizer.getTargetFileName(), null);
		docTarget.replaceItemValue("Photo_Small" + "Filename", imageResizer.getTargetFileName() );
		docTarget.replaceItemValue("Photo_Small_Height", imageResizer.getTargetImageHeight());
		docTarget.replaceItemValue("Photo_Small_Width", imageResizer.getTargetImageWidth());
		docTarget.replaceItemValue("Photo_SmallFilesize", imageResizer.getTargetFileLength() );
		
		//cleanup
		imageResizer.cleanup();		
		
		//create and embed thumbnail
		//if (sessionScope.get("prefUserDebug")) { print ("- resizing for thumbnail"); }
		imageResizer = resizeImage(path, correctedFile.getName(), "TN_" + correctedFile.getName(), sessionScope.get("resizeConfig-Thumbnail"));
		strItemNameOriginal = "Photo_Thumb";
		if (docTarget.hasItem(strItemNameOriginal) ) {
	 		rtFiles = docTarget.getFirstItem(strItemNameOriginal);
	 	} else {
	 		rtFiles = docTarget.createRichTextItem(strItemNameOriginal);
	 	}	 	
		rtFiles.embedObject(lotus.domino.local.EmbeddedObject.EMBED_ATTACHMENT, "",	path + imageResizer.getTargetFileName(), null);
		docTarget.replaceItemValue(strItemNameOriginal + "Filename", imageResizer.getTargetFileName() );
		docTarget.replaceItemValue("Photo_Thumb_Height", imageResizer.getTargetImageHeight());
		docTarget.replaceItemValue("Photo_Thumb_Width", imageResizer.getTargetImageWidth() );
		docTarget.replaceItemValue("Photo_ThumbFilesize", imageResizer.getTargetFileLength() );
		
		//thumbnailURL = facesContext.getExternalContext().getRequestContextPath() + "/0/" + docTarget.getUniversalID() +	"/$file/" + imageResizer.getTargetFileName();
		thumbnailURL = "/" + datasource.getString('DB_FILEPATH') + "/0/" + docTarget.getUniversalID() +	"/$file/" + imageResizer.getTargetFileName();
		//cleanup
		imageResizer.cleanup();	
		
		//read all metadata from uploaded file
	 	readMetaData( tempFile );	 
	 	//if (sessionScope.get("prefUserDebug")) { print ("finished"); }
	} catch(e) {
		throw(e);
	} finally {
		try {			
			if (imageResizer != null) {
				imageResizer.cleanup();
			}			
			if (correctedFile != null) {
				//rename the temporary file back to its original name so it's automatically
			 	//removed from the os' file system.
			 	//we could also do:
			 	//correctedFile.delete()
			 	//but for some reason the Javascript source editor doesn't allow that... 	
			 	correctedFile.renameTo(tempFile);	
			}
		} catch(ee) {			
		}		
	}	 	
 	return thumbnailURL;
}

function readMetaData( file:java.io.File ) {
	//not implemented yet	
	//attempt to get GPS data
	try{
		importPackage(javaxt.io);
		var image = new javaxt.io.Image(tempFile);
		if (image != null){
			print("image not null")
			/*
			gps = image.getGPSCoordinate();
			if (gps != null){
				print("gps not null");
				print(gps);
				docTarget.replaceItemValue("GPS", image.getGPSCoordinate());	
			}
			*/
			
			var height = image.getHeight();
			if (height != null){
				print(height);
			}
			else{
				print("no height")
			}
							
		}
	} catch (e) {
		print("Upload.readMetaData: " + e.toString());
	}	
 		
	
	
	return;	
	importPackage(com.drew);	
	try {	
		var metadata:com.drew.metadata.Metadata = null;
		metadata = com.drew.imaging.jpeg.JpegMetadataReader.readMetadata(tempFile);
		print("metadata read...");		
	} catch (e) {
		print("Upload.readMetaData: " + e.toString());
	}	
}

function resizeImage(path, fileNameIn, fileNameOut, config ) {
	//see class via Package Explorer...
	var resizeObject = new org.openntf.bildr.ScaledImage(path, fileNameIn);	
	resizeObject.setDebug( sessionScope.get("prefUserDebug") );
	resizeObject.setHigherQuality(true);	
	switch (config.mode) {
		case 1:
			resizeObject.setResizeMode(config.mode, config.x, config.y);
			break;
			
		case 2: case 3:
			resizeObject.setResizeMode(config.mode, config.maxValue, 0);
			break;
			
		case 4:
			resizeObject.setResizeMode(config.mode, config.perc, 0);
			break;
	}	
	resizeObject.resize(path + fileNameOut);
	return resizeObject;
}

/*
 * Creates a new image document in the current database
 * 
 * Note: since the flash uploads are done anonymously, we need to read values from the sessionScope here
 * and not in the embedUploadedFile function
 */
function bildrCreateDocument(strFileName) {	
	var docImage:NotesDocument = null;
	try {	
		//GOES WRONG HERE	
		var db:NotesDatabase = session.getDatabase("", datasource.getString('DB_FILEPATH'), false);
		
		docImage = db.createDocument();
		docImage.replaceItemValue("form", "$f-picture");		
		var imageId = docImage.getUniversalID().toLowerCase();		
		docImage.replaceItemValue("Tx_id", imageId );		
		docImage.replaceItemValue("Au_Author", session.getEffectiveUserName());
		 //fill document fields	 
	 	if (!sessionScope.get("uploadTitleType").equals("afterwards") ) {
			docImage.replaceItemValue ("Photo_Title", sessionScope.get("uploadTitle"));
		}		
		if (sessionScope.get("uploadCategory") == "newCat") {
			docImage.replaceItemValue ("Photo_Category", sessionScope.get("uploadCategoryNew"));
		} else {
			docImage.replaceItemValue ("Photo_Category", sessionScope.get("uploadCategory"));
		}
		
		//docImage.replaceItemValue("PhotoLat", sessionScope.get("uploadGeoLat"));
		//docImage.replaceItemValue("PhotoLng", sessionScope.get("uploadGeoLon"));
		//remove empties
		/*
		var tags = sessionScope.get("uploadTags");
		
		if (tags != null){
			if (typeof tags == "string") { 
				tags = [tags]; 
			}
			var tagsCleared = [];
			for(var i = 0; i<tags.length; i++){
				if (tags[i]){
					tagsCleared.push(tags[i]);
				}
			}		
			docImage.replaceItemValue("Photo_Tags", tagsCleared);			
		}
		*/
		
		docImage.replaceItemValue("PhotoDescription", sessionScope.get("uploadDescription") );
		docImage.replaceItemValue("Photo_Carousel", sessionScope.get("uploadinCarousel"));
		docImage.computeWithForm(true, false);
		//add this image to the selected album
		addImageToAlbum( sessionScope.get("uploadAlbumId"), sessionScope.get("uploadAlbumNew"), imageId);
		
	} catch (e) {
		print ("error: " + e.toString() );
		docImage = null;
	}	
	return docImage;
}

/* add an image to an album */
function addImageToAlbum( albumId, albumName, imageId) {	
	if (albumId == "" || albumId==null) {
		return;
	}	
	var docAlbum:NotesDocument = null;
	try{
		if (albumId=="newAlbum") {//create new album	
			
			var db:NotesDatabase = session.getDatabase("", datasource.getString('DB_FILEPATH'), false);
		
			docAlbum = db.createDocument();
			docAlbum.replaceItemValue("form","$f-album");
			docAlbum.replaceItemValue("Tx_name",albumName);
			docAlbum.replaceItemValue("Au_Author",session.getEffectiveUserName() );
			docAlbum.getFirstItem("Au_Author").setAuthors(true);			
			//update album id in sessionscope var
			sessionScope.put("uploadAlbumId",docAlbum.getUniversalID());			
			// write activy
			//var userName:NotesName = session.createName(@UserName());
			//registerActivity(userName.getCommon(),"created","an album",docAlbum.getUniversalID())
		} else {			
			//retrieve existing album
			var db:NotesDatabase = session.getDatabase("", datasource.getString('DB_FILEPATH'), false);
			docAlbum = db.getDocumentByUNID( albumId );
		}		
		if (docAlbum != null) {			
			addItemValue(docAlbum,"Tx_pictureIds",imageId);			
			var n = session.createDateTime("Today");
			n.setNow();
			docAlbum.replaceItemValue( "Dt_lastModified",n);			
			docAlbum.save();
		}		
	} catch(e) {
		print("error in addImageToAlbum:" + e.toString());
	}
}

/*
 * events for the aUpload, aUploadA and aGetAuthKey XPages
 */

var aUpload = {
	beforeRenderResponse : function() {
		try {	
			var con:javax.faces.context.ExternalContext = facesContext.getExternalContext();
			var response = con.getResponse();			
			//validate input
			var strFileName = param.get("name");	
			var dataName = param.get("dataName");		
			if (dataName==null || dataName == "") {
				dataName = "file";	//default name for files uploaded by the SWF uploader
			}		
			//check file extension
			var ext:String = strFileName.substring( strFileName.lastIndexOf(".")+1 ).toLowerCase() ;
			if (!sessionScope.get("prefIMGExtensions").contains(ext)) {
				throw("invalid file extension: "+ext);
			}			
			//check if user has the upload role
			if ( !(sessionScope.get("prefUserRoles").contains("[upload]")) ) {
				throw("not authorized");
			}			
			//create target document in current database
			var docTarget:NotesDocument = bildrCreateDocument(strFileName);
			if (docTarget == null) {
				throw("could not create target document");
			}			
		 	//embed the uploaded file to the target document 
		 	var thumbnailURL = embedUploadedFile( con, dataName, docTarget, strFileName);
		 	docTarget.save();		 	
		 	// write activy
		 	//var userName:NotesName = session.createName(@UserName());
		 	//registerActivity(userName.getCommon(),"uploaded","a photo",docTarget.getUniversalID())
		 	//send required pl upload response
			var writer = response.getWriter();		
			response.setContentType("text/html");
			response.setHeader("Cache-Control", "no-cache");		//no caching
			response.setDateHeader("Expires", -1);		
		 	writer.write( "{\"jsonrpc\" : \"2.0\", \"result\" : null, \"id\" : \"id\", \"thumbnailURL\" : \"" + thumbnailURL + "\"}"); 
		 	facesContext.responseComplete();		
		} catch (e) {
			print(e.toString());
			response.sendError(400, "error: " + e.toString() );
			facesContext.responseComplete();
			return;
		} 	
	}	
}

var aUploadA = {
	beforeRenderResponse : function() {		
		try {	
			var con:javax.faces.context.ExternalContext = facesContext.getExternalContext();
			var response = con.getResponse();			
			//validate input
			var strTargetUNID = context.getUrlParameter("id");//unid of the target document
			var strAuthKey = context.getUrlParameter("authKey");//authentication key: must be available in the target doc
			var strFileName = param.get("name");	
			var dataName = param.get("dataName");		
			if (dataName==null || dataName == "") {
				dataName = "file";//default name for files uploaded by the SWF uploader
			}			
			//check required parameters
			if (strTargetUNID == "" || strAuthKey=="") {
				throw("invalid input");
			} 				
			//get target document in current database
			var docTarget:NotesDocument = null;		
			//since this XPage is called anonymous, we have to re-retrieve the target (current) database using a session as signer
			//var db = sessionAsSigner.getDatabase("", session.getCurrentDatabase().getFilePath());
			
			var db:NotesDatabase = session.getDatabase("", datasource.getString('DB_FILEPATH'), false);
			
			//try-catch around this or it will instantly throw an error
			try {
				docTarget = db.getDocumentByUNID( strTargetUNID );
			} catch(e) { }			
			if (docTarget == null) {
				throw("invalid input");
			}			
			//validate auth key
			if (docTarget.getItemValueString("authKey") != strAuthKey) {
				throw("not authorized");
			}		 	
		 	//embed the uploaded file to the target document 
		 	var thumbnailURL = embedUploadedFile( con, dataName, docTarget, strFileName);
		 	docTarget.removeItem("authKey");
		 	docTarget.save();		 	
		 	//send required pl upload response
			var writer = response.getWriter();		
			response.setContentType("text/html");
			response.setHeader("Cache-Control", "no-cache");		//no caching
			response.setDateHeader("Expires", -1);		
		 	writer.write( "{\"jsonrpc\" : \"2.0\", \"result\" : null, \"id\" : \"id\", \"thumbnailURL\" : \"" + thumbnailURL + "\"}"); 
		 	facesContext.responseComplete();		
		} catch (e) {
			if (docTarget != null) {
				print("cleanup");
				docTarget.remove(true);
			}			
			response.sendError(400, "error: " + e.toString() );
			facesContext.responseComplete();
			return;
		} 		
	}
}

var aGetAuthKey = {	
	beforeRenderResponse : function() {		
		try {	
			var con:javax.faces.context.ExternalContext = facesContext.getExternalContext();
			var response = con.getResponse();
			var writer = response.getWriter();			
			//if (sessionScope.get("prefUserDebug")) { print ("retrieving authorization key"); }
			writer.write( "{" );			
			var isError = false;
			var msgError = "";		
			response.setContentType("content-type: application/json");
			response.setHeader("Cache-Control", "no-cache");		//no caching
			response.setDateHeader("Expires", -1);			
			//authorisation check: [upload] role
			if (!sessionScope.get("prefUserRoles").contains("[upload]") ) {
				throw("authorization failure");	
			}			
			//check for valid file types
			var strFileName:String = context.getUrlParameter("fileName");
			var ext:String = strFileName.substring( strFileName.lastIndexOf(".")+1 ).toLowerCase() ;
			if (!sessionScope.get("prefIMGExtensions").contains( ext) ) {
				throw("invalid image extension (" + ext + ")");
			}		 	
			var docTarget:NotesDocument = bildrCreateDocument(strFileName);
			if (docTarget == null) {
				throw("ccould not create image document");
			}			
			//if (sessionScope.get("prefUserDebug")) { print ("- image document created (id: " + docTarget.getUniversalID() + ")"); }
			writer.write( "targetUnid:\"" + docTarget.getUniversalID() + "\",");
			//generate the authentication key (unid of a temporary document)
			var docTemp:NotesDocument = database.createDocument();
			var strAuthKey:String = docTemp.getUniversalID();
			//store the authentication key in the target document
			//note: this will fail if the web user isn't allowed to write in that document
			docTarget.replaceItemValue("authKey", strAuthKey)
			docTarget.save();			
			//if (sessionScope.get("prefUserDebug")) { print ("- key: " + strAuthKey); }
			writer.write( "authKey:\"" + strAuthKey + "\",");			
		} catch (e) {		
			msgError = e.toString();
			isError = true;			
		} finally {			
			writer.write( "isError:" + (isError ? "true" : "false") + ",");
			writer.write( "errorMsg:\"" + msgError + "\"");
			writer.write( "}" );
			facesContext.responseComplete();			
		}		
	}
}