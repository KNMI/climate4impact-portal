var esgfsearch_pc_mutiplyHexColor = function(hexcolor,factor){
  var r = parseInt('0x'+hexcolor.substring(1,3));r*=factor;if(r<0)r=0;if(r>255)r=255;r = parseInt(r).toString(16);if(r.length==1)r="0"+r;
  var g = parseInt('0x'+hexcolor.substring(3,5));g*=factor;if(g<0)g=0;if(g>255)g=255;g = parseInt(g).toString(16);if(g.length==1)g="0"+g;
  var b = parseInt('0x'+hexcolor.substring(5,7));b*=factor;if(b<0)b=0;if(b>255)b=255;b = parseInt(b).toString(16);if(b.length==1)b="0"+b;
  return "#"+r+g+b;
};

var ProjectPropertyChooser = function(){
}

ProjectPropertyChooser.prototype.html = "<div class=\"esgfsearch-ppc\"></div>";

ProjectPropertyChooser.prototype.init = function(parentEl, facetName,facetList,query,selectPropertyCallback){
  var k = new ESGFSearch_KVP(query);
  var selectedFacets = k.getKeyValues();
  var selectedPropertiesForFacet = selectedFacets[facetName];
  var foundProperties = 0;
  var createTile = function(color,enabled,name,description){
    var extraCls = "";
    var cbcls = "c4i-esgfsearch-checkboxclear";
    if(!enabled){
      color = 'gray';
      extraCls="esgfsearch-ppc-tile-disabled";
      cbcls = "";
    }else{
      foundProperties++;
    }
    if(selectedPropertiesForFacet){
      if(selectedPropertiesForFacet.indexOf(name)!=-1){
        extraCls+= " c4i-esgfsearch-property-selected";
        cbcls = "c4i-esgfsearch-checkbox";
      }
    }
    
    var d= $("<div class=\"esgfsearch-ppc-tile c4i-esgfsearch-property "+extraCls+"\" style=\"background-color:"+color+";\"/>");
    d.attr('name',name);
    var title = name;

    d.html(
      "<div class=\"esgfsearch-ppc-tileheader\">"+ "<div class=\""+cbcls+"\" style=\"float:none;\"></div>&nbsp;<span>"+name+""+
      "</span></div>"+
      "<div class=\"esgfsearch-ppc-tilebody\" style=\"padding:5px 15px 5px 15px;\">"+description+
      "</div>"
    );
    
    if(enabled){
      var colorsel = esgfsearch_pc_mutiplyHexColor(color,1.25);
      d.mouseenter(function(){d.css("background-color",colorsel);});
      d.mouseleave(function(){d.css("background-color",color);});
    }
    return d;
  };
  
  
  var main=$("<div class=\"esgfsearch-ppc-main\" ></div>");
  for(var j=0;j<esgfsearch_pc_project.length;j++){
    var enable = true;
    if(facetList){
      if(facetList.indexOf(esgfsearch_pc_project[j].name)==-1){
        enable = false;
      }
    }
    main.append(createTile(esgfsearch_pc_project[j].color,enable,esgfsearch_pc_project[j].name,esgfsearch_pc_project[j].longname));
  
  }
  parentEl.find(".esgfsearch-ppc").empty();

  parentEl.find(".esgfsearch-ppc").append(main);
  return foundProperties;
};


var VariablePropertyChooser = function(){
}

VariablePropertyChooser.prototype.html = "<div class=\"esgfsearch-ppc\"></div>";

VariablePropertyChooser.prototype.init = function(parentEl, facetName,facetList,query,selectPropertyCallback){
  
  var createTile = function(color,name,description){
    var d= $("<div class=\"esgfsearch-ppc-tile\" style=\"background-color:"+color+";\"/>");
    d.attr('name',name);
    d.html(
      "<div class=\"esgfsearch-ppc-tileheader\">"+name+""+
      "</div>"+
      "<div class=\"esgfsearch-ppc-tilebody\">"+description+
      "</div>"
    );
    

    var colorsel = esgfsearch_pc_mutiplyHexColor(color,1.1);
    
    d.mouseenter(function(){d.css("background-color",colorsel);});
    d.mouseleave(function(){d.css("background-color",color);});
    return d;
  };
  
  var k = new ESGFSearch_KVP(query);
  var selectedFacets = k.getKeyValues();
  var selectedPropertiesForFacet = selectedFacets[facetName];
  
  var main=$("<div class=\"esgfsearch-ppc-main\"></div>");
  var foundProperties = 0;
  for(var j=0;j<esgfsearch_pc_variables.length;j++){
    
    var tilehtml=  "";
    for(var i=0;i<esgfsearch_pc_variables[j].children.length;i++){
      var c= esgfsearch_pc_variables[j].children[i];
      var cls = "";
      var cbcls = "c4i-esgfsearch-checkboxclear";
      
      if(facetList.indexOf(c.name)==-1){
        cls = "c4i-esgfsearch-property-disabled"
      }else{
        foundProperties++;
      }
      
      if(selectedPropertiesForFacet){
        if(selectedPropertiesForFacet.indexOf(c.name)!=-1){
          cls+= " c4i-esgfsearch-property-selected";
          cbcls = "c4i-esgfsearch-checkbox";
          
        }
      }
      
  
      tilehtml+="<div class=\"c4i-esgfsearch-property "+cls+" esgfsearch-ppc-tileproperty\" name=\""+c.name+"\">"+
        "<span class=\""+cbcls+"\"></span>"+c.shortname+" ("+c.name+")</div>";
      
    }
  
    main.append(createTile(esgfsearch_pc_variables[j].color,esgfsearch_pc_variables[j].shortname,tilehtml));
  }
  
  parentEl.find(".esgfsearch-ppc").empty();

  
  parentEl.find(".esgfsearch-ppc").append(main);
  return foundProperties;
};
