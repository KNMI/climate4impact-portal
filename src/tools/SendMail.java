package tools;

//File Name SendEmail.java

import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;


public class SendMail{
  
  public static boolean isValidEmailAddress(String email) {
    boolean result = true;
    try {
       InternetAddress emailAddr = new InternetAddress(email);
       emailAddr.validate();
    } catch (AddressException ex) {
       result = false;
    }
    return result;
  }
    
  public static void sendMail(String[] to,String from, String subject, String messageString) throws Exception{
     
     // Recipient's email ID needs to be mentioned.
     //TODO must be configurable
     subject = subject.replaceAll("\n", " ");
     Debug.println("Sending email from "+from+" with subject ["+subject+"]");
     
     // Assuming you are sending email from localhost
     String host = "localhost";
  
     // Get system properties
     Properties properties = System.getProperties();
  
     // Setup mail server
     properties.setProperty("mail.smtp.host", host);
  
     // Get the default Session object.
     Session session = Session.getDefaultInstance(properties);
  
     try{
        // Create a default MimeMessage object.
        MimeMessage message = new MimeMessage(session);
  
        // Set From: header field of the header.
        message.setFrom(new InternetAddress(from));
  
        // Set To: header field of the header.
        for(int j=0;j<to.length;j++){
          String email = to[j];
          if(tools.SendMail.isValidEmailAddress(email)){
            message.addRecipient(Message.RecipientType.TO,new InternetAddress(to[j]));
          }
        }
        
  
        // Set Subject: header field
        message.setSubject(subject);
  
        // Now set the actual message
        message.setText(messageString);
  
        // Send message
        Transport.send(message);
        System.out.println("Sent message successfully....");
     }catch (MessagingException mex) {
       //Debug.printStackTrace(mex);
        //mex.printStackTrace();
       //throw new Exception("Unable to send mail with subject ["+subject+"] msg: "+mex.getMessage());
     }
  }
}
