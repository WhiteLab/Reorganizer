
/**
 * @author pmakella
 *
 */

//---------------------------------------------------------------------------------------
// class to join strings with a delimiter
// Method join
// input args: list of strings, delimiter
// returns : elements of list joined by delimiter as a single string
//---------------------------------------------------------------------------------------
public class JoinStr {
	
	    //joins array elements of String with delimiter d
		public  String join(String r[],String d)
		{
		        StringBuilder sb = new StringBuilder();
		        int i;
		       
		        for(i=0;i<r.length-1;i++)
		        {
		        	sb.append(r[i]+d);
		        }       
		        return sb.toString()+r[i];
		}

}
//---------------------------------------------------------------------------------------