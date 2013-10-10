package tools;

//File Name SendEmail.java

import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;


public class SendMail{
public static void sendMail(String[] to,String from, String subject, String messageString) throws Exception{
   
   // Recipient's email ID needs to be mentioned.
   //TODO must be configurable
   DebugConsole.println("Sending email from "+from);
   
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
        message.addRecipient(Message.RecipientType.TO,new InternetAddress(to[j]));
      }
      

      // Set Subject: header field
      message.setSubject(subject);

      // Now set the actual message
      message.setText(messageString);

      // Send message
      Transport.send(message);
      System.out.println("Sent message successfully....");
   }catch (MessagingException mex) {
    // DebugConsole.printStackTrace(mex);
      //mex.printStackTrace();
     throw new Exception("Unable to send mail");
   }
}
}
