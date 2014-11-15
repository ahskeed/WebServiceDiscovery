/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webservicediscovery;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import webservicediscovery.HungarianAlgorithm;



public class WebServiceDiscovery {

    static List<String> req_in_params = new ArrayList<>();
    static List<String> adv_in_params = new ArrayList<>();
    static List<String> req_out_params = new ArrayList<>();
    static List<String> adv_out_params = new ArrayList<>();
    
    // static String base_path = "/home/deeksha/WebServices/OWLS-TC4_PDDL/htdocs";
   static String base_path = "http://127.0.0.1";
    
    static int w1=1, w2=2, w3=3, w4=4;
    
    public static void get_description() throws IOException, ParserConfigurationException, SAXException {
        
        BufferedReader br = new BufferedReader(new FileReader("advertisements.txt"));
        PrintWriter write = new PrintWriter(new FileWriter("adv_desc.txt"));
        String adv_file,desc;
        while((adv_file = br.readLine())!=null){
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(base_path+"/services/1.1/"+adv_file);
            doc.getDocumentElement().normalize();
            NodeList nodes = doc.getElementsByTagName("profile:textDescription");
            if(nodes.getLength()==1) {
                desc = nodes.item(0).getTextContent();
                desc = desc.replace("\n", " ");
                write.print(adv_file+"|"+desc+"\n");
            }
        }
        br.close();
        write.close();
    }
    
    public static void get_params(String type, String file_name) throws IOException, ParserConfigurationException, SAXException {
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
                    in_params.add(nodes.item(i).getTextContent().replace("http://127.0.0.1", base_path));
                    break;
                case "process:Output":
                    out_params.add(nodes.item(i).getTextContent().replace("http://127.0.0.1", base_path));
                    break;
            }
        }
        switch (type) {
            case "Request":         req_in_params.clear();
                                    req_out_params.clear();
                                    req_in_params.addAll(in_params);
                                    req_out_params.addAll(out_params);
                                    break;
            case "Advertisement":   adv_in_params.clear();
                                    adv_out_params.clear();
                                    adv_in_params.addAll(in_params);
                                    adv_out_params.addAll(out_params);
                                    break;
        }
    }
    
    public static int match(String query_param, String adv_param) throws OWLOntologyCreationException {
        try {
            int start = base_path.length();
            if(!(query_param.substring(start, query_param.indexOf('#')).equals(adv_param.substring(start, adv_param.indexOf('#'))))) {
                return w4;
            }
//            System.out.println(query_param);
            // File file = new File(query_param.substring(0, query_param.indexOf('#')));
            InputStream file = new URL(query_param.substring(0, query_param.indexOf('#'))).openStream();
            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
            OWLOntology ont = manager.loadOntologyFromOntologyDocument(file);
            for (OWLClass owl_class : ont.getClassesInSignature()) {
                if(owl_class.toStringID().equals("http://www.w3.org/2002/07/owl#Thing")||owl_class.toStringID().equals("http://www.w3.org/2002/07/owl#Property")) {
                    //System.out.println(owl_class);
                    continue;
                }
                else {
                    //System.out.println("Not true: "+owl_class.getIRI());
                    String adv = adv_param.replace(base_path, "http://127.0.0.1");
                    String query = query_param.replace(base_path, "http://127.0.0.1");
                    if(adv.equals(owl_class.toStringID())) {
                        //Check for OutA equivalent to OutR
                        if(adv.equals(query)) {
                            return w1;
                        }
                        for(OWLClassExpression equiv_class : owl_class.getEquivalentClasses(ont)) {
                            if(!equiv_class.isAnonymous()) {
                                if(query.equals(equiv_class.asOWLClass().toStringID())) {
                                    return w1;
                                }
                            }
                        }

                        //Check for OutA is superclass of OutR
                        //Plus check for OutA subsumes OutR
                        int size;
                        List<OWLClassExpression> subsumes = new ArrayList<>();
                        subsumes.addAll(owl_class.getSubClasses(ont));
                        size = subsumes.size();
                        for(int i=0;i<subsumes.size();i++) {
                            if(!subsumes.get(i).isAnonymous()) {
                                if(query.equals(subsumes.get(i).asOWLClass().toStringID())) {
                                    if(i<size) {
                                        return w1;
                                    }
                                    else {
                                        return w2;
                                    }
                                }
                                else {
                                    subsumes.addAll(subsumes.get(i).asOWLClass().getSubClasses(ont));
                                }
                            }
                        }

                        //Check for OutR subsumes OutA
                        List<OWLClassExpression> subsumed = new ArrayList<>();
                        subsumed.addAll(owl_class.getSuperClasses(ont));
                        for(int i=0;i<subsumed.size();i++) {
                            if(!subsumed.get(i).isAnonymous()) {
                                if(query.equals(subsumed.get(i).asOWLClass().toStringID())) {
                                    return w3;
                                }
                                else {
                                    subsumed.addAll(subsumed.get(i).asOWLClass().getSuperClasses(ont));
                                }
                            }
                        }
                        //Every match failed
                        return w4;
                    }
                }
            }
            return w4;
        } catch (Exception ex) {
            ex.printStackTrace();
        } 
        return 0;
    }
    
    public static void print_graph(int[][] graph) {
        for(int i=0;i<graph.length;i++) {
            for(int j=0;j<graph[i].length;j++) {
                System.out.print(graph[i][j]+"  ");
            }
            System.out.println();
        }
    }
    
    public static double get_best_match(String adv_file) 
    {
        try 
        {
            //get_params("Request", base_path+"/queries/1.1/shoppingmall_cameraprice_service.owls");
            get_params("Request", base_path+"/queries/1.1/1personbicyclecar_price_service.owls");
            get_params("Advertisement", base_path+"/services/1.1/"+adv_file);
            int irow = adv_in_params.size(), icol = req_in_params.size();
            int orow = adv_out_params.size(), ocol = req_out_params.size();
            int isize, osize;
            isize = irow>=icol?irow:icol;
            osize = orow>=ocol?orow:ocol;
            int[][] in_spp = new int[isize][isize];
            int[][] out_spp = new int[osize][osize];
            int [][] in_temp = new int[in_spp.length][];
            int [][] out_temp = new int[out_spp.length][];
            int in_score, out_score;
            
            for(int[] row: in_spp) {
                Arrays.fill(row, 0);
            }
            for(int[] row: out_spp) {
                Arrays.fill(row, 0);
            }
            
            //For input parameters
            for(int i=0;i<irow;i++) {
                for(int j=0;j<icol;j++) {
//                    System.out.println("Input");
                    in_spp[i][j] = match(req_in_params.get(j),adv_in_params.get(i));
                }
            }
            
//            System.out.println("The bipartite graph constructed for input parameters is as follows:");
//            print_graph(in_spp);   
            
            for(int i = 0; i < in_spp.length; i++) {
                in_temp[i] = in_spp[i].clone();
            }
            HungarianAlgorithm hungarian = new HungarianAlgorithm();
            int in_res[][] = hungarian.computeAssignments(in_temp);
            /*int result[][] = new int[in_spp.length][in_spp[0].length];
            for(int[] row: result) {
                Arrays.fill(row, -1);
            }*/
            in_score = 0;
            for(int i=0;i<in_res.length;i++) {
                in_score += in_spp[in_res[i][0]][in_res[i][1]];
            }
            System.out.println("Input parameters match score: " + in_score);
            
            //For output parameters
            for(int i=0;i<orow;i++) {
                for(int j=0;j<ocol;j++) {
//                    System.out.println("Output");
                    out_spp[i][j] = match(req_out_params.get(j),adv_out_params.get(i));
                }
            }
            
//            System.out.println("The bipartite graph constructed for output parameters is as follows:");
//            print_graph(out_spp);
            
            
            for(int i = 0; i < out_spp.length; i++) {
                out_temp[i] = out_spp[i].clone();
            }
            int out_res[][] = hungarian.computeAssignments(out_temp);
            /*int result[][] = new int[out_spp.length][out_spp[0].length];
            for(int[] row: result) {
                Arrays.fill(row, -1);
            }*/
            out_score = 0;
            for(int i=0;i<out_res.length;i++) {
                out_score += out_spp[out_res[i][0]][out_res[i][1]];
            }
            System.out.println("Output parameters match score: " + out_score);
            return ((0.4*in_score)+(0.6*out_score));
            
        } 
        catch(Exception ex) 
        {
            ex.printStackTrace();
        }
        return 0.0;
    }
    
    public static void get_files() throws IOException {
        File folder = new File("C:\\xampp\\htdocs\\services\\1.1");
        File[] listOfFiles = folder.listFiles();
        PrintWriter write = new PrintWriter(new FileWriter("advertisements.txt"));

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                System.out.println(listOfFiles[i].getName());
                write.print(listOfFiles[i].getName()+"\n");
            } 
        }
        write.close();
    }
    
    public static void main(String[] args) throws OWLOntologyCreationException, IOException, ParserConfigurationException, SAXException {
        
        BufferedReader br = new BufferedReader(new FileReader("advertisements.txt"));
        HashMap advBipartite = new HashMap();
        String adv;
        int num = 0;
//        adv = "3wheeledcar_price_service.owls";
//        advBipartite.put(adv, get_best_match(adv));
        while((adv = br.readLine())!=null&&num<10){
            advBipartite.put(adv, get_best_match(adv));
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
