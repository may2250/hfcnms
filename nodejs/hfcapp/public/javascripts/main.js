(function($) {
	var webSocket;
	var tbl_devalarm;
	var tbl_optlog;
	var lazyLoadData = null;
	$(function() {
		initWebSocket();		
    	var datastring = '{"cmd":"getInitTree","message":""}';
    	send(datastring);
    	
    	window.__globalobj__ = {
    		    _webSocket:webSocket,
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
    		scrollY:        150,
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
    		scrollY:        150,
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
        }else if(jsonobj.cmd == "lazyLoad"){
	       	 var node = $("#dev-fancytree").fancytree("getTree").getNodeByKey(jsonobj.key);
	       	 lazyLoadData = jsonobj.lazynodes;
	         //node.addChildren(jsonobj.lazynodes);
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
            dblclick: function(event, data) {
            	if(data.node.data.type == "device"){
            		//show deivce detail
            		showDeviceDetail(data.node.data.type);
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
    	        "add": {name: "添加节点", icon: "add",
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
	      	        	    		  var datastring = '{"cmd":"nodeadd","key":"'+node.key +'","type":"'+ node.data.type +'","value":"'+ $("#set_value").val()+'"}';
	      	        	    		  webSocket.send(datastring);
	      	        	              $( this ).dialog( "close" );
	      	        	            }
	      	        	      }
      	        	    });
	      	            $("#set_value").value = "";
	      	            updateTips("请输入添加节点名称:");
	      	            $("#dialog-form").dialog("open");
      	              }
      	            }
    	          },    	        
    	        "edit": {name: "编辑", icon: "edit",
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
		      	        	    		  var datastring = '{"cmd":"nodeedit","key":"'+node.key +'","type":"'+ node.data.type +'","value":"'+ $("#set_value").val()+'"}';
		      	        	    		  webSocket.send(datastring);
		      	        	              $( this ).dialog( "close" );
		      	        	            }
		      	        	      }
	      	        	    });
		      	            $("#set_value").value = "";
		      	            updateTips("请输入要更改的内容:");
		      	            $("#dialog-form").dialog("open");
	      	              }
      	            }
    	          },
    	          "sep1": "----",
    	          "adddevice": {name: "添加设备", icon: "add",
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
    	      	        	    		  var datastring = '{"cmd":"deviceadd","value":"'+ $("#set_value").val()+'"}';
    	      	        	    		  webSocket.send(datastring);
    	      	        	              $( this ).dialog( "close" );
    	      	        	            }
    	      	        	      }
	          	        	    });
	    	      	            $("#set_value").value = "";
	        	            	updateTips("输入搜索设备的IP地址:");
	        	            	$("#dialog-form").dialog("open");
	        	          }
	        	     }
      	          },    	 
    	          "delete": {name: "删除", icon: "delete",
    	        	callback: function(key, opt){
      	              	var node = $.ui.fancytree.getNode(opt.$trigger);
      	              	if((confirm( "确定要删除？ ")==true))
      	              	{
	      	              	if(node.data.type == "group"){
		      	            	//删除节点
	      	              		var datastring = '{"cmd":"nodedel","key":"'+node.key +'","type":"'+ node.data.type +'","pkey":"'+ node.data.pkey +'"}';
	      	              		webSocket.send(datastring);
			      	            $("#dialog-form").dialog("open");
		      	             }else{
		      	            	 //删除设备
		      	            	 
		      	             }
      	              	}	      	            
      	            }
    	          }
    	        }
    	    });
    }
    
    function showDeviceDetail(devtype){
    	//TODO
    	$(".candile").load("/opticalTran");
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