/*
 * Copyright (C) 2010 Ragnar Lárus Sigurðsson
 *
 * This file is part of the IceNLP toolkit.
 * IceNLP is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * IceNLP is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with IceNLP. If not,  see <http://www.gnu.org/licenses/>.
 *
 * Contact information:
 * Hrafn Loftsson, School of Computer Science, Reykjavik University.
 * hrafn@ru.is
 */

package is.iclt.icenlp.core.iceparser;


import java.io.*;
import java.util.*;
import java.util.regex.*;

enum OutputFormatter_Type { FUNC, PHRASE, WORDS, WORD, TAG, ROOT, SENTENCE }

public class OutputFormatter
{
    public enum OutputType {plain, phrase_per_line, json, xml}
	private OutputFormatter_Part root;

	private boolean mergeTags = false;
    private OutputType outType;

    private boolean firstLine=true;
	private StringReader r;
	private StringWriter w;

    
	public void setMergeTags(boolean newVal)
	{
		mergeTags = newVal;
	}

// yyreset yyclose and parse are here to match the transducers.
	/*public void yyreset(java.io.StringReader in)
	{
		this.r = in;
	}*/
	/*public void yyclose()  throws java.io.IOException
	{	
		if(r!=null)
			r.close();
	}*/
	public void parse(java.io.StringWriter _out) throws java.io.IOException
	{
		w = _out;

		root = read();

		if(mergeTags)
			root = mergeFuncPhrase(root, null);

		write(root);
	}

    //public void parse(java.io.StringReader in, java.io.StringWriter _out, OutputType outType, boolean mergeTags) throws java.io.IOException
    public String parse(String str, OutputType outType, boolean mergeTags) throws java.io.IOException
    {
            this.r = new StringReader( str );
			this.w = new StringWriter( );

            this.outType = outType;
            /*switch (outType)
			{
			  case plain:
					setPlain(true);
					break;
			  case phrase_per_line:
					setPlainPerLine(true);
					break;
			  case json:
					setJson(true);
					break;
			  case xml:
					setXml(true);
					break;
			  default:
					setPlain(true);
					break;
			}*/
			if(mergeTags)
				setMergeTags(true);

		   root = read();

		   if(mergeTags)
			  root = mergeFuncPhrase(root, null);

		   write(root);
           // Return the result of the StringWriter;

           firstLine = false;
           return w.toString();
    }

	public OutputFormatter()
	{
	}

	// Puts a function tag to the next phrase tag inside of it.
	private static OutputFormatter_Part mergeFuncPhrase(OutputFormatter_Part curr, OutputFormatter_Part parent)
	{
		if(curr.children.size()>0)
		{
			for(int i=0; i<curr.children.size(); i++)
			{
				mergeFuncPhrase(curr.children.get(i), curr);
			}
		}

		if(curr.OutputFormatter_Type == OutputFormatter_Type.FUNC)
		{
			int currIndex = parent.children.indexOf(curr);
			parent.children.remove(currIndex);

			String tag = curr.data.substring(1,curr.data.length());
			tag = tag.replace("*", "");
			boolean once = false;

			for(int i=0; i<curr.children.size(); i++)
			{
				OutputFormatter_Part child = curr.children.get(i);
				if(child.OutputFormatter_Type == OutputFormatter_Type.PHRASE && !once)
				{
					child.data+=("-"+tag);
					once=true;
				}

				parent.children.add(currIndex, child);
				currIndex++;
			}
		}

		if(parent == null)
			return curr;
		else 
			return parent;
	}
	private void print(String text)	
	{
		try
		{
			w.write(text);
		}
		catch(Exception e)
		{
			System.err.println(e);
		}
	}

    public String finish()
    {
        String str="";
        if(outType == OutputType.json)
            str = "\t}\n}"+"\n";
		else if(outType == OutputType.xml)
            str = "</ParsedText>"+"\n";

        return str;
    }

	private void write(OutputFormatter_Part root)
	{
		if(outType == OutputType.json)
		{
            if (firstLine)
              print("{\n\t\"Parsed Text\":{"+"\n");
			writeJson(root);
		}
		else if(outType == OutputType.xml)
		{
            if (firstLine)
                print("<ParsedText>"+"\n");
			writeXml(root);
		}
		else if(outType == OutputType.plain)
		{
			writePlaintext(root);
		}
		else if(outType == OutputType.phrase_per_line)
		{
			writePlainPerLine(root);
		}
	}

	private OutputFormatter_Part read() throws IOException 
	{
		String str;
		OutputFormatter_Part root = new OutputFormatter_Part(OutputFormatter_Type.ROOT);

	
		char[] arr = new char[8*1024];
		StringBuffer buf = new StringBuffer();
		int numChars;
		while((numChars = r.read(arr, 0, arr.length)) > 0)
		{
			buf.append(arr, 0, numChars);
		}
		str = buf.toString();	

		String [] sentences = null;
		sentences = str.split("\n");

		for( String stre : sentences )
		{
			OutputFormatter_Part sentence = new OutputFormatter_Part("Sentence", OutputFormatter_Type.SENTENCE);
			sentence.children = treeMaker(stre, sentence.children);		
			root.children.add(sentence);
		}
		
		return root;
	}
	//creates a tree from the input text
	private static ArrayList<OutputFormatter_Part> treeMaker(String str, ArrayList<OutputFormatter_Part> list)
	{
		int funcIndex, phraseIndex;
		str = str.trim();
		while(str.length() > 0)
		{
			str = str.trim();
			funcIndex = str.indexOf("{");
			phraseIndex = str.indexOf("[");

			//patterns matching all possible function or phrase tags
			Pattern pPhrase = Pattern.compile("FRWs?|AdvP|APs?|NP[s\\?]?|VP[bgips]?|PP|S?CP|InjP|MWE_(AdvP|AP|CP|PP)");
			Pattern pFunc = Pattern.compile("((\\*SUBJ|\\*I?OBJ(AP|NOM)?|\\*COMP)(<|>)?)|\\*QUAL|\\*TIMEX\\??");

			Matcher mPhrase;
			Matcher mFunc;

			String funcTag, phraseTag;

			if(funcIndex == 0)
			{
				funcTag = str.substring(0, str.indexOf(" "));
				mFunc = pFunc.matcher(funcTag);
				if(!mFunc.find())
					funcIndex = -99;
			}
			if(phraseIndex == 0)
			{
				phraseTag = str.substring(0, str.indexOf(" "));
				mPhrase = pPhrase.matcher(phraseTag);
				if(!mPhrase.find())
					phraseIndex = -99;
			}
			if(funcIndex == 0)
			{
				String tag = str.substring(0, str.indexOf(" "));
				String reverseTag = " " + tag.substring(1, tag.length()) + "}";

				if(str.indexOf(reverseTag) != -1)
				{
					int endIndex = str.indexOf(reverseTag) + reverseTag.length() -1;

					String funcstr = str.substring(0, endIndex+1);
					funcstr = funcstr.trim();

					funcstr = funcstr.substring(funcstr.indexOf(" "), funcstr.lastIndexOf(reverseTag));
			
					OutputFormatter_Part temp = new OutputFormatter_Part(tag, OutputFormatter_Type.FUNC);
					temp.children = treeMaker(funcstr, temp.children);

					list.add(temp);

					str = str.substring(endIndex+1, str.length());
				}
				else
				{
					list.add(words(str));
					str="";
				}
			}
			else if(phraseIndex == 0)
			{
				String tag = str.substring(0, str.indexOf(" "));
				String reverseTag = " " + tag.substring(1, tag.length()) + "]";

				if(str.indexOf(reverseTag) != -1)
				{
					int endIndex = str.indexOf(reverseTag) + reverseTag.length() -1;				

					String phrasestr = str.substring(0, endIndex+1);
					phrasestr = phrasestr.trim();

					phrasestr = phrasestr.substring(phrasestr.indexOf(" "), phrasestr.indexOf(reverseTag));
					OutputFormatter_Part temp = new OutputFormatter_Part(tag, OutputFormatter_Type.PHRASE);
					temp.children = treeMaker(phrasestr, temp.children);

					list.add(temp);

					str = str.substring(endIndex+1, str.length());
				}
				else
				{
					list.add(words(str));
					str="";
				}
			}
			else if(funcIndex == -99 || phraseIndex == -99)
			{
				try
				{
					int countChars = str.indexOf(" ");
					String toAppend = str.substring(0, str.indexOf(" "));
		
					StringBuffer buff = new StringBuffer();
					buff.append(toAppend+" ");
			
					str = str.substring(countChars, str.length());
					str = str.trim();
	
					countChars = str.indexOf(" ");
					toAppend = str.substring(0, str.indexOf(" "));

					buff.append(toAppend+" ");
					list.add(words(buff.toString()));
					str = str.substring(countChars, str.length());
				}
				catch(Exception e)
				{
					list.add(words(str));
					str = "";
				}
			}
			else if(funcIndex > 0 || phraseIndex > 0)
			{
				int lesserIndex;

				if(funcIndex < 0)
					lesserIndex = phraseIndex;
				else if(phraseIndex < 0)
					lesserIndex = funcIndex;
				else if(funcIndex < phraseIndex)
					lesserIndex = funcIndex;
				else
					lesserIndex = phraseIndex;

				list.add(words(str.substring(0, lesserIndex-1)));

				str = str.substring(lesserIndex, str.length());

			}
			else
			{
				list.add(words(str));
				str="";
			}	
		}
	 	return list;
		
	} 
	private static OutputFormatter_Part words(String str)
	{
		String [] temp = null;
      		temp = str.split(" ");

		OutputFormatter_Part retOutputFormatter_Part = new OutputFormatter_Part("WORDS" ,OutputFormatter_Type.WORDS);

		for(int i=0; i< (temp.length/2); i++)
		{
			OutputFormatter_Part tempOutputFormatter_Part = new OutputFormatter_Part(temp[i*2], OutputFormatter_Type.WORD);
			tempOutputFormatter_Part.children.add(new OutputFormatter_Part(temp[(i*2)+1], OutputFormatter_Type.TAG));

			retOutputFormatter_Part.children.add(tempOutputFormatter_Part);
		}
		
		return retOutputFormatter_Part;
	}


//					encoders				//


//
//Plaintext one phrase per line
//
	private void writePlainPerLine(OutputFormatter_Part root)
	{
		plainPerLineTree(root.children, 0);
	}


	private void plainPerLineTree(ArrayList<OutputFormatter_Part> list, int depth)
	{
		for(OutputFormatter_Part child : list)
		{
			if(child.OutputFormatter_Type == OutputFormatter_Type.WORD || child.OutputFormatter_Type == OutputFormatter_Type.TAG || child.OutputFormatter_Type == OutputFormatter_Type.FUNC || child.OutputFormatter_Type == OutputFormatter_Type.PHRASE)
			{
				if(child.OutputFormatter_Type == OutputFormatter_Type.TAG && depth == 3)
					print(child.data);
				else
					print(child.data+" ");				
			}

			plainPerLineTree(child.children, depth+1);

			if(child.OutputFormatter_Type == OutputFormatter_Type.FUNC || child.OutputFormatter_Type == OutputFormatter_Type.PHRASE)
			{
				String closetag = child.data.substring(1, child.data.length());
				if(child.OutputFormatter_Type == OutputFormatter_Type.FUNC)
					closetag+="}";
				else
					closetag+="]";
				if(depth > 1)
					print(closetag+" ");
				else
					print(closetag);
			}
			if(child.OutputFormatter_Type != OutputFormatter_Type.WORDS)
			{
				if(child.OutputFormatter_Type == OutputFormatter_Type.TAG && depth < 4)
				{
					print("\n");
				}
				else if(depth < 2 && child.OutputFormatter_Type != OutputFormatter_Type.TAG)
					print("\n");
			}
		}
	}
//
//Plaintext
//
	private void writePlaintext(OutputFormatter_Part root)
	{
		plaintextTree(root.children);
	}
	private void plaintextTree(ArrayList<OutputFormatter_Part> list)
	{
		for(OutputFormatter_Part child : list)
		{
			if(child.OutputFormatter_Type == OutputFormatter_Type.WORD || child.OutputFormatter_Type == OutputFormatter_Type.TAG || child.OutputFormatter_Type == OutputFormatter_Type.FUNC || child.OutputFormatter_Type == OutputFormatter_Type.PHRASE)
			{
				print(child.data+" ");
			}
			plaintextTree(child.children);
			if(child.OutputFormatter_Type == OutputFormatter_Type.FUNC || child.OutputFormatter_Type == OutputFormatter_Type.PHRASE)
			{
				String closetag = child.data.substring(1, child.data.length());
				if(child.OutputFormatter_Type == OutputFormatter_Type.FUNC)
					closetag+="}";
				else
					closetag+="]";
			
				print(closetag+" ");
			}

			if(child.OutputFormatter_Type == OutputFormatter_Type.SENTENCE)
				print("\n");
		}
	}

//
//XML
//
	private void writeXml(OutputFormatter_Part root)
	{
		printXmltree(root.children, "\t");
	}
	private void printXmltree(ArrayList<OutputFormatter_Part> list, String indent)
	{
		for (OutputFormatter_Part var : list) 
		{	
			//replace xml reserved sybmols
			String data = var.data;
			data = data.replace("&", "&amp;");
			data = data.replace("<", "&lt;");
			data = data.replace(">", "&gt;");
			
			if(var.OutputFormatter_Type == OutputFormatter_Type.TAG)
			{
				print(indent+"<"+var.OutputFormatter_Type+">"+data+"</"+var.OutputFormatter_Type+">"+"\n");
			}
			else if(var.OutputFormatter_Type == OutputFormatter_Type.SENTENCE || var.OutputFormatter_Type == OutputFormatter_Type.WORDS)
			{
				print(indent+"<"+var.OutputFormatter_Type+">"+"\n");
				printXmltree(var.children, indent+"\t");
				print(indent+"</"+var.OutputFormatter_Type+">"+"\n");
			}
			else
			{
				print(indent+"<"+var.OutputFormatter_Type+"> "+""+data+""+"\n");
				printXmltree(var.children, indent+"\t");
				print(indent+"</"+var.OutputFormatter_Type+">"+"\n");
			}
		}
	}

//
//Json
//
	private void writeJson(OutputFormatter_Part root)
	{
		printJtree(root.children, "\t\t");
	}

	private void printJtree(ArrayList<OutputFormatter_Part> list, String indent)
	{
		int count = 0;
		for (OutputFormatter_Part var : list) 
		{	
			if(var.OutputFormatter_Type == OutputFormatter_Type.WORDS)
			{
				print(indent+"\"WORDS\":[{"+"\n");
				int childrenCount = 0;
				for(OutputFormatter_Part word : var.children)
				{
					if(childrenCount < var.children.size()-1)
						print(indent+"\t\t\""+word.data + "\": \"" + word.children.get(0).data+"\","+"\n");
					else
						print(indent+"\t\t\""+word.data + "\": \"" + word.children.get(0).data+"\""+"\n");
				
					childrenCount++;
				}

				if(count < list.size()-1)
					print(indent+"\t\t"+"}],"+"\n");
				else
					print(indent+"\t\t"+"}]"+"\n");

			}
			else
			{
				print(indent+"\""+var.data+"\":{"+"\n");
				if(var.children.size() > 0)
					printJtree(var.children, indent+"\t");
				if(count < list.size()-1)
					print(indent+"},"+"\n");
				else
					print(indent+"}"+"\n");
			}
			count++;
		}
	}



} 
class OutputFormatter_Part
{
	public final OutputFormatter_Type OutputFormatter_Type;	
	public String data;
	public ArrayList<OutputFormatter_Part> children = new ArrayList<OutputFormatter_Part>();

	public OutputFormatter_Part(OutputFormatter_Type _OutputFormatter_Type)
	{	
		OutputFormatter_Type = _OutputFormatter_Type;
	}
	public OutputFormatter_Part( String _data, OutputFormatter_Type _OutputFormatter_Type)
	{
		OutputFormatter_Type = _OutputFormatter_Type;
		data = _data;
	}
}


