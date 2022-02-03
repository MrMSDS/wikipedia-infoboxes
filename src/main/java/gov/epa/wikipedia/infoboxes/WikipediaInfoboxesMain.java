package gov.epa.wikipedia.infoboxes;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import gov.epa.wikipedia.infoboxes.processing.PageDownloader;
import gov.epa.wikipedia.infoboxes.processing.PageParser;
import gov.epa.wikipedia.infoboxes.processing.data.PageHtml;
import gov.epa.wikipedia.infoboxes.processing.data.ParsedPage;
import kong.unirest.Unirest;

public class WikipediaInfoboxesMain {
	
	private static SimpleDateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("yyyy-MM-dd-HHmmss");
	
	private static String generateArchiveFileName(String fileName) {
		String stem = fileName.substring(0, fileName.indexOf("."));
		String extension = fileName.substring(fileName.indexOf("."));
		String timestamp = TIMESTAMP_FORMAT.format(new Date());
		return "archived/" + stem + "_archived-" + timestamp + extension;
	}
	
	private static List<PageHtml> archiveAndUpdatePageHtml(String pageHtmlFileName, String eiTitle, boolean redownloadExisting) 
			throws IOException {
		System.out.println("Downloading pages from " + eiTitle + "...");
		List<PageHtml> existingPageHtml = new ArrayList<PageHtml>();
		try {
			existingPageHtml = PageDownloader.getPageHtmlFromFile(pageHtmlFileName);
		} catch (IOException e) {
			// If the file doesn't exist, just continue with the "existing" HTML as an empty list
		}
		
		System.out.println("Found " + existingPageHtml.size() + " pages in existing file.");
		
		// If redownloading existing files, just download them, archive, and return
		if (redownloadExisting) {
			System.out.println("Redownloading all pages...");
			List<PageHtml> pageHtml = PageDownloader.downloadPageHtml(eiTitle);
			
			// If there is existing page HTML, archive it
			if (existingPageHtml.size() > 0) {
				PageDownloader.savePageHtml(existingPageHtml, generateArchiveFileName(pageHtmlFileName));
			}
			
			PageDownloader.savePageHtml(pageHtml, pageHtmlFileName);
			System.out.println("Downloaded and saved " + pageHtml.size() + " pages to " + pageHtmlFileName + ".");
			return pageHtml;
		}
		
		// Otherwise, check the existing page HTML file and update as needed
		System.out.println("Updating new pages...");
		List<PageHtml> updatedPageHtml = PageDownloader.updatePageHtml(eiTitle, pageHtmlFileName);
		
		// Updating only ever adds entries, does not change existing entries
		// So if size has not changed, no updates have been made and we do not need to archive
		if (existingPageHtml.size() < updatedPageHtml.size()) {
			// If there is existing page HTML, archive it
			if (existingPageHtml.size() > 0) {
				PageDownloader.savePageHtml(existingPageHtml, generateArchiveFileName(pageHtmlFileName));
			}
	
			// Save the new HTML
			PageDownloader.savePageHtml(updatedPageHtml, pageHtmlFileName);
			System.out.println("Downloaded and saved " + (updatedPageHtml.size() - existingPageHtml.size())
				+ " pages to " + pageHtmlFileName + ".");
			return updatedPageHtml;
		}
		
		// Otherwise, return the HTML we already have
		return existingPageHtml;
	}
	
	private static List<ParsedPage> archiveAndUpdateParsedPages(String parsedPagesFileName, List<PageHtml> updatedPageHtml) 
			throws IOException {
		System.out.println("Parsing pages...");
		List<ParsedPage> existingParsedPages = new ArrayList<ParsedPage>();
		try {
			existingParsedPages = PageParser.getParsedPagesFromFile(parsedPagesFileName);
		} catch (IOException e) {
			// If the file doesn't exist, just continue with the "existing" pages as an empty list
		}
		
		System.out.println("Found " + existingParsedPages.size() + " parsed pages in existing file.");
		
		List<ParsedPage> updatedParsedPages = PageParser.parsePageHtml(updatedPageHtml);
		
		// If there are existing parsed pages, save them
		if (existingParsedPages.size() > 0) {
			PageParser.saveParsedPages(existingParsedPages, generateArchiveFileName(parsedPagesFileName));
		}
		
		// Save the new parsed pages
		PageParser.saveParsedPages(updatedParsedPages, parsedPagesFileName);
		System.out.println("Parsed and saved " + updatedParsedPages.size() + " pages to " + parsedPagesFileName + ".");
		
		return updatedParsedPages;
	}
	
	private static void archiveAndUpdateByTemplate(String eiTitle, String pageHtmlFileName, String parsedPagesFileName, 
			boolean redownloadExisting) throws IOException {
		try {
			// Make sure Unirest is configured
			Unirest.config().cookieSpec("standard").connectTimeout(0).socketTimeout(0);
		} catch (Exception e) {
			// Ignore if Unirest already configured
		}
		
		List<PageHtml> updatedPageHtml = archiveAndUpdatePageHtml(pageHtmlFileName, eiTitle, redownloadExisting);
		archiveAndUpdateParsedPages(parsedPagesFileName, updatedPageHtml);
	}
	
	public static void archiveAndUpdateEverything(boolean redownloadExisting) throws IOException {
		archiveAndUpdateByTemplate(WikipediaInfoboxesDict.TEMPLATE_DRUGBOX, WikipediaInfoboxesDict.DRUGBOX_RAW_HTML_FILE_NAME,
				WikipediaInfoboxesDict.DRUGBOX_PARSED_DATA_FILE_NAME, redownloadExisting);
		archiveAndUpdateByTemplate(WikipediaInfoboxesDict.TEMPLATE_CHEMBOX, WikipediaInfoboxesDict.CHEMBOX_RAW_HTML_FILE_NAME,
				WikipediaInfoboxesDict.CHEMBOX_PARSED_DATA_FILE_NAME, redownloadExisting);
	}
	
	public static void main(String[] args) {
		try {
			archiveAndUpdateEverything(false);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
