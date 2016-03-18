package org.buddha.perforce.patch.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import org.buddha.perforce.patch.Item;

/**
 * Reusable utilities for Strings
 *
 * @author jbuddha
 */
public class StringUtils {

    public static String concatStrings(List<String> strings, String separator) {
        StringBuilder sb = new StringBuilder();
        String sep = "";
        for (Object s : strings) {
            sb.append(sep).append(s);
            sep = separator;
        }
        return sb.toString();
    }

    public static String concatItems(List<Item> items, String separator) {
        StringBuilder sb = new StringBuilder();
        String sep = "";
        for (Item s : items) {
            sb.append(sep).append(s.toString());
            sep = separator;
        }
        return sb.toString();
    }

    public static String exceptionToString(Exception ex) {
        StringWriter writer = new StringWriter();
        ex.printStackTrace(new PrintWriter(writer));
        return writer.toString();
    }
    
    public static HashMap parseArgs(String[] args) {
        HashMap results = new HashMap();
        for (int i = 0; i <= args.length - 1; i++) {
            String value = "";
            if (args[i].indexOf("-") == 0) {
                if (args[i].length() == 2) {
                    try {
                        value = args[i+1];
                    } catch (Exception e) {
                        System.out.println("Unable to get value after flag '" + args[i] + "'" );
                    }
                } else if (args[i].length() > 2) {
                    value = args[i].substring(2);
                } else if (args[i].length() == 1) {
                    System.out.println("Invalid flag");
                }
                if (args[i].charAt(1) == 'p') {
                    results.put("-p", value);
                } else if (args[i].charAt(1) == 'u') {
                    results.put("-u", value);
                }
            }       
        }
        return results;
    }
}
