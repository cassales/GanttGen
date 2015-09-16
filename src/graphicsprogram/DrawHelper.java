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
    //image size
    public static final int width = 3285; //must be calculated/adjusted.
    public static final int height = 250;
    
    //steps
    public static final int STEP_HORIZONTAL = 1;
    public static final int STEP_VERTICAL = 10;
    
    //graph size
    public static final int LONGEST_END = 790; //longest container, must be calculated/adjusted
    public static final int HEIGHT_GANTT = 45;
    
    //predefined spaces for components
    public static final int VERTICAL_SPACE = 15;
    public static final int HORIZONTAL_SPACE = 5;
    public static final int NAME_SPACE = 80;
    
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
    
    
    ArrayList<ArrayList<Data>> arrayROOT;
    BufferedImage bufferedImage;
    Graphics2D g2d;
    String scenario;
    public int latest_finish = -1;

    DrawHelper(ArrayList<ArrayList<Data>> arrayArrayNodes, String caso) {
        this.arrayROOT = arrayArrayNodes;
        this.scenario = caso;
        
        // Constructs a BufferedImage of one of the predefined image types.
        bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        // Create a graphics which can be used to draw into the buffered image
        g2d = bufferedImage.createGraphics();
        
        // fill all the image with white
        g2d.setColor(Color.white);
        g2d.fillRect(0, 0, width, height);
        
    }
    
    public void drawGantts() {
        //create Gantts (auxiliar class)
        int end = (latest_finish == -1)? LONGEST_END : latest_finish;
        int i = 0;
        for (ArrayList<Data> a : arrayROOT) {
            Gantt g = new Gantt(a,a.get(0).getNode());
            g.proccess(end);
            drawGantt(g,i);
            i++;
        }
        drawTimeMarkers(i);

        
        try {
            save(bufferedImage);
        } catch (IOException ex) {
            Logger.getLogger(DrawHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void save(BufferedImage buff) throws IOException {
        // Save as PNG
        File file = new File(scenario + ".png");
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
        int x = NAME_SPACE + 2*HORIZONTAL_SPACE, y = HEIGHT_GANTT*id+VERTICAL_SPACE;
        this.drawString(g.resource, HORIZONTAL_SPACE, y + (HEIGHT_GANTT/2) + HEIGHT_GANTT%2, Color.black);
        int size = g.segmentLines.size();
        for (int i = 0; i < size; i++) {
            if (i != size -1) {
                float c = 1 - ((float)g.activeContainers.get(i)/(float)16);
                drawRectangle(x+g.segmentLines.get(i)*PxS,
                        y,
                        (g.segmentLines.get(i+1) - g.segmentLines.get(i))*PxS,
                        HEIGHT_GANTT,
                        true,
                        new Color(c,c,c));
                drawLine(x+g.segmentLines.get(i)*PxS,
                        y,
                        x+g.segmentLines.get(i)*PxS,
                        y+HEIGHT_GANTT,
                        Color.black);
            } else {
                drawLine(x+g.segmentLines.get(i)*PxS,
                        y,
                        x+g.segmentLines.get(i)*PxS,
                        y+HEIGHT_GANTT,
                        Color.black);
            }
        }
        //linhas 
        drawLine(x+(LONGEST_END*PxS), y, x+(LONGEST_END*PxS), y+HEIGHT_GANTT, Color.black);
        drawLine(x, y, x+(LONGEST_END*PxS), y, Color.black);
        drawLine(x, y+HEIGHT_GANTT, x+(LONGEST_END*PxS), y+HEIGHT_GANTT, Color.black);
    }

    private void drawTimeMarkers(int id) {
        int offset = OFFSET_SINGLE;
        int x = NAME_SPACE + 2*HORIZONTAL_SPACE, y = HEIGHT_GANTT*(id)+VERTICAL_SPACE;
        for (int i = 0; i <= LONGEST_END; i+=25) {
            String s = Integer.toString(i);
            if (i == 25) offset = OFFSET_DOUBLE;
            else if (i == 100) offset = OFFSET_TRIPLE;
            else if (i == 1000) offset = OFFSET_QUADRUPLE;
            
            if (i%50 == 0) {
                drawLine(x+i*PxS, y, x+i*PxS, y+MARKER_SPACE_50, Color.black);
                drawString(s, x+i*PxS-offset, y + MARKER_SPACE_50 + VERTICAL_SPACE, Color.black);
            } else {
                drawLine(x+i*PxS, y, x+i*PxS, y+MARKER_SPACE_25, Color.black);
                drawString(s, x+i*PxS-offset, y + MARKER_SPACE_50 + VERTICAL_SPACE, Color.black);
            }
        }
    }
}
