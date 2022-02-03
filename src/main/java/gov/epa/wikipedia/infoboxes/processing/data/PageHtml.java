package gov.epa.wikipedia.infoboxes.processing.data;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import gov.epa.wikipedia.infoboxes.processing.queries.ParseQuery.ParseResult;

/**
 * Class to hold Wikipedia page HTML
 * @author GSINCL01
 *
 */
public class PageHtml {
	public String title;
	public Integer pageId;
	public List<String> infoboxHtml;
	
	public PageHtml(String title, Integer pageId, List<String> infoboxHtml) {
		this.title = title;
		this.pageId = pageId;
		this.infoboxHtml = infoboxHtml;
	}

	/**
	 * Roughly parse a raw API query result and store infobox data alone
	 * @param result	A raw result from a MediaWiki API parse query
	 * @return			A PageHtml object, i.e., a list of strings containing infobox HTML with page info
	 */
	public static PageHtml fromParseResult(ParseResult result) {
		Document doc = Jsoup.parse(result.parse.text.html);
		Elements infoboxes = doc.select("table.infobox");
		List<String> infoboxHtml = new ArrayList<String>();
		for (Element infobox:infoboxes) {
			String html = infobox.outerHtml();
			infoboxHtml.add(html);
		}
		
		return new PageHtml(result.parse.title, result.parse.pageid, infoboxHtml);
	}
}