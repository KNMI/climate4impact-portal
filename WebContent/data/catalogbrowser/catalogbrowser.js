/**
* Renders a catalog browser interface to given element.
* Arguments via:
* options{
*  element : the element to render to.
*  url: the location of the catalog url servlet endpoint.
* }
*/
var renderCatalogBrowser = function(options){
  return new CatalogBrowser(options);
};



var CatalogBrowser = function(options){
  var _this = this;  
  this.renderCatalogBrowser = function(options){
    if(!options.element){
      options.element = jQuery('<div></div>', {
        title: 'Dataset',
      }).dialog({
        width:950,
        height:600
      });
      options.element.html('<div class="ajaxloader"></div>');
    }
    
    var variableFilter = undefined;
    var textFilter = undefined;
    var applyCatalogFilters = function(el){
      variableFilter = '';
      el.find("form.varFilter :input[type=checkbox]").each(function(){
        var input = $(this);
        if(input.is(":checked")){
          if(variableFilter.length>0)variableFilter+="|";
                                                          variableFilter+=input.attr('id');
        }
        
      });
      if(variableFilter.length==0)variableFilter = undefined;
      
      textFilter = '';
      textFilter = el.find(".textfilter").val();
      if(textFilter.length<1)textFilter = undefined;
      
      loadCatalog(options,variableFilter,textFilter);
    };
    
    var preventSubmit = function(event) {
      if(event.keyCode == 13) {
        event.preventDefault();
        applyCatalogFilters(options.element);
        return false;
      }
    };
    
    var loadCatalog = function(options,variableFilter,textFilter){
      options.element.empty().html('<div class="ajaxloader"></div>');
      
      var httpCallback = function(data){
        
        options.element.empty().html(data);
        
        options.element.find(".varFilter").keypress(preventSubmit);
        options.element.find(".varFilter").keydown(preventSubmit);
        options.element.find(".varFilter").keyup(preventSubmit);
        
        options.element.find(".varFilter").find('input[type="button"]').attr('onclick','').click(function(){applyCatalogFilters(options.element);});
        options.element.find('a').attr('target','_blank');
      };
      
      var url = "/impactportal/ImpactService?service=catalogbrowser&format=text/html&node="+encodeURIComponent(options.url);
      var filters = "";
      if(textFilter!=undefined){filters+="&filter="+encodeURIComponent(textFilter);}
      if(variableFilter!=undefined){filters+="&variables="+encodeURIComponent(variableFilter);}
      url+=filters;
      //console.log(url);
      $.ajax({
        url:  url
        //crossDomain:true,
        //dataType:"jsonp"
      }).done(function(d) {
        httpCallback(d)
      }).fail(function() {
        httpCallback("Failed");
      }).always(function(){
      });
    }
    loadCatalog(options);
    
    
    
  };
  this.renderCatalogBrowser(options);
};