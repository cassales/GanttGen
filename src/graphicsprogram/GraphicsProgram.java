/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graphicsprogram;

import static graphicsprogram.GraphicsProgram.verbose;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author cassales
 */
public class GraphicsProgram {

    public static boolean verbose = false;
    public static boolean compose = false;
    public static boolean legends = true;
    public static int latest_finish = 0;
    public static int max_containers = 0;

    private static File[] logFiles;
    private static final int DEFAULT_NUMBER_OF_REDUCES = 1;
    private static int redNumber = 0;
    private static File dir = new File("gen/");
    private static ArrayList<Job> allJobsOrdered = new ArrayList<>();

    public static void main(String[] argS) throws IOException {
        //creates dir if it doesn't exist
        //clear files if it does
        cleanDir();
        //parse arguments and assign control flags
        parseArgS(argS);
        //parse files
        //create control arrays
        //separate jobs
        //calculates things needed for drawing
        prepareJobs();
        //calculate active cont/red in every job
        //before drawing must prepare everything!
        drawAndShow();
    }

    private static File[] getLogFiles(String[] strs, boolean AF) {
        File fs = new File(".");
        //create new filename filter
        MyFilenameFilter MFF = new MyFilenameFilter(strs, AF);
        //return files filtered
        return fs.listFiles(MFF);
    }

    private static void cleanDir() {
        //create/clean directory gen
        if (!dir.exists()) {
            dir.mkdir();
            return;
        }
        String[] entries = dir.list();
        for (String s : entries) {
            File currentFile = new File(dir.getPath(), s);
            currentFile.delete();
        }
    }

    private static void prepareJobs() {
        String f1 = "AssignCut", f2 = "RMCut", name = null;
        //populate array with all jobs. Will consider each .log file as a job.
        for (File f : logFiles) {
            //get .log name
            name = f.getName().substring(0, f.getName().indexOf("."));
            //add a job after parsing all files for info
            allJobsOrdered.add(new Job(Parser.parseFiles(new File(f1 + name), new File(f2 + name)), redNumber, name));
        }

        //sort by job name
        Collections.sort(allJobsOrdered);

        //get number of reducers from file
        Parser.parseConsole(allJobsOrdered);
        for (Job j : allJobsOrdered) {
            j.organizeContainers();
        }

        //get the latest container to finish among all jobs (will SEARCH ONLY among the ones being generated)
        //run through each array to discover the container with the latest finish time
        //and assign smallest multiple of 10 greater than lf
        int lf = Parser.getLatestFinishTime(allJobsOrdered);
        latest_finish = (10 - lf % 10) + lf;
        System.out.println("Latest Finish " + latest_finish);

        //for each job
        for (Job j : allJobsOrdered) {
            //separate containers by node
            j.separeJobsInNodes();
            //create Gantt representations by job
            //each job will process and create separated information
            j.generateGantts();
        }

        //get the highest number of containers active in a given moment
        //among all jobs, including application master and reducers.
        max_containers = Parser.getMostActiveContainers(allJobsOrdered);
        System.out.println("hc: " + max_containers);
    }

    private static void drawAndShow() {
        //for each job, generate Gantt
        for (Job j : allJobsOrdered) {
            System.out.println("------------------------------------------------");
            System.out.println("Scenario: " + j.getName());
            System.out.println("Application Master on Node: " + j.getAppMaster().getNode());
            j.printSynopsis();

            DrawHelper dH = new DrawHelper(j.getNodeSeparatedOrderedContainers().size(), j.getName(), 0);
            for (GanttNode gn : j.getNodes()) {
                dH.drawGantt(gn, j.getNodes().indexOf(gn));
            }
            dH.drawTimeMarkers(j.getNodeSeparatedOrderedContainers().size());

            try {
                dH.save();
            } catch (IOException ex) {
                Logger.getLogger(DrawHelper.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (compose) {
            ArrayList<BufferedImage> abi = new ArrayList<>();
            int height = 0;

            for (Job j : allJobsOrdered) {
                abi.add(DrawHelper.loadImage(dir.getPath() + "/" + j.getName() + ".png"));
                height += abi.get(abi.size() - 1).getHeight() + 1;
            }
            //generate subtitles
            if (legends) {
                DrawHelper dH = new DrawHelper(2, "legen", 1);
                dH.drawLegends();
                try {
                    dH.save();
                } catch (IOException ex) {
                    Logger.getLogger(DrawHelper.class.getName()).log(Level.SEVERE, null, ex);
                }
                abi.add(DrawHelper.loadImage(dir.getPath() + "/" + dH.scenario + ".png"));
                height += abi.get(abi.size() - 1).getHeight() + 1;
            }
            //generate and save composed image
            BufferedImage composedBI = new BufferedImage(abi.get(0).getWidth(), height, BufferedImage.TYPE_INT_RGB);
            height = 0;
            for (BufferedImage b : abi) {
                DrawHelper.addImage(composedBI, b, 1, 0, height);
                height += b.getHeight();
            }
            DrawHelper.saveImage(composedBI, dir.getPath() + "/composed.png");
        }
    }

    private static void parseArgS(String[] argS) {
        boolean error = false, AF = true, minusG = false, minusR = false, mGAppeared = false;
        String[] a = null;
        if (argS.length == 0) {
            ;
        } else if (argS.length == 1) {
            switch (argS[0]) {
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
                default:
                    System.out.println("Unknown option " + argS[0] + ".");
                    System.out.println("-v verbose active (will print status of nodes in each segment)");
                    System.out.println("-c compose active (will create an image containing all images selected)");
                    System.out.println("-g [list of files]");
                    error = true;
                    break;
            }
        } else {
            ArrayList<String> argFiles = new ArrayList<>();
            for (String s : argS) {
                switch (s) {
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
                    default:
                        if (minusG) {
                            argFiles.add(s);
                        } else {
                            System.out.println("Unknown option " + s + ".");
                            System.out.println("-v verbose active (will print status of nodes in each segment)");
                            System.out.println("-c compose active (will create an image containing all images selected)");
                            System.out.println("-g [list of files]");
                            error = true;
                        }
                        break;
                }
            }
            a = argFiles.toArray(new String[argFiles.size()]);
        }
        if (redNumber == 0) {
            redNumber = DEFAULT_NUMBER_OF_REDUCES;
        }
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
}
