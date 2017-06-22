$(function() {
	
	
});

function __getDeviceDetail(devnode){
	switch(devnode.getLastChild().key){
	case "Trans":
		$(".candile").load("/opticalTran");
		break;
	case "other":
		$(".candile").load("/rece_workstation");
		break;
	case "EDFA":
		$(".candile").load("/opticalTran");
		break;
	case "rece_workstation":
		$(".candile").load("/rece_workstation");
		if(devnode.getLastChild().data.hfctype == "HfcMinWorkstation"){
			
		}
		break;
	case "OSW":
		$(".candile").load("/opticalTran");
		break;
	case "RFSW":
		$(".candile").load("/opticalTran");
		break;
	case "PreAMP":
		$(".candile").load("/opticalTran");
		break;
	case "wos":
		$(".candile").load("/opticalTran");
		break;
	default:
		$(".candile").load("/rece_workstation");
		break;
	}
}

function showHfcDevice(jsonobj){
	$(".dev-status").css("color", "lightgreen");
	switch(jsonobj.devtype){
	case "Trans":
		
		break;
	case "other":
		
		break;
	case "EDFA":
		
		break;
	case "rece_workstation":
		parse_rece_workstation(jsonobj);
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

function parse_rece_workstation(jsonobj){
	if(__globalobj__._realDevice.getLastChild().data.hfctype == "HfcMinWorkstation"){
		$("#detail_mineroptical").parent().css("display", "none");
		$("#detail_mineroptical").parent().prev().css("display", "none");
		$("#detail_minerelectric").parent().parent().css("display", "none");
		$("#detail_minieratt").parent().parent().css("display", "none");
	}	
	$('#panel-devip')[0].textContent = __globalobj__._realDevice.key;
	$('#panel-onlinetimeticks')[0].textContent = jsonobj.common.sysUpTime;
	$('#panel-devinfo')[0].textContent = jsonobj.common.sysDescr;
	$('#panel-devcontact')[0].textContent = jsonobj.common.sysContact;
	$('#fnRFChannelNum').val(jsonobj.fnRFChannelNum);
	$('#fnOpticalReceiverPower').val(jsonobj.fnOpticalReceiverPower); 
	$('#commonInternalTemperature').val(jsonobj.common.commonInternalTemperature);
	$('#commonNELogicalID').val(jsonobj.common.commonNELogicalID);
	$('#commonNEModelNumber').val(jsonobj.common.commonNEModelNumber);	
	$('#commonNESerialNumber').val(jsonobj.common.commonNESerialNumber);
	$('#commonDeviceMACAddress').val(jsonobj.common.commonDeviceMACAddress);
	switch(jsonobj.fnOpticalReceiverPower6){
	case "1":
        //normal
		$('#fnOpticalReceiverPower').css("background-color", "green");
        break;
    case "2":
    //hihi
    case "5":
        //lolo
    	$('#fnOpticalReceiverPower').css("background-color", "red");
        break;
    case "3":
    //hi
    case "4":
        //lo
    	$('#fnOpticalReceiverPower').css("background-color", "yellow");
        break;
	}
	var i = 0;
	$.each(jsonobj.powertbl, function(key, itemv) {
		$('.fnDCPowerName_row' + i)[0].textContent = itemv.fnDCPowerName_row;
		$('.fnDCPowerVoltage_row' + i)[0].textContent = itemv.fnDCPowerVoltage_row;
		i++;
	});	
	i = 0;
	$.each(jsonobj.pumptbl, function(key, itemv) {
		$('.fnRFPortName_row' + i)[0].textContent = itemv.fnRFPortName_row;
		$('.fnOutputRFlevelatt_row' + i)[0].textContent = itemv.fnOutputRFlevelatt_row;
		$('.fnOutputRFleveleq_row' + i)[0].textContent = itemv.fnOutputRFleveleq_row;
		$('.fnRFPortOutputRFLevel_row' + i)[0].textContent = itemv.fnRFPortOutputRFLevel_row;
		i++;
	});
	i = 0;
	$.each(jsonobj.common.traptbl, function(key, itemv) {
		$('#commonAgentTrapIP_row' + i)[0].textContent = itemv.commonAgentTrapIP_row;
		i++;
	});
	switch(jsonobj.fnDCPowerVoltage06){
	case "1":
        //normal
		$('.fnDCPowerVoltage_row0').css("background-color", "green");
        //textBoxVariable.BackColor = Color.LightGreen;
        break;
    case "2":
    //hihi
    case "5":
        //lolo
    	$('.fnDCPowerVoltage_row0').css("background-color", "red");
        break;
    case "3":
    //hi
    case "4":
        //lo
    	$('.fnDCPowerVoltage_row0').css("background-color", "yellow");
        break;
	}
	switch(jsonobj.fnDCPowerVoltage16){
	case "1":
        //normal
		$('.fnDCPowerVoltage_row1').css("background-color", "green");
        //textBoxVariable.BackColor = Color.LightGreen;
        break;
    case "2":
    //hihi
    case "5":
        //lolo
    	$('.fnDCPowerVoltage_row1').css("background-color", "red");
        break;
    case "3":
    //hi
    case "4":
        //lo
    	$('.fnDCPowerVoltage_row1').css("background-color", "yellow");
        break;
	}
	switch(jsonobj.fnRFPortOutputRFLevel06){
	case "1":
        //normal
		$('.fnRFPortOutputRFLevel_row0').css("background-color", "green");
        //textBoxVariable.BackColor = Color.LightGreen;
        break;
    case "2":
    //hihi
    case "5":
        //lolo
    	$('.fnRFPortOutputRFLevel_row0').css("background-color", "red");
        break;
    case "3":
    //hi
    case "4":
        //lo
    	$('.fnRFPortOutputRFLevel_row0').css("background-color", "yellow");
        break;
	}
	switch(jsonobj.fnRFPortOutputRFLevel16){
	case "1":
        //normal
		$('.fnRFPortOutputRFLevel_row1').css("background-color", "green");
        //textBoxVariable.BackColor = Color.LightGreen;
        break;
    case "2":
    //hihi
    case "5":
        //lolo
    	$('.fnRFPortOutputRFLevel_row1').css("background-color", "red");
        break;
    case "3":
    //hi
    case "4":
        //lo
    	$('.fnRFPortOutputRFLevel_row1').css("background-color", "yellow");
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
		 if(jsonobj.detail.HIHIen == '1'){
			 $('#ishihi').attr('checked', 'checked');
		 }
		 $("#hihi").val(jsonobj.detail.value0);
		 if(jsonobj.detail.HIen == '1'){
			 $('#ishi').attr('checked', 'checked');
		 }
		 $("#hi").val(jsonobj.detail.value1);
		 if(jsonobj.detail.LOen == '1'){
			 $('#islo').attr('checked', 'checked');
		 }
		 $("#lo").val(jsonobj.detail.value2);
		 if(jsonobj.detail.LOLOen == '1'){
			 $('#islolo').attr('checked', 'checked');
		 }
		 $("#lolo").val(jsonobj.detail.value3);
		 //if(jsonobj.detail.ISDEAD){
		//	 $('#isdead').attr('checked', 'checked');
		 //}
		 $("#dead").val(jsonobj.detail.value4);
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
     	    		  var datastring = '{"cmd":"hfcvalueset","target":"setalarmThreshold","ip":"' + node.title +'","domstr":"'+ jsonobj.domstr +'","devtype":"'+ __globalobj__._realDevice.getLastChild().key
     	    		  	+'","rcommunity":"'+ __globalobj__._realDevice.data.rcommunity +'","wcommunity":"'+ __globalobj__._realDevice.data.wcommunity +'","HIHI":"'+ $("#hihi").val()
     	    		  	+'","HI":"'+ $("#hi").val() +'","LO":"'+ $("#lo").val() +'","LOLO":"'+ $("#lolo").val() +'","DEAD":"'+ $("#dead").val() +'","ISHIHI":"'+ ($('#ishihi').attr('checked') =='checked'?true:false)
     	    		  	+'","ISHI":"'+ ($('#ishi').attr('checked') =='checked'?true:false) +'","ISLO":"'+ ($('#islo').attr('checked') =='checked'?true:false) +'","ISLOLO":"'+ ($('#islolo').attr('checked') =='checked'?true:false)
     	    		  	+'","isRow":"'+ jsonobj.isRow +'","rowNum":"'+ jsonobj.rowNum+'"}';
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

function getNetTypeTostring(pNetTypes)
{
    switch (pNetTypes)
    {
        case "other":
            return "其他设备";
        case "EDFA":
            return "EDFA";
        case "Trans":
            return "光发射机";
        case "rece_workstation":
            return "光接收机/光工作站";
        case "OSW":
            return "光切换开关";
        case "RFSW":
            return "射频切换开关";
        case "PreAMP":
            return "前置放大器";
        case "wos":
            return "光平台";
        default:
            return "其他设备";

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


