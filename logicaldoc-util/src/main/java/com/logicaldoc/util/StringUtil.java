package com.logicaldoc.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.CharacterCodingException;
import java.text.DecimalFormat;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;

/**
 * Some utility methods specialized in string manipulation
 * 
 * @author Marco Meschieri - LogicalDOC
 * @since 4.5
 */
public class StringUtil {

	/**
	 * Splits a string into tokens separated by a separator
	 * 
	 * @param src The source string
	 * @param separator The separator character
	 * @param tokenSize Size or each token
	 * 
	 * @return the string with separators
	 */
	public static String split(String src, char separator, int tokenSize) {
		StringBuilder sb = new StringBuilder();
		String[] tokens = split(src, tokenSize);
		for (int i = 0; i < tokens.length; i++) {
			if (sb.length() > 0)
				sb.append(separator);
			sb.append(tokens[i]);
		}
		return sb.toString();
	}

	/**
	 * Splits a string into an array of tokens
	 * 
	 * @param src The source string
	 * @param tokenSize size of each token
	 * 
	 * @return array of splitted tokens
	 */
	public static String[] split(String src, int tokenSize) {
		ArrayList<String> buf = new ArrayList<String>();
		for (int i = 0; i < src.length(); i += tokenSize) {
			int j = i + tokenSize;
			if (j > src.length())
				j = src.length();
			buf.add(src.substring(i, j));
		}
		return buf.toArray(new String[] {});
	}

	/**
	 * Writes to UFT-8 encoding.
	 * 
	 * @param reader the reader over a string
	 * 
	 * @return a string with the contents readed from the <code>reader</code>
	 * 
	 * @throws IOException raised in case the <code>reader</code> failed to read
	 *         or if the string cannot be written
	 */
	public static String writeToString(Reader reader) throws IOException {
		return writeToString(reader, "UTF-8");
	}

	/**
	 * Writes the content from the reader in a string encoded as specified.
	 * 
	 * @param reader Attention, this will be closed at the end of invocation
	 * @param targetEncoding The output string encoding
	 * @return The encoded string
	 * 
	 * @throws IOException raised in case the <code>reader</code> is unable to
	 *         get the contents
	 */
	public static String writeToString(Reader reader, String targetEncoding) throws IOException {
		String enc = "UTF-8";
		if (StringUtils.isNotEmpty(targetEncoding))
			enc = targetEncoding;

		try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
				OutputStreamWriter osw = new OutputStreamWriter(baos, enc);) {
			BufferedWriter bw = new BufferedWriter(osw);
			BufferedReader br = new BufferedReader(reader);
			String inputLine;
			while ((inputLine = br.readLine()) != null) {
				bw.write(inputLine);
				bw.newLine();
			}
			bw.flush();
			osw.flush();
			return new String(baos.toByteArray(), enc);
		}
	}

	public static String writeToString(InputStream is, String targetEncoding) throws IOException {
		String enc = "UTF-8";
		if (StringUtils.isNotEmpty(targetEncoding))
			enc = targetEncoding;

		Writer writer = new StringWriter();
		char[] buffer = new char[1024];
		try (InputStreamReader isr = new InputStreamReader(is, enc); Reader reader = new BufferedReader(isr);) {
			int n;
			while ((n = reader.read(buffer)) != -1) {
				writer.write(buffer, 0, n);
			}
		} finally {
			if (is != null)
				is.close();
		}
		return writer.toString();
	}

	public static String arrayToString(Object[] a, String separator) {
		StringBuilder result = new StringBuilder("");
		if (a.length > 0) {
			result.append(a[0].toString()); // start with the first element
			for (int i = 1; i < a.length; i++) {
				result.append(separator);
				result.append(a[i]);
			}
		}
		return result.toString();
	}

	public static String collectionToString(Collection<?> collection, String separator) {
		return String.join(separator, collection.stream().map(o -> o.toString()).collect(Collectors.toList()));
	}

	public static String removeNonUtf8Chars(String src) throws CharacterCodingException {
		return src.replace('\uFFFF', ' ').replace('\uD835', ' ');
	}

	/**
	 * Check if a given string matches the <code>includes</code> and not the
	 * <code>excludes</code>
	 * 
	 * @param str The string to consider
	 * @param includes list of includes regular expressions
	 * @param excludes list of excludes regular expressions
	 * 
	 * @return true only if the passed string matches the includes and not the
	 *         excludes
	 */
	public static boolean matches(String str, String[] includes, String[] excludes) {
		if ((excludes == null || excludes.length == 0) && (includes == null || includes.length == 0))
			return true;

		// First of all check if the string must be excluded
		boolean matchesExclusions = filtersCheck(str, excludes);
		if (matchesExclusions)
			return false;

		// Then check if the string must can be included
		boolean matchesInclusions = filtersCheck(str, includes);
		if (matchesInclusions)
			return true;

		if (includes == null || includes.length == 0)
			return true;
		else
			return false;
	}

	private static boolean filtersCheck(String str, String[] filters) {
		if (filters != null && filters.length > 0)
			for (String s : filters)
				if (StringUtils.isNotEmpty(s) && str.matches(s))
					return true;
		return false;
	}

	/**
	 * Converts the non latin chars in the nearest ASCII char
	 * 
	 * @param src the source string to process
	 * 
	 * @return the unaccented string
	 */
	public static String unaccent(String src) {
		return Normalizer.normalize(src, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
	}

	/**
	 * Formats a file size in human readable form (KB, MB, GB, TB)
	 * 
	 * @param size the size to format
	 * 
	 * @return the formatted string
	 */
	public static String printFileSize(long size) {
		if (size <= 0)
			return "0";
		final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
		int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
		return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
	}
}