var createExplorer = function() {
  var explorer = new Ext.create(
      'Ext.container.Container',
      {

        layout : 'border',
        height : 550,

        items : [{
          region : 'west',
          xtype : 'treepanel',
          title : 'Overview',
          multiSelect : true,
          itemId : 'catalog-listing-tree',
          bbar : [
              {
                xtype : 'button',
                text : 'Add current catalog',
                iconCls : 'icon-shoppingbasket',
                handler : function() {
                  var identifiers = [];
                  identifiers.push({
                    id : catalogURL,
                    catalogURL : catalogURL
                  });
                  postIdentifiersToBasket(identifiers);
                }
              },
              {
                xtype : 'tbseparator'
              },
              {
                xtype : 'button',
                text : 'Add file(s)',
                iconCls : 'icon-shoppingbasket',
                handler : function() {
                  var tree = explorer.getComponent('catalog-listing-tree');
                  var numSelectedColumns = tree.getSelectionModel().getCount();
                  var selection = tree.getSelectionModel().getSelection();
                  var identifiers = [];
                  var jsonDataToPost = [];
                  var skipMessage = "";
                  for (var j = 0; j < numSelectedColumns; j++) {
                    var el = {};
                    var id = selection[j].get('text');
                    var dapURL = selection[j].get('OPENDAP');
                    var httpURL = selection[j].get('HTTPServer');
                    el['id'] = id;
                    el['OPENDAP'] = dapURL;
                    el['HTTPServer'] = httpURL;
                    if (dapURL.length < 4)
                      dapURL = undefined;
                    if (httpURL.length < 4)
                      httpURL = undefined;

                    if (dapURL == undefined && httpURL == undefined) {
                      skipMessage += (id + '\n');
                    } else {
                      jsonDataToPost.push(el);
                    }
                  }
                  if (skipMessage.length > 0) {
                    alert("The following data resource(s) have no URL and have been skipped:\n"
                        + skipMessage);
                  }
                  postIdentifiersToBasket(jsonDataToPost);
                }
              },
              {
                xtype : 'tbseparator'
              },
              {
                xtype : 'button',
                text : 'View file header',
                iconCls : 'icon-view',
                handler : function() {
                  var tree = explorer.getComponent('catalog-listing-tree');
                  var numSelectedColumns = tree.getSelectionModel().getCount();
                  var selection = tree.getSelectionModel().getSelection();
                  var skipMessage = "";
                  if (numSelectedColumns != 1) {
                    alert("Please select one file to view (currently "
                        + numSelectedColumns + " files selected)");
                  }
                  var el = {};
                  var id = selection[0].get('text');
                  var dapURL = selection[0].get('OPENDAP');
                  var httpURL = selection[0].get('HTTPServer');
                  if (dapURL.length < 4)
                    dapURL = undefined;
                  if (httpURL.length < 4)
                    httpURL = undefined;
                  if (dapURL == undefined && httpURL == undefined) {
                    return;
                  }
                  if (dapURL == undefined || dapURL.length < 4)
                    dapURL = httpURL.replace("fileServer", "dodsC");
                  window.open(datasetViewerURL + dapURL, '_self', false);
                }
              }],
          store : new Ext.data.TreeStore({
            storeId : 'explorer-store',
            proxy : {
              type : 'ajax',
              url : serviceURL + 'service=catalogbrowser',
              listeners : {
                load : {
                  fn : function(t, aa, records, successful) {
                    alert("Unable to load catalog");
                  }
                }
              }
            },
            root : {
              text : 'catalog',
              id : catalogURL,
              expanded : true
            },
            folderSort : false,
            // sorters: [{property: 'text',direction: 'ASC'}],
            fields : [{
              name : 'id',
              type : 'string'
            },{
              name : 'text',
              type : 'string'
            },{
              name : 'leaf',
              type : 'bool'
            },{
              name : 'cls',
              type : 'string'
            },{
              name : 'OPENDAP',
              type : 'string'
            },{
              name : 'HTTPServer',
              type : 'string'
            }],
            listeners : {
              beforeload : {
                fn : function(t, aa, records, successful) {
                  var t = Ext.getCmp('ext_container');
                  if (t)
                    t.setLoading(true);
                }
              },
              load : {
                fn : function(t, aa, records, successful) {
                  var t = Ext.getCmp('ext_container');
                  t.setLoading(false);
                  if (records == '') {
                    alert("Unable to load catalog:" + t.error);
                  }
                }
              },
              exception : {
                fn : function(t, aa, records, successful) {
                  var t = Ext.getCmp('ext_container');
                  t.setLoading(false);
                  alert("Unable to load catalog");
                }
              }
            }
          }),
          hideHeaders : true,
          rootVisible : false,
          viewConfig : {
            plugins : [{
              ptype : 'treeviewdragdrop'
            }]
          },
          // width: 540,
          title : 'Thredds catalog listing',
          id : 'cataloglisting',
          collapsible : false,
          animate : false,
          listeners : {
            itemdblclick : function(i, record, item, index, e, options) {
              if (record.isLeaf() == true) {
                // this.loadDapURL(i.getStore().getAt(index).get('OPENDAP'));
                var dapURL = i.getStore().getAt(index).get('OPENDAP');
                var identifiers = [];

                var url = dapURL
                var id = url.substring(url.lastIndexOf("/") + 1);
                identifiers.push({
                  id : id,
                  url : url
                });
                postIdentifiersToBasket(identifiers);
              }
            },
            itemclick : function(i, record, item, index, e, options) {
              if (record.isLeaf() == true) {
                explorer.selectedItem = i.getStore().getAt(index)
                    .get('OPENDAP');
              }
            }
          },
          loadDapURL : function(OpenDAPURL) {
            var t = Ext.getCmp('ext_container');
            if (t)
              t.setLoading(true);
            var html = "Dataset <a href=\"/impactportal/data/datasetviewer.jsp?dataset="
                + OpenDAPURL + "\">" + OpenDAPURL + "</a><br/>";
            document.getElementById('datasetinfo').innerHTML = html;
            OpenDAPURL = OpenDAPURL.replace("https", "http");
            var store = Ext.data.StoreManager.lookup("variable-store");
            var proxy = store.getProxy();
            var getVarURL = serviceURL + 'service=getvariables&request='
                + OpenDAPURL;
            proxy.url = getVarURL;

            store.load();
          },
          split : true
        }// , variableBrowser
        ]
      });
  return explorer;
};
