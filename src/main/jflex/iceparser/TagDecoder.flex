/*
 * Copyright (C) 2009 Hrafn Loftsson
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
// this transducer removes the encoding of the tags.
package is.iclt.icenlp.core.iceparser;
import java.util.regex.*;
import java.io.*;
import is.iclt.icenlp.core.utils.IceParserUtils;
%%

%public
%class TagDecoder
%standalone
%line
%extends IceParserTransducer
%unicode

%{
    String encO =  IceParserUtils.encodeOpen;
    String encC =  IceParserUtils.encodeClose;
    
    java.io.Writer out = new BufferedWriter(new OutputStreamWriter(System.out));
      
      public void parse(java.io.Writer _out) throws java.io.IOException
      {
      	out = _out;
      	while (!zzAtEOF) 
      	    yylex();
  }
  
 
%}

%eof{
	try {
	  out.flush();	
	} 
	catch (IOException e) {
            e.printStackTrace();
        }
%eof}

%include src/main/jflex/iceparser/regularDef.txt

tag = {encodeOpen}~{encodeClose}


%%
{tag}	{
			String str = yytext();
			str = str.substring(encO.length(),str.length()-encC.length());
		  	out.write(str);
		}


		
"\n"		{ //System.err.print("Reading line: " + Integer.toString(yyline+1) + "\r"); 
		out.write("\n"); }
.		{ out.write(yytext());}
