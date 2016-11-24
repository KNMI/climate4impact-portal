var Basket = function(){
	var t = this;
	
	var basketElement = undefined;
	
	this.removeId = function(id,done){
		var doneFunction = function(json){
			if(json.error){
				alert(json.error);
		    }else{
				adjustNumberOfDataSetsDisplayedInMenuBar(json);
		    }
			done(json);
		};
	
		$.ajax({
			type: "GET",
			url: impactService,
			data: { service:'basket',request:'removeFromList',id:id }  ,
			success:doneFunction,
			dataType: 'json'
		});
	};
	
	this.requestBasket = function(callbackfunction,failedfunction){
		$.ajax({
    		type: "GET",
    		url: impactService,
    		data: { service:'basket',request:'getoverview' }  ,
    		success:callbackfunction,
    		dataType: 'json'
		});
	};
    
    this.populateBasketList = function(jqelement){
      if(!jqelement)return;
    	if(jqelement){
    		basketElement = jqelement;
    		jqelement.html("Loading basket...");
    	}
    	
   		var passFn = function(json){
   			jqelement.html(t.createBasketHTMLFromJSON(json));
   		}
   		t.requestBasket(passFn,passFn);
   		/*var failFn = function(msg){
   			var data = {};
   			data.responseText='Unable to retrieve basket from server';
   			passFn(data);
   		}*/
    };
	  
    
    this.createBasketSelectorHTMLFromJSON = function(json){
    	return t.createBasketHTMLFromJSON(json);
    };
    
    
  var makeHTMLFromJSONChilds = function(children,viewerurl,browserurl){
    var html="";
    for(var j=0;j<children.length;j++){
      var view = "-"; //Either view or browse
      var get = "-";
      var subset = "-";
      var remove = "";
      var child = children[j];
      if(child.type=='file'){
        if(child.httpurl){
          get="<a target='_blank' href=\""+child.httpurl+"\">get</a>";
        }
        if(child.dapurl){
          view="<a href='"+viewerurl+"dataset="+URLEncode(child.dapurl)+"'>view</a>";
          subset="subset";
        }
      }else{
        if(!child.children){
          view="<a href='"+browserurl+"catalog="+URLEncode(child.catalogurl)+"'>browse</a>";
        }
      }
      remove="<a href=\"#\" onclick='basket.removeId(\""+child.id+"\");return false;'>X</a>";
      html+="<tr>";
      html+="<td>"+child.date+"</td><td>"+child.text+"</td><td>"+view+"</td><td>"+get+"</td><td>"+subset+"</td><td>"+remove+"</td>";
      html+="</tr>";
      if(child.children){
        html+=makeHTMLFromJSONChilds(child.children,viewerurl,browserurl);
      }
      
    }
    return html;
  }
	this.createBasketHTMLFromJSON = function(json){
		if(json.error){
			return "An error occured while retrieving the basket: "+json.error;
		}
		var viewerurl = json.viewer;
		var browserurl = json.browser;
		html='<table class="basket">'
		html+="<td style=\"width:150px;background-color:#DDD;\"><b>Added on:</b></td>";
		html+="<td style=\"width:600px;background-color:#DDD;\"><b>Identifier</b></td>";
		html+="<td style=\"background-color:#DDD;\"><b>View</b></td>";
		html+="<td style=\"background-color:#DDD;\"><b>Get</b></td>";
		html+="<td style=\"background-color:#DDD;\"><b>Subset</b></td>";
	  	html+="<td style=\"background-color:#DDD;\"><b>X</b></td>";
	  	html+="</tr>";
	  html += makeHTMLFromJSONChilds(json.children,viewerurl,browserurl);
		html+='</table>'
		return html;
	};
	
	this.postIdentifiersToBasket = function(options){
	  console.log("postIdentifiersToBasket");
		var doneFunction = function(json){
			if(json.error){
				if(json.statuscode){
					if(json.statuscode==401){
						generateLoginDialog(function(){basket.postIdentifiersToBasket(options);})
						return;
					}
				}
				customalert(json.error);
				
			}
			adjustNumberOfDataSetsDisplayedInMenuBar(json);
			console.log("postIdentifiersToBasket Done");
	    try{
        if(basketWidget){
          basketWidget.reload();
        }
      }catch(e){
       //console.log(e);
      }
		};
		 //alert(dump(options));
		$.ajax({
			type: "POST",
			url: '/impactportal/ImpactService?service=basket&mode=add',
			data: options,
			success:doneFunction,
			dataType: 'json'
			});
	};
	
  function endsWith(str, suffix) {
    return str.indexOf(suffix, str.length - suffix.length) !== -1;
  }
  
  this.expandNodes = function(selectedNodesMixed,_callbackFunction){
    var hasSelection = (selectedNodesMixed.length>0);

    if (hasSelection) {
      
      var pendingRequests = 0;
      var allFilesAssembled = function(selectedNode){
        if(pendingRequests!=0){
          console.log("Still some requests open");
          return;
        }else{
          console.log("OK continue!");
        }

        if (_callbackFunction) {
          if(selectedNode.length == 0){
            Ext.MessageBox.alert('Error','No valid nodes selected.');
          }
          console.log("calling callbackfunction on selectedNode: "+selectedNode);  
          _callbackFunction(selectedNode);
          
        }else{
          Ext.MessageBox.alert('Error','Nothing to apply to.');
        }
        return true;
      };


      

      var selectedNode = [];


      //console.log(selectedNodesMixed);

      for ( var j = 0; j < selectedNodesMixed.length; j++) {
        
        if(selectedNodesMixed[j].data.dapurl||selectedNodesMixed[j].data.httpurl){
          selectedNode.push(selectedNodesMixed[j].data);
        }
        if(selectedNodesMixed[j].data.catalogurl&&selectedNodesMixed[j].data.catalogurl!="null"){
          pendingRequests++;
          var urlRequest = c4iconfigjs.impactservice+"service=catalogbrowser&mode=flat&format=application/json&node="+ URLEncode(selectedNodesMixed[j].data.catalogurl);
          console.log(urlRequest);
          $.ajax({
            url: urlRequest,
          }).done(function(data) {
            //console.log(data);
            for(var j=0;j<data.files.length;j++){
              
              if(data.files[j].dapurl){
                if(endsWith(data.files[j].dapurl,"aggregation")){
                  console.log("Skipping (aggregation) "+data.files[j].dapurl);
                }else{
                  console.log("Pushing "+data.files[j].dapurl);
                  selectedNode.push({dapurl:data.files[j].dapurl});
                }

              }
            }
            pendingRequests--;
            allFilesAssembled(selectedNode);
          }).fail(function() {

          }).always(function() {

          });
          //selectedNode.push({dapurl:selectedNodes[j].data.dapurl});
        }
      }
      return allFilesAssembled(selectedNode);


      //
      // alert(selectedNode[0].data.dapurl + ' was selected');

    } else {
      Ext.MessageBox.alert('Error','No selected files.');
    }
  };
};

//Singleton basket is always there...
var basket = new Basket();

