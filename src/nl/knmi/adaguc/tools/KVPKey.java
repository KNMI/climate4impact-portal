package nl.knmi.adaguc.tools;


import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;

public class KVPKey {
  class KVP{
    public KVP(String key2, String value2) {
      key=key2;
      value=value2;
    }
    String key = null;
    String value = null;  
  }
  private Vector<KVP> kvplist = new Vector<KVP> ();
  
  public void addKVP(String key,String value){
    kvplist.add(new KVP(key,value));
  }
  
  public SortedSet<String> getKeys(){
    SortedSet<String> keys = new TreeSet<String>();
    for(KVP k : kvplist){
      keys.add(k.key);
    }
    return keys;
  }
  public Vector<String> getValue(String key){
    Vector<String> values = new Vector<String>();
    for(KVP k : kvplist){
      if(k.key.equalsIgnoreCase(key)){
        values.add(k.value);
      }
    }
    return values;
    
  }
  
}
