var downloadWin;
//var adagucviewservice='/impactportal/adagucviewer/adagucportal/index.jsp?srs=EPSG:4326&bbox=-180,-90,180,90&service=/impactportal/ImpactService%253Fsource%3D'+service+'&layer='+variable+'$image/png$true$auto/nearest$1$0&selected=0&baselayers=world_raster$nl_world_line&zoomtolayer=1'

var adagucwmservice='/impactportal/ImpactService?source=';
var adagucviewservice='/impactportal/adagucviewer/index.html?srs=EPSG:4326&bbox=-180,-90,180,90&service=/impactportal/ImpactService%253Fsource%3D';
var adagucviewer='/impactportal/adagucviewer/';
var toggleAttributes = function(a){
	
	var divWithAttributes=Ext.get(a+"-attr");
	var imgIcon=Ext.get(a+"-attricon");
	if(divWithAttributes.isVisible()){
		divWithAttributes.setVisible(false);
		divWithAttributes.setHeight(0);
		imgIcon.replaceCls('attribute-icon-open','attribute-icon-closed')
	}else{
		divWithAttributes.setVisible(true);
		divWithAttributes.setHeight('auto');
		imgIcon.replaceCls('attribute-icon-closed','attribute-icon-open')
	}
	
};

var variableBrowser={
	
		getURL:function(){return Ext.data.StoreManager.lookup("variable-store").getProxy().url;},
		xtype:'panel',
	
		region:'center',
        title: 'Available variables',
        id: 'variable-results',
        bbar:[{xtype:'button',text:'Download',iconCls:'icon-download',handler:function(){
            var proxyURL=variableBrowser.getURL();
            var urlParams=Ext.urlDecode(proxyURL.split('?')[1]);
            if(urlParams.request.indexOf('aggregation')>0){
            	alert('Aggregations cannot be downloaded directly.');
            	return;
            }
            var downloadURL = urlParams.request.replace('dodsC','fileServer');
            if(downloadWin)downloadWin.close();
            downloadWin = window.open(downloadURL, 'jav', 'width=600,height=300,resizable=yes');
        	}},,{xtype:'tbseparator'},{
        		xtype:'button',text:'Add to basket',iconCls:'icon-shoppingbasket',handler:function(){
        			var identifiers = [];
        			var proxyURL=variableBrowser.getURL();
                    var urlParams=Ext.urlDecode(proxyURL.split('?')[1]);
                    var url = urlParams.request;
                    var id = url.substring(url.lastIndexOf("/")+1);
        			identifiers.push({id:id,OPENDAP:url});
        			postIdentifiersToBasket(identifiers);
        		}
        	}],
        frame:false,
        listeners: {
        	visualise:{
    			fn: function(variable,service){ 
    				
    				 var w=window.open(adagucviewer+"#addlayer('"+adagucwmservice+URLEncode(service)+"','"+URLEncode(variable)+"')", 'adagucportal', ''); w.focus(); return false;
    				/*
	 				Ext.create('Ext.window.Window', {
	 				    title: variable,
	 				    height: 600,
	 				    width: 1020,
	 				    layout: 'fit',
	 				    items:[Ext.create('Ext.ux.SimpleIFrame', {
	 					   border: false,
	 					   src: adagucviewservice+service+'&layer='+variable+'$image/png$true$default$1$0&selected=0&baselayers=world_raster$nl_world_line&zoomtolayer=1'
	 					 })]
	 				}).show();*/
	 			}
			}
		},
        layout: 'fit',
        items: [{
            autoScroll: true,
            minHeight:300,
            xtype: 'dataview',
            id:'variable-viewer',
            tpl: new Ext.XTemplate(
		            '<tpl for=".">',
		            	'<tpl if="error">',
		            	'<b>{error}</b>',
		            	'</tpl>',
		            	'<tpl if="variable">',
		                '<div class="variable-item">',
		                '<a onclick=toggleAttributes(\'{variable}\');><div id=\'{variable}-attricon\' class="attribute-icon-closed"></div></a><b>{variable}</b> - <i>{longname}</i> - (<tpl for="dimensions">{name}</tpl>)',
		                '<tpl for="isDimension"> dimension of length {length}. </tpl>',
		                '<tpl if="isViewable"> - ',
		                '<a onclick={Ext.getCmp("variable-results").fireEvent("visualise","{variable}","{service}");}>visualize</a>',
		                //' - <a target="_blank" href='+adagucwmservice+'{service}&service=WCS&request=getcoverage&format=AAIGRID&coverage={variable}>get</a>',
		                '</tpl>',
		                '<div id=\'{variable}-attr\' class="variable-attributes">',
		                '<ul><tpl for="attributes">',
		                '<li>{name}: {value}</li>',
		                '</tpl></ul>',
		                '</div>',
		            '</div></tpl></tpl>'
		    		),
		            
            store:new Ext.data.Store({
	            storeId:'variable-store',
			    proxy: {
                    type: 'ajax',
                    reader: {
                        type: 'json',
                        timeout:600000
                    } 
                },
			    fields:[{name: 'variable',       type: 'string'},
			            {name: 'longname',       type: 'string'},
			            {name: 'variabletype',   type: 'string'},
			            {name: 'service',        type: 'string'},
			            {name: 'error',          type: 'string'},
			            {name: 'attributes',     type: 'object'},
			            {name: 'dimensions',     type: 'object'},
			            {name: 'isDimension',    type: 'object'},
                  {name: 'isViewable',    type: 'object'}]
                //, autoLoad: true
			}),
            itemSelector: 'div.variable-item',
            listeners:{
              beforeshow:{
                fn:function(a){
                  a.setHeight(3000);
                  //a.getStore().fireEvent('load'); 
                }
              }
              
            }
        
        }]
    };