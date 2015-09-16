/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graphicsprogram;

import java.util.ArrayList;

/**
 *
 * @author cassales
 */
public class Gantt {
    
    String resource;
    ArrayList<Integer> segmentLines;
    ArrayList<Integer> activeContainers;
    ArrayList<Data> input;
    int larger;

    public Gantt(ArrayList<Data> in, String res) {
        segmentLines = new ArrayList<>();
        activeContainers = new ArrayList<>();
        input = in;
        resource = res;
    }

    public void proccess(int _larger) {
        larger = _larger;
        int nextLine = 0;
        for (int i = 0; i <= larger; i++) {
            if (i < nextLine) 
                continue;
            segmentLines.add(i);
            activeContainers.add(this.getActive(i));
            nextLine = this.getNextLine(i);
            if (nextLine == -1)
                break;
        }
        System.out.println(this);
    }

    private Integer getActive(int i) {
        int ret = 0;
        for (Data d : input) {
            if (d.getStartTime() <= i && d.getEndTime() > i) {
                ret++;
            }
        }
        return ret;
    }

    private int getNextLine(int i) {
        for (i++; i <= larger; i++) {
            if(hasLine(i)) {
                return i;
            }
        }
        return -1;
    }

    private boolean hasLine(int i) {
        for (Data d : input) {
            if(d.getStartTime() == i || d.getEndTime() == i)
                return true;
        }
        return false;
    }

    @Override
    public String toString() {
        String ret = "Gantt " + resource;
        for (int i = 0; i < segmentLines.size(); i++) {
            ret += "\n timeLine " + segmentLines.get(i) + " activeContainers " + activeContainers.get(i);
        }
        return ret;
    }
}