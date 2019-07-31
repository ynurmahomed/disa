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
	      default:	wardConcept="";
		}
		return wardConcept;
	}
	
	public static void main(String[] args) {
		System.out.println(GenericUtil.wardSelection("CCR")); 
	}
}
