/**
 * 
 */

/**
 * @author pmakella
 *
 */



import java.io.FileInputStream;
import java.util.Properties;

//---------------------------------------------------------------------------------------
// class to read config file
// construct takes filename as argument
// all 'key=value' pairs in config will be available as .getProperty
// example config for this application
// FTP.config
// # FTP credentials
// server = xx
// username = xx
// password = xx
//---------------------------------------------------------------------------------------
public class Config {
	
	   Properties prop = new Properties();
	   public Config(String fin)
	   {
		   
		   try
		    {
			   prop.load(new FileInputStream(fin));
		  
	    	}
		    catch(Exception e){
		    e.printStackTrace();
		}
	   }

	   public String getProperty(String key)
	   {
		String value = this.prop.getProperty(key);
		return value;
	   }
	

}
