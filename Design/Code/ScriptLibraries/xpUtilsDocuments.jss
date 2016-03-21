function registerActivity(actor,verb,subject,reference){
	
	var db:NotesDatabase = session.getDatabase("", datasource.getString('DB_FILEPATH'), false);

	var docActivity:NotesDocument = db.createDocument();
	docActivity.appendItemValue("form", "$f-activity");
	docActivity.appendItemValue("Tx_Actor", actor);
	docActivity.appendItemValue("Tx_Action", verb);
	docActivity.appendItemValue("Tx_Subject", subject);
	docActivity.appendItemValue("Tx_Date", @Text(@Now()));
	docActivity.appendItemValue("Tx_Reference", reference);
	docActivity.save();
}

function returnPropery(){
	return datasource.getString('DB_FILEPATH');	
}