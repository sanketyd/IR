import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.Set;
import java.util.Vector;
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
	static Vector gamma = new Vector();

	public static void main(String[] args) throws IOException 
	{
		
		int flag=0;
	//Query parsing
		
		
		
	//Query Parsing	
		
		
	//Step 2
	Path path =  Paths.get("/home/ankit/Downloads/PLIS/PLIS/src/Stopwords").toAbsolutePath();
	List<String> list = Files.lines(path).collect(Collectors.toList());	
	
	String word = "Narutto";

	
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
			

			
			three_letter = word.substring(0,3);
				System.out.println(three_letter);

			

			
			

			return_words_from_document(three_letter, length);
			int gamma_size = gamma.size();
		
			System.out.println(gamma);
			System.out.println(((String) gamma.get(0)).length());
			
			
			//Step 5
			
		int n = word.length();
		int ED[] = new int [gamma_size];
		int index = 0;
		for(int k=0;k<gamma.size();k++)
		{	
			
			String element = (String)gamma.get(k);
			System.out.println(element);
			int m = element.length();
			System.out.println(m);
			int [][] mat = new int [n+1][m+1];
			
			
			for(int i=0;i< n+1;i++) 
			{
				
				for(int j=0;j< m+1;j++)
				{
					if(i==0 && j==0) 
					{
						mat[i][j]=0;
					}
					
					if(i==0 && j!=0) 
					{
						mat[i][j]=j;
					}
					
					if(j==0 && i!=0) 
					{
						mat[i][j]=i;
					}
				}
			}
			
			
			for(int i=1;i<n+1;i++) 
			{
				
				for(int j=1;j<m+1;j++)
				{
					
					
		
					
					if(word.charAt(i-1)==element.charAt(j-1) )
					{
						
						
						mat[i][j]=Math.min(Math.min(mat[i-1][j]+1, mat[i][j-1]+1),mat[i-1][j-1]);
						
					}
					
					
					if(word.charAt(i-1)!=element.charAt(j-1) ) 
					{
						
						mat[i][j]=Math.min(Math.min(mat[i-1][j]+1, mat[i][j-1]+1),mat[i-1][j-1]+2);
					
					}
					
				
				}
			
			}
			
			ED[index]=mat[n][m];
			System.out.println("ED");
			System.out.println(ED[index]);
			
			index = index+1;
			
		
		}
		
		
		
		
		
		int LCS[] = new int [gamma_size];
		index = 0;
		for(int k=0;k<gamma.size();k++)
		{	
			
			String element = (String)gamma.get(k);
			int m = element.length();
			int [][] mat = new int [n+1][m+1];
			
			
			for(int i=0;i< n+1;i++) 
			{
				
				for(int j=0;j< m+1;j++)
				{
					if(i==0) 
					{
						mat[i][j]=0;
					}
					if(j==0) 
					{
						mat[i][j]=0;
					}
				}
			}
			
			
			
			
			
			for(int i=1;i< n+1;i++) 
			{
				
				for(int j=1;j< m+1;j++)
				{
					
					
					if(i==0) 
					{
						mat[i][j]=0;
					}
					if(j==0) 
					{
						mat[i][j]=0;
					}
					
					if(word.charAt(i-1)==element.charAt(j-1))
					{
						mat[i][j]=mat[i-1][j-1]+1;
					}
				
					
					if(word.charAt(i-1)!=element.charAt(j-1)) 
					{
						mat[i][j]=Math.max(mat[i-1][j], mat[i][j-1]);
					}
				
				
				}
			
			}
			LCS[index]=mat[n][m];
			index = index+1;
		
		}
		
		//Step 6 and Step 7
		Vector vec1 = new Vector();
		Vector ED1 = new Vector();
		
		Vector LCS1 = new Vector();
		
		
		
		for(int i=0;i<gamma_size;i++) 
		{
			
			if(ED[i]+LCS[i]==word.length() && ED[i] < LCS[i]) 
			{
				System.out.println("ntoken4");
				vec1.add((String)gamma.get(i));
				ED1.add((int)ED[i]);
				LCS1.add((int)LCS[i]);
			}
			
		}
		
	
		int maxED1=(int)ED1.get(0);
		System.out.println("ntoken5");
		for(int i=1;i<gamma_size;i++) 
		{
			
			if((int)ED1.get(i)>maxED1) 
			{
				maxED1=(int)ED1.get(i);
			}
		}
			
			
		int minLCS1=(int)LCS1.get(0);
		for(int i=0;i<gamma_size;i++) 
		{
			if((int)LCS1.get(i)>minLCS1) 
			{
				minLCS1=(int)LCS1.get(i);
			}
		}
		
		Vector gamma_star = new Vector();
		for(int i=0;i<gamma_size;i++) 
		{
			if((int)ED1.get(i)==maxED1 && (int)LCS1.get(i)==minLCS1) 
			{
				gamma_star.add((String)gamma.get(i));
			}
		}
		
		
		if(gamma_star.size()==1) 
		{
			//return the single word as stem word and goto step 8
		}
		int min_len_index;
		int count =0;
		if(gamma_star.size()>1) 
		{
			int min_len=((String) gamma_star.get(0)).length();
			for(int i=0;i<gamma_star.size();i++) 
			{
				if(((String) gamma_star.get(i)).length()<min_len) 
				{
					min_len=((String) gamma_star.get(i)).length();
					min_len_index=i;
				}
			}
		
			for(int i=0;i<gamma_star.size();i++) 
			{
				if(((String) gamma_star.get(i)).length()==min_len) 
				{
					count=count+1;
				}
			}
			
			if(count > 1) 
			{
				//return most similar word and goto step 8
			}
			
			
			
			//return gamma_star.get(min_len_index) and goto step 8
			
		}
	
	
		
		
		
			
			
			

			
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
		  
    	    String three_letter;
				three_letter = nextToken.substring(0, 3);

		    
		    
		    
		    if (three_letter.equalsIgnoreCase(word))
		    {
		    	
		    	gamma.add(nextToken);
		    	
		    }
		}
		return ;
	}
	
	
	

}