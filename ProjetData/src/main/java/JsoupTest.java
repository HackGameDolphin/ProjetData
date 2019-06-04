    


import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;



public class JsoupTest {
	String html ;
	Document doc;
	public JsoupTest(String URL) throws IOException {
		 doc= Jsoup.connect(URL).get();
        html = doc.html();
	}
	
	
	public boolean check(String query) throws IOException {
		
     

        Pattern pattern = Pattern.compile(query);
        Matcher matcher = pattern.matcher(html);
        
        if (matcher.find()) {
            System.out.println("Found it");
            return true;
        }else {
        	System.out.println("Not Found");
        	return false;
        }

	}
	
    
    	
    
}
