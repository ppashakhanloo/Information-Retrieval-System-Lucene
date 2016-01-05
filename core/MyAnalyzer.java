package core;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;

public class MyAnalyzer extends Analyzer {
	@Override
	protected TokenStreamComponents createComponents(String arg0) {
		StandardTokenizer tokenStream = new StandardTokenizer();
		TokenStream result = new StandardFilter(tokenStream);
		result = new LowerCaseFilter(result);
		result = new StopFilter(result, StopAnalyzer.ENGLISH_STOP_WORDS_SET);
		result = new PorterStemFilter(result);
		return new TokenStreamComponents(tokenStream, result);
	}
}
