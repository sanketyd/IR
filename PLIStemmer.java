import static org.apache.lucene.analysis.util.StemmerUtil.*;

public class PLIStemmer {
	public int stem(char buffer[], int len) {
		if(len > 3) return 3;
		return len;
	}
}
