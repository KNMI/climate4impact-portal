var comboBoxComponents = [];

var callbackError = function(error){
	setErrorForComponents();
	alert("callbackError \n"+error);
	
};

var callbackDrupalTips = function(data){
	Ext.fly('contexthelp').dom.innerHTML=data;
};

Ext.regModel('ComboStoreBox', {
  fields: [
      {type: 'string', name: 'item'},
//      {type: 'string', name: 'shortname'},
      {type: 'string', name: 'description'}
  ]
});

/**
 * Constructor for custom comboboxes, which have a state which is remembered in JSP session
 */
ComboStoreBox = Ext.extend(Ext.form.field.ComboBox, {
  initComponent: function() {
    var _this = this; 
    this.applyState=function(state){
      setSessionProperty(this.id,state.selectedIndex);
       _this.stateValue=state.selectedIndex;
    };
    this.label=this.fieldLabel; 
    Ext.apply(this, {
      typeAhead: true,triggerAction: 'all',lazyRender:true, queryMode: 'local',
      store:  new Ext.create('Ext.data.Store', {model:'ComboStoreBox'}),
      valueField: 'item',displayField: 'item', 
      tpl:'<tpl for="."><div data-qtip="{description}{nick}" class="x-boundlist-item">{item}</div></tpl>',
      emptyText:'loading...',
      stateId:_this.id,
      labelAlign:'top',
      stateful:false,
      stateEvents: ['select','change'],
      allowBlank :false,
      disabled:true,
      width: 200,
      padding:4,
      listeners:{
    	  afterrender:{fn:function(combo, value,nr) {
    		  Ext.create('Ext.tip.ToolTip', {
		        target: _this.id,
		        width: 300,
		        title: '',
		        text: '',
		        dismissDelay: 0,
		        showDelay:300,
		        listeners:{
		      	  beforeshow:{fn:function(a, value,nr) {
		      		  if(_this.drupaltip){
		      		    makeHTTPRequest(drupalServiceURL+'q='+_this.drupaltip,callbackDrupalTips,callbackError);
		      		  }
		      		 return false;
		      	  }}}
    		  });
    		  
    	  }},
	      select:{fn:function(a, value,nr) {
	        _this.stateValue = _this.getValue();
	        searchResultStore.pageToLoad=1;
	        startSearchRequest(false);
	      }
    }}
    });
    ComboStoreBox.superclass.initComponent.apply(this, arguments);
  }
});


/**
 * Callback function from a JSON request which populates the values in the comboboxes.
 * This function is also called by populateSearchResults, for facetted update.
 */
var populateComboBoxes = function(data,forceData){
  if(!data){
    enableComponents();
    alert('unable to populateComboBoxes: no data returned from service\n\n');
    return;
  }
  if(data.error){ 
    enableComponents();
    alert('unable to populateComboBoxes:\n\n'+data.error);
    return;
  }
  try{
    var remotecategories=data;
    if(remotecategories.facets){
      remotecategories=remotecategories.facets;
    }
    // Remotecategories is a jsonobject with certain categories filled. The categories of the jsonobject match the categories in the comboboxes in certain cases
    // Here we will match the remote categories to the combobox category and fill its value accordingly 
    for(var cmbIndex in comboBoxComponents){
      var comboBox=comboBoxComponents[cmbIndex];
      if(comboBox){
        var value = remotecategories[comboBox.category];
        
        if(value){
          comboBox.store.removeAll();
          comboBox.store.add({item:'all',description:'all'});
          if(!comboBox.getValue()||comboBox.getValue()=='reset'){
            comboBox.stateValue = undefined;
            
            comboBox.setValue(comboBox.store.getAt(0));
            comboBox.store.removeAll();      
          }
          if(comboBox.getValue()=='all'){
            comboBox.stateValue = undefined;
          }
          comboBox.store.loadData(value,true);
          comboBox.labelEl.update(comboBox.label+": ("+(value.length)+")"); 
        }
        
        if(comboBox.stateValue){
          if(comboBox.stateValue!='all'&&comboBox.stateValue!='reset'){
            if(comboBox.getValue()){
              if(comboBox.getValue()!=comboBox.stateValue){
                comboBox.setValue(comboBox.stateValue);
                comboBox.setRawValue(comboBox.stateValue);
              }
            }
          }
        }
      }
    }
  }catch(e){
    alert("code: "+e);
  }  
  enableComponents();
};

/**
 * When a search is started, all components are disabled until results return.
 */
var disableComponents = function(includeSearchResults){
  for(var j in comboBoxComponents)comboBoxComponents[j].disable();
  Ext.getCmp('when_from_id').disable();
  Ext.getCmp('when_to_id').disable();
  Ext.getCmp('where_id').disable();
  if(includeSearchResults){
   pagingToolbar.disable();
   searchResultStore.fireEvent('beforeload');
  }
};

/** 
 * Enable all seach components when a call is finished 
 */
var enableComponents = function(){
  for(var j in comboBoxComponents)comboBoxComponents[j].enable();
  Ext.getCmp('when_from_id').enable();
  Ext.getCmp('when_to_id').enable();
  Ext.getCmp('where_id').enable();

  pagingToolbar.enable();
  searchResultStore.fireEvent('load');
};

/**
 * Function is calles when an error has occured
 */
var setErrorForComponents = function(){
  for(var j in comboBoxComponents){
	 var comboBox=comboBoxComponents[j];
     comboBox.store.removeAll();
     comboBox.store.add({item:'all',description:'An error occured'});
     comboBox.setValue(comboBox.store.getAt(0));
     comboBox.stateValue = undefined;
  }
  Ext.getCmp('when_from_id').enable();
  Ext.getCmp('when_to_id').enable();
  Ext.getCmp('where_id').enable();
  pagingToolbar.enable();
	  searchResultStore.fireEvent('load');
};

/**
 * Callback called by JSON search request from method startSearchRequest()
 */
var populateSearchResults = function(data){
  var searchResults=data;
  if(searchResults.topics){
    searchResultStore.loadData(searchResults.topics,false);
    searchResultStore.totalCount=searchResults.totalCount;
    searchResultStore.currentPage=searchResults.currentPage;
    Ext.util.Cookies.set('searchResultPage',searchResultStore.currentPage);
  }else{
   
    searchResultStore.removeAll();
    enableComponents();
    
    try{
      if(data.error){
        alert(data.error);
      }
    }
    catch(e){
      alert("No results");
    }
    
  }  
  if(data.facets){
    populateComboBoxes(data.facets,true);
  }

};


/**
 * Main function to start a search query, calls populatesearchResults as ajax callback
 */
var startSearchRequest = function(includeSearchResults){
  includeSearchResults = true;
  var vercQuery="search?";
  
  var whenTo = Ext.getCmp('when_to_id').getValue();
  var whenFrom = Ext.getCmp('when_from_id').getValue();

  if(whenTo&&whenFrom){

	  vercQuery+='tc_start='+URLEncode(""+toISO8601YYYYMMHH(whenFrom));
  	vercQuery+='&tc_end='+URLEncode(""+toISO8601YYYYMMHH(whenTo));
  	vercQuery+='&';
  }

  var nrOfCategoriesSelected = 0;
  var resetHasBeenRequested=false;
  for(var j in comboBoxComponents){
    var cmb = comboBoxComponents[j];
    if(cmb){ 
      var comboValue = cmb.getValue();
      if(comboValue){
        if(comboValue!='all'&&comboValue!='any'&&comboValue!='reset'){
          vercQuery+=cmb.category+"="+comboValue+"&";
          nrOfCategoriesSelected++;
        }else{
          if(comboValue=='reset'){
            resetHasBeenRequested=true;
            cmb.store.removeAll();
            cmb.stateValue = undefined;
            cmb.store.add({item:'all',description:'all'});
            cmb.setValue(cmb.store.getAt(0));
          }
        }
      }else{
        resetHasBeenRequested=true;
      }
    }
  }  

  

  disableComponents(includeSearchResults);
  
  if(nrOfCategoriesSelected==0){
    
    searchResultStore.removeAll();
    if(resetHasBeenRequested){
      makeJSONRequest(searchServiceURL+'service=search&mode=distinct',populateComboBoxes,callbackError);
    }else{
      enableComponents();
    }
    return;
  }
  
  if(nrOfCategoriesSelected==0){
    if(includeSearchResults){
      alert("Please select a category.");
      includeSearchResults=false;
    }
  }
  
 
  disableComponents(includeSearchResults);
    //vercQuery+="datatype=file&";
    //searchResultStore.searchQuery=URLEncode(vercQuery);
  var impactQuery=searchServiceURL;
  if(includeSearchResults){
    impactQuery+='service=search&mode=search&query=';
  }else{
    impactQuery+='service=search&mode=distinct&query=';
  }
  impactQuery+=URLEncode(vercQuery)+"&";
  impactQuery+='where='+URLEncode(Ext.getCmp('where_id').getValue());  
  impactQuery+='&';
   //Add page to load
  var pageToLoad=searchResultStore.pageToLoad;
  if(!pageToLoad)pageToLoad=1;

  
  impactQuery+="page="+pageToLoad+"&";
  impactQuery+="limit="+searchResultStore.pageSize+"&";
  impactQuery+="includefacets="+true;
  
  //searchResultStore.fireEvent('beforeload');

  if(includeSearchResults){
    makeJSONRequest(impactQuery,populateSearchResults,callbackError);
  }else{
    searchResultStore.removeAll();
    makeJSONRequest(impactQuery,populateComboBoxes,callbackError);
  }

};


 
 var resetAllComboBoxes = function(){
   disableComponents(false);
   searchResultStore.removeAll();
   for(var j in comboBoxComponents){
     var comboBox=comboBoxComponents[j];
     comboBox.stateValue=undefined;
     comboBox.store.removeAll();
   	 comboBox.store.add({item:'all',description:'all'});
     comboBox.setValue(comboBox.store.getAt(0));
   }
   
   Ext.getCmp('when_from_id').setValue("");
   Ext.getCmp('when_to_id').setValue("");
   
  // makeJSONRequest(searchServiceURL+'service=search&mode=distinct',populateComboBoxes,callbackError);
   makeJSONRequest(searchServiceURL+'service=search&mode=distinct',populateComboBoxes,callbackError);
 };


var searchResultStore=new Ext.create('Ext.data.Store', { 
  fields: facetsNames,
  pageSize:100
});

var pagingToolbar = Ext.create('Ext.PagingToolbar', {
   listeners:{beforechange:{fn:function(a, pagenr) {
     searchResultStore.pageToLoad=pagenr;
     startSearchRequest(true);
        return false;
    }}},
    autoLoad: false,
    store: searchResultStore,
    displayInfo: true,
    displayMsg: 'Displaying results {0} - {1}. Found {2} results'
    	//,    buttons:[{xtype:'tbseparator'},{xtype:'button',iconCls:'icon-shoppingbasket',text:'Add to basket',handler:addSearchSelectionToBasket}]
});



 DateFieldStateFul = Ext.extend(Ext.form.field.Date, {
	  initComponent: function() {
	    var _this = this; 
	    this.applyState=function(state){
	       _this.stateValue=state.selectedIndex;
	       _this.setValue(_this.stateValue);
	    };

	    Ext.apply(this, {
	      stateId:_this.id,
	      stateful:false,
	      stateEvents: ['select','change'],
	      hideLabel:true,
	      allowBlank: true,
	      listeners:{
	        select:{fn:function(a, value,nr) {
	          _this.stateValue = _this.getValue();
	          searchResultStore.pageToLoad=1;
	          startSearchRequest(false);
	        }
	      }}
	    });
	    DateFieldStateFul.superclass.initComponent.apply(this, arguments);
	  }
	});

// Create the 'When' box 
var whenBox = new Ext.create('Ext.form.Panel', { 
  //title: 'When:',
  region:'center', 
  border:'1 0 0 0',
  frame:true,
  layout: {type: 'table',columns:5},    
  items: [new DateFieldStateFul({         
    width:140,
    name: 'fromDate',
    id:'when_from_id',
    fieldLabel: 'from',
    hideLabel:true,
    emptyText:'from (mm/dd/yyyy)',
    allowBlank: true,
    margin:'3 5 0 5'

  }),new DateFieldStateFul({
    width:140,
    name: 'toDate',
    id:'when_to_id',
    fieldLabel: 'to',
    emptyText:'to (mm/dd/yyyy)',
    hideLabel:true,
    allowBlank: true,
    margin:'3 5 0 5'
  }),{
    xtype:'button',text:'clear',margin:'3 5 0 5',handler:function(){
      Ext.getCmp('when_from_id').setValue("");
      Ext.getCmp('when_to_id').setValue("");
      searchResultStore.pageToLoad=1;
      startSearchRequest(false);
      
    }},
    {
      xtype:'combo',
      margin:'3 5 0 148',
      hideLabel:true,
      id: 'where_id',
      store: locations,
      queryMode: 'local',
      displayField: 'location', 
      valueField: 'loc',
      value:'GL',
      emptyText: 'Select a location',
      listeners:{
    	  select:{
    		  fn:function(a){
    			  searchResultStore.pageToLoad=1;
    			  startSearchRequest(false);
    		  }
    	  }
      }
    }/*,{
      xtype:'button',text:'Search',margin:'3 5 0 5',handler:function(){
        startSearchRequest();
        
      }}*/
    
    ]
});

var comboPanel = Ext.create('Ext.form.Panel', {
  frame: true,border:false,
  /*bodyPadding: 4,*/
  autoScroll: false,
  layout: {type: 'table',columns: 3},
  region: 'north',
  items: [
      new ComboStoreBox({fieldLabel:'Variable'  ,id:'ComboStoreBox_variable'  ,drupaltip:'searchcontext_variable',category:'variable'}),
      new ComboStoreBox({fieldLabel:'Frequency' ,id:'ComboStoreBox_frequency' ,drupaltip:'searchcontext_frequency',category:'time_frequency'}),
	  new ComboStoreBox({fieldLabel:'Institute' ,id:'ComboStoreBox_institute' ,drupaltip:'searchcontext_institutes',category:'institute'}),
	  new ComboStoreBox({fieldLabel:'Experiment',id:'ComboStoreBox_experiment',drupaltip:'searchcontext_experiment',category:'experiment'}),
	  new ComboStoreBox({fieldLabel:'Model'     ,id:'ComboStoreBox_model'     ,drupaltip:'searchcontext_model',category:'model'}),
	  new ComboStoreBox({fieldLabel:'Realm'     ,id:'ComboStoreBox_realm'     ,drupaltip:'searchcontext_realm',category:'realm'})  ,                           
      new ComboStoreBox({fieldLabel:'Project'     ,id:'ComboStoreBox_project'     ,drupaltip:'searchcontext_project',category:'project'})
	]
});



var searchResultGrid = Ext.create('Ext.grid.Panel', {
  store:   searchResultStore,
  fields: facetsNames,
  region:'center',
  columns:[
   {id: 'id'  ,text: "dataset id" ,dataIndex: 'id'  ,width:360,hidden:false},
   /* {id: 'activity'	,text: "activity"	,dataIndex: 'activity'	,width:60,hidden:true},
   // {id: 'product'	,text: "product"	,dataIndex: 'product'	,width:60, hidden:true},
    {id: 'institute',text: "Institute"	,dataIndex: 'institute'	,width:100,sortable:false},
    {id: 'model'	,text: "Model"		,dataIndex: 'model'		,width:100,sortable:false},
    {id: 'experiment',text: "Experiment",dataIndex: 'experiment',width:80,sortable:false},
    {id: 'time_frequency',text: "Freq."	,dataIndex: 'time_frequency'	,width:45,sortable:false},
    {id: 'realm'	,text: "Realm"		,dataIndex: 'realm'		,width:45,sortable:false},*/
    {id: 'ensemble',text: "Ensemble",dataIndex: 'ensemble',width:60,sortable:true},
    {id: 'dataSize',text: "Size",dataIndex: 'dataSize',width:60,sortable:true},
    {id: 'variable'	,text: "Variables"	,dataIndex: 'variable'	,flex:1,sortable:true}
  ],
  multiSelect:true
});



var searchPanel = Ext.create('Ext.form.Panel', {
  frame: true,
  //height:492,
  height:(492+27+20+34+30), 
  autoScroll:false,
  title: 'Search ESG nodes',
  layout:'border', 
  defaults:{ autoScroll:false }, 
   items: [ 
            { 
              height:260,
              xtype:'panel',
              layout:'border',
              region:'north',
              border:true,frame:true,
              items:[comboPanel,  whenBox],
              buttons:[               
                    {height:30,xtype:'button',text:'Browse',handler:browseSearchSelection},		
                    {height:30,xtype:'button',iconCls:'icon-shoppingbasket',text:'Add to basket',handler:addSearchSelectionToBasket},    
                    {height:30,iconAlign: 'left', scale: 'medium',xtype:'button',text:'Clear',handler:resetAllComboBoxes},
                    {height:30,iconAlign: 'left', scale: 'medium',iconCls:'icon-search24',xtype:'button',text:'Search',handler:function(){startSearchRequest(true);}}
                    ] 
            },searchResultGrid
             ],bbar:  pagingToolbar
});

/**
 * Initialization function which needs to be called for starting the search interface
 */
var initialize=function(searchSession){
	

  Ext.QuickTips.init();
  //alert("INITIALIZED"+searchSession);
  //Populate categories comboboxes 
  if(searchSession!=undefined){
    var currentPage=searchSession.advancedsearchpagenr;
    if(!currentPage)currentPage=1
  };
  
  searchResultStore.pageToLoad=currentPage;

  // Create a quick lookup to all combobox  components
  for(var categoryIndex in categoryNames) {comboBoxComponents.push(Ext.getCmp('ComboStoreBox_'+categoryNames[categoryIndex]));}
  searchResultStore.fireEvent('beforeload');

  for(var j in comboBoxComponents){
    var cmb = comboBoxComponents[j];
    if(cmb){
      if(searchSession!=undefined){
        var sessionValue=searchSession[cmb.category]
        if(sessionValue){
          if(sessionValue!='all'){
            cmb.store.add({item:sessionValue,description:sessionValue});
            cmb.setValue(cmb.store.getAt(0));
          }
        }
      }else{
    	  if(cmb.category=='realm'){
    		   cmb.store.add({item:'atmos',description:'atmosphere'});
    	       cmb.setValue(cmb.store.getAt(0));
    	  }
      }
    }
  }
  
  if(searchSession!=undefined){
    if(searchSession['from']){
      Ext.getCmp('when_from_id').setValue(fromISO8601(searchSession['from'].substring(0,10)));
    }
    if(searchSession['to']){
      Ext.getCmp('when_to_id').setValue(fromISO8601(searchSession['to'].substring(0,10)));
    }
    if(searchSession['where']){
      Ext.getCmp('where_id').setValue(searchSession['where']);
    }


  }
  
  startSearchRequest(false);

  Ext.QuickTips.init();
};


