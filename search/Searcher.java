package search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;

import core.MyAnalyzer;

public class Searcher {
	private DirectoryReader reader;
	private IndexSearcher searcher;

	public Searcher(Directory directory) throws IOException {
		reader = DirectoryReader.open(directory);
		searcher = new IndexSearcher(reader);
	}

	public ArrayList<Document> searchStandardQuery(String userQuery)
			throws ParseException, IOException {
		ArrayList<Document> result = new ArrayList<Document>();

		Analyzer analyzer = new MyAnalyzer();
		HashMap<String, Float> boosts = new HashMap<String, Float>();
		boosts.put("docAuthor", 0.2f);
		boosts.put("docTitle", 0.3f);
		boosts.put("docContent", 0.5f);
		QueryParser parser = new MultiFieldQueryParser(new String[] {
				"docTitle", "docAuthors", "docContent" }, analyzer, boosts);
		// QueryParser parser = new QueryParser(userQuery, analyzer);
		Query query = parser.parse(QueryParser.escape(userQuery));

		TopDocs hits = searcher.search(query, searcher.getIndexReader()
				.numDocs() / 2);
		for (ScoreDoc scoreDoc : hits.scoreDocs) {
			Document doc = searcher.doc(scoreDoc.doc);
			result.add(doc);
		}
		return result;
	}
}