/**
* Renders a catalog browser interface to given element.
* Arguments via:
* options{
*  element : the element to render to.
*  url: the location of the catalog url servlet endpoint.
* }
*/
var renderFileViewer = function(options){
  return new FileViewer(options);
};



var FileViewer = function(options){
  var _this = this;  
  this.renderFileViewer = function(options){
    
    var fileViewer = new ExtFileViewer();
    var w = Ext.create('Ext.Window', {
      width : 900,
      height : 500,
      
      autoScroll : true,
      autoDestroy : true,
      closeAction : 'destroy',
      frame : false,
      title : 'NetCDF metadata',
      layout : 'fit',
      items : fileViewer.getViewer()
    });
    //w.show();
    
    var win = Ext.WindowMgr;
    win.zseed='290000';
    win.get(w).show();
   
  
    fileViewer.load(options.url);
  };
  this.renderFileViewer(options);
};