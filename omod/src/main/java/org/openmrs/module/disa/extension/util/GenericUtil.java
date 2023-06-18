package org.openmrs.module.disa.extension.util;

<<<<<<< HEAD
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
=======
>>>>>>> 1b214cf7795bf40f18a6eaa547506a568c67afb3
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

<<<<<<< HEAD
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.openmrs.module.disa.api.util.Constants;

import com.fasterxml.jackson.databind.node.ArrayNode;

=======
>>>>>>> 1b214cf7795bf40f18a6eaa547506a568c67afb3
/**
 *
 * @author machabane
 *
 */
public class GenericUtil {

	public static void SendMail(String to, final String from, String subject, String actualMessage, String host,
			final String fromPassword, String port) {

		// Get system properties
		Properties properties = System.getProperties();

		// Setup mail server
		properties.put("mail.smtp.host", host);
		properties.put("mail.smtp.port", port);
		properties.put("mail.smtp.ssl.enable", "true");
		properties.put("mail.smtp.auth", "true");

		// Get the Session object.// and pass username and password
		Session session = Session.getInstance(properties, new javax.mail.Authenticator() {

			protected PasswordAuthentication getPasswordAuthentication() {

				return new PasswordAuthentication(from, fromPassword);
			}

		});

		// Used to debug SMTP issues
		session.setDebug(true);

		try {
			// Create a default MimeMessage object.
			MimeMessage message = new MimeMessage(session);

			// Set From: header field of the header.
			message.setFrom(new InternetAddress(from));

			// Set To: header field of the header.
			message.addRecipients(Message.RecipientType.TO, InternetAddress.parse(to));

			// Set Subject: header field
			message.setSubject(subject);

			// Now set the actual message
			message.setText(actualMessage);

			System.out.println("sending...");
			// Send message
			Transport.send(message);
			System.out.println("Sent message successfully....");
		} catch (MessagingException mex) {
			mex.printStackTrace();
		}
	}

	public static void sendMail(String recipients[], String subject, String message, final String from, String host,
			String port, final String fromPassword) {

		try {
			boolean debug = false;
			Properties props = new Properties();

			props.put("mail.smtp.host", host);
			props.put("mail.smtp.auth", "true");
			props.put("mail.debug", "true");
			props.put("mail.smtp.port", port);
			props.put("mail.smtp.socketFactory.port", port);
			props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
			props.put("mail.smtp.socketFactory.fallback", "false");

			Session session = Session.getInstance(props, new javax.mail.Authenticator() {

				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(from, fromPassword);
				}
			});

			session.setDebug(debug);

			Message msg = new MimeMessage(session);
			InternetAddress addressFrom = new InternetAddress(from);
			msg.setFrom(addressFrom);

			InternetAddress[] addressTo = new InternetAddress[recipients.length];
			for (int i = 0; i < recipients.length; i++) {
				addressTo[i] = new InternetAddress(recipients[i]);
			}
			msg.setRecipients(Message.RecipientType.TO, addressTo);

			// Setting the Subject and Content Type
			msg.setSubject(subject);
			msg.setContent(message, "text/plain");
			Transport.send(msg);
		} catch (MessagingException messagingException) {
			messagingException.printStackTrace();
		}
	}
<<<<<<< HEAD

	public static List<NameValuePair> buildParamList(List<String> hfCodes) {
	    List<NameValuePair> hfs = new ArrayList<>();
	    for (String string : hfCodes) {
	        hfs.add(new BasicNameValuePair("healthFacilityLabCode", string));
	    }
	    return hfs;
	}
	
	public static String getStackTrace(Throwable throwable) {
	    StringWriter sw = new StringWriter();
	    PrintWriter pw = new PrintWriter(sw);
	    throwable.printStackTrace(pw);
	    return sw.toString();
	}
	
	public static ArrayNode populateArrayNode(String[] emailAddressList, ArrayNode arrayNode) {
		for (String emailAddress : emailAddressList) {
            arrayNode.add(emailAddress);
        }
		return arrayNode;
	}
=======
>>>>>>> 1b214cf7795bf40f18a6eaa547506a568c67afb3
}
