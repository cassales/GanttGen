/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graphicsprogram;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.Set;

/**
 *
 * @author cassales
 */
public class GraphicsProgram {

    public static void main(String[] argS) throws IOException {
        String f1 = "AssignCut", f2 = "RMCut", name = null;
        ArrayList<Job> allJobsOrdered = new ArrayList<>();
        
        
        
        //get all files with .log extension inside the folder
        File[] logFiles = getLogFiles();
        //populate array with all jobs. Will consider each .log file as a job.
        for (File f : logFiles) {
            name = f.getName().substring(0, f.getName().indexOf("."));
            allJobsOrdered.add(new Job(Parser.parseFiles(new File(f1+name), new File(f2+name)), name));
        }
        //get the latest container to finish among all jobs
        //run through each array to discover the container with the latest finish time 
        int lf = Parser.getLatestFinishTime(allJobsOrdered);
        //smaller 10 multiple greater than lf
        int latest_finish = (10 - lf%10) + lf;
        System.out.println("Latest Finish " + latest_finish);
        //for each job, generate Gantt
        for (Job j : allJobsOrdered) {
            System.out.println("------------------------------------------------");
            System.out.println("Scenario: " + j.getName());
            printSynopsis(j.getJobOrdered());
            //get the node names
            Set<String> uniqueNodes = Parser.getUniqueNodesSet(j.getJobOrdered());
            //array to hold the containers separated by nodes
            ArrayList<ArrayList<ContainerData>> nodeSeparatedOrdered = new ArrayList<>();
            nodeSeparatedOrdered = Parser.separateJobOrderedInNodes(j.getJobOrdered(), uniqueNodes);
            //draw gantt for each node array.
            DrawHelper dH = new DrawHelper(nodeSeparatedOrdered, j.getName(), latest_finish);
            dH.drawGantts();
        }
    }

    private static void printSynopsis(ArrayList<ContainerData> jobOrdered) {
        int latestFinishTime = 0;
        int totalMaps=0, numberMaps=0;
        for (ContainerData d : jobOrdered) {
            if (d.getEndTime() > latestFinishTime)
                latestFinishTime = d.getEndTime();
            numberMaps++;
            totalMaps+=d.getTotalTime();
        }
        //show metrics
        float mediumTime = (float)totalMaps/(float)numberMaps;
        System.out.println("Total Map Time " + latestFinishTime);
        System.out.println("Medium Map Time " + mediumTime);
        System.out.println("Standard Deviation " + calcStdDev(jobOrdered, mediumTime));
    }

    private static float calcStdDev(ArrayList<ContainerData> jobOrdered, float mediumTime) {
        float res = 0;
        float sumOfDiffSquared = 0;
        int numberDiffs = 0;
        for (ContainerData d : jobOrdered) {
            float diff = d.getTotalTime() - mediumTime;
            float pow = diff * diff;
            sumOfDiffSquared += pow;
            numberDiffs++;
        }
        res = (float) Math.sqrt(sumOfDiffSquared/numberDiffs);
        return res;
    }

    private static File[] getLogFiles() {
        File fs = new File(".");
        //create new filename filter
        FilenameFilter fileNameFilter = new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                String str = "";
                if (name.lastIndexOf('.') > 0) {
                    //get extension
                    str = name.substring(name.lastIndexOf('.'));
                    
                    if (str.equals(".log")) {
                        return true;
                    }
                }
                return false;
            }
        };
        return fs.listFiles(fileNameFilter);
    }
}