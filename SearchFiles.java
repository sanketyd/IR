import java.io.*;
import java.nio.file.*;
import java.util.*;

import javax.xml.parsers.*;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.analysis.hi.HindiAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.*;
import org.apache.lucene.search.similarities.*;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.QueryBuilder;
import org.w3c.dom.*;

public class SearchFiles {
	
	public static void main(String[] args) {
		int combination = 1;
		String index = "hi-index" + Integer.toString(combination);
		String field = "contents";
		int k = 15;
		
		try {
			/*******************Read Ground Truth*******************/
			Map<String, Set<String> > AnswerMap = new HashMap<String,Set<String> >();
			
			BufferedReader bufferedReader = new BufferedReader(new FileReader("/home/sanket/6thSem/CS657/IR/hi.qrels.76-125.2010.1.txt"));
			
			String line = null;
			
			while((line = bufferedReader.readLine()) != null) {
				String[] parts = line.split(" ");
				if(AnswerMap.get(parts[0]) == null) {
					Set<String> temp = new HashSet<String>();
					AnswerMap.put(parts[0], temp);
				}
				AnswerMap.get(parts[0]).add(parts[2]);
//				System.out.println(parts[2]);
			}
			
			bufferedReader.close();
			/*******************************************************/
			IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(index)));
			IndexSearcher searcher = new IndexSearcher(reader);
			
			searcher.setSimilarity(new BM25Similarity());
			
			Analyzer analyzer;
			
			switch(combination) {
			case 1: analyzer = CustomAnalyzer.builder(Paths.get("/home/sanket/6thSem/CS657/Assignment1")).withTokenizer("standard").addTokenFilter("lowercase").addTokenFilter("stop","ignoreCase","true","words","stopwords.txt").addTokenFilter("ngram").build();
			break;
			case 2: analyzer = new PLIAnalyzer();
			break;
			default: analyzer = new StandardAnalyzer();
			break;
			}
			
			QueryBuilder builder = new QueryBuilder(analyzer);
			
			File file = new File("/home/sanket/6thSem/CS657/IR/hi.topics.76-125.2010.txt");
			
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			org.w3c.dom.Document xmldoc = dBuilder.parse(file);
			
			xmldoc.getDocumentElement().normalize();
			
			NodeList numList = xmldoc.getElementsByTagName("num");
			NodeList qList = xmldoc.getElementsByTagName("title");
			
			/********************Query Results**********************/
			Map<String, String[]> queryResults = new HashMap<String, String[]>();
			/*******************************************************/
			
			PrintWriter qrelWriter = new PrintWriter("qrel"+Integer.toString(combination)+"k"+Integer.toString(k), "UTF-8");
			PrintWriter scoreWriter = new PrintWriter("score"+Integer.toString(combination)+"k"+Integer.toString(k)+".csv", "UTF-8");
			scoreWriter.println("query no., Returned" + ",P" + ",intersection" +", precision, recall, Fscore");
			
			for(int queryNo = 0; queryNo < numList.getLength(); queryNo++) {
				Query query = builder.createBooleanQuery(field, qList.item(queryNo).getTextContent(), BooleanClause.Occur.MUST);
				System.out.println(query.toString());
				TopDocs results = searcher.search(query, 200000);
				ScoreDoc[] hits = results.scoreDocs;
				
				/*******************************************************/
				String[] temp = new String[k];
				queryResults.put(numList.item(queryNo).getTextContent(), temp);
				/*******************************************************/
				
				/*******************************************************/
				Set<String> querySet = new HashSet<String>();
				/*******************************************************/
				
				int numTotalHits = hits.length;
				for(int resultNo = 0; resultNo < numTotalHits; resultNo++) {
					Document doc = searcher.doc(hits[resultNo].doc);
					String docName = doc.get("path");
					
					
					if(resultNo < k) {
                        /*******************************************************/
						queryResults.get(numList.item(queryNo).getTextContent())[resultNo] = docName;
                        /*******************************************************/
						qrelWriter.println(numList.item(queryNo).getTextContent() + " Q0 " + docName + " 0");
						qrelWriter.println(searcher.explain(query, hits[resultNo].doc));
					}
					
					querySet.add(docName);
				}
				
				Set<String> intersection = new HashSet<String>(querySet);
				Set<String> answer = AnswerMap.get(numList.item(queryNo).getTextContent());
				intersection.retainAll(answer);
				scoreWriter.println(numList.item(queryNo).getTextContent() + "," + querySet.size() + "," + answer.size() + "," + intersection.size() +"," + getPRF(querySet.size(), intersection.size(), answer.size()));
			}
			
			qrelWriter.close();
                        scoreWriter.close();
			
			/***********************Calculate AP***********************/
			PrintWriter statWriter = new PrintWriter("stats"+Integer.toString(combination)+"k"+Integer.toString(k),"UTF-8");
			float MAP = 0f;
			for(int queryNo = 0; queryNo < numList.getLength(); queryNo++) {
				String realQueryNo = numList.item(queryNo).getTextContent();
				int R = AnswerMap.get(realQueryNo).size();
				int relevantCount = 0;
				float AP = 0f;
				String[] answers = queryResults.get(realQueryNo);
				for(int i = 0; i < k; i++) {
					if(AnswerMap.get(realQueryNo).contains(answers[i])) {
//						System.out.println(answers[i]);
						relevantCount++;
						AP += ((float)relevantCount)/((float)(i+1));
					}
				}
				AP = AP/R;
				MAP += AP;
				statWriter.println("Query No. " + realQueryNo + " Average Precision @ " + k + " : " + AP);
			}
			MAP = MAP/((float)numList.getLength());
			statWriter.println("Mean Average Precision @" + k + " : " + MAP);
			statWriter.close();
			/**********************************************************/
			
			System.out.println("Done");
		
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static String getPRF(int P_ , int TP, int P) {
		float Recall = (float)TP/(float)P;
		float Precision = (float)TP/(float)P_;
		float FScore;
		
		if(Recall==0 || Precision==0) FScore = 0.0f;
		else {
			FScore = (2*Recall*Precision)/(Recall + Precision);
		}
		return Float.toString(Precision) + "," + Float.toString(Recall) + "," + Float.toString(FScore);
	}

}
