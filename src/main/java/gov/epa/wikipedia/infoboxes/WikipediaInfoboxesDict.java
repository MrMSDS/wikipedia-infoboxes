package gov.epa.wikipedia.infoboxes;

/**
 * Constant strings and file download locations
 * @author GSINCL01
 *
 */
public class WikipediaInfoboxesDict {
	
	// What it says on the tin
	public static final String WIKIPEDIA_API_URL = "https://en.wikipedia.org/w/api.php";
	
	// Template names for Wikipedia's Drugbox and Chembox infobox templates
	// Don't change these!
	public static final String TEMPLATE_DRUGBOX = "Template:Infobox drug";
	public static final String TEMPLATE_CHEMBOX = "Template:Chembox";
	
	// Folder locations for downloaded and processed files
	// Change as desired
	public static final String RAW_DATA_FOLDER_PATH = "data/raw/";
	public static final String PARSED_DATA_FOLDER_PATH = "data/parsed/";
	
	// Filenames for downloaded and processed files
	// Change as desired
	public static final String DRUGBOX_RAW_HTML_FILE_NAME = "drugbox_raw_html.json";
	public static final String CHEMBOX_RAW_HTML_FILE_NAME = "chembox_raw_html.json";
	public static final String DRUGBOX_PARSED_DATA_FILE_NAME = "drugbox_parsed_data.json";
	public static final String CHEMBOX_PARSED_DATA_FILE_NAME = "chembox_parsed_data.json";

}
