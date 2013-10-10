var impactBase = '/impactportal/';
var impactWPSURL='/impactportal/ImpactService?';

var configuredWPSItems = [];

var processProgressMonitoring = function(status){
	
	var uniqueId=status.uniqueid;
	
	var results = undefined;
	
	
	var p = Ext.create('Ext.ProgressBar', {
		region:'south'
	});
	var t= Ext.create('Ext.form.field.TextArea',{
		region:'center',border:false,frame:false,padding:0,margin:0
	});
	var b= Ext.create('Ext.button.Button',{
		text:'view results',disabled:true,handler:function(){w.showResults(results);}
	});
	var c= Ext.create('Ext.button.Button',{
		text:'close',disabled:true,handler:function(){w.close();}
	});
	var url = {xtype:'panel',height:25,html:'Location: <a target="_blank" href="'+status.wpsurl+'">'+status.wpsurl+'</a>',region:'south',frame:true,border:false,frameHeader:false,padding:0,margin:0};
	var p2=Ext.create('Ext.panel.Panel',{
		layout:'border',
		items:[t,p],region:'center',frame:false,border:false,frameHeader:false,padding:0,margin:0
	});
	var w= Ext.create('Ext.Window',{
		width:600,height:150,title:'Progress '+status.id,collapsible:true,layout:'border',
		listeners:{
			expand:{
				fn:
					function(){
						p2.doLayout();
						p.updateProgress(1,'100 %',false);
						return true; 
					}
				
			}
		},
		items:[p2,url],
		buttons:[b,c]
		
	});
	
	w.showResults = function(json){
		p.updateProgress(1,100 + " %",true);
		t.setValue("Completed: "+json.status);
		b.enable();
		c.enable();
		if(!w.results){
			w.results= Ext.create('Ext.Window',{
				width:600,height:500,
				listeners:{
					beforeclose:{
						fn:
							function(){
								w.results = undefined;
							}
					}
				},
				title:'WPS result',
				collapsible:false,
				html:"<img src='"+impactWPSURL+'service=processor&request=getimage&outputId=undefined&statusLocation='+json.wpsurl+"'></img>"
			});
			
		}
		w.results.show();
	}
	
	w.show();

	
	var wpsConfig={
			 service:'processor',
			 request:'monitorProcessor',
			 id:status.id,
			 statusLocation:status.statusLocation
		 };
	
	var makeMonitorRequest = function(){
		try{
			Ext.Ajax.request({
				url: impactWPSURL,
				success: passFn,   
				failure: failFn,
				params: wpsConfig 
			});	
		}catch(e){
			alert('makeMonitorRequest: '+e);
		}
	}
	var failFn = function(){ alert("Unable to monitor progress for  "+status.id);w.close();};
	var passFn = function(e){
		
		if(e){
		  try{
			  var json= Ext.JSON.decode(e.responseText);
			  if(json.error){
					alert("Error:\n"+json.error);
					//  w.close();
					return;
			  }
		  }catch(error){
			  alert("Invalid JSON string: "+e.reponseText)
			  w.close();
			  return;
		  }
		  
		}
		try{
			t.setValue(json.status);
			
			var percentage= json.progress/100;
			var percentageText = parseInt(percentage*100) + " %";
			p.updateProgress(percentage,percentageText,true);
			w.setTitle('Progress '+status.id+" ("+percentageText+")");
		}catch(e){}
		
		if(!json.ready){
			setTimeout(makeMonitorRequest,500);
		}else{
			results=json;
			w.showResults(results);
		}
		
	};
	 
	makeMonitorRequest();
	
	
}

/**
 * Called when user presses button 'Start processing'
 * Scans all user input grids and fields and composes a JSON object with input information for the Process.
 * This information is posted to the server.
 */
var startProcessing = function (){
	 var wpsparams=Ext.getCmp('wpsparams');
	 var wpsConfig;
	 var h="[";
	 for(var j=0;j<configuredWPSItems.length;j++){
		 if(j>0)h+=";";
		 h+=configuredWPSItems[j].wpsid+"=";
		 var store=configuredWPSItems[j].store;
		 var inputs="";
		 for(var i=0;i<store.getCount();i++){
			 
			 var record=store.getAt(i);
			 if(record.get('enabled')==true){
				 if(inputs.length>0)inputs+=",";
				 inputs+=record.get('value');
			 }
		 }
		 h+=inputs;
	 }
	 h+="]";
//alert(h);
	 wpsConfig={
		 service:'processor',
		 request:'executeProcessor',
		 id:wpsparams.wpsid,
		 dataInputs:h
	 };
	 
	  var failFn = function(){ alert("Unable to execute process.");};
	  var passFn = function(e){
		  try{
		   var json= Ext.JSON.decode(e.responseText);
		  }catch(error){
			  alert("Invalid JSON string: "+e.reponseText)
			  return;
		  }
			 if(json.error){
				alert("Error:\n"+json.error);
				return;
			 }
			 processProgressMonitoring(json);
	 }
	 
	 Ext.Ajax.request({
	     url: impactWPSURL,
	     success: passFn,   
	     failure: failFn,
	     params: wpsConfig 
	  });
}

var wpsProcessorDetails = function(id){
	
	    
   var url = impactWPSURL+'service=processor&request=describeProcessor&id='+id;
	
 
   /**
    * Returns a grid component based on a json object with structure:
    * id:'<id of the component>'
    * title:'<title of the component displayed>;
    * default:'<default values>' Can be a comma separted list.
    * type:'<type, can be string, etc..>'
    */
   var createStringArrayGrid = function(input){
		 var data = [];
		 var id='stringarraygrid_'+input.id;
		 var values=input['default'].split(',');
		 for(var j=0;j<values.length;j++){data.push({value:values[j],remove:'X',enabled:true})}
	
		 var cellEditing = Ext.create('Ext.grid.plugin.CellEditing', {
			    clicksToEdit: 1,
			    listeners:{
			    	edit:{
			    		fn:function(editor,e,eOpts){
			    		}
			    	}
			    }
		 });
			 
		 var grid= {
			   title:input.title,
			   wpsid:input.id,
			   wpstype:input.type,
	           layout:'fit',xtype:'grid',frame:true,width:'auto',border:false, collapsible:true,collapsed:false,id:id,
	           hideHeaders:true,
	           store:  new Ext.data.Store({
	                   fields: [{name: 'value',type: 'string'},
	                            {name: 'remove',type: 'float'},
	                            {name: 'enabled',type: 'bool'}
	                   ],
	                             data: data
	                   }),
	           columns: [
	                   {header: 'value', flex: 1, dataIndex: 'value',   editor: {allowBlank: false}},
	                   {xtype: 'checkcolumn',header: 'use',dataIndex: 'enabled',width: 55},
	                   {xtype: 'actioncolumn',header: 'drop', width: 34, dataIndex: 'remove',items:[{icon:impactBase+'images/close.gif' ,tooltip:'remove',handler:function(grid,rowIndex,colIndex){grid.getStore().removeAt(rowIndex);}}]}
	           ],
	           listeners:{ 
	        	   itemclick:function(i,record){this.itemClicked(i,record);},
	        	   itemdblclick:function(i,record){this.itemClicked(i,record);selectProcessor();}
	           },
	           itemClicked:function(i,record,item,index){selectedProcessor=record;},
	           tbar:[{xtype:'label',text:input.title},{xtype:'tbseparator'},{xtype:'button',iconCls:'icon-add',text:'add new entry',handler:function(d){Ext.getCmp(id).addString();}}],
	           plugins: [cellEditing],
	           //selType: 'cellmodel',
	           //selModel: {selType: 'cellmodel'},
	           addString:function(){
	           	this.getStore().add({value:'click to edit',remove:'X',enabled:true});
	           }
		 }
		 return grid;
   }
   
   //List wpsProcessorDetails
   var failFn = function(){ alert("Unable to describe process:<br/>\n"+url);};
   var passFn = function(e){
	   var json= Ext.JSON.decode(e.responseText);
		 if(json.error){
			alert("Error:\n"+json.error);
			return;
		 }
		 //alert(e.responseText);
		 //alert(json.inputs[0].type);
		 var wpsparams=Ext.getCmp('wpsparams');;
		 
		 wpsparams.wpsid=id;
		 
		 configuredWPSItems = [];
		 for(j=0;j<json.inputs.length;j++){
			 var input = json.inputs[j];
			 if(input.type='string'){
				 try{
					 if(input['default'].indexOf(",")>0){
						 var item =createStringArrayGrid(input);
						 configuredWPSItems.push(item);
						 wpsparams.add(item);
					 }else{
						 var item =createStringArrayGrid(input);
						 configuredWPSItems.push(item);
						 try{
						 wpsparams.add(item);
						 }catch(e){}
					 }
				 }catch(e){
					 alert('Describe process: '+e);
				 }
			 }
		 }
		 
		 
		 
		 var wpsstart=Ext.getCmp('wpsstart');;
		 var html='<table class="wps">';
		 html+='<tr><td>Title:</td><td>'+json.title+'</td></tr>';
		 html+='<tr><td>Identifier:</td><td>'+json.id+'</td></tr>';
		 html+='<tr><td>Abstract:</td><td>'+json.description+'</td></tr>';
		 html+='<tr><td>Location:</td><td><a target="_blank" href="'+json.wpsurl+'">'+json.wpsurl+'</a></td></tr>';
		 
		 html+='</table>'
		 
		 Ext.get('wpsdivdescription').update(html);
		 //startProcessing();
		 
   }
   Ext.Ajax.request({
     url: url,
     success: passFn,   
     failure: failFn
  });

}
