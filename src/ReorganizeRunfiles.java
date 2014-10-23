
/**
 * v1.5 04/2014 - added Pxxxx to allow Pool entries for regex match
 * @author Padma Akella v1.5 04/2014
 * 

 * To Reorganize Run files from Bioanalyzer Expert 2100 software.
		
	1.Splits PDF file saved by Expert software as 1 sample per page into n PDF files; n=no.of samples.
	2.Creates directories for each of n samples and moves respective PDF files. 
	  On a sample re-run, saves pdf with current time stamp to generate unique file name
	3.Logs messages and errors.
	4.Uploads sample folders to dmel.uchospitals.edu over ftp to /home/hgac/public_html folder
	5.Writes tracksamples.ini which is used by dmel.uchospitals.edu/tracksamples.cgi to show samples and uploaded dates as a table. 
	  File tracksamples.ini is uploaded to /home/hgac/public_html folder
	  
	Dependencides: commons-logging-1.1.1.jar,commons-net-3.1.jar,fontbox-1.7.0.jar,jempbox-1.7.0.jar,pdfbox-app-1.7.0.jar
	
	Prerequisites: File should be saved as pdf from Expert software with option 'one sample per page'
	v1.0 has the same functionality with no ftp upload

		
 *
 */
import java.util.List;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.ArrayList;
import java.io.*;

//to read and write pdfs
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.util.*;
import java.util.regex.*;

//---------------------------------------------------------------------------------------
//class with main()
//---------------------------------------------------------------------------------------
public class ReorganizeRunfiles {


	
	static Logger logger = Logger.getLogger("ReorganizeRunfiles");
	
	public static void main(String[] args) throws IOException 
	{
		
		LogManager lm = LogManager.getLogManager();
		DateFormat dateFormat= new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
		Date date = new Date();
		FileHandler fh = new FileHandler("log_"+dateFormat.format(date)+".txt");
		
		//for ftp upload
		List<String> filesToUpload = new ArrayList<String> ();
		List<String> samples = new ArrayList<String> ();
		
		PDDocument pd = null;
		try
		{
			  //logger to log messages
			  setuplogger(logger,fh,lm,dateFormat,date);
		
			  //search for pdf files in current directory and return the latest file
			  File input = latestpdf();
	
			  //process latest pdf if it can be opened for reading
			  if(input.canRead()==true)
		      {
				  PDFMergerUtility mergePdf = new PDFMergerUtility();
				  Splitter sp = new Splitter();
				  
		    	  logger.log(Level.INFO, "opened file:"+ input.getName()+"\n");
		    	  pd = PDDocument.load(input);
			      //split pdf into single page pdfs
			      List<PDDocument> pdlist = sp.split(pd);
			      
			      //----------------------------------------------
			      //get samplenames to make filenames for pdfs
			      int numPages=pdlist.size();
			      String fn = input.getName();
			      fn.trim();
			      //remove extension
			      fn=fn.substring(0, fn.lastIndexOf('.'));
			      fn.trim();
			      String[] fnarray = fn.split("_");
			      //construct sample filename
			      String fnprt1 = fnarray[0];
			      fnarray[0] = "";
			      fnarray[1] = "";
			      JoinStr joinStrObj = new JoinStr();
			      String fnprt2 = joinStrObj.join(fnarray,"_");
			      fnprt2=fnprt2.trim();
			      //remove extra _ at [0]
			      fnprt2=fnprt2.substring(1, fnprt2.length());
			      //-------------------------------------------------
			      
			      //get Electrophoresis Assay Details page and Overall Results for Ladder page to be included in all pdfs
		          AssayPD assayPDobj = new AssayPD(numPages, pd,logger);
			      PDDocument Assaypd = pdlist.get(assayPDobj.assayPageNum);
			      PDDocument Ladderpd = pdlist.get(assayPDobj.ladderPageNum);
			      
			      if(Assaypd!=null && Ladderpd!=null)
			      {
			    	  //Assaypd, Ladderpd will be added to every sample
			    	  mergePdf.appendDocument(Assaypd,Ladderpd);
			      }
			    	  
			     
			      //get individual sample pages and make pdfs for each sample
	              for (int i=0; i<numPages; i++)
			      {
			            PDFTextStripper stripper = new PDFTextStripper();
		
				        // StringBuilder to store the extracted text
				        StringBuilder sb = new StringBuilder();
				        // Add text to the StringBuilder from the PDF
				        sb.append(stripper.getText(pdlist.get(i)));
				       
				        // Regex pattern: string 'Overall Results for sample',followed by BIDs
				        // allow letters,numbers and '-' in BIDs, allow Pool entries, any word character terminated by a new line 
				        Pattern p = Pattern.compile("(Overall Results for sample \\d+\\s*:)(\\s*+)(\\d+-\\d+|P\\d+)([\\s*\\w*]*)(\\n)");
				        
				        // Matcher refers to the actual text where the pattern will be found
				        Matcher m = p.matcher(sb);
	
				        while (m.find())
				        {
				             // group() method refers to the number that follows the pattern we have specified.
				        	 String samplename = m.group(3) + " "+ m.group(4);
				        	 // System.out.println("g1"+m.group(1) +"  g2"+m.group(2) +"  g3"+m.group(3)+"  g4"+m.group(4)+"  g5"+m.group(5));
				        	 samplename = samplename.trim();
				        	 samples.add(samplename);
				             //create a directory with sample name
				             boolean val = (new File(samplename)).mkdir();
				             if(val==true)
				             {
				              	 logger.log(Level.INFO, "created directory for sample:"+samplename+"\n");
				             }
				             else
				             {
				            	 logger.log(Level.INFO, "directory already exists for sample:"+samplename+"\n");
				             }
				             PDDocument pddoc = new PDDocument();
				            //save sample file
			            	 mergePdf.appendDocument(pddoc,Assaypd );
			            	 mergePdf.appendDocument(pddoc,pdlist.get(i) );
			            	 boolean exists = (new File(".//"+samplename+"//"+fnprt1+"_"+samplename+fnprt2+".pdf")).exists();
			            	 //check if file with this name exists in current directory
			            	 if (exists)
			            	 {
			            		 //probably sample re-run, save file with current timestamp
			            		 pddoc.save(".//"+samplename+"//"+fnprt1+"_"+samplename+fnprt2+" "+dateFormat.format(date)+".pdf");
				             	 logger.log(Level.INFO, "pdf file created with current date for sample:"+samplename+"\n");
			            		 filesToUpload.add(".//"+samplename+"//"+fnprt1+"_"+samplename+fnprt2+" "+dateFormat.format(date)+".pdf");
			            	 }
			            	 else
			            	 {
			            		 pddoc.save(".//"+samplename+"//"+fnprt1+"_"+samplename+fnprt2+".pdf");
			            		 logger.log(Level.INFO, "pdf file created for sample:"+samplename+"\n");
			            		 filesToUpload.add(".//"+samplename+"//"+fnprt1+"_"+samplename+fnprt2+".pdf");
			            	 }
			            	 
			            	 closepdf(pddoc);
				             
				         }//end of while
				         
				         
			      }//end of for
	     	      
	              //close Assay+Ladder page pdf	  
			      closepdf(Assaypd);
			      //close pdfs in list
			      for (int i=0; i<numPages; i++)
			      {
			        closepdf(pdlist.get(i));
			      }
			      //upload sample pdfs over ftp
			      uploadSamplePDFs(samples, filesToUpload);
				
		      }//end of if(input.canRead()==true)
		      else
		      {
		    	  logger.log(Level.INFO, "could not open pdf file\n");
	    	  
		      }
	      
		      
		      
		    } catch (Exception e) {
		      System.out.println("Exception thrown in main(): " + e);
		      e.printStackTrace();
		     
		    } 
		    finally
		    {
		    	 //close input pdf
	              closepdf(pd);
	        	  fh.close();
		    }

		  }//end of main
//---------------------------------------------------------------------------------------
// Method to upload pdf files over ftp
// input args: list of sample names, list of filenames on current directory to upload
// returns : --
//---------------------------------------------------------------------------------------
public static void uploadSamplePDFs(List<String> samples,List<String> filesToUpload)
{
	if(!samples.isEmpty())
    {
  	  FTPupload FTPobj = new FTPupload();
	      int ret = FTPobj.ftpUpload(samples,filesToUpload,logger);
	      switch(ret)
	      {
	      case 0:
	      {
	    	  logger.log(Level.INFO, "Files uploaded to dmel successfully\n");
	    	  logger.log(Level.INFO, "BIDs uploaded are:\n");
	    	  //list out all samples uploaded
	    	  for (String str : samples)
              {
	    		  logger.log(Level.INFO,str+"\n"); 
              }
	    	  break;
	      }
	      case -1:
	      {
	    	  logger.log(Level.INFO, "ftp server refused connection\n");
	    	  break;
	      }
	      case -2:
	      {
	    	  logger.log(Level.INFO, "ftp connection closed\n");
	    	  break;
	      }
	      case -3:
	      {
	    	  logger.log(Level.INFO, "some or all files were not uploaded\n");
	    	  break;
	      }
	      case -4:
	      {
	    	  logger.log(Level.INFO, "IO Exception from ftp client\n");
	    	  logger.log(Level.INFO, "Error from ftp client\n");
	    	  break;
	    	 
	      }
	      }
    } //end of if(!samples.isEmpty())
}
//---------------------------------------------------------------------------------------
// Method to close pdf files
// input args : file of type PDDocument to be closed
// returns : --
//---------------------------------------------------------------------------------------
public static void closepdf(PDDocument pd)
{
	if (pd != null)
	{
		try 
		{
			pd.close();
		} 
		catch (IOException e) 
		{
			logger.log(Level.WARNING, "error closing pdf \n");
			e.printStackTrace();
		}
	}
}
//---------------------------------------------------------------------------------------	
// Method to setup logging
// input args: objects of type logger, fileHandler, logManager, date format and date
// returns : --
//---------------------------------------------------------------------------------------
public static void setuplogger(Logger logger,FileHandler fh, LogManager lm, DateFormat dateFormat, Date date)
{
	
	try
	{
		lm.addLogger(logger);
	    logger.setLevel(Level.INFO);
		fh.setFormatter(new SimpleFormatter());
		logger.addHandler(fh);
		// Setting ParentHandlers to False as we don't want messages logged twice both on console and file. Comment this to see messages on console.
		logger.setUseParentHandlers(false);
	}
	catch (Exception e) 
	{
	      System.out.println("Exception thrown in setuplogger(): " + e);
	      e.printStackTrace();
	}
	
	
}//end of setuplogger()

//---------------------------------------------------------------------------------------
// Method to get the most recent pdf in current directory
// input : --
// returns : latest pdf file as File 
//---------------------------------------------------------------------------------------
public static File latestpdf()
{
	File input = null;
	try
	{
		File currDir = new File(".");
	    FilenameFilter filter = new FilenameFilter() {
	  	    public boolean accept(File dir, String name) {
	  	        return name.endsWith(".pdf");
	  	    }
	  	};
	  	
	    logger.log(Level.INFO, "Looking for pdf files in current directory...\n");
	    File[] files = currDir.listFiles(filter);
	    if(files.length<=0)
	    {
	  	  logger.log(Level.INFO, "no pdf files in current directory\n");
	  	  System.exit(0);
	    }
	    
	    int index =0;
	    //find the most recent pdf file - need to process only the latest file
	    for (int i=0; i<files.length;i++)
	    {
	  	  if (files[i].lastModified() > files[index].lastModified())
	  		  index = i;
	    }
	    input = files[index];
	    
	}
	catch (Exception e) 
	{
	      System.out.println("Exception thrown in latestpdf(): " + e);
	      e.printStackTrace();
	}
	return input;
	
}//end of latestpdf()
	
//---------------------------------------------------------------------------------------
//---------------------------------------------------------------------------------------
	
}//end of class ReorganizeRunfiles
	
	




