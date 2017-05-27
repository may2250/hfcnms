(function($) {
	var webSocket;
	var tbl_devalarm;
	var tbl_optlog;
	var lazyLoadData = null;
	$(function() {
		initWebSocket();	   	
    	
    	window.__globalobj__ = {
    		    _webSocket:webSocket,
    		    _realDevice:"",
    		    _send:function(datastring) {  
    		    	if (webSocket.readyState !== 1) {
    		            setTimeout(function() {
    		            	webSocket.send(datastring);
    		            }, 250);
    		        } else {
    		        	webSocket.send(datastring);
    		        };
    		    	
    		    },
    		    _initWebSocket:function(){
    				var hostip = window.location.hostname;
    		        if (window.WebSocket) {
    		        	webSocket = new WebSocket('ws://' + hostip + ':8080/hfcnms/websocketservice');
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
                      { title: "来源" },
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
                  if ( data[1] == "1" ) {
                	  $('td', row).parent().addClass('alarm-danger');
                  }else if(data[1] == "2"){
                	  $('td', row).parent().addClass('alarm-major');
                  }else if(data[1] == "3"){
                	  $('td', row).parent().addClass('alarm-warning');
                  }else if(data[1] == "4"){
                	  $('td', row).parent().addClass('alarm-clear');
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
    	
    	$('#needtype').change(function(){ 
    		$('#salutation').attr("disabled", !$(this).is(':checked'));    		
    	}) 
	});
	
	function initWebSocket() {
		var hostip = window.location.hostname;
        if (window.WebSocket) {
        	webSocket = new WebSocket('ws://' + hostip + ':8080/hfcnms/websocketservice');
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
        	initTree(jsonobj.treenodes);        
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
        			if(realdevice != undefined && realdevice != null){
            			if(jsonobj.key == realdevice.key){
            				$(".dev-status").css("color", "lightgreen");
            			}
            		}
        		}else{
        			node.data.isonline = false;
        			node.icon = "../images/devoff.png";
        			if(realdevice != undefined && realdevice != null){
            			if(jsonobj.key == realdevice.key){
            				$(".dev-status").css("color", "red");
            			}
            		}
        		}
        		node.render();        		
        	}
        	showHfcDevice(jsonobj);
        }else if(jsonobj.cmd == "hfcvalueset"){
        	parseHfcValueSet(jsonobj);        	   	 
        }else if(jsonobj.cmd == "devstatus"){
        	var node = $("#dev-fancytree").fancytree("getTree").getNodeByKey(jsonobj.key);
        	if(node != undefined){
        		if(jsonobj.isonline == true){
        			node.data.isonline = true;
        			node.icon = "../images/device.png";
        			if(realdevice != undefined && realdevice != null){
            			if(jsonobj.key == realdevice.key){
            				$(".dev-status").css("color", "lightgreen");
            			}
            		}
        		}else{
        			node.data.isonline = false;
        			node.icon = "../images/devoff.png";
        			if(realdevice != undefined && realdevice != null){
            			if(jsonobj.key == realdevice.key){
            				$(".dev-status").css("color", "red");
            			}
            		}
        		}
        		node.render();        		
        	}
        }else{
        	document.getElementById('messages').innerHTML
            += '<br />' + event.data;
        }
    }

 
    function onOpen(event) {
    	var datastring = '{"cmd":"getInitTree","message":""}';
    	send(datastring);
    	var datastring = '{"cmd":"getInitLog","message":""}';
    	send(datastring);
    }
 
    function onError(event) {
    	//document.getElementById('messages').innerHTML = event.data;
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
            	if(data.node.data.type == "device"){
            		//show deivce detail
            		__globalobj__._realDevice = data.node;
            		getDeviceDetail(data.node);
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
    
    function parseLogs(jsonobj){
    	$.each(jsonobj.alarms, function (n, value) {
    		tbl_devalarm.row.add( [
    		            value.id,
    		            value.level,
    		            value.source,
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
    
    function getDeviceDetail(devnode){
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
    	var datastring = '{"cmd":"getdevicedetail","ip":"' + devnode.key + '","devtype":"' + devnode.getLastChild().key + '"}';
    	send(datastring);
    	/*if(!devnode.data.isonline){
			$("dev-status").css("color", "red");
		}*/
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
    
       
 
    
    
})(jQuery);