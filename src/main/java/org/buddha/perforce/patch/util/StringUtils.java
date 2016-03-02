package org.buddha.perforce.patch.util;

import java.util.List;
import org.buddha.perforce.patch.Item;

/**
 *
 * @author buddha
 */
public class StringUtils {
		public static String concatStrings(List<String> strings, String separator) {
		StringBuilder sb = new StringBuilder();
		String sep = "";
		for(Object s: strings) {
			sb.append(sep).append(s);
			sep = separator;
		}
		return sb.toString();                           
	}
		
	public static String concatItems(List<Item> items, String separator) {
		StringBuilder sb = new StringBuilder();
		String sep = "";
		for(Item s: items) {
			sb.append(sep).append(s.toString());
			sep = separator;
		}
		return sb.toString();                           
	}	
}
