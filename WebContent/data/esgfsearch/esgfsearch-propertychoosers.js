var esgfsearch_pc_mutiplyHexColor = function(hexcolor,factor){
  var r = parseInt('0x'+hexcolor.substring(1,3));r*=factor;if(r<0)r=0;if(r>255)r=255;r = parseInt(r).toString(16);if(r.length==1)r="0"+r;
  var g = parseInt('0x'+hexcolor.substring(3,5));g*=factor;if(g<0)g=0;if(g>255)g=255;g = parseInt(g).toString(16);if(g.length==1)g="0"+g;
  var b = parseInt('0x'+hexcolor.substring(5,7));b*=factor;if(b<0)b=0;if(b>255)b=255;b = parseInt(b).toString(16);if(b.length==1)b="0"+b;
  return "#"+r+g+b;
};

var PropertyChooser = function(config){
  this.config = config;
}

PropertyChooser.prototype.html = "<div class=\"esgfsearch-ppc\"></div>";

PropertyChooser.prototype.init = function(parentEl, facetName,facetList,query,selectPropertyCallback){
  var config = this.config;
  var k = new ESGFSearch_KVP(query);
  var selectedFacets = k.getKeyValues();
  var selectedPropertiesForFacet = selectedFacets[facetName];
  var foundProperties = 0;
  var createTile = function(color,enabled,name,description,tileobj){
   
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
      if(esgfSearchIndexOf(selectedPropertiesForFacet,name)!=-1){
        extraCls+= " c4i-esgfsearch-property-selected";
        cbcls = "c4i-esgfsearch-checkbox";
      }
    }
    
    var d= $("<div class=\"esgfsearch-ppc-tile c4i-esgfsearch-property "+extraCls+"\" style=\"background-color:"+color+";\"/>");
    d.attr('name',name);
    var title = name;
    if(tileobj.shortname)name=tileobj.shortname;
    if(config.tilewidth){
      d.css({"width":config.tilewidth});
    }
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
  for(var j=0;j<this.config.properties.length;j++){
    var enable = true;
    if(facetList){
      if(esgfSearchIndexOf(facetList,this.config.properties[j].name)==-1){
        enable = false;
      }
    }
    main.append(createTile(this.config.properties[j].color,enable,this.config.properties[j].name,this.config.properties[j].longname,this.config.properties[j]));
  
  }
  parentEl.find(".esgfsearch-ppc").empty();

  parentEl.find(".esgfsearch-ppc").append(main);
  return foundProperties;
};


var NestedPropertyChooser = function(config){
  this.config = config;
}

NestedPropertyChooser.prototype.html = "<div class=\"esgfsearch-ppc\"></div>";

NestedPropertyChooser.prototype.init = function(parentEl, facetName,facetList,query,selectPropertyCallback){
  var config = this.config;
  var createTile = function(color,name,description){
    var d= $("<div class=\"esgfsearch-ppc-tile\" style=\"background-color:"+color+";\"/>");
    if(config.tilewidth){
      d.css({"width":config.tilewidth});
    }
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
  for(var j=0;j<this.config.properties.length;j++){
    
    var tilehtml=  "";
    for(var i=0;i<this.config.properties[j].children.length;i++){
      var c= this.config.properties[j].children[i];
      var cls = "";
      var cbcls = "c4i-esgfsearch-checkboxclear";
      
      if(esgfSearchIndexOf(facetList,c.name)==-1){
        cls = "c4i-esgfsearch-property-disabled"
      }else{
        foundProperties++;
      }
      
      if(selectedPropertiesForFacet){
        if(esgfSearchIndexOf(selectedPropertiesForFacet,c.name)!=-1){
          cls+= " c4i-esgfsearch-property-selected";
          cbcls = "c4i-esgfsearch-checkbox";
          
        }
      }
      
  
      tilehtml+="<div class=\"c4i-esgfsearch-property esgfsearch-ppc-tileproperty "+cls+"\" name=\""+c.name+"\">"+
        "<span class=\""+cbcls+"\"></span>"+c.shortname+" ("+c.name+")</div>";
      
    }
  
    main.append(createTile(this.config.properties[j].color,this.config.properties[j].shortname,tilehtml));
  }
  
  parentEl.find(".esgfsearch-ppc").empty();

  
  parentEl.find(".esgfsearch-ppc").append(main);
  return foundProperties;
};



