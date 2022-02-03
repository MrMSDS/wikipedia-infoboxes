package gov.epa.wikipedia.infoboxes.processing.data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Class to hold chemical identifiers retrieved from an individual infobox on a Wikipedia page
 * @author GSINCL01
 *
 */
public class Infobox {
	public String infoboxTitle;
	public Set<String> dtxsids = new HashSet<String>();
	public Set<String> casrns = new HashSet<String>();
	public Set<String> inchikeys = new HashSet<String>();
	public Set<String> smiles = new HashSet<String>();
	
	private static final Pattern INCHIKEY_PATTERN = Pattern.compile("[A-Z]{14}-[A-Z]{10}-[A-Z]{1}");
	private static final Pattern CASRN_PATTERN = Pattern.compile("[0-9]{2,7}-[0-9]{2}-[0-9]");
	private static final Pattern DTXSID_PATTERN = Pattern.compile("DTXSID[0-9]+");
	
	/**
	 * Check if any data at all was retrieved from the infobox
	 * @return
	 */
	public boolean isEmpty() {
		return infoboxTitle==null && !hasIdentifiers();
	}
	
	/**
	 * Check if usable chemical identifiers other than title were retrieved from the infobox
	 * @return
	 */
	public boolean hasIdentifiers() {
		return !casrns.isEmpty() || !inchikeys.isEmpty() || !smiles.isEmpty() || !dtxsids.isEmpty();
	}
	
	/**
	 * Extract identifiers from a Jsoup element according to the Chembox template
	 * @param table		The Jsoup element containing the Chembox infobox
	 * @return			An infobox object containing the identifiers from the HTML
	 */
	public static Infobox fromChembox(Element table) {
		Infobox infobox = new Infobox();
		infobox.infoboxTitle = table.selectFirst("caption").text();
		
		Elements trs = table.select("tr");
		for (Element tr:trs) {
			Elements tds = tr.select("td");
			for (Element td:tds) {
				String header = td.text();
				if (header.equals("CAS Number")) {
					Element tdNext = td.nextElementSibling();
					List<String> extractedCasrns = extractPatternFromHtmlList(tdNext, CASRN_PATTERN);
					infobox.casrns.addAll(extractedCasrns);
				} else if (header.equals("CompTox Dashboard (EPA)")) {
					Element tdNext = td.nextElementSibling();
					List<String> extractedDtxsids = extractPatternFromHtmlList(tdNext, DTXSID_PATTERN);
					infobox.dtxsids.addAll(extractedDtxsids);
				} else if (header.startsWith("InChI")) {
					List<String> extractedInchikeys = extractPatternFromHtmlList(td, INCHIKEY_PATTERN);
					infobox.inchikeys.addAll(extractedInchikeys);
				} else if (header.startsWith("SMILES")) {
					List<String> strings = getListFromHtml(td);
					List<String> trimmedSmiles = trimToColonFromList(strings);
					infobox.smiles.addAll(trimmedSmiles);
				}
			}
		}
		
		return infobox;
	}
	
	/**
	 * Extract identifiers from a Jsoup element according to the Drugbox template
	 * @param table		The Jsoup element containing the Drugbox infobox
	 * @return			An infobox object containing the identifiers from the HTML
	 */
	public static Infobox fromDrugbox(Element table) {
		Infobox infobox = new Infobox();
		Element caption = table.selectFirst("caption");
		if (caption!=null) {
			infobox.infoboxTitle = caption.text();
		}
		
		Elements trs = table.select("tr");
		for (Element tr:trs) {
			Element th = tr.selectFirst("th");
			if (th!=null) {
				String header = th.text();
				if (header.equals("CAS Number")) {
					Element td = tr.selectFirst("td");
					List<String> extractedCasrns = extractPattern(td, CASRN_PATTERN);
					infobox.casrns.addAll(extractedCasrns);
				} else if (header.equals("CompTox Dashboard (EPA)")) {
					Element td = tr.selectFirst("td");
					List<String> extractedDtxsids = extractPattern(td, DTXSID_PATTERN);
					infobox.dtxsids.addAll(extractedDtxsids);
				}
			} else {
				Element td = tr.selectFirst("td");
				String header = td.text();
				if (header.startsWith("SMILES")) {
					List<String> strings = getListFromHtml(td);
					List<String> trimmedSmiles = trimToColonFromList(strings);
					infobox.smiles.addAll(trimmedSmiles);
				} else if (header.startsWith("InChI")) {
					List<String> extractedInchikeys = extractPattern(td, INCHIKEY_PATTERN);
					infobox.inchikeys.addAll(extractedInchikeys);
				}
			}
		}
		
		return infobox;
	}
	
	private static List<String> extractPattern(Element e, Pattern p) {
		List<String> extractedStrings = null;
		if (e.html().contains("<li>")) {
			extractedStrings = extractPatternFromHtmlList(e, p);
		} else {
			extractedStrings = extractPatternFromString(e.text(), p);
		}
		return extractedStrings;
	}
	
	private static List<String> extractPatternFromString(String str, Pattern p) {
		List<String> extractedStrings = new ArrayList<String>();
		Matcher m = p.matcher(str);
		while (m.find()) {
			extractedStrings.add(m.group());
		}
		
		return extractedStrings;
	}
	
	private static List<String> extractPatternFromHtmlList(Element e, Pattern p) {
		List<String> strings = getListFromHtml(e);
		return extractPatternFromList(strings, p);
	}

	private static List<String> getListFromHtml(Element e) {
		List<String> strings = new ArrayList<String>();
		Elements lis = e.select("li");
		for (Element li:lis) {
			strings.add(li.text());
		}
		
		return strings;
	}

	private static List<String> extractPatternFromList(List<String> strings, Pattern p) {
		List<String> extractedStrings = new ArrayList<String>();
		for (String str:strings) {
			List<String> patternStrings = extractPatternFromString(str, p);
			if (patternStrings!=null) {
				extractedStrings.addAll(patternStrings);
			}
		}
		
		return extractedStrings;
	}

	private static List<String> trimToColonFromList(List<String> strings) {
		List<String> trimmedStrings = new ArrayList<String>();
		for (String str:strings) {
			if (str.contains(": ")) {
				trimmedStrings.add(str.substring(str.indexOf(":") + 1).trim());
			} else {
				trimmedStrings.add(str);
			}
		}
		
		return trimmedStrings;
	}
}