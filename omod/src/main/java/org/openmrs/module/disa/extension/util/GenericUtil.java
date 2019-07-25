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

	public static String removeAccents (String specialCharacter) {
		
		try {
			bs = specialCharacter.getBytes("ISO-8859-15");
			noSpecialCharacter = Normalizer.normalize(new String(bs, "UTF-8"), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]","");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return noSpecialCharacter;
	}
}
