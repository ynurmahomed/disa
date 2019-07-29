package org.openmrs.module.disa.extension.util;

import java.text.Normalizer;

/**
 * 
 * @author machabane
 *
 */
public class GenericUtil {

	private static byte[] bs;
	private static String noSpecialCharacter;
	private static String wardConcept;

	public static String removeAccents (String specialCharacter) {
		
		try {
			bs = specialCharacter.getBytes("ISO-8859-15");
			noSpecialCharacter = Normalizer.normalize(new String(bs, "UTF-8"), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]","");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return noSpecialCharacter;
	}
	
	public static String wardSelection (String we) {
		switch (we) {
	      case "CI":	wardConcept="CONSULTA INTEGRADA";
	      			break;
	      case "SMI":	wardConcept="SAUDE MATERNO INFANTIL";
	      			break;
	      case "CPN": 	wardConcept="CONSULTA PRE-NATAL";
	      			break;
	      case "HDD":	wardConcept="HOSPITAL DO DIA";
	      			break;
	      case "CCR":	wardConcept="CONSULTA DE CRIANCAS EM RISCO";
	      			break;
	      case "TARV":	wardConcept="TARV";
	      			break;
	      case "TAP":	wardConcept="TRIAGEM - PEDIATRIA";
	      			break;
	      case "TAD":	wardConcept="TRIAGEM - ADULTOS";
	      			break;
	      case "PED":	wardConcept="ENF. PEDIATRIA";
	      			break;
	      case "LAB":	wardConcept="LABORATORIO";
	      			break;
	      default:	wardConcept="";
		}
		return wardConcept;
	}
	
	public static void main(String[] args) {
		System.out.println(GenericUtil.wardSelection("CCR")); 
	}
}
