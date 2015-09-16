/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graphicsprogram;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author cassales
 */
public class Data implements Comparable {
    private int containerNumber;
    private String node;
    private int startTime;
    private String strStartTime;
    private int endTime;
    private String strEndTime;
    private long totalTime;

    public Data(int containerNumber, String node, String strStartTime, String strEndTime) {
        this.containerNumber = containerNumber;
        this.node = node;
        this.strStartTime = strStartTime;
        this.strEndTime = strEndTime;
    }

    public Data(int containerNumber, String node, String strStartTime) {
        this.containerNumber = containerNumber;
        this.node = node;
        this.strStartTime = strStartTime;
    }
    
    public int getContNumber() {
        return containerNumber;
    }

    public void setContNumber(int containerNumber) {
        this.containerNumber = containerNumber;
    }

    public String getNode() {
        return node;
    }

    public void setNode(String node) {
        this.node = node;
    }

    public int getStartTime() {
        return startTime;
    }

    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }

    public int getEndTime() {
        return endTime;
    }

    public void setEndTime(int endTime) {
        this.endTime = endTime;
    }

    public String getStrStartTime() {
        return strStartTime;
    }

    public void setStrStartTime(String strStartTime) {
        this.strStartTime = strStartTime;
    }

    public String getStrEndTime() {
        return strEndTime;
    }

    public void setStrEndTime(String strEndTime) {
        this.strEndTime = strEndTime;
    }

    public long getTotalTime() {
        return totalTime;
    }

    @Override
    public String toString() {
        return "Data {" + "containerNumber=" + containerNumber + ", node=" + node + ", startTime=" + startTime + ", strStartTime=" + strStartTime + ", endTime=" + endTime + ", strEndTime=" + strEndTime + ", totalTime=" + totalTime + '}';
    }
    
    @Override
    public int compareTo(Object o) {
        Data d = (Data) o;
        return this.containerNumber-d.containerNumber;
    }

    void setTotalTime() {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss,SSS");
        Date d1 = null;
        Date d2 = null;

        try {
            d1 = format.parse(strStartTime);
            d2 = format.parse(strEndTime);

            //in milliseconds
            long diff = d2.getTime() - d1.getTime();

            this.totalTime = (diff / 1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void normalize(String startTimeFirstContainer) {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss,SSS");
        Date dMine = null;
        Date dFirst = null;
        try {
            dMine = format.parse(strStartTime);
            dFirst = format.parse(startTimeFirstContainer);

            long diff = (dMine.getTime() - dFirst.getTime())/1000;

            this.startTime = (int)diff;
            this.endTime = startTime + (int)totalTime;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
