import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;
import org.apache.pdfbox.util.Splitter;

/**
 * @author pmakella
 *
 */
//---------------------------------------------------------------------------------------
// class to search for Electrophoresis Assay Details page and Ladder page
// constructor initializes member variables with respective page numbers
//---------------------------------------------------------------------------------------
public class AssayPD 
{
	    String ErrMsg;
	    int assayPageNum = -1;
	    int ladderPageNum = -1;
        //constructor initializes AssayDetails page number and Ladder page number
		public  AssayPD(int numPages, PDDocument pd, Logger logger) throws Exception
		{
			Splitter sp = new Splitter();
			List<PDDocument> pdlist = sp.split(pd);
			
			try
			{
				for (int i=0; i<numPages; i++)
				{
		             PDFTextStripper stripper = new PDFTextStripper();

			         // StringBuilder to store the extracted text
			         StringBuilder sb = new StringBuilder();
			         // Add text to the StringBuilder from the PDF
			         sb.append(stripper.getText(pdlist.get(i)));
		      
			         // Regex pattern: 'Overall Results for Ladder', numbers followed by spaces, new line 
			         Pattern p = Pattern.compile("(Overall Results for Ladder)(\\s*\\n)");
			         // Matcher refers to the actual text where the pattern will be found
			         Matcher m = p.matcher(sb);

			         while (m.find())
			         {
			        	 	logger.log(Level.INFO, "found Overall Results for Ladder page \n");
			        	 	//System.out.println("found ladder page\n");
			            	//save page
			            	this.ladderPageNum = i;
			         }
			        Pattern p1 = Pattern.compile("(Electrophoresis Assay Details)(\\s*\\n)");
			        // Matcher refers to the actual text where the pattern will be found
			        Matcher m1 = p1.matcher(sb);

			        while (m1.find())
			        {
			        	 	logger.log(Level.INFO, "found Electrophoresis Assay Details page \n");
			        	 	//System.out.println("found Assay details page\n");
			            	//save page
			            	this.assayPageNum = i;
			        }
			         		         
		         }//for (int i=0; i<numPages; i++)
				 if(this.assayPageNum == -1) 
				 {
					 logger.log(Level.INFO, "could not find Electrophoresis Assay Details page \n");
				 }
		      
				 if(this.ladderPageNum == -1)
				 {
					 logger.log(Level.INFO, "could not find Ladder page \n");
				 }
			}
			catch (Exception e) 
			{
				logger.log(Level.INFO, "Exception thrown \n");
				ErrMsg=e.toString();
				logger.log(Level.INFO,ErrMsg+"\n");
			}
			finally
			{
				for (int i=0; i<numPages; i++)
			    {
				  if( pdlist.get(i) != null )
				  {
					  pdlist.get(i).close();
				  }
			    }
			}
		}//end of constructor


}
//---------------------------------------------------------------------------------------
