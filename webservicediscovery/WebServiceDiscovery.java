package webservicediscovery;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.json.JSONArray;
import org.json.JSONObject;

import webservicediscovery.FunctionMatching;


public class WebServiceDiscovery {

	static List<String> req_in_params = new ArrayList<>();
    static List<String> adv_in_params = new ArrayList<>();
    static List<String> req_out_params = new ArrayList<>();
    static List<String> adv_out_params = new ArrayList<>();
    static String req_description, adv_description;

    static String base_path = "http://127.0.0.1";

    public static void get_adv_details(String file_name)
    {
        try
        {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(file_name);
            doc.getDocumentElement().normalize();
            NodeList nodes = doc.getElementsByTagName("process:parameterType");
            List<String> in_params = new ArrayList<>();
            List<String> out_params = new ArrayList<>();
            for(int i=0;i<nodes.getLength();i++) {
                switch (nodes.item(i).getParentNode().getNodeName()) {
                    case "process:Input":
                        in_params.add(nodes.item(i).getTextContent());
                        break;
                    case "process:Output":
                        out_params.add(nodes.item(i).getTextContent());
                        break;
                }
            }
            adv_in_params.clear();
            adv_out_params.clear();
            adv_in_params.addAll(in_params);
            adv_out_params.addAll(out_params);
            nodes = doc.getElementsByTagName("profile:textDescription");
            if(nodes.getLength()==1) {
                adv_description = nodes.item(0).getTextContent();
                adv_description = adv_description.replace("\n", " ");
            }
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
        
    }

    /*public static void get_req_details(String req_file)
    {
        try
        {
            String file_name = base_path+"/queries/1.1/"+req_file;
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(file_name);
            doc.getDocumentElement().normalize();
            NodeList nodes = doc.getElementsByTagName("process:parameterType");
            List<String> in_params = new ArrayList<>();
            List<String> out_params = new ArrayList<>();
            for(int i=0;i<nodes.getLength();i++) 
            {
                switch (nodes.item(i).getParentNode().getNodeName()) 
                {
                    case "process:Input":
                        in_params.add(nodes.item(i).getTextContent());
                        break;
                    case "process:Output":
                        out_params.add(nodes.item(i).getTextContent());
                        break;
                }
            }
            req_in_params.clear();
            req_out_params.clear();
            req_in_params.addAll(in_params);
            req_out_params.addAll(out_params);
            nodes = doc.getElementsByTagName("profile:textDescription");
            if(nodes.getLength()==1) {
                req_description = nodes.item(0).getTextContent();
                req_description = req_description.replace("\n", " ");
            }
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
    }*/

    public static void get_req_details(String req_file)
    {
    	try
    	{
    		BufferedReader br = new BufferedReader(new FileReader(req_file));
	    	String line = "", jsonString = "";
	    	JSONObject request;
	    	JSONArray jsonArray;
	    	List<String> in_params = new ArrayList<>();
	    	List<String> out_params = new ArrayList<>();
	    	String description = "";

	    	in_params.clear();
	    	out_params.clear();

	    	while ((line = br.readLine()) != null) {
				jsonString += line;
			}

			br.close();

			request = new JSONObject(jsonString);
			jsonArray = request.getJSONArray("input");
			for(int i=0;i<jsonArray.length();i++)
			{
				in_params.add("http://127.0.0.1/ontology/"+jsonArray.getString(i));
			}
			jsonArray = request.getJSONArray("output");
			for(int i=0;i<jsonArray.length();i++)
			{
				out_params.add("http://127.0.0.1/ontology/"+jsonArray.getString(i));
			}
			description = request.getString("description");

			req_in_params.clear();
			req_out_params.clear();
			req_in_params.addAll(in_params);
			req_out_params.addAll(out_params);
			req_description = description;
    	}
    	catch(Exception ex)
        {
            ex.printStackTrace();
        }
    	
    }


    public static double get_match_score(String adv_file, List<String> reqKeywords)
    {
        try
        {
            double func_match_score=0, desc_match_score=0;
            String file_name = base_path+"/services/1.1/"+adv_file;
            
            FunctionMatching funcMatch = new FunctionMatching();
            ServiceDescMatchingEngine serviceDescMatching = new ServiceDescMatchingEngine();
            

            get_adv_details(file_name);

            func_match_score = funcMatch.get_best_match(adv_file, req_in_params, req_out_params, adv_in_params, adv_out_params);
            desc_match_score = 1 - serviceDescMatching.getProximity(req_description, adv_file, reqKeywords);

            return ((0.4*desc_match_score)+(0.6*func_match_score));
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
        return -1.0;
    }

    private static Map<String, Double> sortByComparator(Map<String, Double> unsortMap) {
 
		// Convert Map to List
		List<Map.Entry<String, Double>> list = 
			new LinkedList<Map.Entry<String, Double>>(unsortMap.entrySet());
 
		// Sort list with comparator, to compare the Map values
		Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {
			public int compare(Map.Entry<String, Double> o1,
                                           Map.Entry<String, Double> o2) {
				return (o1.getValue()).compareTo(o2.getValue());
			}
		});
 
		// Convert sorted map back to a Map
		Map<String, Double> sortedMap = new LinkedHashMap<String, Double>();
		for (Iterator<Map.Entry<String, Double>> it = list.iterator(); it.hasNext();) {
			Map.Entry<String, Double> entry = it.next();
			sortedMap.put(entry.getKey(), entry.getValue());
		}
		return sortedMap;
	}
 
	public static void printMap(Map<String, Double> map) {
		int num=0;
		for (Map.Entry<String, Double> entry : map.entrySet()) {
			System.out.println(entry.getKey() + " : " + entry.getValue());
			num++;
			if(num==10) break;
		}
	}

	public static void get_results(Map<String, Double> map) {
		try
		{
			int num=0;
			String adv_file;
			double score;
			JSONObject adv = new JSONObject();
			JSONObject results = new JSONObject();
			FileWriter fileWriter = new FileWriter("result_advs.json");
			for (Map.Entry<String, Double> entry : map.entrySet()) {
				adv_file = entry.getKey();
				score = entry.getValue();
				get_adv_details(base_path+"/services/1.1/"+adv_file);
				adv.put("input", adv_in_params);
				adv.put("output", adv_out_params);
				adv.put("description", adv_description);
				results.put(adv_file, adv);
				System.out.println(adv_file + " : " + score);
				num++;
				if(num==10) break;
			}
			fileWriter.write(results.toString(4));
			fileWriter.close();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
	}
    
    public static void main(String[] args) throws IOException {
        
        Map<String,Double> advBipartite = new HashMap<String,Double>();
        String adv_file;
        BufferedReader br = new BufferedReader(new FileReader("advertisements.txt"));
        double match_score = -1.0;
        List<String> reqKeywords = new ArrayList<String>();
        ServiceDescMatchingEngine serviceDescMatching = new ServiceDescMatchingEngine();

        // get_req_details("1personbicyclecar_price_service.owls");
        get_req_details("config.json");


        serviceDescMatching.extractKeywords(req_description, reqKeywords);
        while((adv_file = br.readLine())!=null){
        	match_score = get_match_score(adv_file, reqKeywords);
            advBipartite.put(adv_file, match_score);
        }

        br.close();

        Map<String, Double> sortedMap = sortByComparator(advBipartite);
		get_results(sortedMap);

        
        // Set set = advBipartite.entrySet();
        // Iterator i = set.iterator();
        // while(i.hasNext()) {
        //     Map.Entry me = (Map.Entry)i.next();
        //     System.out.print(me.getKey() + ": ");
        //     System.out.println(me.getValue());
        // }

        // System.out.println(sorted_map);


        
    }
}
