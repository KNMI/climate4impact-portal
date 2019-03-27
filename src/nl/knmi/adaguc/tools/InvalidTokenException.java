package nl.knmi.adaguc.tools;

public class InvalidTokenException extends Exception {
	String message = null;
	/**
	 * 
	 */
	private static final long serialVersionUID = 6797418585190698615L;

	public InvalidTokenException(String string) {
		message = string;
	}
	
	public String getMessage() {
		return message;
	}

}
