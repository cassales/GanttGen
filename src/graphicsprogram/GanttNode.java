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
    ContainerData appMaster;
    int lastAppMap;

    public GanttNode(ArrayList<ContainerData> cont, ArrayList<ContainerData> red, int LAM) {
        segmentLines = new ArrayList<>();
        activeContainers = new ArrayList<>();
        activeReducers = new ArrayList<>();
        containers = cont;
        reducers = red;
        resource = containers.get(0).getNode();
        appMaster = null;
        lastAppMap = LAM;
    }

    public GanttNode(ArrayList<ContainerData> cont, ArrayList<ContainerData> red, int LAM, ContainerData AM) {
        segmentLines = new ArrayList<>();
        activeContainers = new ArrayList<>();
        activeReducers = new ArrayList<>();
        containers = cont;
        reducers = red;
        resource = containers.get(0).getNode();
        appMaster = AM;
        lastAppMap = LAM;
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
                if ((appMaster != null && this.appMaster.getEndTime() > GraphicsProgram.latest_finish) 
                        || (this.reducers.size() > 0 && this.reducers.get(0).getEndTime() > GraphicsProgram.latest_finish)) {
                    segmentLines.add(GraphicsProgram.latest_finish);
                    activeContainers.add(this.getActiveContainers(GraphicsProgram.latest_finish));
                    activeReducers.add(this.getActiveReducers(GraphicsProgram.latest_finish));
                }
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
        for (int j = 0; j < containers.size() || j < reducers.size(); j++) {
            if (j < containers.size()) {
                if (containers.get(j).getStartTime() == i || containers.get(j).getEndTime() == i) {
                    return true;
                }
            }
            if (j < reducers.size()) {
                if (reducers.get(j).getStartTime() == i || reducers.get(j).getEndTime() == i) {
                    return true;
                }
            }
        }
        if (appMaster != null) {
            if (appMaster.getEndTime() == i) {
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

    public int getLastAppMap() {
        return lastAppMap;
    }
}
