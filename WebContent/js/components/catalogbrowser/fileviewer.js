var downloadWin;
// var
// adagucviewservice='/impactportal/adagucviewer/adagucportal/index.jsp?srs=EPSG:4326&bbox=-180,-90,180,90&service=/impactportal/ImpactService%253Fsource%3D'+service+'&layer='+variable+'$image/png$true$auto/nearest$1$0&selected=0&baselayers=world_raster$nl_world_line&zoomtolayer=1'

var adagucwmservice = '/impactportal/ImpactService?source=';
var adagucviewservice = '/impactportal/adagucviewer/index.html?srs=EPSG:4326&bbox=-180,-90,180,90&service=/impactportal/ImpactService%253Fsource%3D';
var adagucviewer = '/impactportal/adagucviewer/';
var fileheaderservice = '/impactportal/ImpactService?service=getvariables&request=';

var thisSigninWindow;


var toggleAttributes = function(a) {

  var divWithAttributes = Ext.get(a + "-attr");
  var imgIcon = Ext.get(a + "-attricon");
  if (divWithAttributes.isVisible()) {
    divWithAttributes.setVisible(false);
    divWithAttributes.setHeight(0);
    imgIcon.replaceCls('attribute-icon-open', 'attribute-icon-closed')
  } else {
    divWithAttributes.setVisible(true);
    divWithAttributes.setHeight('auto');
    imgIcon.replaceCls('attribute-icon-closed', 'attribute-icon-open')
  }

};

var visualizeVariable = function(variable, service) {
  var w = window.open(adagucviewer + "#addlayer('" + adagucwmservice
      + URLEncode(service) + "','" + URLEncode(variable) + "')",
      'adagucportal', '');
  w.focus();
  return false;
};

var refreshFileViewer = function(id){
  //console.log("File viewer needs to be refreshed"+id);
  //console.log(id);
  id.reload();
 
};

var ExtFileViewer = function() {
  var _this = this;
  thisSigninWindow = this;
  var fileLocation = "";

  var fileStore = new Ext.data.Store({
    proxy : {
      type : 'ajax',
      reader : {
        type : 'json',
        timeout : 600000
      }
    },
    fields : [{
      name : 'variable',
      type : 'string'
    },{
      name : 'longname',
      type : 'string'
    },{
      name : 'variabletype',
      type : 'string'
    },{
      name : 'service',
      type : 'string'
    },{
      name : 'error',
      type : 'string'
    },{
      name : 'attributes',
      type : 'object'
    },{
      name : 'dimensions',
      type : 'object'
    },{
      name : 'isDimension',
      type : 'object'
    },{
      name : 'isViewable',
      type : 'object'
    }]
  // , autoLoad: true
  });

  var fileViewerPanel = Ext.create('Ext.panel.Panel', {
	  //title : 'NetCDF metadata',
	  layout:'border',
	  border:false,
	  frame:false,
	  bodyStyle : "padding:00px;background:#DEEBFF;background-color:#DEEBFF",
	  items:[{
		xtype : 'panel',
		border:false,frame:false,
		cls : 'variable-results',
		region : 'north',
		overflowY:'auto',
		itemId:'variableheader',
		padding:'5px',
		margin:'5px',
		height:50,
		bodyStyle : "padding:00px;background:#DEEBFF;background-color:#DEEBFF"
		
	  },{
		
	    xtype : 'panel',
	    cls : 'variable-results',
	    region : 'center',
	    overflowY:'auto',
	    border:false,frame:false,
	    
	    style: 'border-top: 1px solid #BBBBBB;border-bottom: 1px solid #BBBBBB;',
	   // title : 'NetCDF metadata',
	    bbar : [
	        {
	          xtype : 'button',
	          text : 'Download',
	          iconCls : 'icon-download',
	          handler : function() {
	            var request = _this.getFileLocation();
	            if (request.indexOf('aggregation') > 0) {
	              alert('Aggregations cannot be downloaded directly.');
	              return;
	            }
	            var downloadURL = request.replace('dodsC', 'fileServer');
	            try{
  	            if(openid){
  	              if(openid!=""){
  	                downloadURL+="?openid="+openid;
  	              }
  	            }
	            }catch(e){
	            }

	            if (downloadWin)
	              downloadWin.close();
	            downloadWin = window.open(downloadURL, 'jav',
	                'width=900,height=600,resizable=yes');
	          }
	        },{
	          xtype : 'tbseparator'
	        },{
	          xtype : 'button',
	          text : 'Add to basket',
	          iconCls : 'icon-shoppingbasket',
	          handler : function() {
	          
	            var url = _this.getFileLocation();
	            var id = url.substring(url.lastIndexOf("/") + 1);
	     
	            basket.postIdentifiersToBasket({
	              id : id,
	              OPENDAP : url
	            });
	          }
	        },{
            xtype : 'button',
            text : 'Reload',
            handler : function() {_this.reload();}
	        }],

	    layout : 'fit',
	    items : [{
	  
	     
	      xtype : 'dataview',
	
	      tpl : new Ext.XTemplate(
	          '<tpl for=".">',
	          '<tpl if="error">',
	
	          '<div class="variable-error">{error}<br/>You can try the following:<ul>',
	          '<li>Click directly on the link above, you will get a hint on what is going wrong.</li>',
	          '<li>If you are not signed in, <a href="#" onclick="generateLoginDialog(function(){refreshFileViewer(thisSigninWindow);})">sign in</a>',
	          ' and <a onclick=\'location.reload();\'>refresh this page</a>.</li>',
	          '<li>If you are signed in but still cannot view the data, make sure your account is registered to the right group: <a target="_blank" href="/impactportal/help/howto.jsp?q=create_esgf_account">-> HowTo</a>.</li>',
	          '<li><a onclick="window.open(\'/impactportal/help/contactexpert.jsp\',\'targetWindow\',\'toolbar=no,location=no,status=no,menubar=no,scrollbars=yes,resizable=yes,width=1020,height=800\')" >Request help</a>.</li>',
	          '</ul></div>',
	          '</tpl>',
	          '<tpl if="variable">',
	          '<div class="variable-item">',
	          '<a onclick=toggleAttributes(\'{variable}\');><div id=\'{variable}-attricon\' class="attribute-icon-closed"></div></a><b>{variable}</b> - <i>{longname}</i> - (<tpl for="dimensions">{name}</tpl>)',
	          '<tpl for="isDimension"> dimension of length {length}. </tpl>',
	          '<tpl if="isViewable"> - ',
	          // '<a
	          // onclick={fileViewerPanel.fireEvent("visualize","{variable}","{service}");}>visualize</a>',
	          '<a onclick={visualizeVariable("{variable}","{service}");}>visualize</a>',
	          // ' - <a target="_blank"
	          // href='+adagucwmservice+'{service}&service=WCS&request=getcoverage&format=AAIGRID&coverage={variable}>get</a>',
	          '</tpl>', '<div id=\'{variable}-attr\' class="variable-attributes">',
	          '<ul><tpl for="attributes">', '<li>{name}: {value}</li>',
	          '</tpl></ul>', '</div>', '</div></tpl></tpl>',{
	            reload:function(){
	              console.log("reload in template");
	            }
	          }),
	
	      store : fileStore,
	      itemSelector : 'div.variable-item'
	
	    }]
	  }]
  });
  this.reload = function(){
    this.load (fileLocation);
  }
  this.load = function(_filelocation) {
    fileLocation = _filelocation;
    filelocationwithopenid = fileLocation;
    try{
      if(openid){
        filelocationwithopenid+="?openid="+openid;
      }
    }catch(e){}
    fileStore.fireEvent('load');
    var proxy = fileStore.getProxy();
    proxy.url = fileheaderservice + URLEncode(fileLocation);
    fileViewerPanel.getComponent('variableheader').update('File location: <a target="_blank" href="'+filelocationwithopenid+'">'+_filelocation+'</a>');
    fileStore.load();
  };

  this.getViewer = function() {
    return fileViewerPanel;
  };

  this.getFileLocation = function() {
    return fileLocation;
  };
};