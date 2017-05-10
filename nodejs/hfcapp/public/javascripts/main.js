(function($) {
	var webSocket;
	$(function() {
		initWebSocket();
    	var datastring = '{"cmd":"getInitTree","message":""}';
    	send(datastring);
    	
    	$("#start").click(function(){
    		start();
    	});
	});
	
	function initWebSocket() {
        if (window.WebSocket) {
        	webSocket = new WebSocket('ws://localhost:8080/hfcnms/websocketservice');
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
    		extensions: [],
            source: treedata,
            ajax: { debugDelay: 1000 }
          });
    	
    	$.contextMenu({
    	      selector: "#dev-fancytree span.fancytree-title",
    	      items: {
    	        "cut": {name: "Cut", icon: "cut",
    	            callback: function(key, opt){
    	              var node = $.ui.fancytree.getNode(opt.$trigger);
    	              alert("Clicked on " + key + " on " + node);
    	            }
    	          },
    	        "add": {name: "添加", icon: "add",
    	        	callback: function(key, opt){
      	              var node = $.ui.fancytree.getNode(opt.$trigger);
      	              if(node.data.type != "device"){
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
	      	            $("#dialog-form").dialog("open");
      	              }else{
      	            	  //添加设备
      	            	  
      	              }
      	            }
    	          },    	        
    	        "edit": {name: "编辑", icon: "edit",
    	        	callback: function(key, opt){
      	              var node = $.ui.fancytree.getNode(opt.$trigger);
      	              
      	            }
    	          },
    	        "delete": {name: "删除", icon: "delete",
    	        	callback: function(key, opt){
      	              var node = $.ui.fancytree.getNode(opt.$trigger);
      	              alert("Clicked on " + key + " on " + node);
      	            }
    	          }
    	        }
    	    });
    }
    
    
    
    function lazyLoad(event, data) {
        switch (data.node.key) {
          case "ajax":
            
            break;
          
          default:
            data.result = [];
        }
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
            setTimeout(function() {
            	webSocket.send(datastring);
            }, 250);
        } else {
        	webSocket.send(datastring);
        };
    	
    };
    
       
 
    function start() {
    	var datastring = '{"cmd":"test","message":"hello!"}';
    	if (webSocket.readyState !== 1) {
    		webSocket.close();
            initWebSocket();
            setTimeout(function() {
            	send(datastring);
            }, 250);
        } else {
        	send(datastring);
        };
      return false;
    }
    
})(jQuery);