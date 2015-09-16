/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graphicsprogram;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Set;

/**
 *
 * @author cassales
 */
public class GraphicsProgram {

    public static void main(String[] argS) throws IOException {
        String f1 = "AssignCut", f2 = "RMCut", s = null;
        if (argS.length == 0) {
            Scanner scanner = new Scanner(System.in);
            System.out.println("Digite o caso que deseja (A,B,C,D..)");
            s = scanner.nextLine();
        } else {
            s = argS[0];
        }
        System.out.println("s " + s);
        File start = new File(f1+s);
        File finish = new File(f2+s);
        
        ArrayList<Data> jobOrdered = Parser.parseFiles(start,finish);
        printSynopsis(jobOrdered);
        Set<String> uniqueNodes = Parser.getUniqueNodesSet(jobOrdered);
        
        ArrayList<ArrayList<Data>> nodeSeparatedOrdered = new ArrayList<>();
        nodeSeparatedOrdered = Parser.separateJobOrderedInNodes(jobOrdered,uniqueNodes);

        DrawHelper dH = new DrawHelper(nodeSeparatedOrdered,s);
        dH.drawGantts();
        
    }

    private static void printSynopsis(ArrayList<Data> jobOrdered) {
        int latestFinishTime = 0;
        int totalMaps=0, numberMaps=0;
        for (Data d : jobOrdered) {
            if (d.getEndTime() > latestFinishTime)
                latestFinishTime = d.getEndTime();
            numberMaps++;
            totalMaps+=d.getTotalTime();
        }
        float mediumTime = (float)totalMaps/(float)numberMaps;
        System.out.println("Total Map Time " + latestFinishTime);
        System.out.println("Medium Map Time " + mediumTime);
        System.out.println("Standard Deviation " + calcStdDev(jobOrdered,mediumTime));
    }

    private static float calcStdDev(ArrayList<Data> jobOrdered, float mediumTime) {
        float res = 0;
        float sumOfDiffSquared = 0;
        int numberDiffs = 0;
        for (Data d : jobOrdered) {
            float diff = d.getTotalTime() - mediumTime;
            float pow = diff * diff;
            sumOfDiffSquared += pow;
            numberDiffs++;
        }
        res = (float) Math.sqrt(sumOfDiffSquared/numberDiffs);
        return res;
    }

    
}
