package DRS;

import java.util.TreeMap;

public class DRS {
  /**
   * DRS items
   */
  public static String[] facetNamesOrderedDRS={"activity","product","institute","model","experiment","frequency","realm","MIP_table","ensemble_member","variable"};
  
 
 
  /**
   * Derive DRS components based on DRS syntax
   * @param A DRS identifier
   * @return A TreeMap containing DRS mapped items from the identifier, for example treemap.get("experiment") will return the corresponding experiment of the identifier.
   */
  public static TreeMap<String,String> generateDRSItems(String identifier){
    /* Derive facets based on DRS syntax
     * cmip5.output1.MOHC.HadGEM2-CC.historical.6hr.atmos.6hrPlev.r1i1p1.v20110929.psl_6hrPlev_HadGEM2-CC_historical_r1i1p1_1957120106-1958120100.nc
     * cmip5.output1.MOHC.HadGEM2-CC.historical.6hr.atmos.6hrPlev.r1i1p1.psl.20110929.aggregation 
     */
    TreeMap<String,String>drsItems = new TreeMap<String,String>(); 
    String[] facets=identifier.split("\\.");
    if(facets.length<11){
      for(int facetsNr=0;facetsNr<facets.length;facetsNr++){
        if(facetNamesOrderedDRS[facetsNr].equals("variable")){
          drsItems.put(facetNamesOrderedDRS[facetsNr],"None");
        }else{
          drsItems.put(facetNamesOrderedDRS[facetsNr],facets[facetsNr]);
        }
      }
      return drsItems;
    }
    int numFacets = 10;

    if(identifier.indexOf("aggregation")>0){
      //Aggregation uses different DRS syntax than files
      for(int facetsNr=0;facetsNr<numFacets;facetsNr++){
        drsItems.put(facetNamesOrderedDRS[facetsNr],facets[facetsNr]);
      }
    }else{
      //Files uses different DRS syntax than aggregations
      for(int facetsNr=0;facetsNr<numFacets-1;facetsNr++){
        drsItems.put(facetNamesOrderedDRS[facetsNr],facets[facetsNr]);
      }
      //psl_6hrPlev_HadGEM2-CC_historical_r1i1p1_1957120106-1958120100 <-- keep psl_
      drsItems.put(facetNamesOrderedDRS[9],facets[10].split("_")[0]);
    }
    return drsItems;    
  }

}
