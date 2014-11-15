package webservicediscovery;

import java.io.*;
import java.net.*;
import java.util.*;

import webservicediscovery.HungarianAlgorithm;
import webservicediscovery.FunctionMatching;


public class WebServiceDiscovery {
    
    public static void main(String[] args) throws IOException {
        
        HashMap advBipartite = new HashMap();
        String adv;
        BufferedReader br = new BufferedReader(new FileReader("advertisements.txt"));
        FunctionMatching funcMatch = new FunctionMatching();

        funcMatch.get_req_details("1personbicyclecar_price_service.owls");

        while((adv = br.readLine())!=null){
            advBipartite.put(adv, funcMatch.get_match_score(adv));
        }
        
        
        Set set = advBipartite.entrySet();
        Iterator i = set.iterator();
        while(i.hasNext()) {
            Map.Entry me = (Map.Entry)i.next();
            System.out.print(me.getKey() + ": ");
            System.out.println(me.getValue());
        }
        
        
    }
}
