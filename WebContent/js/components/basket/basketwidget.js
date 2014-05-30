var BasketWidget = function() {
  var t = this;
  var basketWindow = undefined;
  var basketPanel = undefined;
  var _callbackFunction;
  var initialized = false;
  var tree = undefined;
  var store = undefined;
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
                  generateLoginDialog(true);
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
  
    var getButtons = function(){
      var buttons =[{
        text : 'View/Browse file',
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
          if (tree.getSelectionModel().hasSelection()) {
            var selectedNode = tree.getSelectionModel().getSelection();
            
            if(selectedNode.length>1){
              Ext.MessageBox.alert('Error','Please select a single file with HTTP enabled to download.');
              return;
            }
            selectedNode = selectedNode[0];
            var httpURL = selectedNode.data.httpurl;
            if(!httpURL){
              Ext.MessageBox.alert('Error','Please select a file with HTTP enabled to download.');
              return;
            }
            if (downloadWin)
              downloadWin.close();
            downloadWin = window.open(httpURL, 'jav','width=900,height=600,resizable=yes');
          } else {
            Ext.MessageBox.alert('Error','No selected files.');
          }
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
                  for(var j=0;j<selectedNode.length;j++){
                    if(selectedNode[j].data.id){
                    basket.removeId(selectedNode[j].data.id);
                      if( selectedNode[j].data.leaf == true){
                        selectedNode[j].remove();
                      }else{
                        if(selectedNode[j].data.type == 'folder'){
                          selectedNode[j].remove();
                        }
                      }
                    }
                  }
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
            if (tree.getSelectionModel().hasSelection()) {
              var selectedNode = tree.getSelectionModel().getSelection();
    
              // alert(selectedNode[0].data.dapurl + ' was selected');
              if (_callbackFunction) {
                var doClose = _callbackFunction(selectedNode);
                if (doClose === true) {
                  basketWindow.close();
                }
              }else{
                Ext.MessageBox.alert('Error','Nothing to apply to.');
              }
            } else {
              Ext.MessageBox.alert('Error','No selected files.');
            }
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
        var fileViewer = new FileViewer();
        var w = Ext.create('Ext.Window', {
          width : 900,
          height : 500,
          autoScroll : true,
          autoDestroy : true,
          closeAction : 'destroy',
          frame : false,
          title : 'NetCDF metadata',
          layout : 'fit',
          items : fileViewer.getViewer()
        });
        w.show();
        fileViewer.load(record.get('dapurl'));
      }else if(record.get('catalogurl')){
        window.open('/impactportal/data/catalogbrowser.jsp?catalog='+URLEncode(record.get('catalogurl')));
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
            width : 100,
            dataIndex : 'date',
            hidden:true
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
            showFileInfo(node,false);
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
        items : tree
        ,
        listeners : {
          afterrender : {
            fn : function() {
              store.load();

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

};

var basketWidget = new BasketWidget();
