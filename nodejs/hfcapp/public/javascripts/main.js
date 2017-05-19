(function($) {
	var webSocket;
	var tbl_devalarm;
	var tbl_optlog;
	var realdevice;
	var lazyLoadData = null;
	$(function() {
		initWebSocket();		
    	var datastring = '{"cmd":"getInitTree","message":""}';
    	send(datastring);
    	
    	window.__globalobj__ = {
    		    _webSocket:webSocket,
    		    _realDevice:realdevice,
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
    		}else{
    			$('.nav_displaylog i').addClass("icon-eye-open");
    			$('.nav_displaylog i').removeClass(" icon-eye-close"); 
    			$('.nav_displaylog p')[0].textContent = "隐藏日志栏";
    			$("footer").css('display','block');
    			$(".devdetail-content").css('height',$(window).height() - 360); 
    		};    		
    	});
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
        
        }else if(jsonobj.cmd == "test"){
        	document.getElementById('messages').innerHTML
            += '<br />' + jsonobj.message;
        }else if(jsonobj.cmd == "nodeadd"){
        	 var rootNode = $("#dev-fancytree").fancytree("getTree").getNodeByKey(jsonobj.pkey);
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
        }else if(jsonobj.cmd == "nodeedit"){
	       	 var node = $("#dev-fancytree").fancytree("getTree").getNodeByKey(jsonobj.key);
	       	 node.title = jsonobj.title;
	         node.renderTitle();
        }else if(jsonobj.cmd == "nodedel"){
	       	 var node = $("#dev-fancytree").fancytree("getTree").getNodeByKey(jsonobj.key);
	         node.remove();
        }else if(jsonobj.cmd == "deviceadd"){
	       	 var node = $("#dev-fancytree").fancytree("getTree").getNodeByKey(jsonobj.pkey);
	       	 node.addChildren(jsonobj.devnodes);
        }else if(jsonobj.cmd == "lazyLoad"){
	       	 var node = $("#dev-fancytree").fancytree("getTree").getNodeByKey(jsonobj.key);
	       	 lazyLoadData = jsonobj.lazynodes;
        }else if(jsonobj.cmd == "hfcvalueset"){
        	 switch(jsonobj.target){
        	 case "devicetrapedit":
        		 $("#" + jsonobj.domstr)[0].textContent = jsonobj.value;
        		 break;
        	 case "devicechannel":
        		 $("#" + jsonobj.domstr).val(jsonobj.value);
        		 break;
        	 }
	       	 
        }else{
        	document.getElementById('messages').innerHTML
            += '<br />' + event.data;
        }
    }

 
    function onOpen(event) {
      document.getElementById('messages').innerHTML
        = 'Connection established';
    }
 
    function onError(event) {
    	//document.getElementById('messages').innerHTML = event.data;
    }     
    
    function initTree(treedata) {
    	devtree = $("#dev-fancytree").fancytree({
    		extensions: ["dnd"],
            source: treedata,
            clickFolderMode: 1,
            click: function(event, data) {
            	
            },
            dblclick: function(event, data) {
            	if(data.node.data.type == "device"){
            		//show deivce detail
            		__globalobj__._realDevice = data.node;
            		showDeviceDetail(data.node.getLastChild().title);
            	}
            	
            },
            dnd: {
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
              },
              lazyLoad: lazyLoad
          });
    	
    	$.contextMenu({
    	      selector: "#dev-fancytree span.fancytree-title",
    	      items: {
    	    	  "rcommunity": {name: "修改只读团体名", icon: "icon-book",
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
  	      	        	    			  var datastring = '{"cmd":"nodeadd","key":"'+node.key +'","type":"'+ node.data.type +'","value":"'+ $("#set_value").val()+'"}';
  		      	        	    		  webSocket.send(datastring);
  		      	        	    		  $( this ).dialog( "close" );
  	      	        	    		  }else{
  	      	        	    			  $("#set_value").addClass( "ui-state-error-custom" )
  	      	        	    		  }   	        	    		  
  	      	        	              	      	        	              
  	      	        	            }
  	      	        	      },
  	      	        	      close: function() {
  	      	        	    	$("#set_value").css('display','none');
  	      	        	    	$("#set_value").removeClass("ui-state-error-custom");
  	      	        	      }
        	        	    });
	  	      	            $("#set_value").css('display','block');
	  	      	            $("#set_value").value = "";
	  	      	            updateTips("请输入添加节点名称:");
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
	  	      	        	    	$("#set_value").css('display','none');
	  	      	        	    	$("#set_value").removeClass("ui-state-error-custom");
	  	      	        	      }
	        	        	    });
		  	      	            $("#set_value").css('display','block');
		  	      	            $("#set_value").value = "";
		  	      	            updateTips("请输入添加节点名称:");
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
	      	        	    	$("#set_value").css('display','none');
	      	        	    	$("#set_value").removeClass("ui-state-error-custom");
	      	        	      }
      	        	    });
	      	            $("#set_value").css('display','block');
	      	            $("#set_value").value = "";
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
	      	            if(node.data.type == "group"){
	      	            	//编辑节点
		      	            $( "#dialog-form" ).dialog({
		      	        	      autoOpen: false,
		      	        	      height: 240,
		      	        	      width: 300,
		      	        	      modal: true,
		      	        	      buttons: {
		      	        	    	  Ok: function() {	 
		      	        	    		if($("#set_value").val() != ""){
		      	        	    			var datastring = '{"cmd":"nodeedit","key":"'+node.key +'","type":"'+ node.data.type +'","value":"'+ $("#set_value").val()+'"}';
			      	        	    		webSocket.send(datastring);
			      	        	            $( this ).dialog( "close" );
		      	        	    		}else{
		      	        	    			$("#set_value").addClass( "ui-state-error-custom" );
		      	        	    		}
		      	        	    		        	    		  
		      	        	    	  }
		      	        	      },
		      	        	      close: function() {
			      	        	    	$("#set_value").css('display','none');
			      	        	    	$("#set_value").removeClass("ui-state-error-custom");
			      	        	  }
	      	        	    });
		      	          $("#set_value").css('display','block');
		      	            $("#set_value").value = "";
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
        	            	  $( "#dialog-form" ).dialog({
    	      	        	      autoOpen: false,
    	      	        	      height: 240,
    	      	        	      width: 300,
    	      	        	      modal: true,
    	      	        	      buttons: {
    	      	        	    	  Ok: function() {	   
	    	      	        	    		if($("#set_value").val() != "" && ipvalidate($("#set_value").val())){
	    	      	        	    			var datastring = '{"cmd":"deviceadd","key":"'+ node.key + '","value":"'+ $("#set_value").val()+'"}';
	      	      	        	    		  	webSocket.send(datastring);
	      	      	        	    		  	$( this ).dialog( "close" );
	    	      	        	    		}else{
	    	      	        	    			$("#set_value").addClass( "ui-state-error-custom" );
	    	      	        	    		}
    	      	        	    	  }
    	      	        	      },
    	      	        	      close: function() {
    		      	        	    	$("#set_value").css('display','none');
    		      	        	    	$("#set_value").removeClass("ui-state-error-custom");
    		      	        	  }
	          	        	    });
        	            	  	$("#set_value").css('display','block');
	    	      	            $("#set_value").value = "";
	        	            	updateTips("输入设备的IP地址:");
	        	            	$("#dialog-form").dialog("open");
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
    
    function showDeviceDetail(devtype){
    	//TODO
    	$(".candile").load("/opticalTran");
    	showopticalTran();
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
    
    function send(datastring) {  
    	if (webSocket.readyState !== 1) {
            setTimeout(function() {
            	webSocket.send(datastring);
            }, 250);
        } else {
        	webSocket.send(datastring);
        };
    	
    };
    
       
 
    
    
})(jQuery);