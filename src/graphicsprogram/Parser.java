/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graphicsprogram;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author cassales
 */
public class Parser {   
    
    public static ArrayList<ContainerData> parseFiles(File start, File finish, int redNumber) {
        ArrayList<ContainerData> ret = new ArrayList<>();
        
        //parse constainers start
        BufferedReader br = null;
        String line;
        try {
            br = new BufferedReader(new FileReader(start));
            while ((line = br.readLine()) != null) {
                String[] spli = line.split(" ");
                ret.add(new ContainerData(Integer.parseInt(spli[2].substring(spli[2].length()-4)), spli[5].substring(0,spli[5].indexOf(".")), spli[0]));
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        
        
        
        //sort by container
        Collections.sort(ret);
        
        //parse containers finish
        try {
            br = new BufferedReader(new FileReader(finish));
            while ((line = br.readLine()) != null) {
                String[] spli = line.split(" ");
                int id = Integer.parseInt(spli[1].substring(spli[1].length()-4))-1;
                ret.get(id).setStrEndTime(spli[0]);
                ret.get(id).setTotalTime();
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        
        
        //normalize times
        ret = Parser.normalize(ret);
        ret = Parser.removeReducers(ret,redNumber);
        
        return ret;
    }

    //nomalize times referent to the first Map container
    private static ArrayList<ContainerData> normalize(ArrayList<ContainerData> in) {
        in.remove(0); //remove ApplicationMaster, will always be the first container
        String sTimeNorm = in.get(0).getStrStartTime();
        for (ContainerData d : in) { 
            d.normalize(sTimeNorm);
        }
        return in;
    }

    //remove reduce containers (the longest ones)
    private static ArrayList<ContainerData> removeReducers(ArrayList<ContainerData> in, int reduces) {
        for (int i = 0; i < reduces; i++) {
            in = removeLongestFinishTime(in);
        }
        return in;
    }
    
    //auxiliar function for removing reducers
    private static ArrayList<ContainerData> removeLongestFinishTime(ArrayList<ContainerData> in) {
        long longestTime = 0;
        ContainerData objLongest = null;
        for (ContainerData d : in) {
            if (d.getTotalTime() > longestTime) {
                objLongest = d;
                longestTime = objLongest.getTotalTime();
            }
        }
        if (objLongest != null)
            in.remove(objLongest);
        return in;
    }
    
    //returns a set<String> with the nodes used in the experiment.
    static Set<String> getUniqueNodesSet(ArrayList<ContainerData> jobOrdered) {
        List<String> nodesList = new ArrayList<>();
        for (ContainerData d : jobOrdered)
            nodesList.add(d.getNode());
        return new HashSet<String>(nodesList);
    }

    //separate the AppArray in as many arrays as the number of slaves.
    static ArrayList<ArrayList<ContainerData>> separateJobOrderedInNodes(ArrayList<ContainerData> jobOrdered, Set<String> uniqueNodes) {
        ArrayList<ArrayList<ContainerData>> separatedArrayNodes = new ArrayList<ArrayList<ContainerData>>();
        
        for (String s : uniqueNodes) {
            ArrayList<ContainerData> arrayAux = new ArrayList<>();
            for (ContainerData d : jobOrdered) {
                if (d.getNode().equals(s)) {
                    arrayAux.add(d);
                }
            }
            separatedArrayNodes.add(arrayAux);
        }

        return separatedArrayNodes;
    }

    //get the latest finish time in an App.
    static int getLatestFinishTime(ArrayList<Job> arrayROOT) {
        int ret = 0;
        for (Job j : arrayROOT) {
            ArrayList<ContainerData> AL = j.getJobOrdered();
            for (ContainerData d : AL) {
                if (d.getEndTime() > ret)
                    ret = d.getEndTime();
            }
        }
        return ret;
    }
}
