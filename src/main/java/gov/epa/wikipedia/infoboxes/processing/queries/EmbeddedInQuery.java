package gov.epa.wikipedia.infoboxes.processing.queries;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.gson.annotations.SerializedName;

import gov.epa.wikipedia.infoboxes.WikipediaInfoboxesDict;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

/**
 * Class to run the Media Wiki "embedded-in" query for pages embedding a given element
 * API doc: https://www.mediawiki.org/w/api.php?action=help&modules=query%2Bembeddedin
 * @author GSINCL01
 *
 */
public class EmbeddedInQuery {
	
	/**
	 * Json wrapper class for result of embedded-in query
	 * @author GSINCL01
	 *
	 */
	public static class EmbeddedInResult {
		public String batchcomplete;
		@SerializedName("continue")
		public Continue continue_;
		public Query query;
	}
	
	/**
	 * "Continue" element of result of embedded-in query
	 * @author GSINCL01
	 *
	 */
	public static class Continue {
		public String eicontinue;
		@SerializedName("continue")
		public String continue_; // "continue" is reserved in Java
	}
	
	/**
	 * "Query" element of result of embedded-in query
	 * @author GSINCL01
	 *
	 */
	public static class Query {
		public List<EmbeddedIn> embeddedin;
	}
	
	/**
	 * "EmbeddedIn" element of result of embedded-in query
	 * @author GSINCL01
	 *
	 */
	public static class EmbeddedIn {
		public int pageid;
		public int ns;
		public String title;
	}
	
	private static final int EI_LIMIT = 500; // Max permitted query size
	private static final int EI_NAMESPACE = 0; // Wiki pages only (exclude users, templates, etc.)
	
	/**
	 * Send a request to get the first page of results for the embedded-in query
	 * @param eiTitle		The title to retrieve results for
	 * @return				A result set with a list of embedded-in results and pagination info
	 */
	private static EmbeddedInResult runSingleQuery(String eiTitle) {
		HttpResponse<EmbeddedInResult> response = Unirest.get(WikipediaInfoboxesDict.WIKIPEDIA_API_URL)
				.queryString("action", "query")
				.queryString("list", "embeddedin")
				.queryString("eititle", eiTitle)
				.queryString("eilimit", EI_LIMIT)
				.queryString("einamespace", EI_NAMESPACE)
				.queryString("format", "json")
				.asObject(EmbeddedInResult.class);
		
		if (response.getStatus()==200) {
			return response.getBody();
		} else {
			return null;
		}
	}
	
	/**
	 * Send a request to get another page of results for the embedded-in query using the eicontinue field
	 * @param eititle		The title to retrieve results for
	 * @param eiContinue	The eicontinue code from the last page of results
	 * @return				A result set with a list of embedded-in results and pagination info
	 */
	private static EmbeddedInResult runSingleQuery(String eiTitle, String eiContinue) {
		HttpResponse<EmbeddedInResult> response = Unirest.get(WikipediaInfoboxesDict.WIKIPEDIA_API_URL)
				.queryString("action", "query")
				.queryString("list", "embeddedin")
				.queryString("eititle", eiTitle)
				.queryString("eicontinue", eiContinue)
				.queryString("eilimit", EI_LIMIT)
				.queryString("einamespace", EI_NAMESPACE)
				.queryString("format", "json")
				.asObject(EmbeddedInResult.class);
		
		if (response.getStatus()==200) {
			return response.getBody();
		} else {
			return null;
		}
	}
	
	/**
	 * Loop the embedded-in query until it runs out of results
	 * @param eiTitle	The title to retrieve results for
	 * @return			A list of embedded-in results (pageid and title)
	 */
	private static List<EmbeddedIn> run(String eiTitle) {
		List<EmbeddedIn> embeddedIn = new ArrayList<EmbeddedIn>();
		
		EmbeddedInResult result = runSingleQuery(eiTitle);
		while (result!=null) {
			embeddedIn.addAll(result.query.embeddedin);
			
			if (result.continue_!=null) {
				result = runSingleQuery(eiTitle, result.continue_.eicontinue);
			} else {
				break;
			}
		}
		
		return embeddedIn;
	}
	
	/**
	 * Get all embedded-in elements and extract the page IDs alone
	 * @param eiTitle	The title to retrieve results for
	 * @return			A list of page IDs
	 */
	public static Set<Integer> getPageIds(String eiTitle) {
		List<EmbeddedIn> embeddedIn = run(eiTitle);
		Set<Integer> pageIds = embeddedIn.stream().map(e -> e.pageid).collect(Collectors.toSet());
		return pageIds;
	}
}
