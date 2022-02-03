package gov.epa.wikipedia.infoboxes.processing.data;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * Class to hold parsed data from multiple infoboxes on a Wikipedia page
 * @author GSINCL01
 *
 */
public class ParsedPage {
	public String title;
	public Integer pageId;
	public List<Infobox> infoboxes = new ArrayList<Infobox>();
	
	public ParsedPage(String title, Integer pageId) {
		this.title = title;
		this.pageId = pageId;
	}

	/**
	 * Parse all infoboxes in a page to infobox objects and store them with page info
	 * @param pageHtml		The HTML contents of a page to parse
	 * @return				A ParsedPage object, i.e., a list of infobox objects with page info
	 */
	public static ParsedPage fromPageHtml(PageHtml pageHtml) {
		ParsedPage page = new ParsedPage(pageHtml.title, pageHtml.pageId);
		for (String infoboxHtml:pageHtml.infoboxHtml) {
			Document doc = Jsoup.parse(infoboxHtml);
			Element table = doc.selectFirst("table.infobox");
			Infobox infobox = null;
			if (table.hasClass("infobox ib-chembox")) {
				infobox = Infobox.fromChembox(table);
			} else {
				infobox = Infobox.fromDrugbox(table);
			}
			
			if (!infobox.isEmpty()) {
				page.infoboxes.add(infobox);
			}
		}
		
		return page;
	}
}