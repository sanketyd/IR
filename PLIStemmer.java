import static org.apache.lucene.analysis.util.StemmerUtil.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

import javax.xml.parsers.*;
import org.w3c.dom.*;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.*;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.*;
import org.apache.lucene.util.BytesRef;

public class PLIStemmer {
	private char[] b;	//Contains stemmed word, Idea from lucene source code
	private int i;	//Length of stemmed word
	private int k;	//How many characters to match
	private Set<String> allTerms;	//A set that contains all terms
	
	public int getResultLength() { return i; }
	
	public char[] getResultBuffer() { return b; }
	
	//This Function calculate edit distance between 2 words
	private int EditDistance(char[] buffer, int len_buffer, char[] prefixMatchTerm, int len_pmt) {
		int[][] D = new int[len_buffer+1][len_pmt+1];
		
		for(int i = 0; i <= len_buffer; i++) {
			for(int j = 0; j <= len_pmt; j++) {
				if(i==0 && j==0) D[i][j]=0;
				else if(i==0) D[i][j] = j;
				else if(j==0) D[i][j] = i;
				else if(buffer[i-1] == prefixMatchTerm[j-1]) D[i][j] = Math.min(D[i-1][j-1], Math.min(D[i-1][j]+1, D[i][j-1]+1));
				else D[i][j] = Math.min(D[i-1][j]+1, Math.min(D[i][j-1]+1, D[i-1][j-1]+2));
			}
		}
		
		return D[len_buffer][len_pmt];
	}
	
	//This Function returns Longest Common Sequence
	private int LCS(char[] buffer, int len_buffer, char[] prefixMatchTerm, int len_pmt) {
		int[][] D = new int[len_buffer+1][len_pmt+1];
		
		for(int i = 0; i <= len_buffer; i++) {
			for(int j = 0; j <= len_pmt; j++) {
				if(i==0 || j==0) D[i][j] = 0;
				else if(buffer[i-1] == prefixMatchTerm[j-1]) D[i][j] = D[i-1][j-1]+1;
				else D[i][j] = Math.max(D[i-1][j], D[i][j-1]);
			}
		}
		
		return D[len_buffer][len_pmt];
	}
	
	//This function calculates matching characters
	private int CommonChars(char[] buffer, int len_buffer, char[] matchTerm, int len_mt) {
		int commonChars = k;
		
		for(int i = k; i < Math.min(len_buffer, len_mt); i++) {
			if(buffer[i] == matchTerm[i]) commonChars++;
		}
		
		return commonChars;
	}
	
	public boolean stem(char[] buffer, int len) {
		if(len < k) return false;
		
		Set<String> prefixMatchSet = new HashSet<String>();
		//Following terms are from paper this is based on
		Vector<String> gammaStar = new Vector<String>();
		Vector<Integer> EDStar = new Vector<Integer>();
		Vector<Integer> LCSStar = new Vector<Integer>();
		
		/***************************Get terms having first k letters common***************************/
		for(String term:allTerms) {
			if(startsWith(term.toCharArray(), term.length(), String.copyValueOf(buffer, 0, k))) prefixMatchSet.add(term);
		}
		/*********************************************************************************************/
		
		int n = prefixMatchSet.size();
		int[] ED = new int[n];
		int[] LCS = new int[n];
		int index = 0, maxED = -1, minLCS = Integer.MAX_VALUE;
		
		for(String term:prefixMatchSet) {
			ED[index] = EditDistance(buffer, len, term.toCharArray(), term.length());
			LCS[index] = LCS(buffer, len, term.toCharArray(), term.length());
			if(ED[index]+LCS[index] == len && ED[index] < LCS[index]) {
				gammaStar.add(term);
				EDStar.add(ED[index]);
				LCSStar.add(LCS[index]);
				if(maxED < ED[index]) maxED = ED[index];
				if(minLCS > LCS[index]) minLCS = LCS[index];
			}
			index++;
		}
		
		Vector<String> candidates = new Vector<String>();
		
		for(index = 0; index < gammaStar.size(); index++) {
			if(EDStar.get(index) == maxED && LCSStar.get(index) == minLCS) {
				candidates.add(gammaStar.get(index));
			}
		}
		
		if(candidates.size() == 1) {
			b = candidates.get(0).toCharArray();
			i = candidates.get(0).length();
			return true;
		} else if(candidates.size() > 1) {
			int minLength = Integer.MAX_VALUE;
			for(index = 0; index < candidates.size(); index++) {
				if(minLength > candidates.get(index).length()) {
					minLength = candidates.get(index).length();
				}
			}
			
			Vector<String> smallest = new Vector<String>();
			for(index = 0; index < candidates.size(); index++) {
				if(minLength == candidates.get(index).length()) smallest.add(candidates.get(index));
			}
			
			if(smallest.size() == 1) {
				b = smallest.get(0).toCharArray();
				i = candidates.get(0).length();
				return true;
			} else {
				String finalCandidate = "";
				int maxCharMatch = Integer.MIN_VALUE;
				for(index = 0; index < smallest.size(); index++) {
					int tempMaxLength = CommonChars(buffer, len, smallest.get(index).toCharArray(), smallest.get(index).length());
					if(maxCharMatch < tempMaxLength) {
						maxCharMatch = tempMaxLength;
						finalCandidate = smallest.get(index);
					}
				}
				b = finalCandidate.toCharArray();
				i = finalCandidate.length();
				return true;
			}
		}
		
		return false;
		
	}
	
	PLIStemmer() {
		try {
			k = 3;
			b = new char[50];
			allTerms = new HashSet<String>();
			String indexPath = "hiIndex";
			IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexPath)));
		    Fields fields = MultiFields.getFields(reader);
		    Terms terms = fields.terms("contents");
		    TermsEnum iterator = terms.iterator();
		    BytesRef bytref = null;
		    while((bytref = iterator.next()) != null) {
		    	String term = new String(bytref.bytes, bytref.offset, bytref.length);
		    	this.allTerms.add(term);
		    }
//		    long  count = 0;
//		    for(String term:allTerms) {
//		    	if(stem(term.toCharArray(), term.length())) System.out.println(term + " : " + String.copyValueOf(getResultBuffer()));
//		    	count++;
//		    }
//		    System.out.println(count);
		} catch(Exception e) {
			System.out.println(e);
		}
	}
	
	PLIStemmer(String docsPath, String language) {
		k = 3;
		b = new char[50];
		allTerms = new HashSet<String>();
		/***********************************Indexing in RAM Directory to extract terms*******************/
		final  Path docsDir = Paths.get(docsPath);
		try {
			Directory dir = new RAMDirectory();
//			Directory dir = FSDirectory.open(Paths.get("hiIndex"));
			
			Analyzer analyzer;
			
			if(language == "hindi") analyzer = new CorpusAnalyzer();
			else analyzer = new StopAnalyzer();
			
			IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
			iwc.setOpenMode(OpenMode.CREATE);
			IndexWriter writer = new IndexWriter(dir, iwc);
			
			indexDocs(writer, docsDir);
			
			writer.close();
			
			IndexReader reader = DirectoryReader.open(dir);
			
			Fields fields = MultiFields.getFields(reader);
		    Terms terms = fields.terms("contents");
		    TermsEnum iterator = terms.iterator();
		    BytesRef bytref = null;
		    while((bytref = iterator.next()) != null) {
		    	String term = new String(bytref.bytes, bytref.offset, bytref.length);
		    	this.allTerms.add(term);
		    }
		    
		    reader.close();
		    dir.close();
		    
		    for(String term: allTerms) {
		    	if(stem(term.toCharArray(), term.length())) System.out.println(term + " : " + String.copyValueOf(getResultBuffer()));
		    	else System.out.println(term);
		    }
		    
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void indexDocs(IndexWriter writer, Path path) throws IOException {
		if(Files.isDirectory(path)) {
			Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					try {
						indexDoc(writer,file);
					} catch (IOException e) {
						System.out.println("Exception occured while walking file tree " + e);
					}
					return FileVisitResult.CONTINUE;
				}
			});
		} else {
			indexDoc(writer,path);
		}
	}

	private static void indexDoc(IndexWriter writer, Path file) throws IOException {
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			org.w3c.dom.Document xmldoc = dBuilder.parse(new File(file.toString()));
			xmldoc.getDocumentElement().normalize();
			
			Document doc = new Document();
			
			Field pathField = new StringField("path", xmldoc.getElementsByTagName("DOCNO").item(0).getTextContent(), Field.Store.YES);
			doc.add(pathField);
			
			doc.add(new TextField("contents", xmldoc.getElementsByTagName("TEXT").item(0).getTextContent(), Field.Store.NO));
			
			writer.addDocument(doc);
			
		} catch(Exception e) {
			System.out.println("Exception while indexing files " + e);
		}
	}
	
	
//	//TODO: Remove from it once done testing
//	public static void main(String[] args) {
//		PLIStemmer s = new PLIStemmer("hi.doc.2010-copy","hindi");
//	}
}
