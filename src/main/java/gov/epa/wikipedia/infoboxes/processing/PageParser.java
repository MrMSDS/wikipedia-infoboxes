package gov.epa.wikipedia.infoboxes.processing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gov.epa.wikipedia.infoboxes.WikipediaInfoboxesDict;
import gov.epa.wikipedia.infoboxes.processing.data.PageHtml;
import gov.epa.wikipedia.infoboxes.processing.data.ParsedPage;

/**
 * Class to parse Wikipedia page HTML into sets of identifiers of interest
 * @author GSINCL01
 *
 */
public class PageParser {
	
	private static Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

	/**
	 * Parse page HTML contents and extract sets of identifiers of interest
	 * @param pageHtml	The list of page HTML to parse
	 * @return			The list of parsed results
	 */
	public static List<ParsedPage> parsePageHtml(List<PageHtml> pageHtml) {
		List<ParsedPage> parsedPages = pageHtml.stream().map(html -> ParsedPage.fromPageHtml(html)).collect(Collectors.toList());
		return parsedPages;
	}
	
	/**
	 * Write parsed pages to a JSON file
	 * @param parsedPages	The ParsedPage objects to write
	 * @param fileName		The filename to write to
	 * @throws IOException	If the file cannot be written
	 */
	public static void saveParsedPages(List<ParsedPage> parsedPages, String fileName) throws IOException {
		File file = new File(WikipediaInfoboxesDict.PARSED_DATA_FOLDER_PATH + fileName);
		file.getParentFile().mkdirs();
		
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
			bw.write(gson.toJson(parsedPages));
		} catch (IOException e) {
			throw e;
		}
	}
	
	/**
	 * Read parsed pages from a JSON file
	 * @param fileName		The file to read
	 * @return				The contents of the file as ParsedPage objects
	 * @throws IOException	File not found or other I/O problem
	 */
	public static List<ParsedPage> getParsedPagesFromFile(String fileName) throws IOException {
		try (BufferedReader br = new BufferedReader(new FileReader(WikipediaInfoboxesDict.PARSED_DATA_FOLDER_PATH + fileName))) {
			ParsedPage[] parsedPagesArray = gson.fromJson(br, ParsedPage[].class);
			return Arrays.asList(parsedPagesArray);
		} catch (IOException e) {
			throw e;
		}
	}
}
