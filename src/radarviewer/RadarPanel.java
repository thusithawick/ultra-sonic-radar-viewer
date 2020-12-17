/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package radarviewer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import jssc.SerialPort;
import jssc.SerialPortException;

/**
 *
 * @author Thusitha
 */
public class RadarPanel extends JPanel {

    private TreeSet<Reading> readingSet = new TreeSet<>();
    //private int searchAngle = 180;
    //private int skipAngle = 2;
    //int sleepTime = 100;
    private int addAngle = 90;
    private boolean painted;
    private int range = 100;
    private SerialPort port;
    private String readingData = "";
    int cols = 100;
    int rows = 100;

    public RadarPanel() {
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); //To change body of generated methods, choose Tools | Templates.
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(new Color(31, 31, 31, 255));
        g2d.fillRect(0, 0, RadarPanel.this.getWidth(), RadarPanel.this.getHeight());
        int unitWidth = RadarPanel.this.getWidth()/cols;
        int unitHeight = RadarPanel.this.getHeight()/cols;
        g2d.setColor(new Color(25,200,12,30));
        for (int y = 0; y < cols; y++) {
            g2d.drawLine(0, unitHeight*y, RadarPanel.this.getWidth(), unitHeight*y);
        }
        for (int y = 0; y < rows; y++) {
            g2d.drawLine(unitWidth*y, RadarPanel.this.getHeight(), unitWidth*y, 0);
        }
        g2d.setStroke(new BasicStroke(3));
        Iterator<Reading> it = readingSet.descendingIterator();
        while (it.hasNext()) {
            Reading r = it.next();
            //System.out.print("," + r.getDegree() + ":" + r.getDistance());
            //g.drawString("" + r.getDegree(), RadarPanel.this.getWidth() / 2, RadarPanel.this.getHeight() / 2);
            g2d.setColor(new Color(120, 20, 0, 255));
            int x1 = (int) ((RadarPanel.this.getWidth() / 2) + (RadarPanel.this.getHeight() / 2) * Math.sin(degToRad(r.getDegree() + addAngle)));
            int y1 = (int) ((RadarPanel.this.getHeight() / 2) + (RadarPanel.this.getHeight() / 2) * Math.cos(degToRad(r.getDegree() + addAngle)));
            g2d.drawLine(RadarPanel.this.getWidth() / 2, RadarPanel.this.getHeight() / 2, x1, y1);
            g2d.setColor(new Color(20, 120, 0, 255));
            int x2 = (int) ((RadarPanel.this.getWidth() / 2) + ((RadarPanel.this.getHeight() * r.getDistance()) / (range * 2)) * Math.sin(degToRad(r.getDegree() + addAngle)));
            int y2 = (int) ((RadarPanel.this.getHeight() / 2) + ((RadarPanel.this.getHeight() * r.getDistance()) / (range * 2)) * Math.cos(degToRad(r.getDegree() + addAngle)));
            g2d.drawLine(RadarPanel.this.getWidth() / 2, RadarPanel.this.getHeight() / 2, x2, y2);
        }
        g2d.setColor(Color.red);
        g2d.fillOval(unitWidth, unitHeight, unitWidth/4, unitWidth/4);
        painted = true;
    }

    public void clear() {
        readingSet.clear();
        //System.out.println("clear");
    }

    public void put(Reading reading) {
        readingSet.add(reading);
        //System.out.println("put " + reading.getDegree());
    }

    public void sort() {
        Iterator<Reading> it = readingSet.iterator();
        while (it.hasNext()) {
            Reading r = it.next();
            //System.out.println(r.getDegree());
        }
    }

    public static void main(String[] args) {
        RadarPanel rp = new RadarPanel();
        rp.put(new Reading(20, 100));
        rp.put(new Reading(5, 100));
        rp.put(new Reading(8, 100));
        rp.put(new Reading(0, 100));
        rp.put(new Reading(30, 100));
        rp.put(new Reading(100, 100));
        rp.put(new Reading(80, 100));
        rp.put(new Reading(65, 100));
        rp.sort();
    }

    private Thread readThread = new Thread(new Runnable() {

        @Override
        public void run() {
            while (true) {
                if (painted) {
                    transmit();
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(RadarPanel.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }

        private void display() {
            painted = false;
            RadarPanel.this.repaint();
        }

        private void transmit() {
            int degree = 0;
            double distance = 0;
            try {
                //System.out.println("Reading");
                String inputText = port.readString();
                //System.out.println(inputText);
                //System.out.println("Splitting");
                if (inputText != null) {
                    if (inputText.contains("s")) {
                        //System.out.println("contains");
                        inputText = inputText.replaceAll("s", "");
                        readingData = "";
                        clear();
                    }
                    readingData += inputText;
                    //System.out.println(readingData);
                    String[] pairs = readingData.split(";");
                    for (String pair : pairs) {
                        String[] values = pair.split(":");
                        if (values.length > 1) {
                            if (!values[0].equals("") && !values[1].equals("")) {
                                try {
                                    degree = Integer.parseInt(values[0]);
                                    distance = Double.parseDouble(values[1]) >= range ? range : Double.parseDouble(values[1]);
                                    //System.out.println("Parsing");
                                    //System.out.println("got " + degree + " " + distance);
                                    put(new Reading(degree, distance));
                                    //System.out.println(degree+":"+distance);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                    display();
                }
            } catch (SerialPortException ex) {
                Logger.getLogger(RadarPanel.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    });

    public double degToRad(int angle) {
        return angle * Math.PI / 180;
    }

    public void startReading() {
        readThread.start();
    }

    public void setPort(SerialPort port) {
        this.port = port;
        startReading();
    }

    public int getRange() {
        return range;
    }

    public void setRange(int range) {
        this.range = range;
    }

}
