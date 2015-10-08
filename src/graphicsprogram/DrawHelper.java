/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graphicsprogram;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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

    ArrayList<ArrayList<ContainerData>> arrayContainersByNodes;
    ArrayList<ArrayList<ContainerData>> arrayReducersByNodes;
    BufferedImage bufferedImage;
    Graphics2D g2d;
    String scenario;
    int width = -1;
    int height = -1;

    DrawHelper(int numberOfGantts, String scenario) {
        this.scenario = scenario;

        // Calculate sizes before constructing the image
        width = DEFAULT_NAME_SPACE + 4 * DEFAULT_HORIZONTAL_SPACE + (GraphicsProgram.latest_finish * PxS);
        height = 3 * DEFAULT_VERTICAL_SPACE + numberOfGantts * DEFAULT_HEIGHT_GANTT + MARKER_SPACE_50; //depends on the number of nodes of a given experiment

        // Constructs a BufferedImage of one of the predefined image types.
        bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        // Create a graphics which can be used to draw into the buffered image
        g2d = bufferedImage.createGraphics();

        // fill all the image with white
        g2d.setColor(Color.white);
        g2d.fillRect(0, 0, width, DEFAULT_HEIGHT);
    }

    // Save as PNG
    public void save(BufferedImage buff) throws IOException {
        //sanity check for the directory
        File dir = new File("gen/");

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
        if (!fill) {
            g2d.drawRect(x, y, w, h);
        } else {
            g2d.fillRect(x, y, w, h);
        }
    }

    public void drawRectangle(int x, int y, int w, int h, boolean fill, float r, float g, float b) {
        g2d.setColor(new Color(r, g, b));
        if (!fill) {
            g2d.drawRect(x, y, w, h);
        } else {
            g2d.fillRect(x, y, w, h);
        }
    }

    public void drawRectangle(int x, int y, int w, int h, boolean fill, Color c) {
        g2d.setColor(c);
        if (!fill) {
            g2d.drawRect(x, y, w, h);
        } else {
            g2d.fillRect(x, y, w, h);
        }
    }

    private void drawLine(int x1, int y1, int x2, int y2, Color c) {
        g2d.setColor(c);
        g2d.drawLine(x1, y1, x2, y2);
    }

    public void drawGantt(GanttNode g, int id) {
        //the starting point in x and y axis for the chart
        int x = DEFAULT_NAME_SPACE + 2 * DEFAULT_HORIZONTAL_SPACE, y = DEFAULT_HEIGHT_GANTT * id + DEFAULT_VERTICAL_SPACE;

        //draw the resource name before each line
        this.drawString(g.resource, DEFAULT_HORIZONTAL_SPACE, y + (DEFAULT_HEIGHT_GANTT / 2) + DEFAULT_HEIGHT_GANTT % 2, Color.black);

        //controls how many segment lines each node will have
        int size = g.segmentLines.size();
        for (int i = 0; i < size; i++) {
            if (i != size - 1) {
                float c = 1 - ((float) g.activeContainers.get(i) / (float) 16);
                drawRectangle(x + g.segmentLines.get(i) * PxS,
                        y,
                        (g.segmentLines.get(i + 1) - g.segmentLines.get(i)) * PxS,
                        DEFAULT_HEIGHT_GANTT,
                        true,
                        new Color(c, c, c));
                drawLine(x + g.segmentLines.get(i) * PxS,
                        y,
                        x + g.segmentLines.get(i) * PxS,
                        y + DEFAULT_HEIGHT_GANTT,
                        Color.black);
            } else {
                drawLine(x + g.segmentLines.get(i) * PxS,
                        y,
                        x + g.segmentLines.get(i) * PxS,
                        y + DEFAULT_HEIGHT_GANTT,
                        Color.black);
            }
        }
        //cosmetic lines 
        drawLine(x + (GraphicsProgram.latest_finish * PxS), y, x + (GraphicsProgram.latest_finish * PxS), y + DEFAULT_HEIGHT_GANTT, Color.black);
        drawLine(x, y, x + (GraphicsProgram.latest_finish * PxS), y, Color.black);
        drawLine(x, y + DEFAULT_HEIGHT_GANTT, x + (GraphicsProgram.latest_finish * PxS), y + DEFAULT_HEIGHT_GANTT, Color.black);
    }

    /**
     * Draw time markers on the chart.
     *
     * @param numberOfNodes The number of nodes contained in this experiment
     */
    private void drawTimeMarkers(int numberOfNodes) {
        int offset = OFFSET_SINGLE;
        int x = DEFAULT_NAME_SPACE + 2 * DEFAULT_HORIZONTAL_SPACE, y = DEFAULT_HEIGHT_GANTT * (numberOfNodes) + DEFAULT_VERTICAL_SPACE;
        for (int i = 0; i <= GraphicsProgram.latest_finish; i += 25) {
            String s = Integer.toString(i);
            if (i == 25) {
                offset = OFFSET_DOUBLE;
            } else if (i == 100) {
                offset = OFFSET_TRIPLE;
            } else if (i == 1000) {
                offset = OFFSET_QUADRUPLE;
            }

            if (i % 50 == 0) {
                drawLine(x + i * PxS, y, x + i * PxS, y + MARKER_SPACE_50, Color.black);
                drawString(s, x + i * PxS - offset, y + MARKER_SPACE_50 + DEFAULT_VERTICAL_SPACE, Color.black);
            } else {
                drawLine(x + i * PxS, y, x + i * PxS, y + MARKER_SPACE_25, Color.black);
                drawString(s, x + i * PxS - offset, y + MARKER_SPACE_50 + DEFAULT_VERTICAL_SPACE, Color.black);
            }
        }
    }

    /**
     * prints the contents of buff2 on buff1 with the given opaque value.
     *
     * @param buff1 buffer
     * @param buff2 buffer
     * @param opaque how opaque the second buffer should be drawn
     * @param x x position where the second buffer should be drawn
     * @param y y position where the second buffer should be drawn
     */
    public static void addImage(BufferedImage buff1, BufferedImage buff2, float opaque, int x, int y) {
        Graphics2D g2d = buff1.createGraphics();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opaque));
        g2d.drawImage(buff2, x, y, null);
        g2d.dispose();
    }

    /**
     * returns a BufferedImage from the Image provided.
     *
     * @param ref path to image
     * @return loaded image
     */
    public static BufferedImage loadImage(String ref) {
        BufferedImage b1 = null;
        try {
            b1 = ImageIO.read(new File(ref));
        } catch (IOException e) {
            System.out.println("error loading the image: " + ref + " : " + e);
        }
        return b1;
    }

    /**
     * writes the image in the provided buffer to the destination file.
     *
     * @param buff buffer to be saved
     * @param dest destination to save at
     */
    public static void saveImage(BufferedImage buff, String dest) {
        try {
            File outputfile = new File(dest);
            ImageIO.write(buff, "png", outputfile);
        } catch (IOException e) {
            System.out.println("error saving the image: " + dest + ": " + e);
        }
    }
}
