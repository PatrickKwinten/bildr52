/*
<<
Copyright 2010 Mark Leusink
Licensed under the Apache License, Version 2.0 (the "License"); you may not use this 
file except in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under
the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF 
ANY KIND, either express or implied. See the License for the specific language governing
permissions and limitations under the License
>> 
*/

dojo.require("dojo.fx");

//Add onLoad function that adds a wipein / wipeout effect to all sections
dojo.addOnLoad( function() {

	//override init section function
	var oldInitSection = XSP.initSectionScript;
	
	XSP.initSectionScript = function x_iss(targetSectionId, sectionId, expand){ 
		oldInitSection.apply( this, arguments );		//call the original function
		
		//rename the existing content div
		if (expand) { //only do this for one of the two calls to this function
			var section = dojo.byId(sectionId);
			var contents = dojo.byId(sectionId + "_contents");
		
			dojo.attr(contents, "id", sectionId + "_contents2");		//rename id attribute of original contents
		
			//create hidden div with old contents id
			var div = dojo.create( "div", { id : sectionId + "_contents" } , section );
			dojo.style( div, "display", dojo.style( contents, "display") );		//set same style
		}
	}
	
	//override function to show/hide a section
	var oldShowSection = XSP.showSection;
	
	XSP.showSection = function x_ss(sectionId,show) {
		oldShowSection.apply( this, arguments );		//call the original function

		//show/hide the contents node using a wipeIn/wipeOut effect
		if (show) {
			dojo.fx.wipeIn( { node : sectionId + "_contents2" } ).play();
		} else {
			dojo.fx.wipeOut( { node : sectionId + "_contents2" } ).play();
		}
	}

});

//function is called using x$("#{id:inputText1}", " parameters").
function x$(idTag, param){ //Updated 18 Feb 2012
   idTag=idTag.replace(/:/gi, "\\:")+(param ? param : "");
   return($("#"+idTag));
}

//http://openntf.org/XSnippets.nsf/snippet.xsp?id=gethashurlvars
//Thomas Adrian
function getHashUrlVars(){
  var vars = [], hash;
  var hashes = window.location.href.slice(window.location.href.indexOf('#') + 1).split('&');
  for(var i = 0; i < hashes.length; i++)
  {
      hash = hashes[i].split('=');
      vars.push(hash[0]);
      vars[hash[0]] = hash[1];
  }
  return vars;
}


 