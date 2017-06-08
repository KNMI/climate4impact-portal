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
		+ selectedProcessor.get('id') + "&service="+URLEncode(localWPS);
	//alert(selectedProcessor.get('id'));
};



var wpsOverView = function(){
	var states = [
		{"name":"Default climate4impact processing services",url:localWPS}
		];


	try{
		if(otherURLs){
			for(var j=0;j<otherURLs.length;j++){
				states.push(otherURLs[j])
			}
		}
	}catch(e){}

	// Define the model for a State
	Ext.regModel('WPSServices', {
		fields: [
			{type: 'string', name: 'name'},
			{type: 'string', name: 'url'}
			]
	});

	// The data store holding the states
	var store = Ext.create('Ext.data.Store', {
		model: 'WPSServices',
		data: states
	});

	// Simple ComboBox using the data store
	var simpleCombo = Ext.create('Ext.form.field.ComboBox', {
		fieldLabel: 'Select a WPS Service',
		valueField:'url',   
		displayField: 'name',
		value:localWPS,
		width: 500,
		labelWidth: 130,
		store: store,
		queryMode: 'local',
		typeAhead: true,
		forceSelection: true,
		listeners:{
			'select': function(combo,record){
				console.log("value selected" + combo.getValue());
				localWPS = combo.getValue();
				processingStore.load({url:impactWPSURL + 'service=processor&request=getProcessorList&wpsservice='+URLEncode(localWPS)})
				// processingStore.reload()
			}
		}
	});

	var processingStore = new Ext.data.Store({
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
			url : impactWPSURL + 'service=processor&request=getProcessorList&wpsservice='+URLEncode(localWPS),
			reader : {
				type : 'json',
				root : 'processors'
			}
		}
	});
	//simpleCombo.selectByValue('Local', true); 
	return {

		xtype : 'panel',
		frame:false,
		border:false,

		items:[simpleCombo,{

			xtype : 'panel',
			frame:false,
			border:false,
			layout : 'border',
			height :400,
			items : [{
				region : 'center',
				xtype : 'grid',
				frame:true,
				border:false,
				//title : 'Processing overview',
				store : processingStore,
				columns : [{
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
				},{
					text : "WPS identifier",
					flex : 1,
					dataIndex : 'id',
					sortable : true,
					hidden:true
				}

				],

				listeners : {
					itemclick : function(i, record) {
						this.itemClicked(i, record);
						selectProcessor();
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
		}]
	}

};