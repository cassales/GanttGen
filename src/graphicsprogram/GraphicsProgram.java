/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graphicsprogram;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;


/**
 *
 * @author cassales
 */
public class GraphicsProgram {
    static boolean verbose = false, compose = false;
    static File[] logFiles;
    private static final int DEFAULT_NUMBER_OF_REDUCES = 1;
    static int redNumber = 0;
    static File dir = new File("gen/");
    
    public static void main(String[] argS) throws IOException {
        String f1 = "AssignCut", f2 = "RMCut", name = null;
        ArrayList<Job> allJobsOrdered = new ArrayList<>();
        
        cleanDir();
        
        parseArgS(argS);
        
        //populate array with all jobs. Will consider each .log file as a job.
        for (File f : logFiles) {
            name = f.getName().substring(0, f.getName().indexOf("."));
            //number of reducers
            allJobsOrdered.add(new Job(Parser.parseFiles(new File(f1+name), new File(f2+name), redNumber), name));
        }
        
        //sort by job name
        Collections.sort(allJobsOrdered);
        
        //get the latest container to finish among all jobs (will SEARCH ONLY among the ones being generated)
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
            DrawHelper dH = new DrawHelper(nodeSeparatedOrdered, j.getName(), latest_finish, verbose);
            dH.drawGantts();
        }
        if (compose) {
            File composed = new File(dir.getPath() + "/composed.png");
            ArrayList<BufferedImage> abi = new ArrayList<>();
            int height = 0;

            for (Job j : allJobsOrdered) {
                abi.add(DrawHelper.loadImage(dir.getPath() + "/" + j.getName() + ".png"));
                height += abi.get(abi.size()-1).getHeight()+1;
            }
            BufferedImage composedBI = new BufferedImage(abi.get(0).getWidth(), height, BufferedImage.TYPE_INT_RGB);
            height = 0;
            for (BufferedImage b : abi) {
                DrawHelper.addImage(composedBI, b, 1, 0, height);
                height += b.getHeight();
            }
            DrawHelper.saveImage(composedBI, dir.getPath() + "/composed.png");
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

    private static File[] getLogFiles(String[] strs, boolean AF) {
        File fs = new File(".");
        //create new filename filter
        MyFilenameFilter MFF = new MyFilenameFilter(strs, AF);
        //return files filtered
        return fs.listFiles(MFF);
    }

    private static File[] getLogFiles(String dir) {
        File fs = new File(dir);
        //create new filename filter
        MyFilenameFilter MFF = new MyFilenameFilter(null, true, ".png");
        //return files filtered
        return fs.listFiles(MFF);
    }    
    
    private static void parseArgS(String[] argS) {
        boolean error = false, AF = true, minusG = false, minusR = false, mGAppeared = false;
        String[] a = null;
        if (argS.length == 0) {
            ;
        } else if (argS.length == 1) {
            switch(argS[0]) {
                case "-v": 
                    verbose = true;
                    break;
                case "-c": 
                    compose = true;
                    break;
                case "-g":
                    System.out.println("Missing argument! Please insert files after -g.\n");
                    error = true;
                    break;
                case "-r":
                    System.out.println("Missing argument! Please insert the number after -r.\n");
                    error = true;
                    break;
                default: 
                    System.out.println("Unknown option " + argS[0] + ".");
                    System.out.println("-v verbose active (will print status of nodes in each segment)");
                    System.out.println("-c compose active (will create an image containing all images selected)");
                    System.out.println("-g [list of files]");
                    System.out.println("-r number_of_reducers");
                    error = true;
                    break;
            }
        } else {
            ArrayList<String> argFiles = new ArrayList<>();
            for (String s : argS) {
                switch(s) {
                    case "-v": 
                        verbose = true;
                        minusG = false;
                        minusR = false;
                        break;
                    case "-c": 
                        compose = true;
                        minusG = false;
                        minusR = false;
                        break;
                    case "-g":
                        minusG = true;
                        mGAppeared = true;
                        minusR = false;
                        break;
                    case "-r":
                        minusG = false;
                        minusR = true;
                        break;
                    default:
                        if (minusG) {
                            argFiles.add(s);
                        } else if (minusR) {
                            redNumber = Integer.parseInt(s);
                            minusR = false;
                        } else {
                            System.out.println("Unknown option " + s + ".");
                            System.out.println("-v verbose active (will print status of nodes in each segment)");
                            System.out.println("-c compose active (will create an image containing all images selected)");
                            System.out.println("-g [list of files]");
                            System.out.println("-r number_of_reducers");
                            error = true;
                        }
                        break;
                }
            }
            a = argFiles.toArray(new String[argFiles.size()]);
        }
        if (redNumber == 0) redNumber = DEFAULT_NUMBER_OF_REDUCES;
        if (!error) {
            logFiles = getLogFiles(a, !mGAppeared);
            if (logFiles.length == 0) {
                System.out.println("It seems I couldn't find the files you wished for. Make sure they are in the correct directory.");
                System.exit(0);
            }
        } else {
            System.exit(0);
        }
    }

    private static void cleanDir() {
        //create/clean directory gen
        String[]entries = dir.list();
        for(String s: entries){
            File currentFile = new File(dir.getPath(),s);
            currentFile.delete();
        }
        try {
            Files.deleteIfExists(dir.toPath());
        } catch (NoSuchFileException x) {
            System.err.format("%s: no such" + " file or directory%n", dir.toPath());
        } catch (DirectoryNotEmptyException x) {
            System.err.format("%s not empty%n", dir.toPath());
        } catch (IOException x) {
            // File permission problems are caught here.
            System.err.println(x);
        }        
        if (!dir.exists()) {
            dir.mkdir();
        }        
    }
}