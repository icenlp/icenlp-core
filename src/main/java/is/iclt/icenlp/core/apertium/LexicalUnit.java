package is.iclt.icenlp.core.apertium;

// Stores one lexical unit
public class LexicalUnit
{
	private String lemma = null;
	private String symbols = null;
	private String invMWMarker = null;
	private boolean unknown;
	private boolean space;
	private boolean ignore;
	private boolean linkedToPreviousWord;
	
	public LexicalUnit(String lemma, String symbols)
	{
		this.lemma = lemma;
		this.symbols = symbols;
		this.unknown = false;
		this.ignore = false;
		this.linkedToPreviousWord = false;
	}
	
	public LexicalUnit(String lemma, String symbols, boolean unknown)
	{
		this(lemma, symbols);
		
		this.unknown = unknown;
	}
	
	public LexicalUnit(String lemma, String symbols, boolean unknown, boolean space)
	{
		this(lemma, symbols, unknown);
		
		this.space = space;
	}
	
	// Constructor for the invariable part of multiword marker
	public LexicalUnit(String lemma, String symbols, String invMWMarker)
	{
		this(lemma, symbols, false, false);
		
		this.invMWMarker = invMWMarker;
	}
	
	public String getLemma()
	{
		return lemma;
	}
	
	public void setLemma(String lemma)
	{
		this.lemma = lemma;
	}
	
	public String getSymbols()
	{
		return symbols;
	}
	
	public void setSymbols(String symbols)
	{
		this.symbols = symbols;
	}
	
	public void setIgnore(boolean ignore)
	{
		this.ignore = ignore;
	}
	
	public void setLinkedToPreviousWord(boolean linkedToPreviousWord)
	{
		this.linkedToPreviousWord = linkedToPreviousWord;
	}
	
	public void setInvMWMarker(String invMWMarker)
	{
		this.invMWMarker = invMWMarker;
	}
	
	public String getInvMWMarker()
	{
		return invMWMarker;
	}
	
	public boolean hasInvMWMarker()
	{
		return invMWMarker != null;
	}
	
	public boolean isUnknown()
	{
		return unknown;
	}
	
	public boolean isSpace()
	{
		return space;
	}
	
	public boolean isIgnore()
	{
		return ignore;
	}
	
	public boolean isSentence()
	{
		return symbols.equals("<sent>");
	}
	
	public boolean isComma()
	{
		return symbols.equals("<cm>");
	}
	
	public boolean isLinkedToPreviousWord()
	{
		return linkedToPreviousWord;
	}
	
	public boolean isPreposition()
	{
		return symbols.equals("<pr>");
	}
	
	public boolean isMWE()
	{
		return lemma.trim().contains(" ") && lemma.length() > 1;
	}
	
	public boolean isProperNoun()
	{
		return symbols.startsWith("<np>");
	}
	
	public boolean isPronoun()
	{
		return symbols.startsWith("<prn>");
	}
	
	public boolean isVerb()
	{
		return symbols.contains("<vblex>") || 
			   symbols.contains("<vbser>") ||
			   symbols.contains("<vbhaver>") ||
			   symbols.contains("<vaux>");
	}
	
	public boolean isDet()
	{
		return symbols.contains("<det>");
	}
}
