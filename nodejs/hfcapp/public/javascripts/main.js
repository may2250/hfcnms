(function($) {
	var webSocket;
	var regtree;
	var tbl_devalarm;
	var tbl_devalarm_old;
	var tbl_optlog;
	var tbl_loglists = null;
	var tbl_alarmlists = null;
	var lazyLoadData = null;
	var encstrencstr;
	var isEN=true;//网管中英文版本选择
	$(function() {
		encstr = sessionStorage.userName+'/'+ sessionStorage.passWord;
		if(localStorage.void == undefined){
			localStorage.void = 'on';
		}
		loadProperties();
		initWebSocket(encstr);	   	
    //	addtablecontextmenu();
    	window.__globalobj__ = {
			
    		    _webSocket:webSocket,
    		    _realDevice:undefined,
    		    _send:function(datastring) {  
    		    	if (webSocket.readyState !== 1) {
    		            setTimeout(function() {
    		            	webSocket.send(datastring);
    		            }, 250);
    		        } else {
    		        	webSocket.send(datastring);
    		        };
    		    	
    		    },
    		    _initWebSocket:function(str){
    				var hostip = window.location.hostname;
    		        if (window.WebSocket) {
    		        	webSocket = new WebSocket('ws://' + hostip + ':8080/hfcnms/websocketservice/'+str);
    		        	webSocket.onmessage = function(event) {
    		        		onMessage(event);
    		            };
    		            webSocket.onopen = function(event) {
    		            	onOpen(event);
    		            };
    		            webSocket.onclose = function(event) {
    		                webSocket.close();
    		            };
    		            webSocket.onerror = function(event) {
    		            	onError(event);
    		            };
    		        }else{
    		            alert('This browser does not supports WebSocket');
    		        }
    		    }
    		};
    	
    	tbl_devalarm = $('#tbl_devalarm').DataTable({
    		scrollY:        130,
    		scrollX: 		true,
    		scrollCollapse: true,
    		order: 			[[ 0, "desc" ]],
            paging:         false,
            info:     		false,
            searching: 		false,
            /*columns: [
                      { title: "ID" },
                      { title: $.i18n.prop('message_tbllevel') },
                      { title: $.i18n.prop('message_tblip') },
                      { title: $.i18n.prop('message_tblpath') },
                      { title: $.i18n.prop('message_tbloptype') },
                      { title: $.i18n.prop('message_tblparam') },
                      { title: $.i18n.prop('message_tblparamv') },
                      { title: $.i18n.prop('message_tbloptime') },
                      { title: $.i18n.prop('message_tblconfirmation') },
                      { title: $.i18n.prop('message_tblconfirmtime') }
                  ],
            drawCallback: function() {
        	    $.contextMenu({
        	      selector: '#tbl_devalarm tbody tr td',
        	      callback: function(key, options) {
        	        var addr = options.$trigger[0].parentElement.cells[2].textContent;
        	        
        	      },
        	      items: {
        	        "edit": {
        	          name: "处理",
        	          icon: "edit"
        	        }
        	      }
        	    });
        	  },*/
        	  "createdRow": function ( row, data, index ) {
        		  $(row).attr('id', data[0]);
        		  if ( data[1] == "重要告警" || data[1] == "Secondary alarm")  {
                	  $('td', row).eq(0).prepend('<img src="images/Warning.png" class="alarm_ico" />  ');
                	  $('td', row).parent().addClass('alarm-warning');
                  }else if(data[1] == "紧急告警" || data[1] == "Urgent alarm"){
                	  $('td', row).eq(0).prepend('<img src="images/alert.png" class="alarm_ico" />  ');
                	  $('td', row).parent().addClass('alarm-danger');                  
                  }
              }
        } );
    	
    	tbl_devalarm_old = $('#tbl_devalarm_old').DataTable({
    		scrollY:        130,
    		scrollX: 		true,
    		scrollCollapse: true,
    		order: 			[[ 0, "desc" ]],
            paging:         false,
            info:     		false,
            searching: 		false,
            /*columns: [
                      { title: "ID" },
                      { title: $.i18n.prop('message_tbllevel') },
                      { title: $.i18n.prop('message_tblip') },
                      { title: $.i18n.prop('message_tblpath') },
                      { title: $.i18n.prop('message_tbloptype') },
                      { title: $.i18n.prop('message_tblparam') },
                      { title: $.i18n.prop('message_tblparamv') },
                      { title: $.i18n.prop('message_tbloptime') },
                      { title: $.i18n.prop('message_tblconfirmation') },
                      { title: $.i18n.prop('message_tblconfirmtime') }
                  ],*/
        	  "createdRow": function ( row, data, index ) {
        		  $(row).attr('id', data[0]);
                  if ( data[1] == "重要告警" || data[1] == "Secondary alarm")  {
                	  $('td', row).eq(0).prepend('<img src="images/Warning.png" class="alarm_ico" />  ');
                	  //$('td', row).parent().addClass('alarm-warning');
                  }else if(data[1] == "紧急告警" || data[1] == "Urgent alarm"){
                	  $('td', row).eq(0).prepend('<img src="images/alert.png" class="alarm_ico" />  ');
                	//  $('td', row).parent().addClass('alarm-danger');                  
                  }
              }
        } );
    	
    	tbl_optlog = $('#tbl_optlog').DataTable({
    		scrollY:        130,
    		scrollX: 		true,
    		scrollCollapse: true,
    		order: 			[[ 0, "desc" ]],
            paging:         false,
            info:     		false,
            searching: 		false
        } );
    	
    	$('a[data-toggle="tab"]').on( 'shown.bs.tab', function (e) {
            $.fn.dataTable.tables( {visible: true, api: true} ).columns.adjust();
        } );
    	
    	$('.tbl_authman tbody').on( 'click', 'tr', function () {
    		var datastring = '{"cmd":"handleuser","target":"getuserinfo","username":"' + $(this)[0].cells[1].textContent  + '"}';
	    	send(datastring);            
        	$('.tbl_authman tr.selected').removeClass('selected');
            $(this).addClass('selected');
            if($(this)[0].cells[1].textContent == 'admin'){
            	$('#btn-authdel').attr("disabled", true);
            }else{
            	$('#btn-authdel').attr("disabled", false);
            }
            
        } ); 
    	
    	$('.nav_search').click(function(){
    		if(!$('.nav_search').hasClass("nav_disabled")){
    			$( "#dialog-devsearch" ).dialog({
	        	      autoOpen: false,
	        	      height: 300,
	        	      width: 890,
	        	      modal: true,
	        	      buttons: {
	        	    	  "Start": function() {	    
	        	    		  	  if($(".progress-bar").width() == 0 || $(".progress-bar").width() == 100){
	        	    		  		if(!ipvalidate($("#search-sip").val())){
			        	    			  $("#search-sip").addClass( "ui-state-error-custom" );
			        	    			  return;
			        	    		  }else{
			        	    			  $("#search-sip").removeClass("ui-state-error-custom");
			        	    		  };
			        	    		  if(!ipvalidate($("#search-eip").val())){
			        	    			  $("#search-eip").addClass( "ui-state-error-custom" );
			        	    			  return;
			        	    		  }else{
			        	    			  $("#search-eip").removeClass("ui-state-error-custom");
			        	    		  };
			        	              if(compareIP($("#search-sip").val(), $("#search-eip").val()) == 1){
			        	            	  $("#search-eip").addClass( "ui-state-error-custom" );
			        	    			  return;
			        	              };  
			        	              
			        	              var datastring = '{"cmd":"devsearch","community":"'+$("#search-community").val() +'","devtype":"'+ $("#search-stype").prop('selectedIndex') +'","startip":"'+ $("#search-sip").val()+'","endip":"'+ $("#search-eip").val() +'","target":"start"}';
		      	        	    	  send(datastring);
	        	    		  	  };		        	    		  
	        	            }
	        	      },
	        	      close: function() {
	        	    	  $("#search-sip").removeClass("ui-state-error-custom");
	        	    	  $("#search-eip").removeClass("ui-state-error-custom");
	        	      }
    			});
    			$("#dialog-devsearch").dialog("open");
    		}
    		
    	});
    	
    	$('.nav_displaylog').click(function(){
    		if($('.nav_displaylog p')[0].textContent == "隐藏日志栏"){
    			$('.nav_displaylog i').addClass("icon-eye-close");
    			$('.nav_displaylog i').removeClass(" icon-eye-open"); 
    			$('.nav_displaylog p')[0].textContent = "显示日志栏";
    			$("footer").css('display','none');
    			$(".devdetail-content").css('height',$(window).height() - 200); 
    			$("#tab-dev1").removeClass("tab-devpane");
    			$("#tab-dev1").addClass('tab-devpane-h');
    		}else{
    			$('.nav_displaylog i').addClass("icon-eye-open");
    			$('.nav_displaylog i').removeClass(" icon-eye-close"); 
    			$('.nav_displaylog p')[0].textContent = "隐藏日志栏";
    			$("footer").css('display','block');
    			$(".devdetail-content").css('height','295px'); 
    			$("#tab-dev1").removeClass("tab-devpane-h");
    			$("#tab-dev1").addClass('tab-devpane');
    		};    		
    	});
    	
    	$('.nav_server').click(function(){    		
    		var datastring = '{"cmd":"severstatus"}';
	    	send(datastring);
    		
    	});
    	
    	$('.nav_managerauth').click(function(){
    		var datastring = '';
    		if(sessionStorage.userName == 'admin'){
    			datastring = '{"cmd":"getuserlist"}';
    			send(datastring);
    		}else{
    			$('#auth-oripassword').val("");
    			$('#auth-password').val("");
    			$('#auth-rpassword').val("");	
    			$("#modal_authmanbase").modal();
    		}        	
    		
    	});
    	
    	$('.nav_manageralarm').click(function(){
    		$("#modal_alarm").modal();
    	});
    	
    	$('.nav_managerlog').click(function(){
    		$("#modal_optlog").modal();
    	});
    	
    	$('.nav_sound').click(function(){
    		if(localStorage.void == "on"){
    			$('.nav_sound i').addClass("icon-volume-off");
    			$('.nav_sound i').removeClass("icon-volume-up"); 
    			$('.nav_sound p')[0].textContent = $.i18n.prop('message_navvoidoff');
    			playVideo("/alarmwavs/alarm offline.wav");
    			localStorage.void = "off";
    		}else{
    			$('.nav_sound i').addClass("icon-volume-up");
    			$('.nav_sound i').removeClass("icon-volume-off"); 
    			$('.nav_sound p')[0].textContent = $.i18n.prop('message_navvoidon');
    			playVideo("/alarmwavs/alarm online.wav");
    			localStorage.void = "on";
    		};    		
    	});
    	
    	$('.nav_about').click(function(){
    		$("#modal_about").modal();
    	});
    	
    	$('#needtype').change(function(){ 
    		$('#salutation').attr("disabled", !$(this).is(':checked'));
    		if(!$(this).is(':checked')){
    			//$("#salutation").find("option[text='其它设备']").attr("selected",true);
    			$("#salutation").val("other");
    		}
    	});
    	
    	$('#user-logout').click(function(){
    		sessionStorage.passWord = undefined;
    		window.location.href="/login";
    	});
    	
    	$('#btn-authdel').click(function(){
    		if((confirm( $.i18n.prop('message_sure'))==true))
        	{
    			var datastring = '{"cmd":"handleuser","target":"deluser","username":"'+ $('#auth-usernamev').val() + '"}';
       		 	send(datastring);
        	}
    		 
    	});
    	
    	$('#md-password').click(function(){
    		$('#auth-oripassword1').val("");
			$('#auth-password1').val("");
			$('#auth-rpassword1').val("");	
			$("#modal_authmanbase").modal();
	   	});
    	
    	
    	$('#btn-authsub').click(function(){
    		if($('#auth-password').val() == ""){
	   			 $('#auth-password').addClass("ui-state-error-custom");
	   			 return;
	   		 }
    		if($('#auth-password').val().length > 12){
	   			 $('#auth-password').addClass("ui-state-error-custom");
	   			 alert($.i18n.prop('message_passstrerror'));
	   			 return;
	   		 }
	   		 if(!CheckStr($('#auth-password').val())){
	   			 $('#auth-password').addClass("ui-state-error-custom");
	   			 alert($.i18n.prop('message_passstrerror'));
	   			 return;
	   		 }	   		 
	   		 if($('#auth-password').val() != $('#auth-rpassword').val()){
	   			 alert($.i18n.prop('message_passworderr'));
	   			 return;
	   		 }
	   		 var datastring = '{"cmd":"handleuser","target":"modifypassword_admin","username":"'+ $('#auth-usernamev').val() + '","password":"'+ $('#auth-password').val() + '","AuthTotal":'+ $('#userauth-level').val() + '}';
	   		 send(datastring);
	   	});
    	
    	$('#btn-authsub1').click(function(){
    		if($('#auth-password1').val() == ""){
   			 	$('#auth-password1').addClass("ui-state-error-custom");
   			 	return;
	   		 }
	   		 if($('#auth-password1').val().length > 12){
	   			 $('#auth-password1').addClass("ui-state-error-custom");
	   			 alert($.i18n.prop('message_passstrerror'));
	   			 return;
	   		 }
	   		 if(!CheckStr($('#auth-password1').val())){
	   			 $('#auth-password1').addClass("ui-state-error-custom");
	   			 alert($.i18n.prop('message_passstrerror'));
	   			 return;
	   		 }
	   		 if($('#auth-password1').val() != $('#auth-rpassword1').val()){
	   			 alert($.i18n.prop('message_passworderr'));
	   			 return;
	   		 }
	   		 var datastring = '{"cmd":"handleuser","target":"modifypassword","username":"'+ sessionStorage.userName + '","oldpassword":"'+ $('#auth-oripassword1').val() + '","password":"'+ $('#auth-password1').val() + '","AuthTotal":0}';
	   		 send(datastring);
	   	});
    	
    	$('#btn-authadd').click(function(){
    		$('#auth-username2').val('');
    		$('#auth-password2').val('');
    		$('#auth-rpassword2').val('');
    		$('#auth-username2').removeClass("ui-state-error-custom");
    		$('#auth-password2').removeClass("ui-state-error-custom");
    		$('#auth-rpassword2').removeClass("ui-state-error-custom");
    		$("#modal_adduser").modal();
	   	});
    	
    	
    	$('#btn-adduser').click(function(){
    		 if($('#auth-username2').val() == ""){
    			 $('#auth-username2').addClass("ui-state-error-custom");
    			 return;
    		 }
    		 if($('#auth-password2').val() == ""){
    			 $('#auth-password2').addClass("ui-state-error-custom");
    			 return;
    		 }
    		 if($('#auth-password2').val().length > 12){
    			 $('#auth-password2').addClass("ui-state-error-custom");
    			 alert($.i18n.prop('message_passstrerror'));
    			 return;
    		 }
    		 if(!CheckStr($('#auth-password2').val())){
    			 $('#auth-password2').addClass("ui-state-error-custom");
    			 alert($.i18n.prop('message_passstrerror'));
    			 return;
    		 }
    		 if(!CheckStr($('#auth-rpassword2').val())){
    			 $('#auth-rpassword2').addClass("ui-state-error-custom");
    			 alert($.i18n.prop('message_passstrerror'));
    			 return;
    		 }
    		 if($('#auth-rpassword2').val().length > 12){
    			 $('#auth-rpassword2').addClass("ui-state-error-custom");
    			 alert($.i18n.prop('message_passstrerror'));
    			 return;
    		 }
	   		 if($('#auth-password2').val() != $('#auth-rpassword2').val()){
	   			 alert($.i18n.prop('message_passworderr'));
	   			 return;
	   		 }
	   		 var datastring = '{"cmd":"handleuser","target":"adduser","username":"'+ $('#auth-username2').val() + '","password":"'+ $('#auth-password2').val() + '","AuthTotal":"'+ $('#adduser-level').val() + '"}';
	   		 send(datastring);
	   	});
    	
    	$('#modal_searchresult').on('hidden.bs.modal', function (e) {
    		$('#list-newdevs').empty();
    		$('#list-newdevs').append('<a class="list-group-item active"><h4 class="list-group-item-heading">Device List</h4></a>');
    	})
    	
    	$('#btn-regdev').click(function(){
    		var node = $("#reg-grouptree").fancytree("getActiveNode");
    		if(node == null || node == undefined){
    			alert($.i18n.prop('message_sgroup'));
    			return;
    		}
    		if($('input:checkbox[name=dev]:checked').length < 1){
    			alert($.i18n.prop('message_sdevice'));
    			return;
    		}
    		$('input:checkbox[name=dev]:checked').each(function(i){
    			 var strs = new Array(); //定义一数组 
    			 strs = $(this).val().split("/"); //字符分割 
    			 //发送到服务端注册设备
    			 var datastring = '{"cmd":"deviceadd","key":"'+ node.key + '","devtype":"'+ strs[1] + '","rcommunity":"public","wcommunity":"public","devname":"'+ strs[0] + '","netip":"'+ strs[0] +'"}';
 	    		 send(datastring);
    			 
    		     //删除该行
    			 $(this).parent().parent().remove();
    		});
    	});
    	
    	$("select#alarmfilter-date").change(function(){
    		if($(this).val() == $.i18n.prop('message_optionaldate')){
    			$("#datepicker_start").datepicker("enable").removeAttr("readonly");
    			$("#datepicker_end").datepicker("enable").removeAttr("readonly");
    		}else{
    			$("#datepicker_start").datepicker("disable").attr("readonly","readonly");
    			$("#datepicker_end").datepicker("disable").attr("readonly","readonly");
    		}
    	});
    	
    	$( "#datepicker_start" ).datepicker({
  	      changeMonth: true,
  	      changeYear: true,
  	      dateFormat: 'yy-mm-dd'
  	    });
    	$( "#datepicker_start" ).datepicker( 'setDate' , new Date());
    	$( "#datepicker_end" ).datepicker({
  	      changeMonth: true,
  	      changeYear: true,
  	      dateFormat: 'yy-mm-dd'
  	    });    	
    	$( "#datepicker_end" ).datepicker( 'setDate' , new Date());
    	$( "#datepicker_optstart" ).datepicker({
    	      changeMonth: true,
    	      changeYear: true,
    	      dateFormat: 'yy-mm-dd'
    	    });
    	$( "#datepicker_optstart" ).datepicker( 'setDate' , new Date());
      	$( "#datepicker_optend" ).datepicker({
    	      changeMonth: true,
    	      changeYear: true,
    	      dateFormat: 'yy-mm-dd'
    	    });    	
      	$( "#datepicker_optend" ).datepicker( 'setDate' , new Date());
      	
    	$('#btn-alarmok').click(function(){
    		if($("#alarmfilter-date").prop('selectedIndex') == 0){
    			if(getDays($('#datepicker_start').val(), $('#datepicker_end').val()) > 92){
        			alert($.i18n.prop('message_searchdate_error'));
        			return;
        		}
    		}    		    		
    		if($('#alarmfilter-source').val() != "" && !ipvalidate($('#alarmfilter-source').val())){
    			$('#alarmfilter-source').addClass("ui-state-error-custom"); 
    			return;
    		}
    		$('#alarmfilter-source').removeClass("ui-state-error-custom"); 
    		 var datastring = '{"cmd":"alarmsearch","ispage":"0","start":"'+ $('#datepicker_start').val() + '","end":"'+ $('#datepicker_end').val() 
    		 + '","customdate":"'+ $("#alarmfilter-date").prop('selectedIndex') + '","source":"'+ $('#alarmfilter-source').val() 
    		 + '","level":"'+ $("#alarmfilter-level").val() + '","type":"'+ $('#alarmfilter-type').val()
    		 + '","treatment":"'+ $("#alarmfilter-istreatment").val() + '","nename":"'+ $('#alarmfilter-nename').val() +'"}';
    		 send(datastring);
    		 $("#alarmlist-title")[0].textContent = $.i18n.prop('message_navhistoryalarm');
    		 $("#modal_alarm").modal('hide');
    		 $("#modal_alarmlists").modal();
    	});
    	
    	$('#modal_alarmlists').on('show.bs.modal', function () {
    		tbl_alarmlists = $('#tbl_alarmlists').DataTable({
        		scrollY:        430,
        		scrollX: 		true,
        		scrollCollapse: true,
        		order: 			[[ 0, "desc" ]],
                paging:         false,
                info:     		false,
                searching: 		false,
                bRetrieve: 		true,
                columns: [
                          { title: "ID" },
	                      { title: $.i18n.prop('message_tbllevel') },
	                      { title: $.i18n.prop('message_tblip') },
	                      { title: $.i18n.prop('message_tblpath') },
	                      { title: $.i18n.prop('message_tbloptype') },
	                      { title: $.i18n.prop('message_tblparam') },
	                      { title: $.i18n.prop('message_tblparamv') },
	                      { title: $.i18n.prop('message_tbloptime') },
	                      { title: $.i18n.prop('message_tblconfirmation') },
	                      { title: $.i18n.prop('message_tblconfirmtime') }
                      ],
              "createdRow": function ( row, data, index ) {
        		  $(row).attr('id', data[0]);
                  if ( data[1] == $.i18n.prop('message_secalarm')) {
                	  $('td', row).eq(0).prepend('<img src="images/Warning.png" class="alarm_ico" />  ');
  
					  	  if(data[8] == ""){
              	  $('td', row).parent().addClass('alarm-warning');
					  }	
                  }else if(data[1] == $.i18n.prop('message_urgentalarm')){
                	  $('td', row).eq(0).prepend('<img src="images/alert.png" class="alarm_ico" />  ');
					  if(data[8] == ""){
                	  $('td', row).parent().addClass('alarm-danger');   
					  }					  
                  }
              }
            } );
    			
    		
    		
    		setTimeout(function() {
				$.fn.dataTable.tables( {visible: true, api: true} ).columns.adjust();
			}, 400);
    	});
    	
    	$('#modal_optlists').on('show.bs.modal', function () {
			tbl_loglists = $('#tbl_loglists').DataTable({
        		scrollY:        430,
        		scrollX: 		true,
        		scrollCollapse: true,
        		order: 			[[ 0, "desc" ]],
                paging:         false,
                info:     		false,
                searching: 		false,
                bRetrieve: 		true,
                columns: [
                          { title: $.i18n.prop('message_tbloptid') },
                          { title: $.i18n.prop('message_tbloptuser') },
                          { title: $.i18n.prop('message_tbloptype') },
                          { title: $.i18n.prop('message_tbloptcontent') },
                          { title: $.i18n.prop('message_tbloptime') }
                      ]
            } );
    		
    		setTimeout(function() {
				$.fn.dataTable.tables( {visible: true, api: true} ).columns.adjust();
			}, 400);
    	});
    	
    	$('#modal_alarmlists').on('hide.bs.modal', function () {
    		tbl_alarmlists.clear().draw();
		});
    	
    	$('#modal_optlists').on('hide.bs.modal', function () {
    		tbl_loglists.clear().draw();
		});
		
		 	$('#btn-nextpage').click(function(){

			var vbum = $('#btn-nextpage').data("versionnum");
			var pagnum = $('#btn-nextpage').data("pagenum");
		     var datastring = '{"cmd":"alarmsearch","ispage":"1","isleft":"0","versionnum":"'+vbum+'","pagenum":"'+pagnum+'"}';
    		 send(datastring);
    	});
			 	$('#btn-ppage').click(function(){
	
			var vbum = $('#btn-nextpage').data("versionnum");
			var pagnum = $('#btn-nextpage').data("pagenum");;
		     var datastring = '{"cmd":"alarmsearch","ispage":"1","isleft":"1",,"versionnum":"'+vbum+'","pagenum":"'+pagnum+'"}';
    		 send(datastring);
    	});
    	
    	
    	$('#btn-alarmexports').click(function(){
    		tableToExcel('tbl_alarmlists');
    	});
    	
    	$('#btn-optexports').click(function(){
    		tableToExcel('tbl_loglists');
    	});
    	
    	$('#btn-optlogok').click(function(){
	   		 var datastring = '{"cmd":"optlogsearch","start":"'+ $('#datepicker_optstart').val() + '","end":"'+ $('#datepicker_optend').val() 
	   		 + '","optname":"'+ $('#optfilter-name').val() +'"}';
	   		 send(datastring);
	   		 $("#optlist-title")[0].textContent = $.i18n.prop('message_navoptlog');
	   		 $("#modal_optlog").modal('hide');
	   		 $("#modal_optlists").modal();
    	});
    	
    	$('#btn-tree-search').click(function(){
    		searchtreenode($("#searchbar-key").val());
    	});
    	
    	$('#searchbar-key').bind('keydown',function(event){
    	    if(event.keyCode == "13") {
    	    	searchtreenode($("#searchbar-key").val());
    	    }
    	});  
    	
		
		//双击告警条目定位到设备
    	$('#tbl_devalarm tbody').on('dblclick', 'tr', function () {
            var data = tbl_devalarm.row( this ).data();
            searchtreenode(data[2]);
        } );
	
	$('#tbl_devalarm_old tbody').on('dblclick', 'tr', function () {
            var data = tbl_devalarm_old.row( this ).data();
            searchtreenode(data[2]);
        } );
	

	
    	var tableToExcel = (function() {
            var uri = 'data:application/vnd.ms-excel;base64,',
            template = '<html xmlns:o="urn:schemas-microsoft-com:office:office" xmlns:x="urn:schemas-microsoft-com:office:excel" xmlns="http://www.w3.org/TR/REC-html40"><head><!--[if gte mso 9]><xml><x:ExcelWorkbook><x:ExcelWorksheets><x:ExcelWorksheet><x:Name>{worksheet}</x:Name><x:WorksheetOptions><x:DisplayGridlines/></x:WorksheetOptions></x:ExcelWorksheet></x:ExcelWorksheets></x:ExcelWorkbook></xml><![endif]--></head><body><table>{table}</table></body></html>',
              base64 = function(s) { return window.btoa(unescape(encodeURIComponent(s))) },
              format = function(s, c) {
                  return s.replace(/{(\w+)}/g,
                  function(m, p) { return c[p]; }) }
              return function(table, name) {
              if (!table.nodeType) table = document.getElementById(table)
              var ctx = {worksheet: name || 'Worksheet', table: table.innerHTML}
              window.location.href = uri + base64(format(template, ctx))
            }
          })();
	});
	
	
	function addtablecontextmenu(){
		
		 $('#logo').contextMenu('myMenu',
             {

          //菜单样式
          menuStyle: {
            border: '2px solid #000'
          },
          //菜单项样式
          itemStyle: {
            fontFamily : 'verdana',
            backgroundColor : 'green',
            color: 'white',
            border: 'none',
            padding: '1px'

          },
          //菜单项鼠标放在上面样式
          itemHoverStyle: {
            color: 'blue',
            backgroundColor: 'red',
            border: 'none'
          },
                  bindings:
                  {
                    'edit': function(t) {
                                          var url="xxx="+t.getAttribute("itemid");
                        showThickbox("编辑备注",encodeURI(url));
                    },
                    'look': function(t) {
                        var url = "yyy.action?type=look&";
                    url+="&itemid="+t.getAttribute("itemid");
                        showThickbox("查看备注",encodeURI(url));
                    },
                    'quit': function(t) {

                    }
                  }
            });
	
}
	
	
	function initWebSocket(encstr) {
		var hostip = window.location.hostname;
        if (window.WebSocket) {
        	webSocket = new WebSocket('ws://' + hostip + ':8080/hfcnms/websocketservice/' + encstr);
        	webSocket.onmessage = function(event) {
        		onMessage(event);
            };
            webSocket.onopen = function(event) {
            	onOpen(event);
            };
            webSocket.onclose = function(event) {
                webSocket.close();
            };
            webSocket.onerror = function(event) {
            	onError(event);
            };
        }else{
            alert('This browser does not supports WebSocket');
        }
    }
	
	function observerlimits(){
		$('.nav_search').addClass("nav_disabled");
	}
	
	function onMessage(event) {
    	var jsonobj =  eval('(' + event.data + ')');
        if(jsonobj.cmd == "getInitTree"){
        	$('#username')[0].textContent = sessionStorage.userName;
        	initTree(jsonobj.treenodes);    
    	    $("#sfversion")[0].textContent = jsonobj.sfversion;
			  $("#Supporteddevices")[0].textContent = jsonobj.Supporteddevices;			
        }else if(jsonobj.cmd == "loginAuth"){
        	sessionStorage.authlevel = jsonobj.level;
        	if(jsonobj.level == 3){
        		observerlimits();
        	}
        	if(!jsonobj.Authed){
        		window.location.href="/login";
        	}
        }else if(jsonobj.cmd == "getInitLog"){
        	//解析日志并显示
        	parseLogs(jsonobj);
        	
        }else if(jsonobj.cmd == "nodeadd"){
        	 var rootNode = $("#dev-fancytree").fancytree("getTree").getNodeByKey(jsonobj.pkey);
        	 if(rootNode != undefined){
        		 rootNode.addChildren({
                     title: jsonobj.title,
                     tooltip: "This folder and all child nodes were added programmatically.",
                     key: jsonobj.key,
                     pkey: jsonobj.pkey,
                     folder: true,
                     type : jsonobj.type,
                     icon: jsonobj.icon,
                     expand: jsonobj.expand               
                   });
                   rootNode.setExpanded(jsonobj.expand);
        	 }             
        }else if(jsonobj.cmd == "nodeedit"){
	       	 var node = $("#dev-fancytree").fancytree("getTree").getNodeByKey(jsonobj.key);
	       	 if(node != undefined){
	       		node.title = jsonobj.title;
		       	 if(node.data.type == "device"){
		       		node.data.rcommunity = jsonobj.rcommunity;
		       		node.data.wcommunity = jsonobj.wcommunity;
		       	 }
		         node.renderTitle();
	       	 }	       	 
        }else if(jsonobj.cmd == "nodedel"){
	       	 var node = $("#dev-fancytree").fancytree("getTree").getNodeByKey(jsonobj.key);	       	 
	       	 if(node != undefined){
	       		node.remove();
	       	 }
	       	 if(__globalobj__._realDevice.key == jsonobj.key){
	       		 //关闭正打开的设备信息界面
	       		$('.candile').empty();
	       		var datastring = '{"cmd":"deviceclose","ip":"' + node.key + '"}';
	       		send(datastring);
	       	 }
	         
        }else if(jsonobj.cmd == "deviceadd"){
	       	 var node = $("#dev-fancytree").fancytree("getTree").getNodeByKey(jsonobj.pkey);
	       	 if(node != undefined){
	       		node.addChildren(jsonobj.devnodes);
	       	 }	       	 
        }else if(jsonobj.cmd == "lazyLoad"){
	       	 var node = $("#dev-fancytree").fancytree("getTree").getNodeByKey(jsonobj.key);
	       	 lazyLoadData = jsonobj.lazyNodes;
        }else if(jsonobj.cmd == "getdevicedetail"){
        	var node = $("#dev-fancytree").fancytree("getTree").getNodeByKey(jsonobj.key);
        	showHfcDevice(jsonobj);
        	if(node != undefined){
        		if(jsonobj.isonline == true){
        			node.data.isonline = true;
        			node.icon = "../images/device.png";
        			if(__globalobj__._realDevice != undefined && __globalobj__._realDevice != null){
            			if(jsonobj.key == __globalobj__._realDevice.key){
            				$(".dev-status").css("color", "lightgreen");
            			}
            		}
        		}else{
        			node.data.isonline = false;
        			node.icon = "../images/devoff.png";
        			if(__globalobj__._realDevice != undefined && __globalobj__._realDevice != null){
            			if(jsonobj.key == __globalobj__._realDevice.key){
            				$(".dev-status").css("color", "red");
            			}
            		}
        		}
        		node.render(true,false);        		
        	}        	
        }else if(jsonobj.cmd == "hfcvalueset"){
        	parseHfcValueSet(jsonobj);        	   	 
        }else if(jsonobj.cmd == "devstatus"){
        	parseDevStatus(jsonobj);
        }else if(jsonobj.cmd == "realtime-device"){
        	showHfcDevice(jsonobj);
        }else if(jsonobj.cmd == "devsearchprocess"){
        	if(jsonobj.process == 100){        		
        		var datastring = '{"cmd":"getgrouptree"}';
    	    	send(datastring);    	    	
        	}else{
        		$(".progress-bar").width(jsonobj.process+ "%");
        	}
        	
        }else if(jsonobj.cmd == "getgrouptree"){
        	regtree = $("#reg-grouptree").fancytree({
                source: jsonobj.treenodes,
                clickFolderMode: 1,
                minExpandLevel: 2
              });
        }else if(jsonobj.cmd == "devsearch-res"){
        	$("#dialog-devsearch").dialog("close");
    		$(".progress-bar").width( "0%");
        	$("#modal_searchresult").modal();
        	$.each(jsonobj.rsts, function (n, value) {
        		var paramstr = value.ipaddr+ '/' + value.devtype +'/'+value.hfctype;
            	$('#list-newdevs').append('<li class="list-group-item"><label><input name="dev" type="checkbox" value="'+ paramstr + '" />'+value.ipaddr+ '/' + getNetTypeTostring(value.devtype)+'/'+value.hfctype+'</label></li>');
        	});        	
        }else if(jsonobj.cmd == "alarm_message"){
        	alarmSolve(jsonobj);
        }else if(jsonobj.cmd == "log_message"){
        	tbl_optlog.row.add( [
       		       	            jsonobj.id,
       		       	            jsonobj.user,
       		       	            jsonobj.type,
       		       	            jsonobj.content,
       		       	            jsonobj.time
       		       	        ] ).draw( false );     	
        }else if(jsonobj.cmd == "alarmsearch"){
			tbl_alarmlists.clear().draw();		
		     $('#btn-nextpage').data("versionnum", jsonobj.versionnum);  
			  $('#btn-nextpage').data("pagenum", jsonobj.pagenum);  
        	$.each(jsonobj.alarms, function (n, value) {
        		tbl_alarmlists.row.add( [
        		            value.id,
        		            value.level,
        		            value.addr,
        		            value.path,
        		            value.type,
        		            value.paramname,
        		            value.paramvalue,
        		            value.eventtime,
        		            value.solved,
        		            value.solvetime
        		        ] ).draw( false );
            });
        }else if(jsonobj.cmd == "optlogsearch"){
        	$.each(jsonobj.oplogs, function (n, value) {
        		tbl_loglists.row.add( [
        		            value.id,
        		            value.user,
        		            value.type,
        		            value.content,
        		            value.time
        		        ] ).draw( false );
            });
        }else if(jsonobj.cmd == "dbclosed"){
        	if(jsonobj.flag){
        		$('.dbstatus').removeClass("icon-ok-circle");
        		$('.dbstatus').addClass("icon-remove-circle");   
        		$('.dbstatus-lb').addClass("ui-state-error-custom");        		
        	}else{
        		$('.dbstatus').removeClass("icon-remove-circle");
        		$('.dbstatus').addClass("icon-ok-circle");
        		$('.dbstatus-lb').removeClass("ui-state-error-custom"); 
        	}
        }else if(jsonobj.cmd == "severstatus"){
        	parseServer(jsonobj);
        }else if(jsonobj.cmd == "getuserlist"){
        	parseUserlist(jsonobj);
        }else if(jsonobj.cmd == "handleuser"){
        	parseUserinfo(jsonobj);
        }else if(jsonobj.cmd == "devaddfalse"){
        	parseAddevfalse(jsonobj);
        }else{
        	document.getElementById('messages').innerHTML
            += '<br />' + event.data;
        }
    }
	
	function parseAddevfalse(jsonobj){
		alert(jsonobj.desc);
		searchtreenode(jsonobj.netip);
	}
	
	function parseUserinfo(jsonobj){		
		$("#userauth-level").empty();
		if(jsonobj.target == 'modifypassword' || jsonobj.target == 'modifypassword_admin'){
			if(jsonobj.AuthTotal != 0){
				//$('#auth-oripassword').val(jsonobj.password);
				if(sessionStorage.userName == 'admin'){
					$(".tbl_authman").find("tr").each(function(){
				       if($(this)[0].cells[1].textContent == jsonobj.username){
				    	   if(jsonobj.AuthTotal == 1){
				    		   $(this)[0].cells[2].textContent = $.i18n.prop('message_superadmin');
							}else if(jsonobj.AuthTotal == 2){
								$(this)[0].cells[2].textContent = $.i18n.prop('message_admin');
							}else{
								$(this)[0].cells[2].textContent = $.i18n.prop('message_observer');
							}	
				       }
				    });
					$('#auth-password').removeClass("ui-state-error-custom");
		    		$('#auth-rpassword').removeClass("ui-state-error-custom");
					alert('Success!');
				}
			}else{
				$("#modal_authmanbase").modal('hide');
			}			
		}else if(jsonobj.target == 'deluser'){
			$('.tbl_authman tr.selected').remove();
			$('#auth-usernamev').val("");
			$('#auth-oripassword').val("");
			$('#auth-password').val("");
			$('#auth-rpassword').val("");
			$('#btn-authdel').attr("disabled", true);		
			$('#btn-authsub').attr("disabled", true);
		}else if(jsonobj.target == 'adduser'){
			$("#modal_adduser").modal('hide');
			var levelstr = "";
			if(jsonobj.AuthTotal == 1){
				levelstr = $.i18n.prop('message_superadmin');
			}else if(jsonobj.AuthTotal == 2){
				levelstr = $.i18n.prop('message_admin');
			}else{
				levelstr = $.i18n.prop('message_observer');
			}	
			var tr = '<tr>'+
            '<td>'+
                jsonobj.key +                       
            '</td>'+
            '<td>'+
            	jsonobj.username + 
            '</td>'+
            '<td data-level=' + value.level + '>'+
            	levelstr +  
        	'</td>'+
        	'<td>'+
        		'No'+  
        	'</td>'+
        '</tr>';
		$('.tbl_authman tbody').append(tr);
		}else{
			$('#auth-usernamev').val(jsonobj.username);
			$('#auth-oripassword').val(jsonobj.PassWord1);
			$('#auth-password').val(jsonobj.PassWord1);
			$('#auth-rpassword').val(jsonobj.PassWord1);	
			$('#btn-authsub').attr("disabled", false);
		}	
		if(jsonobj.username == 'admin'){
			$("#userauth-level").append("<option value='1'>"+$.i18n.prop('message_superadmin')+"</option>");
			$("#userauth-level").get(0).selectedIndex = 0;
			
			$('#userauth-level').attr("disabled", true);
			
			
		}else{
			$("#userauth-level").append("<option value='2'>"+$.i18n.prop('message_admin')+"</option>");
			$("#userauth-level").append("<option value='3'>"+$.i18n.prop('message_observer')+"</option>");
			$("#userauth-level").val(jsonobj.AuthTotal);
		$('#userauth-level').attr("disabled", false);
		   
		}		
	}
	
	function parseUserlist(jsonobj){
		$('.tbl_authman tbody').empty();	
		$('#auth-usernamev').val("");
		$('#auth-oripassword').val("");
		$('#auth-password').val("");
		$('#auth-rpassword').val("");
		$('#btn-authdel').attr("disabled", true);		
		$('#btn-authsub').attr("disabled", true);
		$("#modal_authman").modal();
		var levelstr = "";
		$.each(jsonobj.userlist, function (n, value) {
			if(value.level == 1){
				levelstr = $.i18n.prop('message_superadmin');
			}else if(value.level == 2){
				levelstr = $.i18n.prop('message_admin');
			}else{
				levelstr = $.i18n.prop('message_observer');
			}			
			var tr = '<tr>'+
                '<td>'+
                    value.userid +                       
                '</td>'+
                '<td>'+
                	value.username + 
                '</td>'+
                '<td data-level=' + value.level +'>'+
                	levelstr +
            	'</td>'+
            	'<td>'+
	        		(value.istrap==true?"Yes":"No") + 
	        	'</td>'+
            '</tr>';
			$('.tbl_authman tbody').append(tr);
        });		
	}
	
	function parseServer(jsonobj){
		$("#dialog-server").modal();
		if(!jsonobj.CDatabaseEngineflag){
    		$('.dbstatus').removeClass("icon-ok-circle");
    		$('.dbstatus').addClass("icon-remove-circle");   
    		$('.dbstatus-lb').addClass("ui-state-error-custom");        		
    	}else{
    		$('.dbstatus').removeClass("icon-remove-circle");
    		$('.dbstatus').addClass("icon-ok-circle");
    		$('.dbstatus-lb').removeClass("ui-state-error-custom"); 
    	}
    	if(!jsonobj.TrapPduServerstatus){
    		$('.trapstatus').removeClass("icon-ok-circle");
    		$('.trapstatus').addClass("icon-remove-circle");   
    		$('.trapstatus-lb').addClass("ui-state-error-custom");        		
    	}else{
    		$('.trapstatus').removeClass("icon-remove-circle");
    		$('.trapstatus').addClass("icon-ok-circle");
    		$('.trapstatus-lb').removeClass("ui-state-error-custom"); 
    	}    
		
/* 	    if(jsonobj.redisStatus){  
	    	$('.redisstatus').removeClass("icon-remove-circle");
    		$('.redisstatus').addClass("icon-ok-circle");
    		$('.redisstatus-lb').removeClass("ui-state-error-custom");
        }   
        else{
        	$('.redisstatus').removeClass("icon-ok-circle");
    		$('.redisstatus').addClass("icon-remove-circle");   
    		$('.redisstatus-lb').addClass("ui-state-error-custom");         
        }          */  
	    $("#server-clients")[0].textContent = jsonobj.clientNum;

 
	}
	
	function alarmSolve(jsonobj){
		if(jsonobj.opt == false){    		
    		tbl_devalarm_old.row.add( [
       		       	            jsonobj.id,
       		       	            jsonobj.level,
       		       	            jsonobj.addr,
       		       	            jsonobj.path,
       		       	            jsonobj.type,
       		       	            jsonobj.paramname,
       		       	            jsonobj.paramvalue,
       		       	            jsonobj.eventtime,
       		       	            jsonobj.solved,
       		       	            jsonobj.solvetime
       		       	        ] ).draw( false );
    		tbl_devalarm.row("#" + jsonobj.id).remove().draw(false);        		
    	}else{
    		tbl_devalarm.row.add( [
    		       	            jsonobj.id,
    		       	            jsonobj.level,
    		       	            jsonobj.addr,
    		       	            jsonobj.path,
    		       	            jsonobj.type,
    		       	            jsonobj.paramname,
    		       	            jsonobj.paramvalue,
    		       	            jsonobj.eventtime,
    		       	            jsonobj.solved,
    		       	            jsonobj.solvetime
    		       	        ] ).draw( false );
							
							
							    if(localStorage.void == 'on'){
							
					if ( jsonobj.level == "重要告警" || jsonobj.level == "Secondary alarm")  {
         		playVideo("/alarmwavs/BEEP hi.WAV");

                  }else if(jsonobj.level == "紧急告警" || jsonobj.level== "Urgent alarm"){
                			playVideo("/alarmwavs/BEEP hihi.WAV");
             
                  }
								}
    	}        	
	}

 
    function onOpen(event) {

    }
 
    function onError(event) {
    	//document.getElementById('messages').innerHTML = event.data;
    }     
    
    function playVideo(src) {
        if (src == null || src == "") {
            src = "";
        }        
             
        var wavsound = document.getElementById("alarmwav");
        wavsound.src = src; 
        wavsound.play();
    }
    
    function parseDevStatus(jsonobj){
    	var node = $("#dev-fancytree").fancytree("getTree").getNodeByKey(jsonobj.ip);
    	if(node != undefined){
    		if(jsonobj.isonline == true){
    			node.data.isonline = true;
    			switch(jsonobj.hfctype){
    			case 1:
    				node.icon = "../images/treeEDFA.png";
    				break;
    			case 14:
    				node.icon = "../images/treeRece.png";
    				break;
    				
				default:
					node.icon = "../images/device.png";
					break;
    			}
    			/*if(node.getLastChild().key == "rece_workstation"){
    				node.icon = "../images/treeRece.png";
    			}else if(node.getLastChild().key == "EDFA"){
    				node.icon = "../images/treeEDFA.png";
    			}else if(node.getLastChild().key == "Trans"){
    				node.icon = "../images/treeTrans.png";
    			}else{
    				node.icon = "../images/device.png";
    			}  */      			
    			node.getLastChild().data.hfctype = jsonobj.hfctype;
    			/*if(__globalobj__._realDevice != undefined && __globalobj__._realDevice != null){
        			if(jsonobj.ip == __globalobj__._realDevice.key){
        				$(".dev-status").css("color", "lightgreen");
        				$("#dev-status-text")[0].textContent = $.i18n.prop('message_devconnected');
        			}
        		}*/
    			if($(".nav_sound i").hasClass("icon-volume-up")){
    				playVideo("/alarmwavs/alarm online.wav");
    			}
    		}else{
    			node.data.isonline = false;
    			node.icon = "../images/devoff.png";
    			if(__globalobj__._realDevice != undefined && __globalobj__._realDevice != null){
        			if(jsonobj.ip == __globalobj__._realDevice.key){
        				//设备下线，关闭详细信息界面
        				var datastring = '{"cmd":"deviceclose","ip":"' + jsonobj.ip + '"}';
        				send(datastring);
        	  		  	$('.candile').empty();
        	  		  	__globalobj__._realDevice = undefined;
        			}
        		}
    			if($(".nav_sound i").hasClass("icon-volume-up")){
    				playVideo("/alarmwavs/alarm offline.wav");
    			}
    		}
    		node.render(true,false);        		
    	}
    }
    
    function initTree(treedata) {
    	devtree = $("#dev-fancytree").fancytree({
    		extensions: ["persist"],
            source: treedata,
            clickFolderMode: 1,
            minExpandLevel: 2,
            click: function(event, data) {
            	
            },
            dblclick: function(event, data) {
            	if(data.node.data.type == "device" && data.node.data.isonline){            		
            		//show deivce detail            		
            		var preDevice = __globalobj__._realDevice;
            		__globalobj__._realDevice = data.node;
            		getDeviceDetail(data.node,preDevice);
            	}
            	
            },
            persist: {
                expandLazy: true,
                // fireActivate: false,    // false: suppress `activate` event after active node was restored
                // overrideSource: false,  // true: cookie takes precedence over `source` data attributes.
                store: "auto" // 'cookie', 'local': use localStore, 'session': sessionStore
              },
            /*dnd: {
                autoExpandMS: 400,
                focusOnClick: true,
                preventVoidMoves: true, // Prevent dropping nodes 'before self', etc.
                preventRecursiveMoves: true, // Prevent dropping nodes on own descendants
                dragStart: function(node, data) {

                  return true;
                },
                dragEnter: function(node, data) {

                   return true;
                },
                dragDrop: function(node, data) {                  
                	if(node.data.type == "group"){
                		data.otherNode.moveTo(node, data.hitMode);
                    	node.setExpanded();
                    	var datastring = '{"cmd":"nodemove","key":"'+data.otherNode.key +'","pkey":"'+ data.otherNode.data.pkey +'","moveto":"'+ node.key +'"}';
        	    		webSocket.send(datastring);
                	}else{
                		alert("移动到错误的节点!");
                	}                	
                }
              },*/
              lazyLoad: lazyLoad
          });    	
    	
    	$.contextMenu({
    	      selector: "#dev-fancytree span.fancytree-title",
    	      items: {    	    	  
    	        "add": {name: "添加组", icon: "add",
    	        	disabled: function(key, opt){
    	        		  var node = $.ui.fancytree.getNode(opt.$trigger);
    	        		  if(sessionStorage.authlevel == 3){
	  	        			return true;
	  	        		  }
        	              if(node.data.type == "group"){
        	            	  return false;
        	              }else{
        	            	  return true;
        	              }
    	        	  },
    	        	callback: function(key, opt){
      	              var node = $.ui.fancytree.getNode(opt.$trigger);
      	              if(node.data.type == "group"){
      	            	//添加节点
	      	            $( "#dialog-form" ).dialog({
	      	        	      autoOpen: false,
	      	        	      height: 240,
	      	        	      width: 300,
	      	        	      modal: true,
	      	        	      buttons: {
	      	        	    	  Ok: function() {	    
	      	        	    		  if($("#set_value").val() != ""){
	      	        	    			  var datastring = '{"cmd":"nodeadd","key":"'+node.key +'","type":"'+ node.data.type +'","value":"'+ $("#set_value").val()+'"}';
		      	        	    		  send(datastring);
		      	        	    		  $( this ).dialog( "close" );
	      	        	    		  }else{
	      	        	    			  $("#set_value").addClass( "ui-state-error-custom" )
	      	        	    		  }   	        	    		  
	      	        	              	      	        	              
	      	        	            }
	      	        	      },
	      	        	      close: function() {
	      	        	    	$("#set_value").removeClass("ui-state-error-custom");
	      	        	      }
      	        	    });
	      	            $("#set_value").val("");
	      	            updateTips($.i18n.prop('message_newgroupname'));
	      	            $("#dialog-form").dialog("open");
      	              }
      	            }
    	          }, 
    	          "adddevice": {name: "添加设备", icon: "add",
    	        	  disabled: function(key, opt){
    	        		  var node = $.ui.fancytree.getNode(opt.$trigger);
    	        		  if(sessionStorage.authlevel == 3){
	  	        			return true;
	  	        		  }
        	              if(node.data.type == "group"){
        	            	  return false;
        	              }else{
        	            	  return true;
        	              }
    	        	  },
      	        	  callback: function(key, opt){
        	              var node = $.ui.fancytree.getNode(opt.$trigger);
        	              if(node.data.type == "group"){
        	            	//添加设备
        	            	  $( "#dialog-newdev" ).dialog({
    	      	        	      autoOpen: false,
    	      	        	      height: 400,
    	      	        	      width: 730,
    	      	        	      modal: true,
    	      	        	      buttons: {
    	      	        	    	  Ok: function() {	   
	    	      	        	    		if($("#newdev_ip").val() != "" && ipvalidate($("#newdev_ip").val())){
	    	      	        	    			var datastring = '{"cmd":"deviceadd","key":"'+ node.key + '","devtype":"'+ $("#salutation").children('option:selected').val()+ '","rcommunity":"'+ $("#newdev_rcommunity").val()+ '","wcommunity":"'+ $("#newdev_wcommunity").val() + '","devname":"'+ $("#newdev_devname").val()+ '","netip":"'+ $("#newdev_ip").val()+'"}';
	      	      	        	    		  	send(datastring);
	      	      	        	    		  	$( this ).dialog( "close" );
	    	      	        	    		}else{
	    	      	        	    			$("#newdev_ip").addClass( "ui-state-error-custom" );
	    	      	        	    		}
    	      	        	    	  }
    	      	        	      },
    	      	        	      close: function() {
    		      	        	    	$("#newdev_ip").removeClass("ui-state-error-custom");
    		      	        	  }
	          	        	    });
	    	      	            $("#newdev_ip").val("");
	        	            	$("#dialog-newdev").dialog("open");
	        	          }
	        	     }
      	          }, 
    	        "edit": {name: "编辑", icon: "edit",
    	        	disabled: function(key, opt){
  	        		  var node = $.ui.fancytree.getNode(opt.$trigger);
  	        		  if(sessionStorage.authlevel == 3){
  	        			return true;
  	        		  }
      	              if(node.data.type == "group" || node.data.type == "device"){
      	            	  return false;
      	              }else{
      	            	  return true;
      	              }      	              
  	        	  	},
    	        	callback: function(key, opt){
      	              	var node = $.ui.fancytree.getNode(opt.$trigger);
	      	            if(node.data.type == "group" || node.data.type == "device"){
	      	            	//编辑节点
		      	            $( "#dialog-form" ).dialog({
		      	        	      autoOpen: false,
		      	        	      height: 240,
		      	        	      width: 300,
		      	        	      modal: true,
		      	        	      buttons: {
		      	        	    	  Ok: function() {	 
		      	        	    		if($("#set_value").val() != ""){
		      	        	    			var datastring = '{"cmd":"nodeedit","key":"'+node.key +'","title":"'+ $("#set_value").val() +'","type":"'+ node.data.type +'","rcommunity":"'+ node.data.rcommunity +'","wcommunity":"'+ node.data.wcommunity+'"}';
			      	        	    		send(datastring);
			      	        	            $( this ).dialog( "close" );
		      	        	    		}else{
		      	        	    			$("#set_value").addClass( "ui-state-error-custom" );
		      	        	    		}
		      	        	    		        	    		  
		      	        	    	  }
		      	        	      },
		      	        	      close: function() {
			      	        	    	$("#set_value").removeClass("ui-state-error-custom");
			      	        	  }
	      	        	    });
		      	            $("#set_value").val(node.title);
		      	            updateTips($.i18n.prop('message_modifyname'));
		    
		      	            $("#dialog-form").dialog("open");
		      	            
	      	              }
      	            }
    	          },
    	          "rcommunity": {name: "修改只读团体名", icon: "edit",
    	    		  disabled: function(key, opt){
    	        		  var node = $.ui.fancytree.getNode(opt.$trigger);
    	        		  if(sessionStorage.authlevel == 3){
	  	        			return true;
	  	        		  }
        	              if(node.data.type == "device"){
        	            	  return false;
        	              }else{
        	            	  return true;
        	              }
    	        	  },
      	        	callback: function(key, opt){
        	              var node = $.ui.fancytree.getNode(opt.$trigger);
        	              if(node.data.type == "device"){
        	            	//添加节点
  	      	            $( "#dialog-form" ).dialog({
  	      	        	      autoOpen: false,
  	      	        	      height: 240,
  	      	        	      width: 300,
  	      	        	      modal: true,
  	      	        	      buttons: {
  	      	        	    	  Ok: function() {	    
  	      	        	    		  if($("#set_value").val() != ""){
  	      	        	    			  var datastring = '{"cmd":"nodeedit","key":"'+node.key +'","title":"'+ node.title+'","type":"'+ node.data.type +'","wcommunity":"'+ node.data.wcommunity +'","rcommunity":"'+ $("#set_value").val()+'"}';
  		      	        	    		  send(datastring);
  		      	        	    		  $( this ).dialog( "close" );
  	      	        	    		  }else{
  	      	        	    			  $("#set_value").addClass( "ui-state-error-custom" )
  	      	        	    		  }   	        	    		  
  	      	        	              	      	        	              
  	      	        	            }
  	      	        	      },
  	      	        	      close: function() {
  	      	        	    	$("#set_value").removeClass("ui-state-error-custom");
  	      	        	      }
        	        	    });
	  	      	            $("#set_value").val(node.data.rcommunity);
	  	      	            updateTips($.i18n.prop('message_newcommunity'));
	  	      	            $("#dialog-form").dialog("open");
        	              }
        	            }
      	          },
      	        "wcommunity": {name: "修改只写团体名", icon: "edit",
      	        	disabled: function(key, opt){
  	        		  var node = $.ui.fancytree.getNode(opt.$trigger);
  	        		  if(sessionStorage.authlevel == 3){
  	        			return true;
  	        		  }
      	              if(node.data.type == "device"){
      	            	  return false;
      	              }else{
      	            	  return true;
      	              }
  	        	  },
      	        	callback: function(key, opt){
        	              var node = $.ui.fancytree.getNode(opt.$trigger);
        	              if(node.data.type == "device"){
	  	      	            $( "#dialog-form" ).dialog({
	  	      	        	      autoOpen: false,
	  	      	        	      height: 240,
	  	      	        	      width: 300,
	  	      	        	      modal: true,
	  	      	        	      buttons: {
	  	      	        	    	  Ok: function() {	    
	  	      	        	    		  if($("#set_value").val() != ""){
	  	      	        	    			  var datastring = '{"cmd":"nodeedit","key":"'+node.key +'","title":"'+ node.title+'","type":"'+ node.data.type +'","rcommunity":"'+ node.data.rcommunity +'","wcommunity":"'+ $("#set_value").val()+'"}';
	  		      	        	    		  send(datastring);
	  		      	        	    		  $( this ).dialog( "close" );
	  	      	        	    		  }else{
	  	      	        	    			  $("#set_value").addClass( "ui-state-error-custom" )
	  	      	        	    		  }   	        	    		  
	  	      	        	              	      	        	              
	  	      	        	            }
	  	      	        	      },
	  	      	        	      close: function() {
	  	      	        	    	$("#set_value").removeClass("ui-state-error-custom");
	  	      	        	      }
	        	        	    });
		  	      	            $("#set_value").val(node.data.wcommunity);
		  	      	            updateTips($.i18n.prop('message_newcommunity'));
		  	      	            $("#dialog-form").dialog("open");
	        	              }
        	         }
      	          },    	   	             	 
    	          "delete": {name: "删除", icon: "delete",
    	        	  disabled: function(key, opt){
      	        		  var node = $.ui.fancytree.getNode(opt.$trigger);    
      	        		  if(sessionStorage.authlevel == 3){
      	        			return true;
      	        		  }
          	              if(node.data.type == "group" || node.data.type == "device"){
          	            	  return false;
          	              }else{
          	            	  return true;
          	              }
      	        	  	},
    	        	callback: function(key, opt){
      	              	var node = $.ui.fancytree.getNode(opt.$trigger);
      	              	if(node.key == "1"){
      	              		return false;
      	              	}
      	              	if(!node.isLoaded()){
      	              		alert($.i18n.prop('message_nodedelerr'));
      	              		return false;
      	              	}
      	              	if(node.data.type == "group" && node.hasChildren()){
      	              		alert($.i18n.prop('message_nodedel'));
      	              		return false;
      	              	}
      	              	if((confirm( $.i18n.prop('message_sure'))==true))
      	              	{
      	              		//删除节点
      	              		var datastring = '{"cmd":"nodedel","key":"'+node.key +'","type":"'+ node.data.type +'","pkey":"'+ node.data.pkey +'"}';
      	              		send(datastring);
      	              	}	      	            
      	            }
    	          }
    	        }
    	    });
    }
    
    function searchtreenode(search_val){		
		if(ipvalidate(search_val)){
			var node = $("#dev-fancytree").fancytree("getTree").getNodeByKey(search_val);	
			node.setActive(true);
			var activeLi = node && node.li;
		  	$('.fancytree-container').animate({
		  		scrollTop: $(activeLi).offset().top - $('.fancytree-container').offset().top + $('.fancytree-container').scrollTop()}, 'slow');
		}else{
			//Title搜索
			var activenode = $("#dev-fancytree").fancytree("getActiveNode");
			var node = $("#dev-fancytree").fancytree("getTree").findAll(search_val);
			var firstnode = $("#dev-fancytree").fancytree("getTree").findFirst(search_val);
			var flag = false;
			if(activenode == null){				
				firstnode.setActive(true);
				var activeLi = firstnode && firstnode.li;
			  	$('.fancytree-container').animate({
			  		scrollTop: $(activeLi).offset().top - $('.fancytree-container').offset().top + $('.fancytree-container').scrollTop()}, 'slow');
				return;
			}
			
			if($.inArray(activenode, node) == -1){
				firstnode.setActive(true);
				var activeLi = firstnode && firstnode.li;
			  	$('.fancytree-container').animate({
			  		scrollTop: $(activeLi).offset().top - $('.fancytree-container').offset().top + $('.fancytree-container').scrollTop()}, 'slow');
			}else{
				$.each(node, function (n, value) {
					if(!value.isActive()){
						if(flag){
							value.setActive(true);
							flag = false;
							var activeLi = value && value.li;
						  	$('.fancytree-container').animate({
						  		scrollTop: $(activeLi).offset().top - $('.fancytree-container').offset().top + $('.fancytree-container').scrollTop()}, 'slow');
						  	return;
						}						
					}else{
						flag = true;
					}
				});		
				if(flag){
					firstnode.setActive(true);
					var activeLi = firstnode && firstnode.li;
				  	$('.fancytree-container').animate({
				  		scrollTop: $(activeLi).offset().top - $('.fancytree-container').offset().top + $('.fancytree-container').scrollTop()}, 'slow');
				}
			}	
		}
		
    }
    
    function parseLogs(jsonobj){
    	$.each(jsonobj.alarms, function (n, value) {
    		tbl_devalarm.row.add( [
    		            value.id,
    		            value.level,
    		            value.addr,
    		            value.path,
    		            value.type,
    		            value.paramname,
    		            value.paramvalue,
    		            value.eventtime,
    		            value.solved,
    		            value.solvetime
    		        ] ).draw( false );
        });
    	
    	$.each(jsonobj.invalidalarms, function (n, value) {
    		tbl_devalarm_old.row.add( [
    		            value.id,
    		            value.level,
    		            value.addr,
    		            value.path,
    		            value.type,
    		            value.paramname,
    		            value.paramvalue,
    		            value.eventtime,
    		            value.solved,
    		            value.solvetime
    		        ] ).draw( false );
        });
    }
    
    function getDeviceDetail(devnode,preDevice){
    	//__getDeviceDetail(devnode);
    	var datastring;
    	if(preDevice == undefined){
    		datastring = '{"cmd":"getdevicedetail","ip":"' + devnode.key + '","devtype":"' + devnode.getLastChild().data.hfctype 
        	+ '","rcommunity":"' + devnode.data.rcommunity + '","wcommunity":"' + devnode.data.wcommunity + '","predev":""}';
    	}else{
    		datastring = '{"cmd":"getdevicedetail","ip":"' + devnode.key + '","devtype":"' + devnode.getLastChild().data.hfctype 
        	+ '","rcommunity":"' + devnode.data.rcommunity + '","wcommunity":"' + devnode.data.wcommunity + '","predev":"' + preDevice.key + '"}';
    	}   
    	$(".candile").empty();
    	send(datastring);
    }  
    
    
    function lazyLoad(event, data) {
    	var datastring = '{"cmd":"lazyLoad","key":"'+ data.node.key + '"}';
    	send(datastring);
    	data.result = $.Deferred(function (dfd) {
            setTimeout(function () {
              dfd.resolve(lazyLoadData);
            }, 1000);
          });
    	lazyLoadData = null;
    }
      
      function loadError(e,data) {
          var error = data.error;
          if (error.status && error.statusText) {
            data.message = "Ajax error: " + data.message;
            data.details = "Ajax error: " + error.statusText + ", status code = " + error.status;
          } else {
            data.message = "Custom error: " + data.message;
            data.details = "An error occurred during loading: " + error;
          }
        } 
    
    function send(datastring) {  
    	if (webSocket.readyState !== 1) {
    		initWebSocket(encstr);
            setTimeout(function() {
            	webSocket.send(datastring);
            }, 250);
        } else {
        	webSocket.send(datastring);
        };
    	
    };
    
    function encryptByDES(message, key) {    
        
        var keyHex = CryptoJS.enc.Utf8.parse(key);  
        var encrypted = CryptoJS.DES.encrypt(message, keyHex, {    
        mode: CryptoJS.mode.ECB,    
        padding: CryptoJS.pad.Pkcs7    
        });   
        return encrypted.toString();    
    }    
    function decryptByDES(ciphertext, key) {    
        var keyHex = CryptoJS.enc.Utf8.parse(key);    
         
        // direct decrypt ciphertext  
        var decrypted = CryptoJS.DES.decrypt({    
            ciphertext: CryptoJS.enc.Base64.parse(ciphertext)    
        }, keyHex, {    
            mode: CryptoJS.mode.ECB,    
            padding: CryptoJS.pad.Pkcs7    
        });    
         
        return decrypted.toString(CryptoJS.enc.Utf8);    
    }            
 

 
 
    function loadProperties() {
    	$.i18n.properties({
            name : 'strings', //资源文件名称
            path : '../i18n/', //资源文件路径
            mode : 'both', //用Map的方式使用资源文件中的值
            language :isEN?'en':'zh',
            async: true,
            callback : function() {//加载成功后设置显示内容
                $('.nav_search p')[0].textContent = $.i18n.prop('message_navsearch');
                $('.nav_manageralarm p')[0].textContent = $.i18n.prop('message_navhistoryalarm');
                $('.nav_managerlog p')[0].textContent = $.i18n.prop('message_navoptlog');
                $('.nav_server p')[0].textContent = $.i18n.prop('message_navserver');
                $('.nav_managerauth p')[0].textContent = $.i18n.prop('message_navauth');
                if(localStorage.void == 'on'){
                	$('.nav_sound p')[0].textContent = $.i18n.prop('message_navvoidon');
                }else{
                	$('.nav_sound p')[0].textContent = $.i18n.prop('message_navvoidoff');
                	$('.nav_sound i').addClass("icon-volume-off");
        			$('.nav_sound i').removeClass("icon-volume-up"); 
                }                
                $('.nav_about p')[0].textContent = $.i18n.prop('message_navabout');
                $('#md-password')[0].textContent = $.i18n.prop('message_navchangep');
                $('#user-logout')[0].textContent = $.i18n.prop('message_navlogout');
                $('#footer_tblalarm')[0].textContent = $.i18n.prop('message_tblalarm');
                $('#footer_tblinvalidalarm')[0].textContent = $.i18n.prop('message_tblalarminvalid');
                $('#footer_tbloptlog')[0].textContent = $.i18n.prop('message_tbllog');                
                $('#tbl_optid')[0].textContent = $.i18n.prop('message_tbloptid');
                $('#tbl_optuser')[0].textContent = $.i18n.prop('message_tbloptuser');
                $('#tbl_optype')[0].textContent = $.i18n.prop('message_tbloptype');
                $('#tbl_optcontent')[0].textContent = $.i18n.prop('message_tbloptcontent');
                $('#tbl_optime')[0].textContent = $.i18n.prop('message_tbloptime');                
                $('.validateTips')[0].textContent = $.i18n.prop('message_validateTips');
                
                $('#server-info')[0].textContent = $.i18n.prop('message_statusinfo');
                $('#server-trap')[0].textContent = $.i18n.prop('message_traplisten');
                $('#server-db')[0].textContent = $.i18n.prop('message_dbstatus');
                $('#server-client')[0].textContent = $.i18n.prop('message_clients');
                
                $('#dilg-sdevtype')[0].textContent = $.i18n.prop('message_searchdevtype');                
                $('#dilg-rcommunity')[0].textContent = $.i18n.prop('message_rcommunity');
                $('#dilg-wcommunity')[0].textContent = $.i18n.prop('message_wcommunity');
                $('#dilg-devname')[0].textContent = $.i18n.prop('message_devname');
                $('#dilg-invalidchar')[0].textContent = $.i18n.prop('message_invalidcharater');
                $('#dilg-community')[0].textContent = $.i18n.prop('message_community');                
                $('#dilg-startIP')[0].textContent = $.i18n.prop('message_beginip');
                $('#dilg-endIP')[0].textContent = $.i18n.prop('message_endip');
                $('#dilg-devtype')[0].textContent = $.i18n.prop('message_searchdevtype'); 
                $('#reg-newdev')[0].textContent = $.i18n.prop('message_regnewdev');
                $('.list-group-item-heading')[0].textContent = $.i18n.prop('message_newdevlist');
                $('#btn-regdev')[0].textContent = $.i18n.prop('message_register'); 
                $('#btn_regclose')[0].textContent = $.i18n.prop('message_close');
                $('#dilg-historytitle')[0].textContent = $.i18n.prop('message_navhistoryalarm');
                $('#dilg-start')[0].textContent = $.i18n.prop('message_start');
                $('#dilg-end')[0].textContent = $.i18n.prop('message_stop');
                $('#dilg-source')[0].textContent = $.i18n.prop('message_source');                
                $('#btn-alarmok')[0].textContent = $.i18n.prop('message_ok');
                $('#btn-alarmclose')[0].textContent = $.i18n.prop('message_close');
                $('#btn-alarmclose1')[0].textContent = $.i18n.prop('message_close');
                $('#alarmlist-title')[0].textContent = $.i18n.prop('message_navhistoryalarm');
                $('#btn-alarmexports')[0].textContent = $.i18n.prop('message_export');
                $('#dilg-optlogtitle')[0].textContent = $.i18n.prop('message_navoptlog');
                $('#dilg-start1')[0].textContent = $.i18n.prop('message_start');
                $('#dilg-end1')[0].textContent = $.i18n.prop('message_stop');
                $('#dilg-operater')[0].textContent = $.i18n.prop('message_tbloptuser');
                $('#btn-optlogok')[0].textContent = $.i18n.prop('message_ok');
                $('#btn-alarmclose2')[0].textContent = $.i18n.prop('message_close');
                $('#btn-alarmclose3')[0].textContent = $.i18n.prop('message_close');
                $('#mymodal_about')[0].textContent = $.i18n.prop('message_navabout');
                $('#dialog-alarmThreshold')[0].title = $.i18n.prop('message_alarmThreshold');
                $('#newdev_devname').val($.i18n.prop('message_newdev'));
                $('#dialog-devsearch')[0].title = $.i18n.prop('message_search');
                $('#alarmfilter-date')[0].options[0].textContent = $.i18n.prop('message_optionaldate');
                $('#alarmfilter-date')[0].options[1].textContent = $.i18n.prop('message_recentday');
                $('#alarmfilter-date')[0].options[2].textContent = $.i18n.prop('message_recentweek');
                $('#alarmfilter-date')[0].options[3].textContent = $.i18n.prop('message_recentmonth');
                $('#btn-optclose1')[0].textContent = $.i18n.prop('message_close');
                $('#tbl_alarmlevel')[0].textContent = $.i18n.prop('message_tbllevel');
                $('#tbl_alarmip')[0].textContent = $.i18n.prop('message_tblip');
                $('#tbl_alarmpath')[0].textContent = $.i18n.prop('message_tblpath');                
                $('#tbl_alarmtype')[0].textContent = $.i18n.prop('message_tbloptype');
                $('#tbl_alarmpname')[0].textContent = $.i18n.prop('message_tblparam');
                $('#tbl_alarmvalue')[0].textContent = $.i18n.prop('message_tblparamv');
                $('#tbl_alarmtime')[0].textContent = $.i18n.prop('message_tbloptime');
                $('#tbl_alarmcomfirm')[0].textContent = $.i18n.prop('message_tblconfirmation');
                $('#tbl_alarmcomfirmtime')[0].textContent = $.i18n.prop('message_tblconfirmtime');
                $('#tbl_alarmlevel1')[0].textContent = $.i18n.prop('message_tbllevel');
                $('#tbl_alarmip1')[0].textContent = $.i18n.prop('message_tblip');
                $('#tbl_alarmpath1')[0].textContent = $.i18n.prop('message_tblpath');                
                $('#tbl_alarmtype1')[0].textContent = $.i18n.prop('message_tbloptype');
                $('#tbl_alarmpname1')[0].textContent = $.i18n.prop('message_tblparam');
                $('#tbl_alarmvalue1')[0].textContent = $.i18n.prop('message_tblparamv');
                $('#tbl_alarmtime1')[0].textContent = $.i18n.prop('message_tbloptime');
                $('#tbl_alarmcomfirm1')[0].textContent = $.i18n.prop('message_tblconfirmation');
                $('#tbl_alarmcomfirmtime1')[0].textContent = $.i18n.prop('message_tblconfirmtime');
                $('#dialog-newdev')[0].title = $.i18n.prop('message_newdev');
                $('#server-status')[0].textContent = $.i18n.prop('message_navserver');
                $('#dilg-ipaddr')[0].textContent = $.i18n.prop('message_ipaddr');
                $('#salutation')[0].options[0].textContent = $.i18n.prop('message_other');
                $('#salutation')[0].options[2].textContent = $.i18n.prop('message_trans');
                $('#salutation')[0].options[3].textContent = $.i18n.prop('message_rece');
                $('#salutation')[0].options[4].textContent = $.i18n.prop('message_osw');
                $('#salutation')[0].options[5].textContent = $.i18n.prop('message_rfsw');
                $('#salutation')[0].options[6].textContent = $.i18n.prop('message_preamp');
                $('#salutation')[0].options[7].textContent = $.i18n.prop('message_wos');
                
                $('#mymodal_authman')[0].textContent = $.i18n.prop('message_navauth');
                $('.i18n-level')[0].textContent = $.i18n.prop('message_tbllevel');
                $('#auth_base')[0].textContent = $.i18n.prop('message_basic');
                $('.i18n-username')[0].textContent = $.i18n.prop('message_username');
                $('.i18n-username1')[0].textContent = $.i18n.prop('message_username');
                $('.i18n-username2')[0].textContent = $.i18n.prop('message_username');
                $('.i18n-userid')[0].textContent = $.i18n.prop('message_userid');
                $('.i18n-trapnotice')[0].textContent = $.i18n.prop('message_trapnotice');
                $('.i18n-usergroup')[0].textContent = $.i18n.prop('message_usergroup');
                //$('.i18n-oripassword')[0].textContent = $.i18n.prop('message_oripassword');
                $('.i18n-password')[0].textContent = $.i18n.prop('message_password');
                $('.i18n-password2')[0].textContent = $.i18n.prop('message_password');
                $('.i18n-repeat')[0].textContent = $.i18n.prop('message_repeat');
                $('.i18n-repeat2')[0].textContent = $.i18n.prop('message_repeat');
                $('.i18n-oripassword1')[0].textContent = $.i18n.prop('message_oripassword');
                $('.i18n-password1')[0].textContent = $.i18n.prop('message_password');
                $('.i18n-repeat1')[0].textContent = $.i18n.prop('message_repeat');
                $('#btn-authsub')[0].textContent = $.i18n.prop('message_submit');
                $('#btn-authsub1')[0].textContent = $.i18n.prop('message_submit');
                $('#btn-authdel')[0].textContent = $.i18n.prop('message_del');
                $('#btn-authadd')[0].textContent = $.i18n.prop('message_add');
                $('#mymodal_authmanbase')[0].textContent = $.i18n.prop('message_navchangep');
                $('.i18n-level1')[0].textContent = $.i18n.prop('message_tbllevel');
                $('#btn-adduser')[0].textContent = $.i18n.prop('message_add');
                $('#mymodal_adduser')[0].textContent = $.i18n.prop('message_adduser');
                $('.i18n-ladmin')[0].textContent = $.i18n.prop('message_admin');
                $('.i18n-lobserver')[0].textContent = $.i18n.prop('message_observer');
                
                $('#alarmfilter-level-lb')[0].textContent = $.i18n.prop('message_tbllevel');
                $('#alarmfilter-level')[0].options[0].textContent = $.i18n.prop('message_all');
                $('#alarmfilter-level')[0].options[1].textContent = $.i18n.prop('message_urgentalarm');
                $('#alarmfilter-level')[0].options[2].textContent = $.i18n.prop('message_secalarm');
                $('#alarmfilter-level')[0].options[3].textContent = $.i18n.prop('message_generalalarm');
                
                $('#alarmfilter-istreatment-lb')[0].textContent = $.i18n.prop('message_statusinfo');
                $('#alarmfilter-istreatment')[0].options[0].textContent = $.i18n.prop('message_all');
                $('#alarmfilter-istreatment')[0].options[1].textContent = $.i18n.prop('message_acknowledged');
                $('#alarmfilter-istreatment')[0].options[2].textContent = $.i18n.prop('message_unacknowledged');
      
                $(".context-menu-list li").each(function(i){
        			switch(i){
        			case 0:
        				$(this)[0].childNodes[0].textContent = $.i18n.prop('message_addgroup');
        				break;
        			case 1:
        				$(this)[0].childNodes[0].textContent = $.i18n.prop('message_adddevice');
        				break;
        			case 2:
        				$(this)[0].childNodes[0].textContent = $.i18n.prop('message_edit');
        				break;
        			case 3:
        				$(this)[0].childNodes[0].textContent = $.i18n.prop('message_mrocommunity');
        				break;
        			case 4:
        				$(this)[0].childNodes[0].textContent = $.i18n.prop('message_mwocommunity');
        				break;
        			case 5:
        				$(this)[0].childNodes[0].textContent = $.i18n.prop('message_del');
        				break;
        			}
           		});
            }
        });
    }
    
})(jQuery);