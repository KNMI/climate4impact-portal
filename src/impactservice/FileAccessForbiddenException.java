package impactservice;

public class FileAccessForbiddenException extends Exception {
  private String message = null;

  public FileAccessForbiddenException(String string) {
    message = string;
  }

  public String getMessage(){
    return message;
  }
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

}
