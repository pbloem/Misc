package nl.peterbloem.languages;

import java.io.*;
import java.util.*; 

import org.lilian.Global;
import org.lilian.models.BasicFrequencyModel;

import au.com.bytecode.opencsv.CSVReader;

public class Main {

	private static final String CHINESE_DATA = "/data/chinese/modern.csv";
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
		String language = console.readLine("Which language would you like to practice? (chinese, croatian, or enter for random)");
		
		language = language.trim().toLowerCase();
		if(language.equals(""))
			if(Math.random() < 0.05)
				language = "chinese";
			else
				language = "croatian";
		
		if(language.equals("chinese"))
			chinese();
		if(language.equals("croatian"))
			croatian();
	}
	
	private static void croatian() 
	{
		System.out.println("Croatian");
	}

	public static void chinese() throws IOException
	{
		Global.random = new Random();
		while(true)
		{
			String category = console.readLine("Which category of characters (direction, function, verb, time, location, food, family, prepposition ,part, basic, number for top n)?");
			if(isParsableToInt(category))
				topN = Integer.parseInt(category);
			else
				useCategory = true;
			
			InputStream in = Main.class.getClassLoader()					
                    .getResourceAsStream(CHINESE_DATA);
					
		    CSVReader reader = new CSVReader(new InputStreamReader(in));
		    List<String[]> charactersRaw = reader.readAll();
		    List<Character> characters = new ArrayList<Character>(charactersRaw.size());
		    
		    for(String[] raw: charactersRaw)
		    	characters.add(new Character(raw[1], raw[3],raw[4], raw[5], 
		    			Integer.parseInt(raw[0]), Integer.parseInt(raw[2])));
		    
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
	        
	        Character last = null, choice = null;
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
	        		 else if(!last.equals(choice)) // make sure the same character 
	        			 selected = true;          // does not appear twice in a row
	        	}
	        	
	        	assert(choice != null);
	        	
	        	console.readLine(choice.character());
	        	console.printf(choice.pinYin() + "\n");
	        	
	        	console.printf(choice.translation() + "\n");
	        	
	        	String answer = "";
	        		
	       		answer = console.readLine("Remove? (y or any key)");
	       		
	        	console.printf(((char) 27)+"[2J"); // ANSI clear screen...
	        	
	        	if(answer.equals("y"))
	        	{
	        		selection.remove(choice);
	        		console.printf(choice.character() + " removed. \n");
	        	}
	        }
	        
        	console.printf(((char) 27)+"[2J"); // ANSI clear screen...	        
		}    	
	}
	
	private static final class Character 
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
