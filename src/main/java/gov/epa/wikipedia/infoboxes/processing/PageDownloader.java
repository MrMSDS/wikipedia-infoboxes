package gov.epa.wikipedia.infoboxes.processing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gov.epa.wikipedia.infoboxes.WikipediaInfoboxesDict;
import gov.epa.wikipedia.infoboxes.processing.data.PageHtml;
import gov.epa.wikipedia.infoboxes.processing.queries.EmbeddedInQuery;
import gov.epa.wikipedia.infoboxes.processing.queries.ParseQuery;
import gov.epa.wikipedia.infoboxes.processing.queries.ParseQuery.ParseResult;

/**
 * Class to download raw HTML from Wikipedia pages using the Media Wiki API
 * @author GSINCL01
 *
 */
public class PageDownloader {
	
	private static Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
	
	/**
	 * Download all pages that embed an element with the given title (e.g. "Template:Chembox")
	 * @param eiTitle	The element title to query
	 * @return			A list of PageHtml objects from the relevant pages
	 */
	public static List<PageHtml> downloadPageHtml(String eiTitle) {
		List<PageHtml> pageHtml = new ArrayList<PageHtml>();
		Set<Integer> newPageIds = EmbeddedInQuery.getPageIds(eiTitle);
		
		if (!newPageIds.isEmpty()) {
			List<PageHtml> newPageHtml = getPageHtmlFromApi(newPageIds);
			pageHtml.addAll(newPageHtml);
		}
		
		return pageHtml;
	}
	
	/**
	 * Update any new (not previously downloaded) pages that embed an element with the given title (e.g. "Template:Chembox")
	 * @param eiTitle					The element title to query
	 * @param existingPageHtmlFileName	The name of the file containing existing HTML
	 * @return							A list of PageHtml objects from the relevant pages
	 */
	public static List<PageHtml> updatePageHtml(String eiTitle, String existingPageHtmlFileName) {
		List<PageHtml> pageHtml = new ArrayList<PageHtml>();
		
		try {
			// Try to find the existing page HTML file
			pageHtml.addAll(PageDownloader.getPageHtmlFromFile(existingPageHtmlFileName));
		} catch (IOException e) {
			// If the file doesn't exist, go straight to download all HTML directly
			return downloadPageHtml(eiTitle);
		}

		Set<Integer> existingPageIds = pageHtml.stream().map(html -> html.pageId).collect(Collectors.toSet());
		Set<Integer> newPageIds = EmbeddedInQuery.getPageIds(eiTitle);
		
		for (Integer pageId:existingPageIds) {
			newPageIds.remove(pageId);
		}
		
		if (!newPageIds.isEmpty()) {
			List<PageHtml> newPageHtml = getPageHtmlFromApi(newPageIds);
			pageHtml.addAll(newPageHtml);
		}
		
		return pageHtml;
	}
	
	/**
	 * Run queries to get HTML from a set of page IDs
	 * @param pageIds	The set of page IDs to query
	 * @return			The HTML contents of the queried pages
	 */
	private static List<PageHtml> getPageHtmlFromApi(Set<Integer> pageIds) {
		List<PageHtml> pageHtml = new ArrayList<PageHtml>();
		for (Integer pageId:pageIds) {
			ParseResult result = ParseQuery.run(pageId, "text");
			PageHtml thisPageHtml = PageHtml.fromParseResult(result);
			pageHtml.add(thisPageHtml);
		}
		
		return pageHtml;
	}

	/**
	 * Write page HTML to a JSON file
	 * @param pageHtml		The PageHtml objects to write
	 * @param fileName		The filename to write to
	 * @throws IOException	If the file cannot be written
	 */
	public static void savePageHtml(List<PageHtml> pageHtml, String fileName) throws IOException {
		File file = new File(WikipediaInfoboxesDict.RAW_DATA_FOLDER_PATH + fileName);
		file.getParentFile().mkdirs();
		
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
			bw.write(gson.toJson(pageHtml));
		} catch (IOException e) {
			throw e;
		}
	}

	/**
	 * Read page HTML from a JSON file
	 * @param fileName		The file to read
	 * @return				The contents of the file as PageHtml objects
	 * @throws IOException	File not found or other I/O problem
	 */
	public static List<PageHtml> getPageHtmlFromFile(String fileName) throws IOException {
		try (BufferedReader br = new BufferedReader(new FileReader(WikipediaInfoboxesDict.RAW_DATA_FOLDER_PATH + fileName))) {
			PageHtml[] pageHtmlArray = gson.fromJson(br, PageHtml[].class);
			return Arrays.asList(pageHtmlArray);
		} catch (IOException e) {
			throw e;
		}
	}
}
