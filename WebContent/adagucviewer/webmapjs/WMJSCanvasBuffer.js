

var WMJSCanvasBuffer = function(webmapJSCallback,_type,_imageStore,w,h){
  //console.log("WMJSCanvasBuffer created with "+w+","+h);
  var _this = this;
  
  this.canvas = 
    $('<canvas/>',{'class':'WMJSCanvasBuffer'})
    .width(w)
    .height(h);
  

  
  var ctx=_this.canvas[0].getContext("2d");
  ctx.canvas.width=w;
  ctx.canvas.height=h;
  
  var imageStore =_imageStore;
  this.ready = true;
  this.hidden = true;
  this.layerstodisplay = [];
  this.layers = [];
  var width = w;
  var height = h;
  var imageWidth = w;
  var imageHeight = w;
  var type = _type;
  
  if(type=='imagebuffer'){
    this.canvas.addClass("wmjsimagebuffer");
  }
  if(type=='legendbuffer'){
    this.canvas.addClass("wmjslegendbuffer");
  }
  
  this.canvas.addClass("WMJSCanvasBuffer-noselect");
  
  this.imageLoadComplete = function(image){
    statDivBufferImageLoaded();
    webmapJSCallback.triggerEvent("onimageload");
  };
  
  

  
  var statDivBufferImageLoaded = function(){
    //console.log("WMJSCanvasBuffer:statDivBufferImageLoaded");
    
    for(var j=0;j<_this.layers.length;j++){
      if(_this.layers[j].isLoaded() == false){
        return;
      }
    }
    //console.log("WMJSCanvasBuffer:statDivBufferImageLoaded OK!");
    _this.finishedLoading();

  };
  
  
  var defaultImage = new WMJSImage("webmapjs/img/stoploading.png",function(){console.log("fake image loaded");statDivBufferImageLoaded();},type);
  this.hide = function(){
   // console.log("WMJSCanvasBuffer:hide");
    _this.hidden = true;
    _this.canvas.hide();
    _this.layers = [];
    imageStore.releaseImages();
  };
  
  this.display = function(){
     // console.log("======= WMJSCanvasBuffer:display");
    _this.hidden = false;
    ctx.globalAlpha = 1;
     ctx.beginPath();
      ctx.rect(0, 0, width, height);
      ctx.fillStyle = 'white';
      ctx.fill();
    for(var j=0;j<_this.layerstodisplay.length;j++){
      _this.layerstodisplay[j].setSize(width,height);
      if(_this.layerstodisplay[j].hasError() == false){
        //Draw
        var op = _this.layerstodisplay[j].getOpacity();
        ctx.globalAlpha = op;
        var el  = _this.layerstodisplay[j].getElement()[0];
        ctx.drawImage(el,0,0);
      }else{
        error("<a target=\'_blank\' href='"+_this.layerstodisplay[j].getSrc()+"'>"+_this.layerstodisplay[j].getSrc()+"</a>",false);
      }
    }
    _this.canvas.show();
    imageStore.releaseImages();
  };
  
  this.finishedLoading = function(){
//     console.log("======= WMJSCanvasBuffer:finishedLoading");
    if(_this.ready)return;
    _this.ready=true;
    for(var j=0;j<_this.layers.length;j++){
      _this.layerstodisplay[j] = _this.layers[j];
    }
    try{
      if(isDefined(_this.onLoadReadyFunction)){
        _this.onLoadReadyFunction(_this);
      }
    }catch(e){
      error("Exception in Divbuffer::finishedLoading: "+e);
    }
    imageStore.releaseImages();
  
  };
  
  this.setPosition = function(x,y){
    if(isNaN(x)||isNaN(y)){
      x=0;y=0;
    }
    _this.canvas.css({top: y, left: x});
//      for(var j=0;j<this.layers.length;j++){
//       this.layers[j].setPosition(x,y);
//     }
  };
  
  this.setSize = function(w,h){
    w=parseInt(w);
    h=parseInt(h);
    imageWidth = w;
    imageHeight = h;
    _this.canvas.width(w);
    _this.canvas.height(h);
    for(var j=0;j<this.layers.length;j++){
      this.layers[j].setSize(w,h);
    }


    
//    console.log("WMJSCanvasBuffer setSize with "+w+","+h);
  };
  
  this.resize = function(w,h){
    w=parseInt(w);
    h=parseInt(h);
    width=w;
    height=h;
    imageWidth=w;
    imageHeight=h;
    _this.setSize(w,h);
    _this.canvas.width(w);
    _this.canvas.height(h);
    ctx.canvas.height =h;
    ctx.canvas.width = w;

  };
  
  this.load = function(callback){
    if(_this.ready==false){
      //console.log(" ===== Still busy ====== ");
      return;
    }
    _this.ready = false;
    _this.layerstodisplay = [];
    
    
    //console.log("WMJSCanvasBuffer:load");
    //this.setPosition(0,0);
    if(callback){this.onLoadReadyFunction = callback;}else _this.onLoadReadyFunction = function(){}
    _this.nrLoading = 0;
    //console.log("WMJSCanvasBuffer:this.layers.length = "+this.layers.length);
    
    for(var j=0;j<this.layers.length;j++){
      _this.layers[j].loadThisOne = false;

      if(_this.layers[j].isLoaded()==false){
        _this.layers[j].loadThisOne = true;
        _this.nrLoading++;
      }
    }
    //console.log("WMJSCanvasBuffer.nrLoading = "+this.nrLoading +" nrImages = "+this.layers.length );
    if(this.nrLoading==0){
      statDivBufferImageLoaded();
    }else{
      if(type=='imagebuffer'){debug("GetMap:");}
      if(type=='legendbuffer'){debug("GetLegendGraphic:");}
      for(var j=0;j<this.layers.length;j++){
        if(this.layers[j].loadThisOne == true){
          debug("<a target=\'_blank\' href='"+this.layers[j].getSrc()+"'>"+this.layers[j].getSrc()+"</a>",false);
        }
      }
      
      for(var j=0;j<this.layers.length;j++){
        if(this.layers[j].loadThisOne == true){
          //console.log("WMJSCanvasBuffer.loading = "+this.layers[j].getSrc());
          this.layers[j].load();
        }
      }
    }
  };
  
  this.setSrc = function (layerIndex,imageSource,width,height){
    if(!isDefined(imageSource)){console.log("undefined");return;}
    while(layerIndex>=this.layers.length){
        this.layers.push(defaultImage);
    }
    var image = imageStore.getImage(imageSource);
    image.setZIndex(layerIndex);
    this.layers[layerIndex]=image;
  };
  
  this.setOpacity = function (layerIndex,opacity){
    if(layerIndex>=this.layers.length){
      error("setOpacity :invalid id");
      return;
    }
    var op=parseFloat(opacity);
    
    if(this.layers[layerIndex].getOpacity()!=op){
      this.layers[layerIndex].setOpacity(op);
    }
  };
  
  this.getBuffer = function(){
    return _this.canvas;
  };
};
  