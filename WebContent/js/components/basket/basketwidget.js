var BasketWidget = function() {
  var t = this;
  var basketWindow = undefined;
  var _callbackFunction;

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
      name : 'date',
      type : 'string'
    },{
      name : 'filesize',
      type : 'string'
    }]
  });

  var store = Ext.create('Ext.data.TreeStore', {
    model : 'basketgrid',
    root : {
      expanded : true,
      children : []
    }
  });

  var tree = Ext.create('Ext.tree.Panel', {
    store : store,
    rootVisible : false,
    seArrows : true,
    multiSelect : true,
    singleExpand : true,
    columns : [
        {
          xtype : 'treecolumn', // this is so we know which column will show the
                                // tree
          text : 'File Id',
          flex : 2,
          sortable : true,
          dataIndex : 'text'
        },
        {
          text : 'size',
          dataIndex : 'filesize'
        },
        {
          text : 'Info',
          width : 40,
          menuDisabled : true,
          xtype : 'actioncolumn',
          tooltip : 'Show file headers',
          align : 'center',
          iconCls : 'button_info',
          // icon: '../simple-tasks/resources/images/edit_task.png',
          handler : function(grid, rowIndex, colIndex, actionItem, event,
              record, row) {
            if (!record.get('dapurl')) {
              alert("There is no information available for this entry");
              return;
            }
            var fileViewer = new FileViewer();
            var w = Ext.create('Ext.Window', {
              width : 600,
              height : 400,
              autoScroll : true,
              autoDestroy : true,
              closeAction : 'destroy',
              frame : false,
              title : 'NetCDF header metadata',
              layout : 'fit',
              items : fileViewer.getViewer()
            });
            w.show();
            fileViewer.load(record.get('dapurl'));
            // _callbackFunction( record.get('dapurl'));
          }
        }],
    buttons : [{
      text : 'reload',
      handler : function() {
        basket.requestBasket(function(json) {
          tree.getRootNode().removeAll();
          tree.getRootNode().appendChild(json);
          tree.getRootNode().getChildAt(0).expand();
        });
      }
    },{
      text : 'use',
      handler : function() {
        if (tree.getSelectionModel().hasSelection()) {
          var selectedNode = tree.getSelectionModel().getSelection();

          // alert(selectedNode[0].data.dapurl + ' was selected');
          if (_callbackFunction) {
            var doClose = _callbackFunction(selectedNode);
            if (doClose === true) {
              basketWindow.close();
            }
          }
        } else {
          Ext.MessageBox.alert('No node selected!');
        }
      }

    }]
  });

  t.show = function(callbackFunction) {
    _callbackFunction = callbackFunction;
    if (basketWindow == undefined) {
      basketWindow = Ext.create('Ext.Window', {
        width : 800,
        height : 360,
        autoScroll : true,
        autoDestroy : false,
        closeAction : 'hide',
        maximizable : true,
        frame : false,
        title : 'Basket widget',
        layout : 'fit',
        items : tree,
        listeners : {
          afterrender : {
            fn : function() {
              // store.removeAll();

              basket.requestBasket(function(json) {
                tree.getRootNode().removeAll();
                tree.getRootNode().appendChild(json);
                tree.getRootNode().getChildAt(0).expand();
              });

              // tree.setLoading(false);

            }
          }
        }
      });
    }

    basketWindow.show();
  };

};

var basketWidget = new BasketWidget();
