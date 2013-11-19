var impactWPSURL = '/impactportal/ImpactService?';

var wpsProcessorDetails = function(record) {
  var url = impactWPSURL + 'service=processor&request=describeProcessor&id='
      + record.get('id');

  var failFn = function() {
    alert("fail");
  };
  var passFn = function(e) {
    var json = Ext.JSON.decode(e.responseText);
    if (json.error) {
      alert("Error<br/>" + json.error);
      return;
    }
    alert("wpsProcessorDetails: " + e.responseText);
  }
  Ext.Ajax.request({
    url : url,
    success : passFn,
    failure : failFn,
    params : {}
  });

};

function columnWrap(val) {
  return '<div style="white-space:normal !important;">' + val + '</div>';
};

var selectedProcessor = undefined;

var selectProcessor = function() {
  if (selectedProcessor == undefined) {
    alert("Please select a processor");
    return;
  }
  ;
  window.location = "wpsuseprocessor.jsp?processor="
      + selectedProcessor.get('id');
  //alert(selectedProcessor.get('id'));
};

var wpsOverView = {
  xtype : 'container',
  layout : 'border',
  height : 500,
  items : [{
    region : 'center',
    xtype : 'grid',
    title : 'WPS Overview',
    store : new Ext.data.Store({
      autoLoad : true,
      fields : [{
        name : 'id',
        type : 'string'
      },{
        name : 'name',
        type : 'string'
      },{
        name : 'abstract',
        type : 'string'
      }],
      proxy : {
        type : 'ajax',
        url : impactWPSURL + 'service=processor&request=getProcessorList',
        reader : {
          type : 'json',
          root : 'processors'
        }
      }
    }),
    columns : [{
      text : "Identifier",
      flex : 1,
      dataIndex : 'id',
      sortable : true
    },{
      text : "Name",
      flex : 1,
      dataIndex : 'name',
      sortable : true
    },{
      text : "Abstract",
      flex : 2,
      dataIndex : 'abstract',
      sortable : true,
      renderer : columnWrap
    }

    ],
    height : 250,
    listeners : {
      itemclick : function(i, record) {
        this.itemClicked(i, record);
      },
      itemdblclick : function(i, record) {
        this.itemClicked(i, record);
        selectProcessor();
      }
    },
    itemClicked : function(i, record, item, index) {
      selectedProcessor = record;
    },
    buttons : [{
      xtype : 'button',
      text : 'use',
      handler : function() {
        selectProcessor();
      }
    }]
  }]
};

