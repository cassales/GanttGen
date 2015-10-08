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
public class GanttNode {

    String resource;
    ArrayList<Integer> segmentLines;
    ArrayList<Integer> activeContainers;
    ArrayList<Integer> activeReducers;
    ArrayList<ContainerData> containers;
    ArrayList<ContainerData> reducers;
    
    public GanttNode(ArrayList<ContainerData> cont, ArrayList<ContainerData> red) {
        segmentLines = new ArrayList<>();
        activeContainers = new ArrayList<>();
        activeReducers = new ArrayList<>();
        containers = cont;
        reducers = red;
        resource = containers.get(0).getNode();
    }

    public void proccess() {
        int nextLine = 0;
        for (int i = 0; i <= GraphicsProgram.latest_finish; i++) {
            if (i < nextLine) {
                continue;
            }
            segmentLines.add(i);
            activeContainers.add(this.getActiveContainers(i));
            activeReducers.add(this.getActiveReducers(i));
            nextLine = this.getNextLine(i);
            if (nextLine == -1) {
                break;
            }
        }
        if (GraphicsProgram.verbose) {
            System.out.println(this);
        }
    }

    private Integer getActiveContainers(int i) {
        int ret = 0;
        for (ContainerData d : containers) {
            if (d.getStartTime() <= i && d.getEndTime() > i) {
                ret++;
            }
        }
        return ret;
    }
    
    private Integer getActiveReducers(int i) {
        int ret = 0;
        for (ContainerData d : reducers) {
            if (d.getStartTime() <= i && d.getEndTime() > i) {
                ret++;
            }
        }
        return ret;
    }

    private int getNextLine(int i) {
        for (i++; i <= GraphicsProgram.latest_finish; i++) {
            if (hasLine(i)) {
                return i;
            }
        }
        return -1;
    }

    private boolean hasLine(int i) {
        for (ContainerData d : containers) {
            if (d.getStartTime() == i || d.getEndTime() == i) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        String ret = "Gantt " + resource;
        for (int i = 0; i < segmentLines.size(); i++) {
            ret += "\n timeLine " + segmentLines.get(i) + " activeContainers " + activeContainers.get(i) + " activeReducers " + activeReducers.get(i);
        }
        return ret;
    }
}
