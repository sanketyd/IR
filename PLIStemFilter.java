import java.io.IOException;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.*;

public class PLIStemFilter extends TokenFilter {
	private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
	private final KeywordAttribute keywordAtt = addAttribute(KeywordAttribute.class);
	private final PLIStemmer stemmer = new PLIStemmer();
	
	public PLIStemFilter(TokenStream input) {
		super(input);
	}

	@Override
	public boolean incrementToken() throws IOException {
		if(input.incrementToken()) {
			if(!keywordAtt.isKeyword() && stemmer.stem(termAtt.buffer(), termAtt.length())) termAtt.copyBuffer(stemmer.getResultBuffer(),0,stemmer.getResultLength());
			return true;
		} else {
			return false;
		}
	}

}