package Jvakt;

import java.time.LocalDateTime;
//import java.io.UnsupportedEncodingException;
import java.util.Date;
//import javax.mail.Message;
//import javax.mail.internet.*;
//import javax.mail.internet.InternetAddress;
//import javax.mail.Session;
//import javax.mail.Transport;
import jakarta.mail.Message;
import jakarta.mail.internet.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.Session;
import jakarta.mail.Transport;

//import jakarta.activation.DataHandler;
////import javax.activation.DataSource;
////import javax.activation.FileDataSource;
////import javax.mail.BodyPart;
////import javax.mail.MessagingException;
////import javax.mail.Multipart;
//import jakarta.mail.internet.*;
////import javax.mail.internet.MimeBodyPart;
////import javax.mail.internet.MimeMultipart;

public class EmailUtil {

	/**
	 * Utility method to send simple HTML email
	 * @param session
	 * @param toEmail
	 * @param subject
	 * @param body
	 */
	public static boolean sendEmail(Session session, String toEmail, String subject, String body, String fromEmail){
		try
	    {
	      MimeMessage msg = new MimeMessage(session);
	      //set message headers
	      msg.addHeader("Content-type", "text/HTML; charset=UTF-8");
	      msg.addHeader("format", "flowed");
	      msg.addHeader("Content-Transfer-Encoding", "8bit");

	      msg.setFrom(new InternetAddress(fromEmail, fromEmail));

	      msg.setReplyTo(InternetAddress.parse(fromEmail, false));

	      msg.setSubject(subject, "UTF-8");

//	      msg.setText(body, "UTF-8");
	      msg.setContent(body,"text/html");

	      msg.setSentDate(new Date());

	      msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail, false));
	      System.out.println(LocalDateTime.now()+" - Message is ready");
    	  Transport.send(msg);  

	      
	    }
	    catch (Exception e) {
	      e.printStackTrace();
	      System.out.println(LocalDateTime.now()+" - EMail failed!! \n "+e );
	      return false;
	    }
		System.out.println(LocalDateTime.now()+" - EMail Sent Successfully!!");
		return true;
	}
}