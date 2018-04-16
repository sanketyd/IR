import java.io.IOException;

import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.StopwordAnalyzerBase;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.en.EnglishPossessiveFilter;
import org.apache.lucene.analysis.miscellaneous.SetKeywordMarkerFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;

public class PLIEnglishAnalyzer extends StopwordAnalyzerBase {
	
	private final CharArraySet stemExclusionSet;
	
	public final static String DEFAULT_STOPWORD_FILE = "stopwords-en.txt";
	private static final String STOPWORDS_COMMENT = "#";
	
	public static CharArraySet getDefaultStopSet() {
		return DefaultSetHolder.DEFAULT_STOP_SET;
	}
	
	private static class DefaultSetHolder {
		static final CharArraySet DEFAULT_STOP_SET;
		
		static {
			try {
				DEFAULT_STOP_SET = loadStopwordSet(false, PLIEnglishAnalyzer.class, DEFAULT_STOPWORD_FILE, STOPWORDS_COMMENT);
			} catch (IOException ex) {
				throw new RuntimeException("Unable to load default stopword set");
			}
		}
	}
	
	public PLIEnglishAnalyzer(CharArraySet stopwords, CharArraySet stemExclusionSet) {
		super(stopwords);
		this.stemExclusionSet = CharArraySet.unmodifiableSet(CharArraySet.copy(stemExclusionSet));
	}
	
	public PLIEnglishAnalyzer(CharArraySet stopwords) {
		this(stopwords, CharArraySet.EMPTY_SET);
	}
	
	public PLIEnglishAnalyzer() {
		this(DefaultSetHolder.DEFAULT_STOP_SET);
	}

	@Override
	protected TokenStreamComponents createComponents(String arg0) {
		final Tokenizer source = new StandardTokenizer();
	    TokenStream result = new StandardFilter(source);
	    result = new EnglishPossessiveFilter(result);
	    result = new LowerCaseFilter(result);
	    result = new StopFilter(result, stopwords);
	    if(!stemExclusionSet.isEmpty())
	      result = new SetKeywordMarkerFilter(result, stemExclusionSet);
	    result = new PLIStemFilter(result);
	    return new TokenStreamComponents(source, result);
	}
	
	@Override
	  protected TokenStream normalize(String fieldName, TokenStream in) {
	    TokenStream result = new StandardFilter(in);
	    result = new LowerCaseFilter(result);
	    return result;
	}

}
