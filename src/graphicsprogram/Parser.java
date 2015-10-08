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

    public static ArrayList<ContainerData> parseFiles(File start, File finish) {
        ArrayList<ContainerData> jobOrdered = new ArrayList<>();

        //parse constainers start
        BufferedReader br = null;
        String line;
        try {
            br = new BufferedReader(new FileReader(start));
            while ((line = br.readLine()) != null) {
                String[] spli = line.split(" ");
                jobOrdered.add(new ContainerData(Integer.parseInt(spli[2].substring(spli[2].length() - 4)), spli[5].substring(0, spli[5].indexOf(".")), spli[0]));
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
        Collections.sort(jobOrdered);

        //parse containers finish
        try {
            br = new BufferedReader(new FileReader(finish));
            while ((line = br.readLine()) != null) {
                String[] spli = line.split(" ");
                int id = Integer.parseInt(spli[1].substring(spli[1].length() - 4)) - 1;
                jobOrdered.get(id).setStrEndTime(spli[0]);
                jobOrdered.get(id).setTotalTime();
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

//        jobOrdered = new Job(jobOrdered);
        //normalize times
//        jobOrdered = Parser.normalize(jobOrdered);
//        jobOrdered = Parser.removeReducers(jobOrdered,redNumber);
        return jobOrdered;
    }

    //returns a set<String> with the nodes used in the experiment.
    static Set<String> getUniqueNodesSet(ArrayList<ContainerData> jobOrdered) {
        List<String> nodesList = new ArrayList<>();
        for (ContainerData d : jobOrdered) {
            nodesList.add(d.getNode());
        }
        return new HashSet<String>(nodesList);
    }

    //get the latest finish time in an App.
    static int getLatestFinishTime(ArrayList<Job> arrayROOT) {
        int ret = 0;
        for (Job j : arrayROOT) {
            ArrayList<ContainerData> AL = j.getJobOrdered();
            for (ContainerData d : AL) {
                if (d.getEndTime() > ret) {
                    ret = d.getEndTime();
                }
            }
        }
        return ret;
    }

    static int getMostActiveContainers(ArrayList<Job> allJobsOrdered) {
        int max = 0;
        int soma = 0;
        for (Job j : allJobsOrdered) {
            for (GanttNode gn : j.getNodes()) {
                for (int i = 0; i < gn.activeContainers.size(); i++) {
                    soma = gn.activeContainers.get(i) + gn.activeReducers.get(i);
                    if (soma > max) {
                        max = soma;
                    }
                }
            }
        }
        return max;
    }
}
