var defaultGridHeight=190-24-4;
var gridHeightExpanded = 470-24-15;

var searchServiceURL='searchServiceURL not set';  
var setSearchServiceURL=function(url){
  searchServiceURL=url;
};

// Generic error function in case the post has failed  
var failFn = function(){ 
  alert("Search failed");
  searchResultStore.fireEvent('load');
};

  
  var startSearchRequest = function(){

    if(!Ext.getCmp('parameter_id').getValue()&&Ext.getCmp('frequency_id').getValue()=='all'){
      alert('Please specify a variable');
      return;
    }
    
    //Clean current store
    searchResultStore.removeAll();
    
    // Data is returned upon succesful post
    var passFn = function(e){
      //TODO hier ergens een fireEvent('load') doen.... 
      var searchResults = Ext.JSON.decode(e.responseText);  
      if(searchResults.error){
        alert(searchResults.error);
        searchResultStore.fireEvent('load');
        return;
      }
      searchResultStore.loadData(searchResults.topics,false);
      searchResultStore.totalCount = searchResults.totalCount;
      searchResultStore.currentPage = searchResults.currentPage;
      //pagingToolbar.setVisible(true);
      searchResultGrid.expand();
      searchResultStore.fireEvent('load');
    } ;
    Ext.getCmp('basicSearchPanel').setHeight(gridHeightExpanded);
    searchResultGrid.expand();
    
    
    searchResultStore.fireEvent('beforeload');
    
    var impactQuery = searchServiceURL+'service=basicsearch&';
    
    var pageToLoad=searchResultStore.pageToLoad;
    if(!pageToLoad)pageToLoad=1;
    if(pageToLoad==0)pageToLoad=1;
    impactQuery+="mode=search&";
    impactQuery+="page="+pageToLoad+"&";
    impactQuery+="limit="+searchResultStore.pageSize;
    
    Ext.Ajax.request({
      url: impactQuery,
      method:'GET',
      success: passFn,   
      failure: failFn,
      timeout:timeout,
      params: { 
        'variable': Ext.getCmp('parameter_id').getValue(),
        'whenFrom': Ext.getCmp('when_from_id').getValue(),
        'whenTo': Ext.getCmp('when_to_id').getValue(),
        'frequency' : Ext.getCmp('frequency_id').getValue(),
        'where' : Ext.getCmp('where_id').getValue()
      }  
    });
  };


  //Search results store
  var searchResultStore=new Ext.create('Ext.data.Store', { 
    fields: facetsNames,
    pageSize:10
  });
  
  // paging toolbar
  var pagingToolbar = Ext.create('Ext.PagingToolbar', {
    listeners:{beforechange:{fn:function(a, pagenr) {
      searchResultStore.pageToLoad=pagenr;
      startSearchRequest();
      return false;
    }}},
    autoLoad: false,
    store: searchResultStore,
    displayInfo: true,
    displayMsg: 'Displaying results {0} - {1}. Found {2} results',
      buttons:[{xtype:'tbseparator'},{xtype:'button',iconCls:'icon-shoppingbasket',text:'Add to basket',handler:addSearchSelectionToBasket},{xtype:'button',iconCls:'icon-close',text:'Close',handler:function(){searchResultGrid.collapse();}}]
  }); 
  
  
  //pagingToolbar.setVisible(false);
  
  //Create the grid
  var searchResultGrid = Ext.create('Ext.grid.Panel', {
    height:290,
    collapsible: true,
    collapsed: true,
    bbar:  pagingToolbar,
    listeners:{
      collapse:{fn:function(a,options){
        Ext.getCmp('basicSearchPanel').setHeight(defaultGridHeight);           
      }}, 
      expand:{fn:function(a){
        var basicSearchPanel = Ext.getCmp('basicSearchPanel');
        basicSearchPanel.setHeight(gridHeightExpanded);
        basicSearchPanel.doLayout();
        basicSearchPanel.render();
        
      }}
    },
    store:   searchResultStore,
    fields: facetsNames,
    region:'center',
    
    columns:[
             {id: 'activity' ,text: "activity" ,dataIndex: 'activity'  ,width:60, hidden:true},
             {id: 'product'  ,text: "product"  ,dataIndex: 'product' ,width:60, hidden:true},
             {id: 'institute',text: "institute"  ,dataIndex: 'institute' ,width:100, hidden:true},
             {id: 'model'  ,text: "model"    ,dataIndex: 'model'   ,width:90, hidden:false},
             {id: 'experiment',text: "experiment",dataIndex: 'experiment',width:70, hidden:true},
             {id: 'frequency',text: "frequency"  ,dataIndex: 'frequency' ,width:60, hidden:true},
             {id: 'realm'  ,text: "realm"    ,dataIndex: 'realm'   ,width:40, hidden:true},
             {id: 'ensemble_member',text: "ensemble member",dataIndex: 'ensemble_member',width:100, hidden:true},
             {id: 'variable' ,text: "variable" ,dataIndex: 'variable'  ,width:100},
             {id: 'id' ,text: "id" ,dataIndex: 'id',flex:1  }
             
             ],
             multiSelect:true
  });
  
  // Create the Parameter box
  var parameterBox = new Ext.create('Ext.form.Panel', {
    title: 'Variable:',
    region:'west',
    width:240,
    layout:'fit',
    frame:true,
    bodyPadding:8,
 
    items: [ {
     
      xtype: 'combo',
     lazyRender:true, 
      queryMode: 'local',
      store: variableStore,
      valueField: 'item',
      displayField: 'shortname',
      tpl:'<tpl for="."><div data-qtip="{item}: {decription}" class="x-boundlist-item">{shortname}</div></tpl>',
      triggerAction:'all',
      typeAhead:true,
      //minChars:1, 
      //forceSelection:true,
      hideTrigger:true,
      name: 'basicSearchVariable',
      id:'parameter_id',
      hideLabel:true,
      allowBlank: true,
      emptyText: 'type a variable',
      enableKeyEvents:  true,
      disabled:true,
      listeners:{
        // Hook into the keypress event to detect if the user pressed the ENTER key
        // code snippet from: http://www.fusioncube.net/index.php?s=To+search,+type+and+hit+enter
        keypress: function(comboBox, e){
          if (e.getCharCode() == e.ENTER) {
            startSearchRequest();
          }
        }
      }
    }]
  });
  
  // Create the 'When' box 
  var whenBox = new Ext.create('Ext.form.Panel', { 
    title: 'When:',
    region:'center',   
    frame:true,
    layout: {type: 'table',columns:2},    
    items: [{         
      width:120,
      xtype: 'datefield',
      name: 'fromDate',
      id:'when_from_id',
      fieldLabel: 'from',
      hideLabel:true,
      emptyText:'from',
      allowBlank: true,
      margin:'8 5 0 5'
    },{
      width:120,
      xtype: 'datefield',
      name: 'toDate',
      id:'when_to_id',
      fieldLabel: 'to',
      emptyText:'to',
      hideLabel:true,
      allowBlank: true,
      margin:'8 5 0 5'
    }  
    ],
    bbar: [{xtype:'tbfill'},{xtype:'button',text:'clear',handler:function(){
        Ext.getCmp('when_from_id').setValue("");
        Ext.getCmp('when_to_id').setValue("");
    }}]
  });

  // Create the frequency box, 
  var frequencyBox = new Ext.create('Ext.form.ComboBox', {
    width:200, 
    foreceSelection: true,
    hideLabel:true,
    id: 'frequency_id',
    //frame:true,
    store: frequencies,
    queryMode: 'freq',
    displayField: 'name',
    valueField: 'freq',
    value:'all',
    emptyText: 'Select a sampling frequency'
  });
 
  // Create the 'Where' box      
  var whereBox = new Ext.create('Ext.form.ComboBox', {
    hideLabel:true,
    id: 'where_id',
    //frame:true,
    store: locations,
    queryMode: 'local',
    displayField: 'location', 
    valueField: 'loc',
    value:'GL',
    emptyText: 'Select a location'
  });

  var northPanel = Ext.create('Ext.form.Panel', {
    region:'north',
    height:92, 
    frame:false,
    border:false,
    layout:'border',
    items:[
           {
             xtype:'panel',
             region:'center',
             layout:'border',
             border:false,              
   
             items:[parameterBox,whenBox]
                     
           },
           {
             xtype:'panel',
             region:'east',
             width:380,
             //frame:false,
             
             border:false,
             layout:'border',
             items:[
               {
                 xtype:'panel', 
                 frame:true,
                 bodyPadding: 8,
                 region:'west',
                 title:'Sampling Frequency',
                 items:[frequencyBox]
               },
               {
                 xtype:'panel', 
                 frame:true,
                 layout:'fit',
                 bodyPadding: 8,
                 region:'center',
                 title:'Where',
                 items:[whereBox]
               }
             ]               
           }
         ]        
  });
  
  
  var getCategoriesRetriesFailed = 0;
  var getCategories = function(){
    var populateVars = function(e){
      Ext.getCmp('parameter_id').store.removeAll();
      Ext.getCmp('parameter_id').setValue(undefined);
      Ext.getCmp('parameter_id').enable();
      
      
      
      getCategoriesRetriesFailed=0;
      var categoryResults = Ext.JSON.decode(e.responseText);       
      if(categoryResults.facets==undefined){
        alert("Unable to get variable list from server:\n\n"+categoryResults.error);
        return;
      }
      variableStore.loadData(categoryResults.facets.variable,false);    

      if(searchSession){
        if(searchSession.variable){
          var cmb=Ext.getCmp('parameter_id');
          cmb.setValue(searchSession.variable);
          startSearchRequest();
        }
      }
    } ;
    var populateVarsFailed = function(){
      getCategoriesRetriesFailed++;
      if(getCategoriesRetriesFailed>3){
        alert("Unable to get categories from server");
        getCategoriesRetriesFailed=0; 
        return;
      }
      getCategories();
    };
    
    var parameterId = Ext.getCmp('parameter_id');
    
    parameterId.store.removeAll();
    parameterId.store.add({shortname:'loading data...'});
    parameterId.setValue(parameterId.store.getAt(0));
    parameterId.disable();
    
    
    Ext.Ajax.request({
      url: searchServiceURL+'service=basicsearch&mode=init',
      method:'GET',
      timeout:timeout,
      success: populateVars,   
      failure: populateVarsFailed
    });
  };


var searchPanel = Ext.create('Ext.form.Panel', {

  id: 'basicSearchPanel',
  listeners:{afterrender:{fn:function(a, options) {
    getCategories();
  }}},
  height:defaultGridHeight,
  frame:true,
  items:[northPanel,searchResultGrid ],
  buttons:[{xtype:'button',text:'Search',handler:startSearchRequest}]
});
  
 var initialize = function(){   


    if(searchSession!=undefined){
      var currentPage=searchSession.basicsearchpagenr;
      if(!currentPage)currentPage=1
    };
    searchResultStore.pageToLoad=currentPage;
    
    if(searchSession!=undefined){

      Ext.getCmp('frequency_id').setValue(searchSession.frequency);
      if(searchSession['from']){
        Ext.getCmp('when_from_id').setValue(fromISO8601(searchSession['from'].substring(0,10)));
      }
      if(searchSession['to']){
        Ext.getCmp('when_to_id').setValue(fromISO8601(searchSession['to'].substring(0,10)));
      }
      if(searchSession['where']){
        Ext.getCmp('where_id').setValue(searchSession['where']);
      }
      return;
    }  
 };
