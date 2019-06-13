package org.openmrs.module.disa.extension.util;

import java.text.Normalizer;

/**
 * 
 * @author machabane
 *
 */
public class GenericUtil {

	public static String removeAccents (String str) {
		return Normalizer.normalize(str, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]","");
	}
}
