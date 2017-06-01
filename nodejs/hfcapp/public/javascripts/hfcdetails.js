$(function() {
	
	
});

function showHfcDevice(jsonobj){
	switch(jsonobj.devtype){
	case "Trans":
		
		break;
	case "other":
		
		break;
	case "EDFA":
		
		break;
	case "rece_workstation":
		
		break;
	case "OSW":
		
		break;
	case "RFSW":
		
		break;
	case "PreAMP":
		
		break;
	case "wos":
		
		break;
	default:
			
			break;
		
	}
}

function parseHfcValueSet(jsonobj){
	switch(jsonobj.target){
	 case "devicetrapedit":
		 $("#" + jsonobj.domstr)[0].textContent = jsonobj.value;
		 break;
	 case "devicechannel":
		 $("#" + jsonobj.domstr).val(jsonobj.value);
		 break;
	 case "getalarmThreshold":
		 $("#hihi").val(jsonobj.detail.HIHI);
		 $("#hi").val(jsonobj.detail.HI);
		 $("#lo").val(jsonobj.detail.LO);
		 $("#lolo").val(jsonobj.detail.LOLO);
		 $("#dead").val(jsonobj.detail.DEAD);
		 $( "#dialog-alarmThreshold" ).dialog({
	   	      autoOpen: false,
	   	      height: 393,
	   	      width: 390,
	   	      modal: true,
	   	      buttons: {
	   	    	  Ok: function() {
	   	    		  if(isNaN($("#hihi").val())){
	   	    			  $("#hihi").addClass( "ui-state-error-custom" );	 
	   	    			  return;
	   	    		  }
	   	    		  if(isNaN($("#hi").val())){
	   	    			  $("#hi").addClass( "ui-state-error-custom" );
	   	    			  return;
	   	    		  }
	   	    		  if(isNaN($("#lo").val())){
	   	    			  $("#lo").addClass( "ui-state-error-custom" );
	   	    			  return;
	   	    		  }
	   	    		  if(isNaN($("#lolo").val())){
	   	    			  $("#lolo").addClass( "ui-state-error-custom" );
	   	    			  return;
	   	    		  }
	   	    		  if(isNaN($("#dead").val())){
	   	    			  $("#dead").addClass( "ui-state-error-custom" );
	   	    			  return;
	   	    		  }
	   	    		  var node = __globalobj__._realDevice.getFirstChild();
     	    		  var datastring = '{"cmd":"hfcvalueset","target":"setalarmThreshold","ip":"' + node.title +'","domstr":"'+ jsonobj.domstr +'","HIHI":"'+ $("#hihi").val()
     	    		  	+'","HI":"'+ $("#hi").val() +'","LO":"'+ $("#lo").val() +'","LOLO":"'+ $("#lolo").val() +'","DEAD":"'+ $("#dead").val() +'"}';
     	    		  __globalobj__._send(datastring);
     	              $( this ).dialog( "close" );
	   	          }
	   	      },
	   	      close: function() {
	   	    	  	$("#hihi").removeClass("ui-state-error-custom");
		   	    	$("#hi").removeClass("ui-state-error-custom");
		   	    	$("#lo").removeClass("ui-state-error-custom");
		   	    	$("#lolo").removeClass("ui-state-error-custom");
		   	    	$("#dead").removeClass("ui-state-error-custom");
	       	  }
		 });
		 $("#dialog-alarmThreshold").dialog("open");
		 break;
	 }	     
}

function updateTips( t ) {
	  $( ".validateTips" )
      .text( t );
}

function ipvalidate(ip) {  
    var val = /^(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])$/;  
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

//比较两个ip的大小,如果大于，返回1，等于返回0，小于返回-1  
function compareIP(ipBegin, ipEnd)  
{  
    var temp1;  
    var temp2;    
    temp1 = ipBegin.split(".");  
    temp2 = ipEnd.split(".");     
    for (var i = 0; i < 4; i++)  
    {  
        if (temp1[i]>temp2[i])  
        {  
            return 1;  
        }  
        else if (temp1[i]<temp2[i])  
        {  
            return -1;  
        }  
    }  
    return 0;     
}  

function checkInt(str){
	var re = /^[1-9]+[0-9]*]*$/; 
　　 if (!re.test(str)) {
　　　　return false;
　　}
	return true;
}

function checkFloat(str){
	var re = /^-?([1-9]\d*\.\d*|0\.\d*[1-9]\d*|0?\.0+|0)$/;
	if (!re.test(str)) {
	　　　　return false;
	　　}
	return true;
}


