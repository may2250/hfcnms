(function($) {
	var webSocket;
	var regtree;
	var tbl_devalarm;
	var tbl_optlog;
	var tbl_loglists = null;
	var lazyLoadData = null;
	$(function() {
		var encstr = localStorage.userName+'/'+ sessionStorage.passWord;
		initWebSocket(encstr);	   	
    	
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
            columns: [
                      { title: "ID" },
                      { title: "级别" },
                      { title: "路径" },
                      { title: "类型" },
                      { title: "参数名" },
                      { title: "参数值" },
                      { title: "发生时间" },
                      { title: "处理提交" },
                      { title: "确认时间" }
                  ],
            drawCallback: function() {
        	    $.contextMenu({
        	      selector: '#tbl_devalarm tbody tr td',
        	      callback: function(key, options) {
        	        var id = options.$trigger[0].parentElement.id;
        	        var m = "clicked: " + key + ' ' + id;
        	        window.console && console.log(m) || alert(m);
        	      },
        	      items: {
        	        "edit": {
        	          name: "处理",
        	          icon: "edit"
        	        }
        	      }
        	    });
        	  },
        	  "createdRow": function ( row, data, index ) {
        		  $(row).attr('id', data[0]);
                  if ( data[1] == "1" ) {
                	  $('td', row).parent().addClass('alarm-warning');
                  }else if(data[1] == "2"){
                	  $('td', row).parent().addClass('alarm-danger');                  
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
    	
    	$('#tbl_devalarm tbody').on( 'click', 'tr', function () {
            if ( $(this).hasClass('selected') ) {
                $(this).removeClass('selected');
            }
            else {
            	tbl_devalarm.$('tr.selected').removeClass('selected');
                $(this).addClass('selected');
            }
        } ); 
    	
    	$('.nav_search').click(function(){
    		$( "#dialog-devsearch" ).dialog({
	        	      autoOpen: false,
	        	      height: 300,
	        	      width: 890,
	        	      modal: true,
	        	      buttons: {
	        	    	  "开始": function() {	    
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
	      	        	    	  webSocket.send(datastring);
	        	            }
	        	      },
	        	      close: function() {
	        	    	  $("#search-sip").removeClass("ui-state-error-custom");
	        	    	  $("#search-eip").removeClass("ui-state-error-custom");
	        	      }
      	    });
            $("#dialog-devsearch").dialog("open");
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
    	
    	$('.nav_manageralarm').click(function(){
    		$("#modal_alarm").modal();
    	});
    	
    	$('.nav_managerlog').click(function(){
    		$("#modal_optlog").modal();
    	});
    	
    	$('.nav_sound').click(function(){
    		if($('.nav_sound p')[0].textContent == "声讯告警控制开"){
    			$('.nav_sound i').addClass("icon-volume-off");
    			$('.nav_sound i').removeClass("icon-volume-up"); 
    			$('.nav_sound p')[0].textContent = "声讯告警控制关";
    			playVideo("/alarmwavs/alarm offline.wav");
    		}else{
    			$('.nav_sound i').addClass("icon-volume-up");
    			$('.nav_sound i').removeClass("icon-volume-off"); 
    			$('.nav_sound p')[0].textContent = "声讯告警控制开";
    			playVideo("/alarmwavs/alarm online.wav");
    		};    		
    	});
    	
    	$('.nav_about').click(function(){
    		$("#modal_about").modal();
    	});
    	
    	$('#needtype').change(function(){ 
    		$('#salutation').attr("disabled", !$(this).is(':checked'));    		
    	});
    	
    	$('#user-logout').click(function(){
    		sessionStorage.passWord = undefined;
    		window.location.href="/login";
    	});
    	
    	$('#modal_searchresult').on('hidden.bs.modal', function (e) {
    		$('#list-newdevs').empty();
    		$('#list-newdevs').append('<a class="list-group-item active"><h4 class="list-group-item-heading">新设备列表</h4></a>');
    	})
    	
    	$('#btn-regdev').click(function(){
    		var node = $("#reg-grouptree").fancytree("getActiveNode");
    		if(node == null || node == undefined){
    			alert("请指定要注册设备的组!");
    			return;
    		}
    		if($('input:checkbox[name=dev]:checked').length < 1){
    			alert("请指定要注册设备!");
    			return;
    		}
    		$('input:checkbox[name=dev]:checked').each(function(i){
    			 var strs = new Array(); //定义一数组 
    			 strs = $(this).val().split("/"); //字符分割 
    			 //发送到服务端注册设备
    			 var datastring = '{"cmd":"deviceadd","key":"'+ node.key + '","devtype":"'+ strs[1] + '","rcommunity":"public","wcommunity":"public","devname":"'+ strs[0] + '","netip":"'+ strs[0] +'"}';
 	    		 webSocket.send(datastring);
    			 
    		     //删除该行
    			 $(this).parent().parent().remove();
    		});
    	});
    	$("select#alarmfilter-date").change(function(){
    		if($(this).val() == "自选日期"){
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
    		if(getDays($('#datepicker_start').val(), $('#datepicker_end').val()) > 92){
    			alert("查询间隔不能大于3个月!");
    			return;
    		}
    		 var datastring = '{"cmd":"alarmsearch","start":"'+ $('#datepicker_start').val() + '","end":"'+ $('#datepicker_end').val() 
    		 + '","customdate":"'+ $("#alarmfilter-date").prop('selectedIndex') + '","source":"'+ $('#alarmfilter-source').val() 
    		 + '","level":"'+ $("#alarmfilter-level").prop('selectedIndex') + '","type":"'+ $('#alarmfilter-type').val()
    		 + '","treatment":"'+ $("#alarmfilter-istreatment").prop('selectedIndex') + '","nename":"'+ $('#alarmfilter-nename').val() +'"}';
    		 webSocket.send(datastring);
    		 $("#alarmlist-title")[0].textContent = "历史告警日志";
    		 $("#modal_alarm").modal('hide');
    		 $("#modal_alarmlists").modal();
    	});
    	
    	$('#modal_alarmlists').on('show.bs.modal', function () {
    		if($("#alarmlist-title")[0].textContent == "历史告警日志"){
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
                              { title: "ID" },
                              { title: "级别" },
                              { title: "路径" },
                              { title: "类型" },
                              { title: "参数名" },
                              { title: "参数值" },
                              { title: "发生时间" },
                              { title: "处理提交" },
                              { title: "确认时间" }
                          ]
                } );
    		}else{
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
                              { title: "日志编号" },
                              { title: "日志类型" },
                              { title: "日志内容" },
                              { title: "登记时间" }
                          ]
                } );
    		}
    		
    		setTimeout(function() {
				$.fn.dataTable.tables( {visible: true, api: true} ).columns.adjust();
			}, 400);
    	});
    	
    	$('#modal_alarmlists').on('hide.bs.modal', function () {
    		tbl_loglists.clear().draw();
		});
    	
    	$('#btn-alarmexports').click(function(){
    		/*tbl_loglists.data().each( function (d) {
    		    d.counter++;
    		} );*/
    		tableToExcel('tbl_loglists');
    	});
    	
    	$('#btn-optlogok').click(function(){
   		 var datastring = '{"cmd":"optlogsearch","start":"'+ $('#datepicker_start').val() + '","end":"'+ $('#datepicker_end').val() 
   		 + '","optname":"'+ $('#optfilter-name').val() +'"}';
   		 webSocket.send(datastring);
   		 $("#alarmlist-title")[0].textContent = "历史/事件日志";
   		 $("#modal_optlog").modal('hide');
   		 $("#modal_alarmlists").modal();
   	});
    	
    	$('#btn-tree-search').click(function(){
    		searchtreenode($("#searchbar-key").val());
    	});
    	
    	$('#searchbar-key').bind('keydown',function(event){
    	    if(event.keyCode == "13") {
    	    	searchtreenode($("#searchbar-key").val());
    	    }
    	});  
		
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
	
	function onMessage(event) {
    	var jsonobj =  eval('(' + event.data + ')');
        if(jsonobj.cmd == "getInitTree"){
        	$('#username')[0].textContent = localStorage.userName;
        	initTree(jsonobj.treenodes);        
        }else if(jsonobj.cmd == "loginAuth"){
        	if(!jsonobj.Authed){
        		window.location.href="/login";
        	}
        }else if(jsonobj.cmd == "getInitLog"){
        	//解析日志并显示
        	parseLogs(jsonobj);
        	
        }else if(jsonobj.cmd == "test"){
        	document.getElementById('messages').innerHTML
            += '<br />' + jsonobj.message;
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
	       		webSocket.send(datastring);
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
        	showHfcDevice(jsonobj);
        }else if(jsonobj.cmd == "hfcvalueset"){
        	parseHfcValueSet(jsonobj);        	   	 
        }else if(jsonobj.cmd == "devstatus"){
        	parseDevStatus(jsonobj);
        }else if(jsonobj.cmd == "realtime-device"){
        	showHfcDevice(jsonobj);
        }else if(jsonobj.cmd == "devsearchprocess"){
        	if(jsonobj.process == 100){
        		$("#dialog-devsearch").dialog("close");
        		$(".progress-bar").width( "0%");
        		var datastring = '{"cmd":"getgrouptree"}';
    	    	webSocket.send(datastring);
    	    	$("#modal_searchresult").modal();
        	}
        	$(".progress-bar").width(jsonobj.process+ "%");
        }else if(jsonobj.cmd == "getgrouptree"){
        	regtree = $("#reg-grouptree").fancytree({
                source: jsonobj.treenodes,
                clickFolderMode: 1,
                minExpandLevel: 2
              });
        }else if(jsonobj.cmd == "devsearch-result"){
        	var paramstr = jsonobj.ipaddr+ '/' + jsonobj.devtype +'/'+jsonobj.hfctype;
        	$('#list-newdevs').append('<li class="list-group-item"><label><input name="dev" type="checkbox" value="'+ paramstr + '" />'+jsonobj.ipaddr+ '/' + getNetTypeTostring(jsonobj.devtype)+'/'+jsonobj.hfctype+'</label></li>');
        }else if(jsonobj.cmd == "alarm_message"){
        	if(jsonobj.opt == false){
        		tbl_devalarm.row("#" + jsonobj.id).remove().draw(false);        		
        	}else{
        		tbl_devalarm.row.add( [
        		       	            jsonobj.id,
        		       	            jsonobj.level,
        		       	            jsonobj.path,
        		       	            jsonobj.type,
        		       	            jsonobj.paramname,
        		       	            jsonobj.paramvalue,
        		       	            jsonobj.eventtime,
        		       	            jsonobj.solved,
        		       	            jsonobj.solvetime
        		       	        ] ).draw( false );
        	}        	
        }else if(jsonobj.cmd == "alarmsearch"){
        	$.each(jsonobj.alarms, function (n, value) {
        		tbl_loglists.row.add( [
        		            value.id,
        		            value.level,
        		            value.path,
        		            value.type,
        		            value.paramname,
        		            value.paramvalue,
        		            value.eventtime,
        		            value.solved,
        		            value.solvetime
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
        }else{
        	document.getElementById('messages').innerHTML
            += '<br />' + event.data;
        }
    }

 
    function onOpen(event) {
    	/*var datastring = '{"cmd":"getInitTree","message":""}';
    	send(datastring);
    	var datastring = '{"cmd":"getInitLog","message":""}';
    	send(datastring);*/
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
    			if(node.getLastChild().key == "rece_workstation"){
    				node.icon = "../images/treeRece.png";
    			}else if(node.getLastChild().key == "EDFA"){
    				node.icon = "../images/treeEDFA.png";
    			}else if(node.getLastChild().key == "Trans"){
    				node.icon = "../images/treeTrans.png";
    			}else{
    				node.icon = "../images/device.png";
    			}        			
    			node.getLastChild().data.hfctype = jsonobj.hfctype;
    			if(__globalobj__._realDevice != undefined && __globalobj__._realDevice != null){
        			if(jsonobj.ip == __globalobj__._realDevice.key){
        				$(".dev-status").css("color", "lightgreen");
        				$("#dev-status-text")[0].textContent = "设备连接正常...";
        			}
        		}
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
        				webSocket.send(datastring);
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
    		extensions: ["dnd"],
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
    	    	  "rcommunity": {name: "修改只读团体名", icon: "edit",
    	    		  disabled: function(key, opt){
    	        		  var node = $.ui.fancytree.getNode(opt.$trigger);
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
  		      	        	    		  webSocket.send(datastring);
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
	  	      	            updateTips("请输入新的团体名:");
	  	      	            $("#dialog-form").dialog("open");
        	              }
        	            }
      	          },
      	        "wcommunity": {name: "修改只写团体名", icon: "edit",
      	        	disabled: function(key, opt){
  	        		  var node = $.ui.fancytree.getNode(opt.$trigger);
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
	  		      	        	    		  webSocket.send(datastring);
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
		  	      	            updateTips("请输入新的团体名:");
		  	      	            $("#dialog-form").dialog("open");
	        	              }
        	         }
      	          },    	
    	        "add": {name: "添加节点", icon: "add",
    	        	disabled: function(key, opt){
    	        		  var node = $.ui.fancytree.getNode(opt.$trigger);
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
		      	        	    		  webSocket.send(datastring);
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
	      	            updateTips("请输入添加节点名称:");
	      	            $("#dialog-form").dialog("open");
      	              }
      	            }
    	          },    	        
    	        "edit": {name: "编辑", icon: "edit",
    	        	disabled: function(key, opt){
  	        		  var node = $.ui.fancytree.getNode(opt.$trigger);
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
			      	        	    		webSocket.send(datastring);
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
		      	            $("#set_value").val("");
		      	            updateTips("请输入要更改的内容:");
		      	            $("#dialog-form").dialog("open");
	      	              }
      	            }
    	          },
    	          "sep1": "----",
    	          "adddevice": {name: "添加设备", icon: "add",
    	        	  disabled: function(key, opt){
    	        		  var node = $.ui.fancytree.getNode(opt.$trigger);
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
	      	      	        	    		  	webSocket.send(datastring);
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
    	          "delete": {name: "删除", icon: "delete",
    	        	  disabled: function(key, opt){
      	        		  var node = $.ui.fancytree.getNode(opt.$trigger);
          	              if(node.data.type == "group" || node.data.type == "device"){
          	            	  return false;
          	              }else{
          	            	  return true;
          	              }
      	        	  	},
    	        	callback: function(key, opt){
      	              	var node = $.ui.fancytree.getNode(opt.$trigger);
      	              	if(node.data.type == "group" && node.hasChildren()){
      	              		alert("不是子节点，无法删除!");
      	              		return false;
      	              	}
      	              	if((confirm( "确定要删除？ ")==true))
      	              	{
      	              		//删除节点
      	              		var datastring = '{"cmd":"nodedel","key":"'+node.key +'","type":"'+ node.data.type +'","pkey":"'+ node.data.pkey +'"}';
      	              		webSocket.send(datastring);
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
    		            value.path,
    		            value.type,
    		            value.paramname,
    		            value.paramvalue,
    		            value.eventtime,
    		            value.solved,
    		            value.solvetime
    		        ] ).draw( false );
        });
    	
    	$.each(jsonobj.logs, function (n, value) {
    		tbl_optlog.row.add( [
    		            value.id,
    		            value.type,
    		            value.content,
    		            value.time
    		        ] ).draw( false );
        });
    }
    
    function getDeviceDetail(devnode,preDevice){
    	__getDeviceDetail(devnode);
    	var datastring;
    	if(preDevice == undefined){
    		datastring = '{"cmd":"getdevicedetail","ip":"' + devnode.key + '","devtype":"' + devnode.getLastChild().key 
        	+ '","rcommunity":"' + devnode.data.rcommunity + '","wcommunity":"' + devnode.data.wcommunity + '","predev":""}';
    	}else{
    		datastring = '{"cmd":"getdevicedetail","ip":"' + devnode.key + '","devtype":"' + devnode.getLastChild().key 
        	+ '","rcommunity":"' + devnode.data.rcommunity + '","wcommunity":"' + devnode.data.wcommunity + '","predev":"' + preDevice.key + '"}';
    	}    	
    	send(datastring);
    }  
    
    
    function lazyLoad(event, data) {
    	var datastring = '{"cmd":"lazyLoad","key":"'+ data.node.key + '"}';
    	webSocket.send(datastring);
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
    		initWebSocket();
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
 
    
    
})(jQuery);