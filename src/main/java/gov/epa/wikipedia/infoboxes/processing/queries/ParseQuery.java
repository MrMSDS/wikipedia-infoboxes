package gov.epa.wikipedia.infoboxes.processing.queries;

import com.google.gson.annotations.SerializedName;

import gov.epa.wikipedia.infoboxes.WikipediaInfoboxesDict;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

/**
 * Class to run the Media Wiki "parse" query for page content and properties
 * API doc: https://www.mediawiki.org/wiki/API:Parsing_wikitext
 * @author GSINCL01
 *
 */
public class ParseQuery {
	
	/**
	 * Json wrapper class for result of parse query
	 * @author GSINCL01
	 *
	 */
	public static class ParseResult {
		public Parse parse;
		public Error error;
		public String servedBy;
	}
	
	/**
	 * "Parse" element of result of parse query
	 * @author GSINCL01
	 *
	 */
	public static class Parse {
		public String title;
		public Integer pageid;
		public Text text;
		
		// If we just need a fast query to associate page ID and title, use this instead of full text
		public String displaytitle;
	}
	
	/**
	 * "Text" element of result of parse query
	 * @author GSINCL01
	 *
	 */
	public static class Text {
		@SerializedName("*")
		public String html;
	}
	
	/**
	 * "Error" element of result of parse query
	 * @author GSINCL01
	 *
	 */
	public static class Error {
		public String code;
		public String info;
		@SerializedName("*")
		public String note;
	}
	
	/**
	 * Send a request to get the HTML contents of a page
	 * @param pageId	The page ID to retrieve contents for
	 * @param prop 		The property to retrieve contents for
	 * @return			A result set with page info and contents
	 */
	public static ParseResult run(Integer pageId, String prop) {
		HttpResponse<ParseResult> response = Unirest.get(WikipediaInfoboxesDict.WIKIPEDIA_API_URL)
				.queryString("action", "parse")
				.queryString("pageid", pageId)
				.queryString("prop", prop)
				.queryString("format", "json")
				.asObject(ParseResult.class);
		
		if (response.getStatus()==200) {
			return response.getBody();
		} else {
			return null;
		}
	}
	
	/**
	 * Send a request to get the HTML contents of a page
	 * @param page	The page name to retrieve contents for
	 * @param prop	The property to retrieve contents for
	 * @return		A result set with page info and contents
	 */
	public static ParseResult run(String page, String prop) {
		HttpResponse<ParseResult> response = Unirest.get(WikipediaInfoboxesDict.WIKIPEDIA_API_URL)
				.queryString("action", "parse")
				.queryString("page", page)
				.queryString("prop", prop)
				.queryString("format", "json")
				.asObject(ParseResult.class);
		
		if (response.getStatus()==200) {
			return response.getBody();
		} else {
			return null;
		}
	}
}
