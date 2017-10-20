/**
 *
 */
$(".hfcunit").dblclick(function () {
	if (sessionStorage.authlevel == 3) {
		return;
	}
	var domstr = $(this)[0].id;
	$("#dialog-form").dialog({
		autoOpen: false,
		height: 240,
		width: 300,
		modal: true,
		buttons: {
			Ok: function () {
				if (checkInt($("#set_value").val())) {
					var node = __globalobj__._realDevice.getFirstChild();
					var datastring = '{"cmd":"hfcvalueset","target":"setVars","ip":"' + __globalobj__._realDevice.key + '","isRow":"false","domstr":"' + domstr + '","value":"' + $("#set_value").val() + '"}';
					__globalobj__._send(datastring);
					$(this).dialog("close");
				} else {
					$("#set_value").addClass("ui-state-error-custom")
				}

			}
		},
		close: function () {
			$("#set_value").removeClass("ui-state-error-custom");
		}
	});

	$("#set_value").val($(this).val().replace(/[^\d.-]/g, ''));
	//updateTips($.i18n.prop('message_devnewval'));
		var comvaluess = $(this).data("comvalues");
	updateTips(comvaluess);
	$("#dialog-form").dialog("open");
});

$(".hfcunitcombox").dblclick(function () {
	if (sessionStorage.authlevel == 3) {
		return;
	}
	var domstr = $(this)[0].id;
		var comvaluess = $(this).data("comvalues");
	//alert(comvaluess);
	var mArray = comvaluess.split(",");
	$('#comvaluelist').empty();
	for (var i = 0; i < mArray.length; i++) {
		$('#comvaluelist').append($("<option value='" + mArray[i] + "'>" + mArray[i] + "</option>"));
	}
	
	$('#comvaluelist').val($(this).val());
	$("#dialog-comboxset").dialog({
		autoOpen: false,
		height: 240,
		width: 300,
		modal: true,
		buttons: {
			Ok: function () {

					var node = __globalobj__._realDevice.getFirstChild();
					var sleetindex = $("#comvaluelist").get(0).selectedIndex + 1;
					var datastring = '{"cmd":"hfcvalueset","target":"setVars","ip":"' + __globalobj__._realDevice.key + '","isRow":"false","domstr":"' + domstr + '","value":"' + sleetindex+ '"}';
					__globalobj__._send(datastring);
					$(this).dialog("close");
			

			}
		},
		close: function () {
			$("#set_value").removeClass("ui-state-error-custom");
		}
	});
	//$("#set_value").val($(this).val().replace(/[^\d.]/g, ''));
	//updateTips($.i18n.prop('message_devnewval'));
	$("#dialog-comboxset").dialog("open");
});

$(".tbl_hfcunit").dblclick(function () {
	if (sessionStorage.authlevel == 3) {
		return;
	}
	var domstr = $(this).data("paramname");
	var rownum = $(this).data("rownum");
	$("#dialog-form").dialog({
		autoOpen: false,
		height: 240,
		width: 300,
		modal: true,
		buttons: {
			Ok: function () {
				if (checkInt($("#set_value").val())) {
					var node = __globalobj__._realDevice.getFirstChild();
					var datastring = '{"cmd":"hfcvalueset","target":"setVars","ip":"' + __globalobj__._realDevice.key + '","rowNum":"' + rownum
						 + '","isRow":"true","domstr":"' + domstr + '","value":"' + $("#set_value").val() + '"}';
					__globalobj__._send(datastring);
					$(this).dialog("close");
				} else {
					$("#set_value").addClass("ui-state-error-custom")
				}

			}
		},
		close: function () {
			$("#set_value").removeClass("ui-state-error-custom");
		}
	});
	$("#set_value").val($(this)[0].textContent.replace(/[^\d.]/g, ''));
	// updateTips($.i18n.prop('message_devnewval'));
	var comvaluess = $(this).data("comvalues");
	updateTips(comvaluess);
	$("#dialog-form").dialog("open");
});

$(".tbl_hfcunitcombox").dblclick(function () {
	if (sessionStorage.authlevel == 3) {
		return;
	}
	var domstr = $(this).data("paramname");
	var rownum = $(this).data("rownum");
	var comvaluess = $(this).data("comvalues");
	//alert(comvaluess);
	var mArray = comvaluess.split(",");
	$('#comvaluelist').empty();
	for (var i = 0; i < mArray.length; i++) {
		$('#comvaluelist').append($("<option value='" + mArray[i] + "'>" + mArray[i] + "</option>"));
	}
	$('#comvaluelist').val($(this)[0].textContent);

	$("#dialog-comboxset").dialog({
		autoOpen: false,
		height: 240,
		width: 300,
		modal: true,
		buttons: {
			Ok: function () {
				var node = __globalobj__._realDevice.getFirstChild();
				var sleetindex = $("#comvaluelist").get(0).selectedIndex + 1;
				//  	alert(sleetindex);
				var datastring = '{"cmd":"hfcvalueset","target":"setVars","ip":"' + __globalobj__._realDevice.key + '","rowNum":"' + rownum
					 + '","isRow":"true","domstr":"' + domstr + '","value":"' + sleetindex + '"}';
				__globalobj__._send(datastring);
				$(this).dialog("close");

			}
		},
		close: function () {
			$("#set_value").removeClass("ui-state-error-custom");
		}
	});
	$("#set_value").val($(this)[0].textContent.replace(/[^\d.]/g, ''));
	updateTips($.i18n.prop('message_devnewval'));
	$("#dialog-comboxset").dialog("open");
});

$(".trapaddr").dblclick(function () {
	if (sessionStorage.authlevel == 3) {
		return;
	}
	var domstr = $(this).data("paramname");
	var rownum = $(this).data("rownum");
	$("#dialog-form").dialog({
		autoOpen: false,
		height: 240,
		width: 300,
		modal: true,
		buttons: {
			Ok: function () {
				if (ipvalidate($("#set_value").val())) {
					var node = __globalobj__._realDevice.getFirstChild();
					var datastring = '{"cmd":"hfcvalueset","target":"setTrapHost","ip":"' + __globalobj__._realDevice.key + '","domstr":"' + domstr + '","isRow":"true","rowNum":"' + rownum + '","value":"' + $("#set_value").val() + '"}';
					__globalobj__._send(datastring);
					$(this).dialog("close");
				} else {
					$("#set_value").addClass("ui-state-error-custom")
				}
			}
		},
		close: function () {
			$("#set_value").removeClass("ui-state-error-custom");
		}
	});
	$("#set_value").val($(this)[0].textContent);
	updateTips($.i18n.prop('message_devnewtrap'));
	$("#dialog-form").dialog("open");
});

$(".isalarmThreshold").dblclick(function () {
	var node = __globalobj__._realDevice.getFirstChild();
	var domstr = $(this)[0].id;
	var datastring = '{"cmd":"hfcvalueset","target":"getalarmThreshold","ip":"' + __globalobj__._realDevice.key + '","domstr":"' + domstr
		 + '","isRow":"false","rcommunity":"' + __globalobj__._realDevice.data.rcommunity + '","devtype":"' + __globalobj__._realDevice.getLastChild().key + '"}';
	__globalobj__._send(datastring);
});

$(".tbl_isalarmThreshold").dblclick(function () {
	var node = __globalobj__._realDevice.getFirstChild();
	var domstr = $(this).data("paramname");
	var rownum = $(this).data("rownum");
	var datastring = '{"cmd":"hfcvalueset","target":"getalarmThreshold","ip":"' + __globalobj__._realDevice.key + '","domstr":"' + domstr + '","rowNum":"' + rownum
		 + '","isRow":"true","rcommunity":"' + __globalobj__._realDevice.data.rcommunity + '","devtype":"' + __globalobj__._realDevice.getLastChild().key + '"}';
	__globalobj__._send(datastring);
});

$('.device-close').click(function () {
	var node = __globalobj__._realDevice.getFirstChild();
	var datastring = '{"cmd":"deviceclose","ip":"' + __globalobj__._realDevice.key + '"}';
	__globalobj__._send(datastring);
	$('.candile').empty();
});


    function trapaddrEvent() {
		

	if (sessionStorage.authlevel == 3) {
		return;
	}
	var domstr = $(this).data("paramname");
	var rownum = $(this).data("rownum");
	$("#dialog-form").dialog({
		autoOpen: false,
		height: 240,
		width: 300,
		modal: true,
		buttons: {
			Ok: function () {
				if (ipvalidate($("#set_value").val())) {
					var node = __globalobj__._realDevice.getFirstChild();
					var datastring = '{"cmd":"hfcvalueset","target":"setTrapHost","ip":"' + __globalobj__._realDevice.key + '","domstr":"' + domstr + '","isRow":"true","rowNum":"' + rownum + '","value":"' + $("#set_value").val() + '"}';
					__globalobj__._send(datastring);
					$(this).dialog("close");
				} else {
					$("#set_value").addClass("ui-state-error-custom")
				}
			}
		},
		close: function () {
			$("#set_value").removeClass("ui-state-error-custom");
		}
	});
	$("#set_value").val($(this)[0].textContent);
	updateTips($.i18n.prop('message_devnewtrap'));
	$("#dialog-form").dialog("open");
		
	}
	
	
	function commonViewLoad() {		
		
		$('#i18n-devip')[0].textContent = $.i18n.prop('message_devip');
		$('#i18n-sysdesc')[0].textContent = $.i18n.prop('message_devsysdesc');
		$('#i18n-contact')[0].textContent = $.i18n.prop('message_devcontact');
		$('#i18n-uptime')[0].textContent = $.i18n.prop('message_devuptime');
	
	}
	function trapViewload() {		
		
		   $('#i18n-traptbl')[0].textContent = $.i18n.prop('message_devtraptbl');		
		$('#i18n-trapindex')[0].textContent = $.i18n.prop('message_devtrapindex');
		$('#i18n-trapip')[0].textContent = $.i18n.prop('message_devtrapip');
		
		
				$('#i18n-publictemper')[0].textContent = $.i18n.prop('message_devtemper');
		$('#i18n-publicID')[0].textContent = $.i18n.prop('message_devlogicid');		
		$('#i18n-publictype')[0].textContent = $.i18n.prop('message_devtype');		
		$('#i18n-publicsn')[0].textContent = $.i18n.prop('message_devsn');	
				$('#i18n-publictype')[0].textContent = $.i18n.prop('message_devtype');	
		
	
	}
	
		
	


