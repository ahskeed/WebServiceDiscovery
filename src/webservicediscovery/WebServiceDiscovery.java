package webservicediscovery;

import java.io.*;
import java.net.*;
import java.util.*;

import webservicediscovery.HungarianAlgorithm;
import webservicediscovery.FunctionMatching;


public class WebServiceDiscovery {
    
    public static void main(String[] args) throws IOException {
        
        BufferedReader br = new BufferedReader(new FileReader("advertisements.txt"));
        HashMap advBipartite = new HashMap();
        String adv;
        int num = 0;
        
        FunctionMatching funcMatch = new FunctionMatching();

        while((adv = br.readLine())!=null){
            advBipartite.put(adv, funcMatch.get_best_match(adv));
            num++;
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
