package edu.univalle.utils;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class Utils {

	
	public static final Pattern DIACRITICS_AND_FRIENDS = Pattern.compile(
            "[\\p{InCombiningDiacriticalMarks}\\p{IsLm}\\p{IsSk}]+");
	/**
	 * Gets rid of the diacritics (´  `  ~  ^  etc)
	 * @param str - the string chain to be processed
	 * @return The string chain without diacritics.
	 */
	public static String stripAccents(String str) {
		str = Normalizer.normalize(str, Normalizer.Form.NFD);
		str = DIACRITICS_AND_FRIENDS.matcher(str).replaceAll("");
		return str;
	}
	
	/**
	 * Parses strings of the form [elem1, elem2, elem3,...]
	 * @param s - the string to be parsed
	 * @return ArrayList containing elem1, elem2, elem3, etc
	 */
	public static List<String> parseToList(String s) {
		List<String> aux = new ArrayList<String>();
		if ( (s != null) && !(s.equals("")) ) {
		  String listString = s.substring(1, s.length() - 1); // chop off brackets
		  aux = Arrays.asList(listString.split("\\s*,\\s*"));
		}
		// This is because Lists resulting from Arrays.asList DO NOT support add operation
		// so you have to copy it to a list object which does
		ArrayList<String> output = new ArrayList<String>(aux);
		return output;
	}
	
	/**
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isNumeric(String str)
	{
	  return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
	}

}
