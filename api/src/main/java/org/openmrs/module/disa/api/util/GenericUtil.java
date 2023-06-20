package org.openmrs.module.disa.api.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.node.ArrayNode;

/**
 *
 * @author machabane
 *
 */
public class GenericUtil {

	static Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");

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
}
