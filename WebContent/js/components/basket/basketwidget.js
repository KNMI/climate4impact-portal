var BasketWidget = function() {
  var t = this;
  var basketWindow = undefined;
  var basketPanel = undefined;
  var _callbackFunction;
  var initialized = false;
  var tree = undefined;
  var store = undefined;
  var openedWindows = [];
  var _init = function(){
    if(initialized == true)return;
    initialized = true;
  
    Ext.define('basketgrid', {
      extend : 'Ext.data.Model',
      fields : [{
        name : 'text',
        type : 'string'
      },{
        name : 'id',
        type : 'string'
      },{
        name : 'dapurl',
        type : 'string'
      },{
        name : 'hasdap',
        type : 'string'
      },{
        name : 'hashttp',
        type : 'string'
      },{
        name : 'httpurl',
        type : 'string'
      },{
        name : 'catalogurl',
        type : 'string'
      },{
        name : 'date',
        type : 'string'
      },{
        name : 'type',
        type : 'string'
      },{
        name : 'filesize',
        type : 'string'
      }]
    });
  
    store = Ext.create('Ext.data.TreeStore', {
      model : 'basketgrid',
      root : {
        expanded : true,
        children : []
      },
      proxy: {
        type: 'ajax',
        url : '/impactportal/ImpactService?&service=basket&request=getoverview',
        listeners:{
          exception:{
            fn:function(t, type, action, options, response, arg ){
              if(type == 'remote'){
                Ext.MessageBox.alert("Error","Remote: Unable to load basket from server: "+response+ arg);
              }else{
                if(t.getReader().rawData.statuscode == 401){
                  generateLoginDialog(function(){ 
                	  store.reload();
                	//  basketWindow.show();
            	  });
                  return;
                }else{
                  Ext.MessageBox.alert("Error","Response: Unable to load basket from server:\n"+ (t.getReader().rawData.message));
                }
              }
            }
          }
        }
      },
      listeners:{
        beforeload:{
          fn:function(t,a){
            tree.setLoading(true);
          }
        },
        load:{
          fn:function(t,a){
            tree.setLoading(false);
          }
        }
      }
      
      
    });
    
    function endsWith(str, suffix) {
      return str.indexOf(suffix, str.length - suffix.length) !== -1;
    }
    
    var getSelectedFilesForUsage = function(){
	      if (tree.getSelectionModel().hasSelection()) {
	        var pendingRequests = 0;
	        var allFilesAssembled = function(selectedNode){
	          if(pendingRequests!=0){
	            console.log("Still some requests open");
	            return;
	          }else{
	            console.log("OK continue!");
	          }
	          
	          if (_callbackFunction) {
	            if(selectedNode.length == 0){
	              Ext.MessageBox.alert('Error','No valid nodes selected.');
	            }
	            var doClose = _callbackFunction(selectedNode);
	            if (doClose === true) {
	              for(w in openedWindows){
	                w = openedWindows[w];
	                try{
	                  w.close();
	                  w.destroy();
	                }catch(e){
	                }
	              }
	              openedWindows = [];
	              
	              basketWindow.close();
	            }
	          }else{
	            Ext.MessageBox.alert('Error','Nothing to apply to.');
	          }
	        };
	        
	        
	        var selectedNodesMixed = tree.getSelectionModel().getSelection();
	        
	        var selectedNode = [];
	        
	       
	        //console.log(selectedNodesMixed);
	        
	        for ( var j = 0; j < selectedNodesMixed.length || j < 1; j++) {
	          if(selectedNodesMixed[j].data.dapurl){
	            selectedNode.push({dapurl:selectedNodesMixed[j].data.dapurl});
	          }
	          if(selectedNodesMixed[j].data.catalogurl){
	            pendingRequests++;
	            var urlRequest = c4iconfigjs.impactservice+"service=catalogbrowser&mode=flat&format=application/json&node="+ URLEncode(selectedNodesMixed[j].data.catalogurl);
	            console.log(urlRequest);
	            $.ajax({
	              url: urlRequest,
	            }).done(function(data) {
	            
	              for(var j=0;j<data.files.length;j++){
	                if(data.files[j].opendap){
	                  if(endsWith(data.files[j].opendap,"aggregation")){
	                    console.log("Skipping (aggregation) "+data.files[j].opendap);
	                  }else{
	                    console.log("Pushing "+data.files[j].opendap);
	                    selectedNode.push({dapurl:data.files[j].opendap});
	                  }
	                  
	                }
	              }
	              pendingRequests--;
	              allFilesAssembled(selectedNode);
	            }).fail(function() {
              
              }).always(function() {
              
              });
	            //selectedNode.push({dapurl:selectedNodes[j].data.dapurl});
	          }
	        }
	        allFilesAssembled(selectedNode);
	      
	        
	        //
	            // alert(selectedNode[0].data.dapurl + ' was selected');
	  
	      } else {
	        Ext.MessageBox.alert('Error','No selected files.');
	      }
    };
  
    var getButtons = function(){
      var buttons =[{
        text : 'Search',
        handler : function() {
          var el=jQuery('<div/>');
          renderSearchInterface({
            element:el,
            service:c4iconfigjs.searchservice,
            query:"",
            catalogbrowserservice:c4iconfigjs.impactservice,
            dialog:true
          });
          
        }
         
      },{
        text : 'View file',
        handler : function() {
          if (tree.getSelectionModel().hasSelection()) {
            var selectedNode = tree.getSelectionModel().getSelection();
            
            if(selectedNode.length>1){
              Ext.MessageBox.alert('Error','Please select a single file to browse.');
              return;
            }
            selectedNode = selectedNode[0];
            showFileInfo(selectedNode,true);
          } else {
            Ext.MessageBox.alert('Error','No selected files.');
          }
        }
      },{
          text : 'Download file',
          iconCls : 'icon-download',
          handler : function() {
            var downloadWin;
            if (tree.getSelectionModel().hasSelection()) {
              var selectedNodes = tree.getSelectionModel().getSelection();
              if ((selectedNodes.length>1)||(selectedNodes.length==0)) { 
              	  Ext.MessageBox.alert('Error','Please select exactly one file with HTTP enabled to download.');
              } else {
                var securePage=location.protocol=="https:";
                var selectedNode = selectedNodes[0];
                var httpURL = selectedNode.data.httpurl;
                if(!httpURL){
                  Ext.MessageBox.alert('Error','Please select a file with HTTP enabled to download.');
                } else {
                  if(openid){
                    if(openid!=""){
                      httpURL+="?openid="+openid;
                    }
                  }
               	  if (downloadWin) {
                	downloadwin.close();
                  }
              	  downloadWin=window.open(httpURL, 'dwnl', 'width=900,height=600,resizable=yes');
                }
              }
            } else {
              Ext.MessageBox.alert('Error','No selected files.');
            }
          }
      },{
        text : 'Script download',
        iconCls : 'icon-download',
        handler : function() {
          if (tree.getSelectionModel().hasSelection()) {
            var selectedNodes = tree.getSelectionModel().getSelection();
            
            console.log('INFO Script Download: '+selectedNodes.length+" files");
            var urlList=[];
            var i;
            for (i=0; i<selectedNodes.length; i++){
              var selectedNode = selectedNodes[i];
              var httpURL = selectedNode.data.httpurl;
              if(!httpURL){
            	  Ext.MessageBox.alert('Error','Please select a file with HTTP enabled to download.');
            	  continue; //return;
              }
              urlList.push(httpURL);
              console.log(selectedNode);
            }
            if (urlList.length>0) {
            	var mySecureHostname="https:"+"//"+window.location.hostname;
            	var scriptForm = $('<form/>', {
            		action: serverurlhttps+"/account/downloadscript.jsp",
            		target: "_blank",
            		method: "post"
            	});
            	scriptForm.append($("<input/>", {
            		type: "hidden",
            		name: "urls",
            		value: urlList.join("\r\n")
            	}));
            	if ((openid) && (openid != "")) {
            		scriptForm.append($("<input/>", {
            			type: "hidden",
            			name: "openid",
            			value: openid
            		}));
            	}
            	$("body").append(scriptForm);
            	scriptForm.submit();
            } else {
            	Ext.MessageBox.alert('Error','No selected files.');
            }
          } else {
            Ext.MessageBox.alert('Error','No selected files.');
          }
        }
      },{
          text : 'Upload file',
          iconCls : 'icon-download',
          handler : function() {
        	 window.location='upload.jsp'; 
          }
        },{
        text : 'Delete file(s)',
        iconCls : 'button_remove',
        handler : function() {
          
         
          
          
          if (tree.getSelectionModel().hasSelection()) {
            Ext.MessageBox.show({
              title:'Messagebox Title',
              msg: 'Are you sure want to delete these item(s)?',
              buttonText: {yes: "Yes, delete",no: "No!",cancel: "Cancel"},
              fn: function(btn){
                if(btn == 'yes'){
                  var selectedNode = tree.getSelectionModel().getSelection();
                  var itemsToRemove = [];
                  for(var j=0;j<selectedNode.length;j++){
                    if(selectedNode[j].data.id){
                      itemsToRemove.push(selectedNode[j].data.id);
                      if( selectedNode[j].data.leaf == true){
                        selectedNode[j].remove();
                      }else{
                        if(selectedNode[j].data.type == 'folder'){
                          selectedNode[j].remove();
                        }
                      }
                    }
                    
                  }
                  basket.removeId(itemsToRemove);
                  store.sync();
                }
              }
            });
          } else {
            Ext.MessageBox.alert('Error','No selected files.');
          }
        }
      },{
        text : 'Reload basket',
        
        iconCls : 'button_refresh',
        handler : function() {
          store.reload();
        }
      }];
      
      if(_callbackFunction){
        var useFileButton =  {
          text : 'Use file(s)',
          handler : function() {
        	  getSelectedFilesForUsage();
          }
          
        };
        buttons.push(useFileButton);
      }
      return buttons;
    }
    
    var showFileInfo = function(record,frombutton){
      if (!record.get('dapurl')&&!record.get('catalogurl')) {
        if(record.get('leaf')==false){
          if(frombutton==true){
            alert("There is no information available for this entry");
          }
        }else{
          alert("There is no information available for this entry");
        }
        return;
      }
      if(record.get('dapurl')){
//        var fileViewer = new ExtFileViewer();
//        var w = Ext.create('Ext.Window', {
//          width : 900,
//          height : 680,
//          autoScroll : true,
//          autoDestroy : true,
//          closeAction : 'destroy',
//          frame : false,
//          title : 'NetCDF metadata',
//          layout : 'fit',
//          items : fileViewer.getViewer()
//        });
//        w.show();
//        openedWindows.push(w);
//        fileViewer.load(record.get('dapurl'));
        
        var el=jQuery('<div/>');
        renderFileViewerInterface({element:el,
          service:c4iconfigjs.impactservice,
          adagucservice:c4iconfigjs.adagucservice,
          adagucviewer:c4iconfigjs.adagucviewer,
          provenanceservice:c4iconfigjs.provenanceservice,
          //query:"http://opendap.knmi.nl/knmi/thredds/dodsC/CLIPC/jrc/tier2/SPI3.nc",
          query:record.get('dapurl'),
          dialog:true
        });   
      }else if(record.get('catalogurl')){
        var el = jQuery('<div></div>', {
            title: record.get("id"),
          }).dialog({
            width:900,
            height:500
          });
          el.html('<div class="ajaxloader"></div>');
        //var callback = function(data){
          
          renderCatalogBrowser({element:el,url:record.get('catalogurl')});
          //alert("Found "+data.numFiles+" files with totalsize of "+data.fileSize+" bytes");
        //}
        //window.open('/impactportal/data/catalogbrowser.jsp?catalog='+URLEncode(record.get('catalogurl')));
      }
    };
    
    tree = Ext.create('Ext.tree.Panel', {
      store : store,
      rootVisible : false,
      seArrows : true,
      multiSelect : true,
      singleExpand : false,
      border:false,
      columns : [
          {
            xtype : 'treecolumn', // this is so we know which column will show the
                                  // tree
            text : 'File',
            flex : 2,
            sortable : true,
            dataIndex : 'text'
          },{
            text : 'Type',
            width : 65,
            dataIndex : 'type',
            hidden:true
          },{
            text : 'DAP',
            width : 60,
            dataIndex : 'hasdap'
          },{
            text : 'HTTP',
            width : 60,
            dataIndex : 'hashttp'
          },{
            text : 'Filesize',
            width : 80,
            dataIndex : 'filesize'
          },{
            text : 'Date',
            width : 200,
            dataIndex : 'date',
            hidden:false
          }/*,{
            text : 'Info',
            width : 40,
            menuDisabled : true,
            xtype : 'actioncolumn',
            tooltip : 'Show file information',
            align : 'center',
            iconCls : 'button_info',
            handler : function(grid, rowIndex, colIndex, actionItem, event,record, row) {
              showFileInfo(record);
            }
          }*/],
      buttons : getButtons(),
      listeners:{
        itemdblclick:{
          fn:function(e,node,e){
        	if(_callbackFunction){
        		getSelectedFilesForUsage();
        	}else{
        		showFileInfo(node,false);
        	}
          }
        }
      }
    });
  }

  t.show = function(callbackFunction) {
	  
    _callbackFunction = callbackFunction;
    if (basketWindow == undefined) {
	  _init();
      basketWindow = Ext.create('Ext.Window', {
        width : 900,
        height : 550,
        autoScroll : true,
        autoDestroy : false,
        closeAction : 'hide',
        maximizable : true,
        frame : false,
        title : 'Basket',
        layout : 'fit',
        items : tree,
        listeners : {
          afterrender : {
            fn : function() {
              window.setTimeout(function(){store.load();;}, 300);    
              

            }
          }
        }
      });
    }
  
    basketWindow.show();
  };
  
  t.embed = function(element,callbackFunction){
    _callbackFunction = callbackFunction;
    _init();
    basketPanel = Ext.create('Ext.panel.Panel', {
      renderTo:element,
      autoScroll : true,
      autoDestroy : false,
      minHeight:480,
      frame : true,
      border:false,
      //title : 'Basket',
      layout : 'fit',
      items : tree,
      listeners : {
        afterrender : {
          fn : function() {
            store.load();
          }
        }
      }
    });
  }

  this.reload = function(){
    store.reload();
  };
};

var basketWidget = new BasketWidget();
