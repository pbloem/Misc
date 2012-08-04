package nl.peterbloem.languages;

import java.io.*;
import java.util.*; 

import org.lilian.Global;
import org.lilian.models.BasicFrequencyModel;
import org.lilian.util.Functions;

import au.com.bytecode.opencsv.CSVReader;

public class Main {

	private static final String CHINESE_DATA = "data/chinese/modern.csv";
	private static final String CROATIAN_DATA = "data/croatian/manual.csv";
	private static final String ITALIAN_DATA = "data/italian/italian.csv";
	private static final String FRENCH_DATA = "data/french/french.csv";
	private static final String GERMAN_DATA = "data/german/german.csv";
	private static final String SPANISH_DATA = "data/spanish/spanish.csv";
	
	
	private static int subsetSize;
	
	private static int topN;
	
	// Whether to use a word category, or just the top n
	private static boolean useCategory = false;
	
	private static Console console = System.console();
	
	/**
	 * @param args
	 */
	public static void main(String[] args) 
		throws IOException
	{
		Global.random = new Random();
		while(true)
		{		

			Functions.tic();
			
			String language = console.readLine("Which language would you like to practice? (chinese, croatian, italian, french, or enter for random)");
			
			language = language.trim().toLowerCase();
			if(language.equals(""))
				if(Math.random() < 0.2)
					language = "chinese";
				if(Math.random() < 0.4)
					language = "italian";
				if(Math.random() < 0.6)
					language = "french";
				if(Math.random() < 0.8)
					language = "german";
				if(Math.random() < 0.98)
					language = "spanish";
				else
					language = "croatian";
			
			if(language.equals("chinese"))
				chinese();
			if(language.equals("croatian"))
				croatian();
			if(language.equals("italian"))
				generic(ITALIAN_DATA);
			if(language.equals("french"))
				generic(FRENCH_DATA);
			if(language.equals("german"))
				generic(GERMAN_DATA);
			if(language.equals("spanish"))
				generic(SPANISH_DATA);
			
			System.out.println("Time taken: " + Functions.toc()/60.0 + " minutes");
		}
	}
	
	private static void croatian() throws IOException 
	{
		String etc = console.readLine("English to croatian?");
		boolean e2c = etc.trim().toLowerCase().equals("y");		
		
		InputStream in = Main.class.getClassLoader()					
                .getResourceAsStream(CROATIAN_DATA);
		
		if(in == null)
			throw new IOException("Resource " + CROATIAN_DATA + " not found.");			
		
	    CSVReader reader = new CSVReader(new InputStreamReader(in));
	    List<String[]> tokensRaw = reader.readAll();
	    List<CroatianToken> tokens = new ArrayList<CroatianToken>(tokensRaw.size());
	    
	    int i = 0;
	    for(String[] raw: tokensRaw)
	    {
	    	i++;
	    	try {
	    		tokens.add(new CroatianToken(Integer.parseInt(raw[0]), raw[1], raw[2], raw[3], raw[4], e2c));
	    	} catch (Exception e)
	    	{
	    		throw new RuntimeException("Error in line "+i+" of csv file. ("+Arrays.toString(raw)+")", e);
	    	}
	    }
	    
	    System.out.println("Number of tokens " + tokens.size());		
		
		String category = console.readLine("Which category of tokens (weekday, numbers, months, number for top n)?");
		if(isParsableToInt(category))
			topN = Integer.parseInt(category);
		else
			useCategory = true;
		
	    if(! useCategory)
	    	tokens = tokens.subList(0, topN);
	    else
	    {
	    	Iterator<CroatianToken> it = tokens.iterator();
	    	while(it.hasNext())
	    		if(! (it.next().category().contains(category)))
	    			it.remove();
	    }
	    
		String numChar = console.readLine("How many characters (max "+tokens.size()+")?");
		subsetSize = Integer.parseInt(numChar);	    	    
	    
	    BasicFrequencyModel<CroatianToken> model = new BasicFrequencyModel<CroatianToken>();
	    
	    for(CroatianToken c: tokens)
    		model.add(c, c.weight());
	    
        List<CroatianToken> selection = new ArrayList<CroatianToken>(
        		model.chooseWithoutReplacement(subsetSize));
        
        quiz(selection);
	}	
	
	private static void generic(String dataSource) throws IOException 
	{
		String etc = console.readLine("English to language?");
		boolean e2c = etc.trim().toLowerCase().equals("y");		
		
		InputStream in = Main.class.getClassLoader()					
                .getResourceAsStream(dataSource);
		
		if(in == null)
			throw new IOException("Resource " + dataSource + " not found.");			
		
	    CSVReader reader = new CSVReader(new InputStreamReader(in));
	    List<String[]> tokensRaw = reader.readAll();
	    List<GenericToken> tokens = new ArrayList<GenericToken>(tokensRaw.size());
	    
	    int i = 0;
	    for(String[] raw: tokensRaw)
	    {
	    	i++;
	    	try {
	    		tokens.add(new GenericToken(Integer.parseInt(raw[0]), raw[1], raw[2], raw[3], raw[4], e2c));
	    	} catch (Exception e)
	    	{
	    		throw new RuntimeException("Error in line "+i+" of csv file. ("+Arrays.toString(raw)+")", e);
	    	}
	    }
	    
	    System.out.println("Number of tokens " + tokens.size());		
		
		String category = console.readLine("Which category of tokens (vocab, verbs, ..., number for top n)?");
		if(isParsableToInt(category))
			topN = Integer.parseInt(category);
		else
			useCategory = true;
		
	    if(! useCategory)
	    	tokens = tokens.subList(0, topN);
	    else
	    {
	    	Iterator<GenericToken> it = tokens.iterator();
	    	while(it.hasNext())
	    		if(! (it.next().category().contains(category)))
	    			it.remove();
	    }
	    
		String numChar = console.readLine("How many characters (max "+tokens.size()+")?");
		subsetSize = Integer.parseInt(numChar);	    	    
	    
	    BasicFrequencyModel<GenericToken> model = new BasicFrequencyModel<GenericToken>();
	    
	    for(GenericToken c: tokens)
    		model.add(c, c.weight());
	    
        List<GenericToken> selection = new ArrayList<GenericToken>(
        		model.chooseWithoutReplacement(subsetSize));
        
        quiz(selection);
	}		
	
	public static void chinese() throws IOException
	{
		String category = console.readLine("Which category of characters (direction, function, verb, time, location, food, family, prepposition ,part, basic, number for top n)?");
		if(isParsableToInt(category))
			topN = Integer.parseInt(category);
		else
			useCategory = true;
		
		InputStream in = Main.class.getClassLoader()					
                .getResourceAsStream(CHINESE_DATA);
		
		if(in == null)
			throw new IOException("Resource " + CHINESE_DATA + " not found.");			
		
	    CSVReader reader = new CSVReader(new InputStreamReader(in));
	    List<String[]> charactersRaw = reader.readAll();
	    List<Character> characters = new ArrayList<Character>(charactersRaw.size());
	    
	    for(String[] raw: charactersRaw)
	    	characters.add(new Character(raw[1], raw[3],raw[4], raw[5], 
	    			Integer.parseInt(raw[0]), Integer.parseInt(raw[2])));
	    
	    System.out.println("Number of characters " + characters.size());
	    
	    if(! useCategory)
	    	characters = characters.subList(0, topN);
	    else
	    {
	    	Iterator<Character> it = characters.iterator();
	    	while(it.hasNext())
	    		if(! (it.next().category().contains(category)))
	    			it.remove();
	    }
	    
		String numChar = console.readLine("How many characters (max "+characters.size()+")?");
		subsetSize = Integer.parseInt(numChar);	    	    
	    
	    BasicFrequencyModel<Character> model = new BasicFrequencyModel<Character>();
	    
	    for(Character c: characters)
    		model.add(c, c.frequency());
	    
        List<Character> selection = new ArrayList<Character>(
        		model.chooseWithoutReplacement(subsetSize));
        
        quiz(selection);
	}
	
	public static void quiz(List<? extends Token> selection)
	{
        Token last = null, choice = null;
        // Main loop
        while(selection.size() > 0)
        {
        	console.printf(selection.size() + " characters left\n");
        	
        	boolean selected = false;
        	
        	last = choice;
        	while(!selected)
        	{
        		 choice = selection.get(Global.random.nextInt(selection.size()));
        		 if(last == null || selection.size() == 1)
        			 selected = true;
        		 else if(!last.equals(choice)) // make sure the same token 
        			 selected = true;          // does not appear twice in a row
        	}
        	
        	assert(choice != null);
        	
        	console.readLine(choice.hint());
        	console.printf(choice.full() + "\n");
        	
        	String answer = "";
        		
       		answer = console.readLine("Remove? (y or any key)");
       		
        	console.printf(((char) 27)+"[2J"); // ANSI clear screen...
        	
        	if(answer.equals("y"))
        	{
        		selection.remove(choice);
        		console.printf(choice.hint() + " removed. \n");
        	}
        }
        
    	console.printf(((char) 27)+"[2J"); // ANSI clear screen...	        
	}   
	
	public static interface Token
	{
		/**
		 * The "question"
		 * @return
		 */
		public String hint();
		
		/**
		 * The full answer
		 * @return
		 */
		public String full();
	}
	
	public static class Character implements Token
	{
		private String character;
		private String pinYin;
		private String translation;
		private String category;
		
		private int rank;
		private int frequency;
		
		public Character(
				String character, String pinYin, 
				String translation, String category,
				int rank, int frequency)
		{
			this.character = character;
			this.pinYin = pinYin;
			this.translation = translation;
			this.category = category;
			
			this.rank = rank;
			this.frequency = frequency;
		}
		
		public String character()
		{
			return character;
		}
		
		public String pinYin()
		{
			return pinYin;
		}
		public String translation()
		{
			return translation;
		}
		public String category()
		{
			return category;
		}
		public int rank()
		{
			return rank;
		}
		
		public int frequency()
		{
			return frequency;
		}
		
		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((character == null) ? 0 : character.hashCode());
			result = prime * result
					+ ((pinYin == null) ? 0 : pinYin.hashCode());
			result = prime * result
					+ ((translation == null) ? 0 : translation.hashCode());
			result = prime * result
					+ ((category == null) ? 0 : category.hashCode());
			result = prime * result + rank;
			result = prime * result + frequency;
			
			return result;
		}
		
		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Character other = (Character) obj;
			if (character == null)
			{
				if (other.character != null)
					return false;
			} else if (!character.equals(other.character))
				return false;
			if (pinYin == null)
			{
				if (other.pinYin != null)
					return false;
			} else if (!pinYin.equals(other.pinYin))
				return false;
			if (translation == null)
			{
				if (other.translation != null)
					return false;
			} else if (!translation.equals(other.translation))
				return false;
			
			if (category == null)
			{
				if (other.category != null)
					return false;
			} else if (!category.equals(other.category))
				return false;
			
			if (! (rank == other.rank))
				return false;

			if (! (frequency == other.frequency))
				return false;
			
			return true;
			
		}

		@Override
		public String toString()
		{
			return "[" + character + ", " + pinYin
					+ ", \'" + translation + "\", " + category + ", r=" +rank + ", f=" + frequency + "]";
		}

		public String hint() 
		{
			return character();
		}

		public String full() 
		{
			return pinYin() + " " + translation();
		}
	}
	
	public static class CroatianToken implements Token 
	{
		private int weight;
		private String english;
		private String croatian;
		private String pronunciation;
		private String category;
		private boolean etc;
		
		public CroatianToken(int weight, String english, String croatian,
				String pronunciation, String category, boolean etc) 
		{
			this.weight = weight;
			this.english = english;
			this.croatian = croatian;
			this.pronunciation = pronunciation;
			this.category = category;
			this.etc = etc;
			
		}

		public String hint() 
		{
			return etc ? english : croatian;
		}

		public String full() 
		{
			return etc 
					? croatian + " (" + pronunciation + ") "
				    : english + " (" + pronunciation + ") ";
		}
		
		public String category()
		{
			return category;
		}
		
		public int weight()
		{
			return weight;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((category == null) ? 0 : category.hashCode());
			result = prime * result
					+ ((croatian == null) ? 0 : croatian.hashCode());
			result = prime * result
					+ ((english == null) ? 0 : english.hashCode());
			result = prime * result
					+ ((pronunciation == null) ? 0 : pronunciation.hashCode());
			result = prime * result + weight;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			CroatianToken other = (CroatianToken) obj;
			if (category == null) {
				if (other.category != null)
					return false;
			} else if (!category.equals(other.category))
				return false;
			if (croatian == null) {
				if (other.croatian != null)
					return false;
			} else if (!croatian.equals(other.croatian))
				return false;
			if (english == null) {
				if (other.english != null)
					return false;
			} else if (!english.equals(other.english))
				return false;
			if (pronunciation == null) {
				if (other.pronunciation != null)
					return false;
			} else if (!pronunciation.equals(other.pronunciation))
				return false;
			if (weight != other.weight)
				return false;
			return true;
		}

	}
	
	public static class GenericToken implements Token 
	{
		private int weight;
		private String english;
		private String croatian;
		private String pronunciation;
		private String category;
		private boolean etc;
		
		public GenericToken(int weight, String english, String croatian,
				String pronunciation, String category, boolean etc) 
		{
			this.weight = weight;
			this.english = english;
			this.croatian = croatian;
			this.pronunciation = pronunciation;
			this.category = category;
			this.etc = etc;
			
		}

		public String hint() 
		{
			return etc ? english : croatian;
		}

		public String full() 
		{
			return etc 
					? croatian + " (" + pronunciation + ") "
				    : english + " (" + pronunciation + ") ";
		}
		
		public String category()
		{
			return category;
		}
		
		public int weight()
		{
			return weight;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((category == null) ? 0 : category.hashCode());
			result = prime * result
					+ ((croatian == null) ? 0 : croatian.hashCode());
			result = prime * result
					+ ((english == null) ? 0 : english.hashCode());
			result = prime * result + (etc ? 1231 : 1237);
			result = prime * result
					+ ((pronunciation == null) ? 0 : pronunciation.hashCode());
			result = prime * result + weight;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			GenericToken other = (GenericToken) obj;
			if (category == null) {
				if (other.category != null)
					return false;
			} else if (!category.equals(other.category))
				return false;
			if (croatian == null) {
				if (other.croatian != null)
					return false;
			} else if (!croatian.equals(other.croatian))
				return false;
			if (english == null) {
				if (other.english != null)
					return false;
			} else if (!english.equals(other.english))
				return false;
			if (etc != other.etc)
				return false;
			if (pronunciation == null) {
				if (other.pronunciation != null)
					return false;
			} else if (!pronunciation.equals(other.pronunciation))
				return false;
			if (weight != other.weight)
				return false;
			return true;
		}
		
		
	}	

	public static boolean isParsableToInt(String i)
	{
		try
		{
			Integer.parseInt(i);
			return true;
		} catch(NumberFormatException nfe)
		{
			return false;
		}
	}	
}
