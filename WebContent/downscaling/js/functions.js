/*
 * ORDERED MAP LIBRARY
 * AUTHOR: MANUEL VEGA SALDARRIAGA
 */




function insertKey(map, key, value){
  return map[key] = value;
}

function removeKey(map, key){
  delete map[key];
}

function getValue(map, key){
  return map[key];
}


function containsKey(map, key){
  return key in map;
}


function replaceKey(map, key, value){
  return map[key] = value;
}

function getKeys(map){
  var keys = [];
  for(var key in map){
    keys.push(key);
  };
  return keys;
}

/*
 * Hash specific functions
 * 
 */

function getMapFromHash(){
  var currentHash = location.hash;
  var pairs = currentHash.substring(1,currentHash.length).split("&");
  var map = {};
  for(var i=0; i<pairs.length; i++){
    var keyValue = pairs[i].split("=");
    map[keyValue[0]] = keyValue[1];
  }
  return map;
}

function replaceHash(map){
  var newHash = "#";
  for(var key in map){
    newHash += key + "=" + map[key] + "&";
  }
  return location.hash = newHash.substring(0,newHash.length-1);
}

function replaceHashBySortedKeys(map, sortedKeys){
  var newHash = "#";
  for(var i=0; i<sortedKeys.length; i++){
    var key = sortedKeys[i];
    if(map.hasOwnProperty(sortedKeys[i]))
        newHash += key + "=" + map[key] + "&";
  }
  return location.hash = newHash.substring(0,newHash.length-1);
}

function insertHashProperty(key, value, sortedKeys){
  var map = getMapFromHash();
  if(containsKey(map, key))
    replaceKey(map, key, value);
  else
    insertKey(map,key,value);
  return replaceHashBySortedKeys(map, sortedKeys);
}

function removeHashProperty(key){
  var map = getMapFromHash();
  if(containsKey(map, key))
    removeKey(map,key);
  return replaceHash(map);
}

function getValueFromHash(key){
  var map = getMapFromHash();
  return getValue(map,key);
}

function containsKeyInHash(key){
  var map = getMapFromHash();
  return containsKey(map,key);
}
