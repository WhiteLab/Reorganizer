import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPConnectionClosedException;
import org.apache.commons.net.ftp.FTPReply;

/**
 * @author pmakella
 *
 */
//---------------------------------------------------------------------------------------
// class to manage file uploads over ftp
//---------------------------------------------------------------------------------------
public class FTPupload {
	
	 String ErrMsg;

//---------------------------------------------------------------------------------------
// Method to upload pdf files for all samples and tracksamples.ini to ftp server 
// input args : list of sample names, list of files to be uploaded from current directory, logger
// returns : status of upload; 0 - success, <1 - errors
//---------------------------------------------------------------------------------------
	
		public int ftpUpload(List<String> samples, List<String> filesToUpload,Logger logger)
		{
			FTPClient ftp = new FTPClient();
			int rvalue =-1;
			String fnTracksamples = "tracksamples.ini"; //filename on dmel that has list of samples and date
			
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		    //get current date time with Date()
		    Date date = new Date();
			try
	        {
	            int reply;
	            //debug
	            //String absolutePath = new File("src/FTPcredentials.config").getAbsolutePath();
	            //System.out.println(absolutePath);
	            //from jar
	            //String absolutePath = new File("./FTPcredentials.config").getAbsolutePath();
	            Config configFile = new Config("./FTPcredentials.config");
	            String server   = configFile.getProperty("server");// "xxx.xxx.xxx";
	            //System.out.println(server);
	            logger.log(Level.INFO,server+"\n");
	            String username = configFile.getProperty("username");//"xxxx";
	            String password = configFile.getProperty("password"); //"xxxx";
	            ftp.connect(server);
	            // After connection attempt, check the reply code to verify success
	            reply = ftp.getReplyCode();

	            if (!FTPReply.isPositiveCompletion(reply))
	            {
	                ftp.disconnect();
	                rvalue = -1;
	                ErrMsg = "ftp.getReplyCode returned value: " + reply +"\n";
	                logger.log(Level.INFO,ErrMsg+"\n");
	                return rvalue;
	            }
	            
	            try
	            {
	            	// Use passive mode 
	                ftp.enterLocalPassiveMode();
	                
	                if (!ftp.login(username, password))
	                {
	                    ftp.logout();
	                    rvalue = -1;
	                    ErrMsg = " Could not login to dmel with existing username and password\n";
	                    logger.log(Level.INFO,ErrMsg+"\n");
	                    return rvalue;
	                }
	                //set to binary mode, pdf files are to be transferred in binary mode
	                ftp.setFileType(FTP.BINARY_FILE_TYPE);
	                //change to public_html
	            	ftp.changeWorkingDirectory("public_html");
	            	
	            	int count =0;
	                for (String str : samples)
	                {
	                	//check if directory exists on server
	                	boolean direxists = ftp.changeWorkingDirectory(str);
	                    boolean makedir=true;
	                    boolean cddir =true;
	                	
	                	if(!direxists)
	                	{
	                		//directory does not exist, create one and cd to it
	                		makedir = ftp.makeDirectory(str);
	                		cddir = ftp.changeWorkingDirectory(str);
	                		direxists=true;
	                	}
	                	
	                	if(direxists&&makedir&&cddir)
	                	{
	                		//check local pdf file and upload
		            		boolean fileexists = (new File(filesToUpload.get(count))).exists();
	            		
		            		if(fileexists)
		            		{
		            			InputStream input;
			                    input = new FileInputStream(filesToUpload.get(count));
			                    
			                    //remove folder name ".//sample//" from pdf filename
			                    String f=filesToUpload.get(count).substring(str.length()+5);
			                    
			                    //System.out.println(filesToUpload.get(count));
			                    //System.out.println("f   " +f);
			
			                    ftp.storeFile(f, input);
			
			                    input.close();
			                    
			                    //change it back to public_html
			                    ftp.changeToParentDirectory();
			                    
			                    rvalue =0;
		            		}//end of if(fileexists)
		            	}
	                	else
	                	{
	                		rvalue = -3;
	                		ErrMsg = "Could not create sample folder on dmel\n";
	                		logger.log(Level.INFO,ErrMsg+"\n");
	                	}//end of if(direxists&&makedir&&cddir)
	                	//upload next sample
	                	count++;
	                }//end of for (String str : samples)
	                
	                
	                
	                
	                //get contents of tracksamples.ini from dmel, add sample information and store it back
	                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	                //get tracksamples.ini if it exists, else write to new outputstream
	                ftp.retrieveFile(fnTracksamples, outputStream);
	                //write each sample on a new line
	                String data="";
	                for (String temp : samples) {
	                	data = data + dateFormat.format(date) + " -- " + temp +"\n";
	            	}
	                byte[] bdata= data.getBytes();
	                outputStream.write(bdata);
	                //System.out.println("ini data"+ outputStream.toString()+"\n");
	                
	                ftp.setFileType(FTP.BINARY_FILE_TYPE, FTP.BINARY_FILE_TYPE);
	                ftp.setFileTransferMode(FTP.BINARY_FILE_TYPE);
	                InputStream istream = new ByteArrayInputStream(outputStream.toByteArray());
	                ftp.storeFile(fnTracksamples, istream);
	                istream.close();
	                outputStream.close();
	                
	                //logout
	                ftp.logout();
	            }
	            catch (FTPConnectionClosedException e)
	            {
	            	rvalue = -2;
	            	ErrMsg=e.toString();
	            	logger.log(Level.INFO,ErrMsg+"\n");
	                return rvalue;
	            }
	            catch (IOException e)
	            {
	            	rvalue = -4;
	            	ErrMsg=e.toString();
	            	logger.log(Level.INFO,ErrMsg+"\n");
	                return rvalue;
	            }
	            
	        }
	        catch (IOException e)
	        {
	        	rvalue = -4;
	        	ErrMsg=e.toString();
	        	logger.log(Level.INFO,ErrMsg+"\n");
	            return rvalue;
	        }
			finally
	        {
	            if (ftp.isConnected())
	            {
	                try
	                {
	                    ftp.disconnect();
	                }
	                catch (IOException f)
	                {
	                	ErrMsg=f.toString();
	                	logger.log(Level.INFO,ErrMsg+"\n");
	                }
	            }
	        }
			
			return rvalue;
		}//end of function ftpUpload
		
}//end of class FTPupload

//---------------------------------------------------------------------------------------
