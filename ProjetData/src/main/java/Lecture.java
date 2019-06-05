import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import au.com.bytecode.opencsv.CSVReader;

public class Lecture {
	public static List<String> ID = new ArrayList<String>();
	
	public static List<String> URL = new ArrayList<String>();
	public List<String> domaine;
	public List<String> Email;
	public List<String> email_status;
	public List<String> email_domaine;
	public List<String> Email_exit;
	public List<String> Entreprise;
	public List<String> Entreprise_Clean;
	public List<String> Adresses;
	public List<String> Adresses_Clean ;
	
	

	
	
	public static void main(String[] args) throws IOException {
		JsoupTest s;
		List<String> plateforme=new ArrayList<>();
		plateforme.add("wix");
		plateforme.add("woocommerce");
		plateforme.add("magento");
		plateforme.add("shopify");
		plateforme.add("prestashop");
		plateforme.add("oxatis");
		
		Map<String,Set<String>> platf= new HashMap<String,Set<String>>();
		Map<String, Integer> taille= new HashMap<String,Integer>();
		platf.put("wix", new HashSet<String>());
		platf.put("woocommerce", new HashSet<String>());
		platf.put("magento", new HashSet<String>());
		platf.put("shopify", new HashSet<String>());
		

        BufferedReader br = new BufferedReader(new FileReader("select_from_EMB_Madagascar_FR_dbo_Madagascar_France_where_e_comm_201906031057.csv"));
        String ligne =  br.readLine();
        int j=0;
        while ((ligne = br.readLine()) != null)
         {
          // Retourner la ligne dans un tableau
          String[] data = ligne.split(",");
     
          // Afficher le contenu du tableau
          j++;
          if(j==100)break;
      
          	if(!data[0].equals("822")) {
          		
            ID.add(data[0]);
            URL.add(data[1]);
            try {
            s=new JsoupTest(data[1]);
           
        		for(String e: plateforme) {
        			
        	if(s.check( e)) {
        	
        		platf.get(e).add(data[1]);
        		break;
        	}
        	
        	
        	
        		}s.getPageLinks(data[1], 0);
        		System.out.println(JsoupTest.getPAGE_NUMBER());
        		taille.put(data[1], JsoupTest.getPAGE_NUMBER());
  }catch ( Exception e){
        	
        }
           
          	}
        }
        for(Map.Entry<String,Integer> e : taille.entrySet()) {
        	System.out.println("url =" +e.getKey()+ " taille ="+e.getValue());
        }
        br.close();
        
        	

	
}
}
