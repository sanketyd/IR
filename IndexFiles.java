import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.util.TokenFilterFactory;
import org.apache.lucene.document.*;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import javax.xml.parsers.*;
import org.w3c.dom.*;

public class IndexFiles {
	static long count = 0;
	
	public static void main(String[] args) {
		int combination = 2;
		String indexPath = "en-index" + Integer.toString(combination);
		String docsPath = "TELEGRAPH_UTF8";
		
		final Path docDir = Paths.get(docsPath);
		if(!Files.isReadable(docDir)) {
			System.exit(1);
		}
		
		Date start = new Date();
		
		try {
			System.out.println("Indexing to directory '" + indexPath + "'...");
			Directory dir = FSDirectory.open(Paths.get(indexPath));
			Analyzer analyzer;
			switch(combination) {
				case 1: analyzer = CustomAnalyzer.builder(Paths.get("/home/sanket/6thSem/CS657/Assignment1")).withTokenizer("standard").addTokenFilter("lowercase").addTokenFilter("stop","ignoreCase","true","words","stopwords.txt").addTokenFilter("ngram","min","3","max","5").build();
					break;
				case 2: analyzer = new PLIAnalyzer();
					break;
				default: analyzer = new StandardAnalyzer();
					break;
			}
			
			IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
			iwc.setSimilarity(new BM25Similarity());
			iwc.setOpenMode(OpenMode.CREATE);
			
			IndexWriter writer = new IndexWriter(dir, iwc);
			
			indexDocs(writer, docDir);
			
			writer.close();
			
			Date end = new Date();
			System.out.println(end.getTime() - start.getTime() + "ms\n");
			
			PrintWriter pwriter = new PrintWriter("timeIndex"+Integer.toString(combination)+".txt", "UTF-8");
                        pwriter.println(end.getTime() - start.getTime() + "ms\n");
                        pwriter.close();
		    
		} catch (IOException e) {
			System.out.println("Error!\n");
		}
		
	}
	
	static void indexDocs(final IndexWriter writer, Path path) throws IOException {
		if(Files.isDirectory(path)) {
			Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					try {
						indexDoc(writer, file);
					} catch (IOException ignore) {
						
					}
					return FileVisitResult.CONTINUE;
				}
			});
		} else {
			indexDoc(writer, path);
		}
	}
	
	static void indexDoc(IndexWriter writer, Path file) throws IOException {
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
			
			System.out.println(count++);
			
		} catch(Exception e) {
			System.out.println("Problem while Indexing The document" + e);
			e.printStackTrace();
		}
	}

}
