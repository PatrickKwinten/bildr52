/** ****************************************************************************
* @URLEncode()
* provides closely the same functionality its @Formula pendant, that is it
* encodes an object to a URL encoded format
*
* @param encodeObject the Object to encode. The Objects toString() method is
*                 used to retrieve a String to encode!
* @param encSch optional encoding scheme to use
* @see java.net.URLEncoder
*         (http://java.sun.com/j2se/1.5.0/docs/api/java/net/URLEncoder.html)
* @see http://java.sun.com/j2se/1.5.0/docs/api/java/lang/package-summary.html
* @returns URL encoded version of encodeObject or null in case of any error
* @author Michael Gollmick
* @version 1.0
* @date 20090509
* @depends java.net.URLEncoder
**************************************************************************** **/
function @URLEncode(encodeObject, encSch:String) {
	try {
		var encScheme = ((encSch) && (encSch !== null))?encSch:"UTF-8";
		return java.net.URLEncoder.encode(encodeObject.toString(),encScheme);
	} catch (e) {
		print("ERROR in @URLEncode:" + e);
	}
	return null;
}

/** ****************************************************************************
* @URLDecode()
* provides closely the same functionality its @Formula pendant, that is it
* Decodes a URL Encoded string to normal format
*
* @param strToDecode the String to decode
* @param encodeObject optional encoding scheme to use
* @see java.net.URLDecoder
*         (http://java.sun.com/j2se/1.4.2/docs/api/java/net/URLDecoder.html)
* @see http://java.sun.com/j2se/1.5.0/docs/api/java/lang/package-summary.html
* @returns decoded version of strToDecode or null in case of any error
* @author Michael Gollmick
* @version 1.0
* @date 20090509
* @depends java.net.URLDecoder
**************************************************************************** **/
function @URLDecode(strToDecode:String, encSch:String) {
	if (strToDecode == null) { return null; }
        try {
                var encScheme = ((encSch) && (encSch !== null))?encSch:"UTF-8";
                return java.net.URLDecoder.decode(strToDecode, encScheme);
        } catch (e) {
                print("ERROR in @URLDecode:" + e);
        }
        return null;
}

// add a value to an item (doesn't replace existing values)
function addItemValue(doc:NotesDocument, itemName:String, itemValue) {
	var item:NotesItem = doc.getFirstItem(itemName);
	if ( item == null ) {	//item doesn't exist
		doc.replaceItemValue(itemName,itemValue );
	} else if (!item.containsValue(itemValue)) {
		var values:java.util.Vector = item.getValues();
		if (values == null) {		
			//item doesn't have a value (e.g. empty string)
			doc.replaceItemValue(itemName,itemValue);
		} else {
			values.add(itemValue);
			item.setValues(values);
		}
	}	
}

/* 

This server side script library implements a class named "CGIVariables" which allows for easy access 
to most CGI variables in XPages via javascript. 
                
For example, to dump the remote users name, IP address and browser string to the server console, use: 

var cgi = new CGIVariables(); 
print ("Username: " + cgi.REMOTE_USER); 
print ("Address : " + cgi.REMOTE_ADDR); 
print ("Browser : " + cgi.HTTP_USER_AGENT); 
        
Written July 2008 by Thomas Gumz, IBM.  - Apache license
https://www-10.lotus.com/ldd/ddwiki.nsf/dx/xpages-cgi-variables.htm
https://openntf.org/XSnippets.nsf/snippet.xsp?id=how-to-access-cgi-variables-in-xpages 

*/ 

function CGIVariables() { 

// setup our object by getting refs to the request and servlet objects         
try { 
        this.request        = facesContext.getExternalContext().getRequest(); 
        this.servlet        = facesContext.getExternalContext().getContext(); 
} catch(e) { 
        print (e.message); 
} 

this.prototype.AUTH_TYPE		= this.request.getAuthType(); 
this.prototype.CONTENT_LENGTH	= this.request.getContentLength(); 
this.prototype.CONTENT_TYPE		= this.request.getContentType(); 
this.prototype.CONTEXT_PATH		= this.request.getContextPath(); 
this.prototype.GATEWATY_INTERFACE	= "CGI/1.1"; 
this.prototype.HTTPS			= this.request.isSecure() ? "ON" : "OFF"; 
this.prototype.PATH_INFO		= this.request.getPathInfo(); 
this.prototype.PATH_TRANSLATED	= this.request.getPathTranslated(); 
this.prototype.QUERY_STRING		= this.request.getQueryString(); 
this.prototype.REMOTE_ADDR    	= this.request.getRemoteAddr(); 
this.prototype.REMOTE_HOST 		= this.request.getRemoteHost(); 
this.prototype.REMOTE_USER   	= this.request.getRemoteUser(); 
this.prototype.REQUEST_METHOD  	= this.request.getMethod(); 
this.prototype.REQUEST_SCHEME 	= this.request.getScheme(); 
this.prototype.REQUEST_URI    	= this.request.getRequestURI(); 
this.prototype.SCRIPT_NAME     	= this.request.getServletPath(); 
this.prototype.SERVER_NAME    	= this.request.getServerName(); 
this.prototype.SERVER_PORT  	= this.request.getServerPort(); 
this.prototype.SERVER_PROTOCOL 	= this.request.getProtocol(); 
this.prototype.SERVER_SOFTWARE	= this.servlet.getServerInfo(); 

// these are not really CGI variables, but useful, so lets just add them for convenience 
this.prototype.HTTP_ACCEPT  	= this.request.getHeader("Accept"); 
this.prototype.HTTP_ACCEPT_ENCODING 	= this.request.getHeader("Accept-Encoding"); 
this.prototype.HTTP_ACCEPT_LANGUAGE = this.request.getHeader("Accept-Language"); 
this.prototype.HTTP_CONNECTION	= this.request.getHeader("Connection"); 
this.prototype.HTTP_COOKIE 		 = this.request.getHeader("Cookie");         
this.prototype.HTTP_HOST                        = this.request.getHeader("Host");         
this.prototype.HTTP_REFERER                        = this.request.getHeader("Referer"); 
this.prototype.HTTP_USER_AGENT                = this.request.getHeader("User-Agent"); 

this.prototype.getURLParam = function (name){ 
        name = name.replace(/[\[]/,"\\\[").replace(/[\]]/,"\\\]"); 
        var regexS = "[\\?&]"+name+"=([^&#]*)"; 
        var regex = new RegExp( regexS ); 
        var results = regex.exec( "?" + this.request.getQueryString() ); 
        if( results == null ) 
                return ""; 
        else 
                return results[1]; 
}
}


function isPagerVisible(pager: com.ibm.xsp.component.xp.XspPager): boolean {
    var state: com.ibm.xsp.component.UIPager.PagerState = pager.createPagerState();
    return state.getRowCount() > state.getRows(); 
}
