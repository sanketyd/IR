import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

public class Test {
	public static void main(String[] args) throws Exception{
		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get("index2")));
	    Fields fields = MultiFields.getFields(reader);
	    Terms terms = fields.terms("contents");
	    TermsEnum iterator = terms.iterator();
	    BytesRef bytref = null;
	    
	    long count = 0;
	    
	    while((bytref = iterator.next()) != null) {
	    	String term = new String(bytref.bytes, bytref.offset, bytref.length);
	    	if(term.toCharArray()[0] == 'म') System.out.println(term);
	    	count++;
	    }
	    
	    System.out.println(count);
	}
}
