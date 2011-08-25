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

package is.iclt.icenlp.runner;

import is.iclt.icenlp.core.utils.FileEncoding;
import is.iclt.icenlp.core.iceparser.*;

import java.io.*;

public class RunIceParserOut extends RunIceParserBase
{	
	private void readwrite(IceParserTransducer transducer, String inputFileAndPath, String outputFileAndPath) throws IOException
	{
		BufferedReader br;
        BufferedWriter bw;
		
		StringReader sr;
		StringWriter sw;
		String str;	

	    br = FileEncoding.getReader(inputFileAndPath);
		bw = FileEncoding.getWriter(outputFileAndPath);
		while((str = br.readLine()) != null)
		{
				sr = new StringReader(str);
				sw = new StringWriter();
			
				transducer.yyclose();
				transducer.yyreset(sr);
				transducer.parse(sw);

                bw.write(sw.toString());           
				bw.write("\n");
		}
		
		bw.flush();
		bw.close();
	}

	private void formatter(OutputFormatter of, String inputFileAndPath, String outputFileAndPath, OutputFormatter.OutputType outputType, boolean mergeTags) throws IOException
	{
		BufferedReader br;
		BufferedWriter bw;

		StringReader sr;
		StringWriter sw;
		String str;

		br = FileEncoding.getReader(inputFileAndPath);
		bw = FileEncoding.getWriter(outputFileAndPath);

		//StringBuffer buf = new StringBuffer();

		while((str = br.readLine()) != null)
		{
			//buf.append(str+"\n");
            //String result = of.parse(buf.toString(), outputType, mergeTags);
            bw.write(of.parse(str, outputType, mergeTags));
            bw.write("\n");
		}

        //String result = of.parse(buf.toString(), outputType, mergeTags);
        //bw.write(result);
		//bw.write("\n");
        bw.write(of.finish());
		bw.flush();
		bw.close();
		br.close();
	}
    
    private void parse() throws IOException
    {	
		if(inputFile == null || outputFile == null || outputPath == null)
		{
			showParametersExit();
		}
		StringReader sr;
		String str;			

		printHeader(); 
		
		System.out.println( "Input file: " + inputFile );
        System.out.println( "Output file: " + outputFile );
		System.out.println( "Output path: " + outputPath);
		//System.out.println( "Phrase per line on: " + phrasePerLine);
		System.out.println( "Annotate functions on: " + includeFunc);
		System.out.println( "Agreement on: " + agreement);
		System.out.println( "Grammar check on: " + markGrammarError);
		System.out.println( "Merge labels: " + mergeLabels);
		
		sr = new StringReader("empty");
		// TagEncode															-- TagEncode
		TagEncoder tagEncdr = new TagEncoder(sr);
		readwrite(tagEncdr, inputFile, outputPath+"/"+"tagencoder.out");

		// Preprocess															-- Preprocess

		Preprocess preprocess = new Preprocess(sr);
		readwrite(preprocess, outputPath+"/"+"tagencoder.out", outputPath+"/"+"preprocess.out");

		// phrase_FOREIGN														-- phrase_FOREIGN
		Phrase_FOREIGN frgn = new Phrase_FOREIGN(sr);
		readwrite(frgn, outputPath+"/"+"preprocess.out", outputPath+"/"+"phrase_FOREIGN.out");		

		// phrase_MWE															-- phrase_MWE

		Phrase_MWE mwe = new Phrase_MWE(sr);
		readwrite(mwe, outputPath+"/"+"phrase_FOREIGN.out", outputPath+"/"+"phrase_MWE.out");			

		// phrase_MWEP1															-- phrase_MWEP1

		Phrase_MWEP1 mwep1 = new Phrase_MWEP1(sr);
		readwrite(mwep1, outputPath+"/"+"phrase_MWE.out", outputPath+"/"+"phrase_MWEP1.out");

		// phrase_MWEP2															-- phrase_MWEP2

		Phrase_MWEP2 mwep2 = new Phrase_MWEP2(sr);
		readwrite(mwep2, outputPath+"/"+"phrase_MWEP1.out", outputPath+"/"+"phrase_MWEP2.out");

		// phrase_AdvP															-- phrase_AdvP

	    Phrase_AdvP advp = new Phrase_AdvP(sr);
		readwrite(advp, outputPath+"/"+"phrase_MWEP2.out", outputPath+"/"+"phrase_AdvP.out");		

		// Phrase_AP															-- phrase_AP

		Phrase_AP ap = new Phrase_AP(sr);
		readwrite(ap, outputPath+"/"+"phrase_AdvP.out", outputPath+"/"+"phrase_AP.out");		
		

		// case_AP																-- case_AP

		Case_AP cap = new Case_AP(sr);
		readwrite(cap, outputPath+"/"+"phrase_AP.out", outputPath+"/"+"case_AP.out");		

		// phrase_APs															-- phrase_APs

        Phrase_APs aps = new Phrase_APs(sr);
		readwrite(aps, outputPath+"/"+"case_AP.out", outputPath+"/"+"phrase_APs.out");		
		
		// phrase_NP															-- phrase_NP

		Phrase_NP np = new Phrase_NP(sr);

		if(agreement)
		{
			np.set_doAgreementCheck(true);	
		}
		if(markGrammarError)
		{
			np.set_markGrammarError(true); 
		}
		readwrite(np, outputPath+"/"+"phrase_APs.out", outputPath+"/"+"phrase_NP.out");
		
		// phrase_NP2															-- phrase_NP2
		
		if(agreement && !markGrammarError)
		{
			Phrase_NP2 np2 = new Phrase_NP2(sr);
			readwrite(np2, outputPath+"/"+"phrase_NP.out", outputPath+"/"+"phrase_NP2.out");
		}

		// phrase_VP															-- phrase_VP

		Phrase_VP vp = new Phrase_VP(sr);
		
		if(agreement && !markGrammarError)
		{
			readwrite(vp, outputPath+"/"+"phrase_NP2.out", outputPath+"/"+"phrase_VP.out");
		}
		else
		{
			readwrite(vp, outputPath+"/"+"phrase_NP.out", outputPath+"/"+"phrase_VP.out");
		}
		
		// case_NP																-- case_NP

	    Case_NP cnp = new Case_NP(sr);
		readwrite(cnp, outputPath+"/"+"phrase_VP.out", outputPath+"/"+"case_NP.out");
		
		// phrase_NPs															-- phrase_NPs

	    Phrase_NPs nps = new Phrase_NPs(sr);
		readwrite(nps, outputPath+"/"+"case_NP.out", outputPath+"/"+"phrase_NPs.out");
		
		// phrase_PP															-- phrase_PP

		Phrase_PP pp = new Phrase_PP(sr);
		readwrite(pp, outputPath+"/"+"phrase_NPs.out", outputPath+"/"+"phrase_PP.out");
		
		// clean1																-- clean1
	    
	    Clean1 cl1 = new Clean1(sr);
		readwrite(cl1, outputPath+"/"+"phrase_PP.out", outputPath+"/"+"clean1.out");
		

		if(includeFunc)
		{
			// func_Timex															-- func_Timex

			Func_TIMEX f_time = new Func_TIMEX(sr);
			readwrite(f_time, outputPath+"/"+"clean1.out", outputPath+"/"+"func_timex.out");
			

			// func_Qual															-- func_Qual
		
			Func_QUAL f_qual = new Func_QUAL(sr);
			readwrite(f_qual, outputPath+"/"+"func_timex.out", outputPath+"/"+"func_qual.out");					


			// func_Subj															-- func_Subj

			Func_SUBJ f_subj = new Func_SUBJ(sr);
			if(agreement)
			{
				f_subj.set_doAgreementCheck(true);	
			}
			if(markGrammarError)
			{
				f_subj.set_markGrammarError(true);
			}
			readwrite(f_subj, outputPath+"/"+"func_qual.out", outputPath+"/"+"func_subj.out");				
	

			// func_Comp															-- func_Comp

			Func_COMP f_comp = new Func_COMP(sr);
			readwrite(f_comp, outputPath+"/"+"func_subj.out", outputPath+"/"+"func_comp.out");
							
			// func_Obj																-- func_Obj
		
			Func_OBJ f_obj = new Func_OBJ(sr);
			readwrite(f_obj, outputPath+"/"+"func_comp.out", outputPath+"/"+"func_obj.out");				

			// func_Obj2															-- func_Obj2
		
			Func_OBJ2 f_obj2 = new Func_OBJ2(sr);
			readwrite(f_obj2, outputPath+"/"+"func_obj.out", outputPath+"/"+"func_obj2.out");				
	
			// func_Obj3															-- func_Obj3
		
			Func_OBJ3 f_obj3 = new Func_OBJ3(sr);
			readwrite(f_obj3, outputPath+"/"+"func_obj2.out", outputPath+"/"+"func_obj3.out");	

			// func_Subj2															-- func_Subj2
		
			Func_SUBJ2 f_subj2 = new Func_SUBJ2(sr);
			readwrite(f_subj2, outputPath+"/"+"func_obj3.out", outputPath+"/"+"func_subj2.out");
		}

		// clean2																-- clean2

		Clean2 cl2 = new Clean2(sr);		

		if(includeFunc)
			readwrite(cl2, outputPath+"/"+"func_subj2.out", outputPath+"/"+"clean2.out");
		else
			readwrite(cl2, outputPath+"/"+"clean1.out", outputPath+"/"+"clean2.out");

		// Phrase Per Line														-- Phrase Per Line
		// Decoder																-- Decoder 
		// Formatter															-- Formatter
		Phrase_Per_Line ppl = new Phrase_Per_Line(sr);
		TagDecoder tagDecdr = new TagDecoder(sr);


		if( (outputType== OutputFormatter.OutputType.plain || outputType== OutputFormatter.OutputType.phrase_per_line) && !mergeLabels)
		{
			if(outputType== OutputFormatter.OutputType.plain)
			{
				// decode straight to outputfile
				readwrite(tagDecdr, outputPath+"/"+"clean2.out", outputFile);
			}
			else
			{
				//decode from phrase per line to output file
				readwrite(ppl, outputPath+"/"+"clean2.out", outputPath+"/"+"phrase_per_line.out");
				readwrite(tagDecdr, outputPath+"/"+"phrase_per_line.out", outputFile);
			}
		}
		else
		{
			readwrite(tagDecdr, outputPath+"/"+"clean2.out", outputPath+"/"+"decoded.out");
			OutputFormatter of = new OutputFormatter();
			formatter(of, outputPath+"/"+"decoded.out", outputFile, outputType, mergeLabels); 

		}



		
	}
    public static void main(String[] args) throws IOException 
	{
		RunIceParserOut runner = new RunIceParserOut();

		runner.getParam(args);

		long start = System.currentTimeMillis();

        runner.parse();

        long end = System.currentTimeMillis();
		
		System.out.println("Time in msec " + (end-start));
    }
}



