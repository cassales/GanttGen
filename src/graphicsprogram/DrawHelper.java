/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graphicsprogram;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 *
 * @author cassales
 */
public class DrawHelper {
    //image size. must be calculated/adjusted.
    public static final int DEFAULT_WIDTH = 3285;
    public static final int DEFAULT_HEIGHT = 250;
    
    //steps
    public static final int STEP_HORIZONTAL = 1;
    public static final int STEP_VERTICAL = 10;
    
    //chart maximum x axis
    public static final int DEFAULT_LATEST_END = 500;
    //height of each node line
    public static final int DEFAULT_HEIGHT_GANTT = 45;
    
    //predefined spaces for components
    public static final int DEFAULT_VERTICAL_SPACE = 15;
    public static final int DEFAULT_HORIZONTAL_SPACE = 5;
    public static final int DEFAULT_NAME_SPACE = 80;
    
    //predefined markers size
    public static final int MARKER_SPACE_50 = 20;
    public static final int MARKER_SPACE_25 = 10;
    
    //magic variable
    public static final int PxS = 3;
    
    //predefined offsets
    //used to centralize the time on markers
    public static final int OFFSET_SINGLE = 3;
    public static final int OFFSET_DOUBLE = 8;
    public static final int OFFSET_TRIPLE = 12;
    public static final int OFFSET_QUADRUPLE = 16;
    
//    BACKUP ORIGINAL
//    public static final int width = 2485;
//    public static final int height = 250;
//    
//    public static final int STEP_HOR = 1;
//    public static final int STEP_VER = 10;
//    
//    public static final int MAIOR_FIM = 790;
//    public static final int ALTURA_GANTT = 45;
//    public static final int ESPACO_VERT = 15;
//    public static final int ESPACO_HOR = 5;
//    public static final int ESPACO_NOME = 80;
//    
//    public static final int ESPACO_MARCADOR_50 = 20;
//    public static final int ESPACO_MARCADOR_25 = 10;
//    
//    public static final int PxS = 3;
//    
//    public static final int OFFSET_SINGLE = 3;
//    public static final int OFFSET_DOUBLE = 8;
//    public static final int OFFSET_TRIPLE = 12;
//    public static final int OFFSET_QUADRUPLE = 16;
    
    
    ArrayList<ArrayList<ContainerData>> arrayROOT;
    BufferedImage bufferedImage;
    Graphics2D g2d;
    String scenario;
    int latest_finish = -1;
    int width = -1;
    int height = -1;
    boolean verbose = false;

    DrawHelper(ArrayList<ArrayList<ContainerData>> arrayArrayNodes, String scenario) {
        this.arrayROOT = arrayArrayNodes;
        this.scenario = scenario;
        latest_finish = DEFAULT_LATEST_END;
        
        // Calculate sizes before constructing the image
        width = DEFAULT_NAME_SPACE + 3*DEFAULT_HORIZONTAL_SPACE + (latest_finish*PxS); //depends on latest_finish
        height = DEFAULT_VERTICAL_SPACE + arrayROOT.size()*DEFAULT_HEIGHT_GANTT + MARKER_SPACE_50; //depends on the number of nodes of a given experiment
        
        // Constructs a BufferedImage of one of the predefined image types.
        bufferedImage = new BufferedImage(width, DEFAULT_HEIGHT, BufferedImage.TYPE_INT_RGB);

        // Create a graphics which can be used to draw into the buffered image
        g2d = bufferedImage.createGraphics();
        
        // fill all the image with white
        g2d.setColor(Color.white);
        g2d.fillRect(0, 0, width, DEFAULT_HEIGHT);
    }
    
    DrawHelper(ArrayList<ArrayList<ContainerData>> arrayArrayNodes, String scenario, int LF) {
        this.arrayROOT = arrayArrayNodes;
        this.scenario = scenario;
        latest_finish = LF;
        
        // Calculate sizes before constructing the image
        width = DEFAULT_NAME_SPACE + 4*DEFAULT_HORIZONTAL_SPACE + (latest_finish*PxS);
        height = 3*DEFAULT_VERTICAL_SPACE + arrayROOT.size()*DEFAULT_HEIGHT_GANTT + MARKER_SPACE_50; //depends on the number of nodes of a given experiment
        
        // Constructs a BufferedImage of one of the predefined image types.
        bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        // Create a graphics which can be used to draw into the buffered image
        g2d = bufferedImage.createGraphics();
        
        // fill all the image with white
        g2d.setColor(Color.white);
        g2d.fillRect(0, 0, width, DEFAULT_HEIGHT);        
    }
    
    public void drawGantts() {
        //create Gantts
        int i = 0;
        for (ArrayList<ContainerData> a : arrayROOT) {
            //create the data for a node
            Gantt g = new Gantt(a,a.get(0).getNode());
            //calculates the position of segment lines and how many active containers in each segment
            g.proccess(latest_finish, verbose);
            //draw a node line
            drawGantt(g,arrayROOT.indexOf(a));
        }
        drawTimeMarkers(arrayROOT.size());
        
        try {
            save(bufferedImage);
        } catch (IOException ex) {
            Logger.getLogger(DrawHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // Save as PNG
    private void save(BufferedImage buff) throws IOException {
        //sanity check for the directory
        File dir = new File("gen/");
        if (!dir.exists()) {
            dir.mkdir();
        }
        
        //create and save file
        File file = new File(dir.getPath() + "/" + scenario + ".png");
        ImageIO.write(buff, "png", file);
    }
    
    public void drawString(String s, float x, float y, Color col) {
        g2d.setColor(col);
        g2d.drawString(s, x, y);
    }
    
    public void drawString(String s, float x, float y, float r, float g, float b) {
        g2d.setColor(new Color(r, g, b));
        g2d.drawString(s, x, y);
    }
    
    public void drawRectangle(int x, int y, int w, int h, boolean fill) {
        if (!fill)
            g2d.drawRect(x, y, w, h);
        else
            g2d.fillRect(x, y, w, h);
    }
    
    public void drawRectangle(int x, int y, int w, int h, boolean fill, float r, float g, float b) {
        g2d.setColor(new Color(r,g,b));
        if (!fill)
            g2d.drawRect(x, y, w, h);
        else
            g2d.fillRect(x, y, w, h);
    }
    
    public void drawRectangle(int x, int y, int w, int h, boolean fill, Color c) {
        g2d.setColor(c);
        if (!fill)
            g2d.drawRect(x, y, w, h);
        else
            g2d.fillRect(x, y, w, h);
    }
        
    private void drawLine(int x1, int y1, int x2, int y2, float r, float g, float b) {
        g2d.setColor(new Color(r,g,b));
        g2d.drawLine(x1, y1, x2, y2);
    }
    
    private void drawLine(int x1, int y1, int x2, int y2, Color c) {
        g2d.setColor(c);
        g2d.drawLine(x1, y1, x2, y2);
    }

    private void drawGantt(Gantt g, int id) {
        //the starting point in x and y axis for the chart
        int x = DEFAULT_NAME_SPACE + 2*DEFAULT_HORIZONTAL_SPACE, y = DEFAULT_HEIGHT_GANTT*id+DEFAULT_VERTICAL_SPACE;
        
        //draw the resource name before each line
        this.drawString(g.resource, DEFAULT_HORIZONTAL_SPACE, y + (DEFAULT_HEIGHT_GANTT/2) + DEFAULT_HEIGHT_GANTT%2, Color.black);
        
        //controls how many segment lines each node will have
        int size = g.segmentLines.size();
        for (int i = 0; i < size; i++) {
            if (i != size -1) {
                float c = 1 - ((float)g.activeContainers.get(i)/(float)16);
                drawRectangle(x+g.segmentLines.get(i)*PxS,
                        y,
                        (g.segmentLines.get(i+1) - g.segmentLines.get(i))*PxS,
                        DEFAULT_HEIGHT_GANTT,
                        true,
                        new Color(c,c,c));
                drawLine(x+g.segmentLines.get(i)*PxS,
                        y,
                        x+g.segmentLines.get(i)*PxS,
                        y+DEFAULT_HEIGHT_GANTT,
                        Color.black);
            } else {
                drawLine(x+g.segmentLines.get(i)*PxS,
                        y,
                        x+g.segmentLines.get(i)*PxS,
                        y+DEFAULT_HEIGHT_GANTT,
                        Color.black);
            }
        }
        //cosmetic lines 
        drawLine(x+(latest_finish*PxS), y, x+(latest_finish*PxS), y+DEFAULT_HEIGHT_GANTT, Color.black);
        drawLine(x, y, x+(latest_finish*PxS), y, Color.black);
        drawLine(x, y+DEFAULT_HEIGHT_GANTT, x+(latest_finish*PxS), y+DEFAULT_HEIGHT_GANTT, Color.black);
    }

    /**
     * Draw time markers on the chart.
     * @param numberOfNodes The number of nodes contained in this experiment
     */
    private void drawTimeMarkers(int numberOfNodes) {
        int offset = OFFSET_SINGLE;
        int x = DEFAULT_NAME_SPACE + 2*DEFAULT_HORIZONTAL_SPACE, y = DEFAULT_HEIGHT_GANTT*(numberOfNodes)+DEFAULT_VERTICAL_SPACE;
        for (int i = 0; i <= latest_finish; i+=25) {
            String s = Integer.toString(i);
            if (i == 25) offset = OFFSET_DOUBLE;
            else if (i == 100) offset = OFFSET_TRIPLE;
            else if (i == 1000) offset = OFFSET_QUADRUPLE;
            
            if (i%50 == 0) {
                drawLine(x+i*PxS, y, x+i*PxS, y+MARKER_SPACE_50, Color.black);
                drawString(s, x+i*PxS-offset, y + MARKER_SPACE_50 + DEFAULT_VERTICAL_SPACE, Color.black);
            } else {
                drawLine(x+i*PxS, y, x+i*PxS, y+MARKER_SPACE_25, Color.black);
                drawString(s, x+i*PxS-offset, y + MARKER_SPACE_50 + DEFAULT_VERTICAL_SPACE, Color.black);
            }
        }
    }
}
