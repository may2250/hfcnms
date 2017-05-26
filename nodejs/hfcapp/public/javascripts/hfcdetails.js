$(function() {
	
	
});

function showopticalTran(jsonobj){
	switch(jsonobj.devtype){
	case "devtye1":
		
		break;
	case "devtype2":
		
		break;
	default:
			
			break;
		
	}
}

function updateTips( t ) {
	  $( ".validateTips" )
      .text( t );
}

function ipvalidate(ip) {  
    var val = /([0-9]{1,3}\.{1}){3}[0-9]{1,3}/;  
    var vald = val.exec(ip);  
    if (vald == null) {    
        return false;  
    }  
    if (vald != '') {  
        if (vald[0] != ip) {    
            return false;  
        }  
    }
    return true;
}

function checkInt(str){
	var re = /^[1-9]+[0-9]*]*$/; 
　　 if (!re.test(str)) {
　　　　return false;
　　}
	return true;
}


