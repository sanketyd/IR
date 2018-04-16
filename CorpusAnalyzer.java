import java.io.IOException;
import java.io.Reader;

import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.Analyzer.TokenStreamComponents;
import org.apache.lucene.analysis.core.DecimalDigitFilter;
import org.apache.lucene.analysis.in.IndicNormalizationFilter;
import org.apache.lucene.analysis.miscellaneous.SetKeywordMarkerFilter;
import org.apache.lucene.analysis.standard.*;

import org.apache.lucene.analysis.hi.*;

public class CorpusAnalyzer extends StopwordAnalyzerBase {
	private final CharArraySet stemExclusionSet;
	
	public final static String DEFAULT_STOPWORD_FILE = "stopwords.txt";
	private static final String STOPWORDS_COMMENT = "#";
	
	public static CharArraySet getDefaultStopSet() {
		return DefaultSetHolder.DEFAULT_STOP_SET;
	}
	
	private static class DefaultSetHolder {
		static final CharArraySet DEFAULT_STOP_SET;
		
		static {
			try {
				DEFAULT_STOP_SET = loadStopwordSet(false, CorpusAnalyzer.class, DEFAULT_STOPWORD_FILE, STOPWORDS_COMMENT);
			} catch (IOException ex) {
				throw new RuntimeException("Unable to load default stopword set");
			}
		}
	}
	
	public CorpusAnalyzer(CharArraySet stopwords, CharArraySet stemExclusionSet) {
		super(stopwords);
		this.stemExclusionSet = CharArraySet.unmodifiableSet(CharArraySet.copy(stemExclusionSet));
	}
	
	public CorpusAnalyzer(CharArraySet stopwords) {
		this(stopwords, CharArraySet.EMPTY_SET);
	}
	
	public CorpusAnalyzer() {
		this(DefaultSetHolder.DEFAULT_STOP_SET);
	}


	@Override
	protected TokenStreamComponents createComponents(String arg0) {
		final Tokenizer source = new StandardTokenizer();
		TokenStream result = new LowerCaseFilter(source);
		result = new DecimalDigitFilter(result);
		if(!stemExclusionSet.isEmpty()) result = new SetKeywordMarkerFilter(result, stemExclusionSet);
		result = new IndicNormalizationFilter(result);
		result = new HindiNormalizationFilter(result);
		result = new StopFilter(result, stopwords);
		return new TokenStreamComponents(source, result);
	}
	
	@Override
	protected TokenStream normalize(String fieldName, TokenStream in) {
		TokenStream result = new StandardFilter(in);
		result = new LowerCaseFilter(result);
		result = new DecimalDigitFilter(result);
		result = new IndicNormalizationFilter(result);
		result = new HindiNormalizationFilter(result);
		return result;
	}

}
