package org.openmrs.module.disa.extension.util;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.openmrs.module.disa.api.util.Constants;

/**
 *
 * @author machabane
 *
 */
public class GenericUtil {

	private static byte[] bs;
	private static String noSpecialCharacter;
	private static String wardConcept;
	static Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");

	public static String removeAccents (String specialCharacter) {

		try {
			bs = specialCharacter.getBytes("ISO-8859-15");
			noSpecialCharacter = Normalizer.normalize(new String(bs, "UTF-8"), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]","");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return noSpecialCharacter;
	}

	public static boolean isNumeric(String strNum) {
	    if (strNum == null) { return false; }
	    return pattern.matcher(strNum).matches();
	}

	public static String unaccent(String src) {
		return Normalizer
				.normalize(src, Normalizer.Form.NFD)
				.replaceAll("[^\\p{ASCII}]", "");
	}

	public static String wardSelection (String we) {
		switch (we) {
	      case "CI":	wardConcept=Constants.CONSULTA_INTEGRADA;
	      			break;
	      case "SMI":	wardConcept=Constants.SAUDE_MATERNO_INFANTIL;
	      			break;
	      case "CPN": 	wardConcept=Constants.CONSULTA_PRE_NATAL;
	      			break;
	      case "HDD":	wardConcept=Constants.HOSPITAL_DO_DIA;
	      			break;
	      case "CCR":	wardConcept=Constants.CONSULTA_DE_CRIANCAS_EM_RISCO;
	      			break;
	      case "TARV":	wardConcept=Constants.TARV;
	      			break;
	      case "TAP":	wardConcept=Constants.TRIAGEM_PEDIATRIA;
	      			break;
	      case "TAD":	wardConcept=Constants.TRIAGEM_ADULTOS;
	      			break;
	      case "PED":	wardConcept=Constants.ENF_PEDIATRIA;
	      			break;
	      case "LAB":	wardConcept=Constants.LABORATORIO;
	      			break;
	      default:		wardConcept=Constants.OUTRO_NAO_CODIFICADO;
		}
		return wardConcept;
	}

	public static void SendMail(String to, final String from, String subject, String actualMessage, String host, final String fromPassword, String port) {

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

	public static void sendMail(String recipients[], String subject, String message, final String from, String host, String port, final String fromPassword)
		{

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

	public static List<NameValuePair> buildParamList(List<String> hfCodes) {
	    List<NameValuePair> hfs = new ArrayList<>();
	    for (String string : hfCodes) {
	        hfs.add(new BasicNameValuePair("healthFacilityLabCode", string));
	    }
	    return hfs;
	}
}
