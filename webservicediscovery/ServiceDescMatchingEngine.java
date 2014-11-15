package webservicediscovery;

import java.io.*;
import java.util.*;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import com.aliasi.spell.JaccardDistance;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;

public class ServiceDescMatchingEngine {
	//Function that uses alchemy api to extract keywords from a string and stores as a list
	public static void extractKeywords(String text, List<String> keywords) {
		HttpClient client = new DefaultHttpClient();
		HttpPost post = new HttpPost(
				"http://access.alchemyapi.com/calls/text/TextGetRankedKeywords");
		try {
			
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
			nameValuePairs.add(new BasicNameValuePair("apikey",
					"a8d0e155bc9d3d2d340b8484028ee02b34db267a"));//c1c093e87c8baa1aaff9f2500e3626722ae38e9d
			nameValuePairs.add(new BasicNameValuePair("text", text));
			nameValuePairs.add(new BasicNameValuePair("outputMode", "json"));
			post.setEntity(new UrlEncodedFormEntity(nameValuePairs));

			HttpResponse response = client.execute(post);
			BufferedReader rd = new BufferedReader(new InputStreamReader(
					response.getEntity().getContent()));
			String line = "", jsonString = "";
			while ((line = rd.readLine()) != null) {
				jsonString += line;
			}
			
			JSONObject mainObj = new JSONObject(jsonString);
			JSONArray jsonArray = mainObj.getJSONArray("keywords");
			JSONObject jsonObject;
			int count = jsonArray.length(); // get totalCount of all jsonObjects
			for (int i = 0; i < count; i++) { // iterate through jsonArray
				jsonObject = jsonArray.getJSONObject(i); // get jsonObject @ i position
				keywords.add(jsonObject.get("text").toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	//Function that calculates the Jaccard distance and returns the average proximity between the two lists
    public static double calcJaccardDistance(List<String> reqKeywords, List<String> advKeywords) {
        TokenizerFactory tokenizerFactory = IndoEuropeanTokenizerFactory.INSTANCE;
        JaccardDistance jaccard = new JaccardDistance(tokenizerFactory);
        double proximity=0;
        for (String s1 : reqKeywords) {
            for (String s2 : advKeywords) {
                proximity+=jaccard.proximity(s1,s2);
            }
        }
        return proximity/(advKeywords.size()*reqKeywords.size());
    }

    public static double fuzzyLevenshteinDistance(String a_s1,String a_s2)
    {
    	try{
        StringBuffer sb1 = new StringBuffer(a_s1);
        StringBuffer sb2 = new StringBuffer(a_s2);
        int i, j;
        int n = sb1.length ();
        int m = sb2.length ();
        double x, y, z;
        String s;
        Hashtable ht = new Hashtable ();
        for(i =0; i<n+1; i++)
        {
            s = i + ",0" ;
            ht.put (s,new Double (0));
        }
        for(j =0; j<m+1; j++)
        {
            s = "0," + j ;
            ht.put (s,new Double (0));
        }
        for(i =1; i<n+1; i++)
        {
            for(j =1; j<m+1; j++)
            {
                s =(i -1) + "," + j ;
                x = (( Double) ht.get (s)).doubleValue () + 1.0;
                s = i + "," +(j -1);
                y = (( Double) ht.get (s)).doubleValue () + 1.0;
                if(sb1.charAt (i -1) == sb2.charAt (j -1))
                {
                    s =(i -1) + "," +(j -1);
                    z = (( Double) ht.get (s)).doubleValue ();
                }
                else
                {
                    s =(i -1) + "," +(j -1);
                    z = (( Double)ht.get(s)).doubleValue() + 1.0;
                }
                s = i + "," + j ;
                ht.put (s,new Double(Math.min(x,Math.min(y,z))));
            }
        }
        s = n + "," + m ;
        return (( Double)ht.get(s)).doubleValue();
        }
        catch(Exception e) {
        	e.printStackTrace();
        }
        return 0;
    }
    
    public static double fNear(String a_sValue1,String a_sValue2)
    {
        double dDistance = 0.0;
        double dMinLen = 0.0;
        String str1 = a_sValue1.trim().toUpperCase();
        String str2 = a_sValue2.trim().toUpperCase();
        dMinLen = Math.min(str1.length(),str2.length());
        // Levensthein Distance returns value [0,inf)
        dDistance = fuzzyLevenshteinDistance(str1,str2);
        if (dDistance > dMinLen) 
        {
           return 0.0;
        }
        return 1 - (dDistance/dMinLen);
    }

    public static double calcLevenshteinDistance(List<String> reqKeywords, List<String> advKeywords) {
        double proximity=0;
        for (String s1 : reqKeywords) {
            for (String s2 : advKeywords) {
                proximity+=fNear(s1,s2);
            }
        }
        return proximity/(advKeywords.size()*reqKeywords.size());
    }

    public static double getProximity(String reqDesc, String advFile, List<String> reqKeywords)
    {
		List<String> advKeywords = new ArrayList<String>();
		String line = new String();
		String advDesc = new String();
		String id = new String();
		String[] split=new String[2];
		double proximity = -1.0, jaccard, levensthein;
		try {
			BufferedReader inputStream = new BufferedReader(new FileReader("adv_keywords_final.txt"));
			while ((line = inputStream.readLine()) != null) 
            {
				split = line.split("\\|");
				advDesc = split[1];
				id = split[0];
				if(!id.equalsIgnoreCase(advFile))
				{
					advKeywords = Arrays.asList(advDesc.split(",")); 
					jaccard = calcJaccardDistance(reqKeywords, advKeywords);
					levensthein = calcLevenshteinDistance(reqKeywords, advKeywords);
					proximity = (0.5*jaccard) + (0.5*levensthein);
					return proximity;
				}
            }
			inputStream.close();
		}
		catch(Exception e) {
			System.out.println("Exception");
		}
		return proximity;
    }
}
