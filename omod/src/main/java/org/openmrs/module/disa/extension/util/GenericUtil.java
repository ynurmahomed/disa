package org.openmrs.module.disa.extension.util;

import java.text.Normalizer;
import java.util.Properties;
import java.util.regex.Pattern;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

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
	
	public static void main(String[] args) {
		String hh = " < 20 copias/ml";
		System.out.println(hh
				.trim()
				.substring(1)
				.replace("<", "")
				.replace(Constants.COPIES, "")
				.replace(Constants.FORWARD_SLASH, "")
				.replace(Constants.ML, "")
				.trim()); 
	}
}
