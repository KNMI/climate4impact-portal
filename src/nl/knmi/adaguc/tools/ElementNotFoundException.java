package nl.knmi.adaguc.tools;


public class ElementNotFoundException extends Exception{
	private String configItem = null;
	public ElementNotFoundException(String string) {
		configItem = string;
	}
	
	 public String getMessage() {
	      
	        return "Configuration item missing or misconfigured: "+configItem;
	     }

	/**
	 * 
	 */
	private static final long serialVersionUID = 5420129800584623839L;

}
