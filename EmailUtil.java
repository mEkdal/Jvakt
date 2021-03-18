package Jvakt;

import java.time.LocalDateTime;
import java.util.Date;
import jakarta.mail.Message;
//import jakarta.mail.internet.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.Session;
import jakarta.mail.Transport;
//import jakarta.mail.internet.InternetAddress;
import jakarta.mail.BodyPart;
import jakarta.mail.Multipart;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.activation.DataSource;
import jakarta.activation.FileDataSource;
import jakarta.activation.DataHandler;
 
public class EmailUtil {

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

	public static boolean sendEmail(Session session, String toEmail, String subject, String body, String fromEmail, String attach){
		try
		{
			MimeMessage msg = new MimeMessage(session);

			msg.setFrom(new InternetAddress(fromEmail, fromEmail));
			msg.setReplyTo(InternetAddress.parse(fromEmail, false));
			msg.setSubject(subject, "UTF-8");

			BodyPart messageBodyPart = new MimeBodyPart();
			messageBodyPart.setText(body);
			
			Multipart multipart = new MimeMultipart();
			multipart.addBodyPart(messageBodyPart);
			
			messageBodyPart = new MimeBodyPart();
	         DataSource source = new FileDataSource(attach);
	         messageBodyPart.setDataHandler(new DataHandler(source));
	         messageBodyPart.setFileName(source.getName());
	         multipart.addBodyPart(messageBodyPart);
			
			msg.setContent(multipart);
			
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
