import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.Set;
import java.awt.ComponentOrientation;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

public class PLIS 
{
	 static Set<String> gamma = new HashSet<String>();
	public static void main(String[] args) throws IOException 
	{
		int flag=0;
	//Query parsing
		
		
		
	//Query Parsing	
		
		
	//Step 2
	Path path =  Paths.get("/home/ankit/Downloads/PLIS/PLIS/src/Stopwords").toAbsolutePath();
	List<String> list = Files.lines(path).collect(Collectors.toList());	
	
	String word = "ىشقعففخ";

	
	boolean inFile = list.stream().anyMatch(p->p.equalsIgnoreCase(word));
	if(inFile) 
	{
		
		System.out.println("nextToken0");
		//Step 8	
		
	}
	else 
	{
		//Step 3 
		int length = word.length();
		
		
		if(length < 3) 
		{
			System.out.println("nextToken3");
			//return the word as the stem word
		}
		else 
		{
			String three_letter;
			
			//Step 4
			
//			char[] chars = word.toCharArray();
//			for(char c: chars){
//			    if(c >= 0x600 && c <= 0x6ff){
//			    	System.out.println("yolo");
//			        flag=1;
//			        System.out.println("yolo2");
//			        break;
//			     }
//			}
			
/*			if(flag==1) 
			{
				;
				three_letter = word.substring(length-3,length);
				
			}
			else 
			{
				
*/				three_letter = word.substring(0,3);
//			}
			
//			String s= "مورومومرممورمو";
			
			

			return_words_from_document(three_letter, length);
			System.out.println(gamma);
		}
	}
		
  
	
	
	}
	public static void return_words_from_document(String word, int length) throws FileNotFoundException 
	{
		int count = 0;
	
		Scanner scanner = new Scanner(new File("/home/ankit/Downloads/PLIS/PLIS/src/Stopwords"));
		
		
		while (scanner.hasNext())
		{	
			
		    String nextToken = scanner.next();
		    //System.out.println(nextToken);
    	    String three_letter;
/*		    if(flag==1) 
			{
		    
				three_letter = nextToken.substring(length-3,length);
		}
			else 
			{
*/				three_letter = nextToken.substring(0, 3);
//			}
		    
		    
		    
		    if (three_letter.equalsIgnoreCase(word))
		    {
		    	
		    	gamma.add(nextToken);
		    	
		    }
		}
		return ;
	}
	
	
	

}