var facetsNames=['activity','product','institute','model','experiment','frequency','realm','MIP_table','ensemble','variable','search_id','id','url','instance_id','dataSize'];
var categoryNames=['institute','model','experiment','frequency','realm','variable','project'];

var searchServiceURL='searchServiceURL not set';//'http://localhost:8081/ImpactPortal/ImpactService?';
var drupalServiceURL='drupalServiceURL not set';
var catalogBrowserURL='catalogBrowserURL not set';


var setSearchServiceURL=function(url){
  searchServiceURL=url;
};

var setDrupalServiceURL = function(url){
	drupalServiceURL=url;
};

var setCatalogBrowserURL = function(url){
	catalogBrowserURL = url;
};


/**
 * The timeout for JSON http calls.
 */
var timeout = 60000*4;

//Category results store for variable
var variableStore=new Ext.create('Ext.data.Store', { 
  fields: ['item','shortname','decription']
});

//Category results store for frequency
var frequencyStore=new Ext.create('Ext.data.Store', { 
  fields: ['frequency','decription']
});


//The data store containing the list of frequencies
//NOTE: this is the mapping from the names in the frequency box to the names used in the query
var frequencies = Ext.create('Ext.data.Store', {
  fields: ['freq', 'name'],
  data : [
          {"freq":"all", "name":"any"},
          {"freq":"subhr", "name":"sub hourly"},
          {"freq":"3hr", "name":"3 hourly"},
          {"freq":"6hr", "name":"6 hourly"},
          {"freq":"day", "name":"Daily"},
          {"freq":"mon", "name":"Monthly"},
          {"freq":"monClim", "name":"Climatological monthly mean"},
          {"freq":"yr", "name":"Annual"},
          // {"freq":"DC", "name":"Decadal"}, not there yet
          {"freq":"fx", "name":"Time independent"}
          ]
});       

//The data store containing the list of locations
var locations = Ext.create('Ext.data.Store', {
  fields: ['loc', 'location'],
  data : [
          {"loc":"GL", "location":"Global"},
          {"loc":"AZ", "location":"Asia"},
          {"loc":"EU", "location":"Europe"},
          {"loc":"AF", "location":"Africa"},
          {"loc":"NA", "location":"North America"},
          {"loc":"SA", "location":"South America"},
          {"loc":"AU", "location":"Australia"}
          ]
});



/**
 * Posts file or dataset identifiers to the shoppingbasket
 */
var postIdentifiersToBasket = function (jsonDataToPost){

  var passFn = function(e){
    var json= Ext.JSON.decode(e.responseText);
    if(json.error){
      alert(json.error);
      return;
    }
    
    adjustNumberOfDataSetsDisplayedInMenuBar(json);
    var numproductsadded = json.numproductsadded;
    var msgHTML='Added '+numproductsadded+' product(s) to your basket.';

    if(numproductsadded==0){
      var msgHTML='Product(s) already in your basket.';
    }
    itemAddedToolTip.update("<div style='padding:10px;'>"+msgHTML+"</div>");
    var XY=Ext.fly('baskettext1').getXY();
    //alert(XY);
    itemAddedToolTip.showPeriod(XY[0]+40,XY[1]+7);
    
  };

  var failFn = function(){ alert("fail");};
  Ext.Ajax.request({
    url: searchServiceURL+'service=basket&mode=add',
    success: passFn,   
    failure: failFn,
    timeout:timeout,
    params: { json:Ext.encode(jsonDataToPost) }  
  });
};

/**
 * Function which transfers search selection to the basket on the server
 */
var addSearchSelectionToBasket = function(){
  var numSelectedColumns = searchResultGrid.getSelectionModel().getCount();
  var selection = searchResultGrid.getSelectionModel().getSelection();
  var jsonDataToPost = [];
  var skipMessage="";
  for(var j=0;j<numSelectedColumns;j++){
    var el = {};

    var id=selection[j].get('id'); ;
    var url=selection[j].get('url');
    el['id']=id;
    el['catalogURL']=url;
    if(url==undefined||url=='None'){
      skipMessage+=(id+'\n');
    }else{
      jsonDataToPost.push(el);
    }
  }
  if(skipMessage.length>0){
    alert("The following data resource(s) have no URL and have been skipped:\n"+skipMessage);
  }
  postIdentifiersToBasket(jsonDataToPost);

};


/**
 * Function which browses a dataset
 */
var browseSearchSelection = function(){
  var numSelectedColumns = searchResultGrid.getSelectionModel().getCount();
  var selection = searchResultGrid.getSelectionModel().getSelection();
  if(numSelectedColumns!=1){
	  alert("Please select exactly one dataset");
	  return;
  }
  var url=selection[0].get('url');
  if(url){ 
	  window.location.href=catalogBrowserURL+"catalog="+url;
  }
};


/*var addSearchSelectionToBasket2 = function(){
  var numSelectedColumns = searchResultGrid.getSelectionModel().getCount();
  var selection = searchResultGrid.getSelectionModel().getSelection();
  var jsonDataToPost = [];
  for(var j=0;j<numSelectedColumns;j++){
    var el = {};
    el['id']=selection[j].get('id'); 
    el['url']=selection[j].get('url');
    jsonDataToPost.push(el);
  }

  var passFn = function(e){
  
    var json= Ext.JSON.decode(e.responseText);
    if(json.error){
      alert(json.error);
      return;
    }
    var numproducts = json.numproducts;
    var numproductsadded = json.numproductsadded;

    if(numproducts!=0){
      Ext.fly('baskettext').dom.innerHTML='('+numproducts+')';
    }else{
      Ext.fly('baskettext').dom.innerHTML ="";            
    }
   
  }; 

  var failFn = function(){ 
    alert("Failed ");
  };

  Ext.Ajax.request({
    url: searchServiceURL+'service=basket&mode=add',
    success: passFn,   
    failure: failFn,
    timeout:timeout,
    params: { json:Ext.encode(jsonDataToPost) }  
  });

};*/


var itemAddedToolTip = Ext.create('Ext.window.Window', {
  title: 'Hello',
  height: 10,
  width: 230,
  layout: 'fit',
  padding:'2px 2px 2px 2px',
  closable:true,
  header:false,
  listeners:{
    show:{
      fn:function(e){
       
      }
    }
  },
  showPeriod:function(x,y,a){
    var w=this;
    if(!this.runner){
      this.runner = new Ext.util.TaskRunner();
      this.task = this.runner.newTask({
        run: function () {
          w.hide();
        },
        repeat: 1,
        interval:1000
       });
    }

    this.task.stop();
    this.task.start();
    this.showAt(x,y,a);
  }
});
 

var HashSearch = new function () {
    var params;

    this.set = function (key, value) {
       params[key] = value;
       this.push();
    };

    this.remove = function (key, value) {
       delete params[key];
       this.push();
    };


    this.get = function (key, value) {
        return params[key];
    };

    this.keyExists = function (key) {
        return params.hasOwnProperty(key);
    };

    this.push= function () {
        var hashBuilder = [], key, value;

        for(key in params) if (params.hasOwnProperty(key)) {
            key = escape(key), value = escape(params[key]); // escape(undefined) == "undefined"
            hashBuilder.push(key + ( (value !== "undefined") ? '=' + value : "" ));
        }

        window.location.hash = hashBuilder.join("&");
    };

    (this.load = function () {
        params = {}
        var hashStr = window.location.hash, hashArray, keyVal
        hashStr = hashStr.substring(1, hashStr.length);
        hashArray = hashStr.split('&');

        for(var i = 0; i < hashArray.length; i++) {
            keyVal = hashArray[i].split('=');
            params[unescape(keyVal[0])] = (typeof keyVal[1] != "undefined") ? unescape(keyVal[1]) : keyVal[1];
        }
    })();
 };