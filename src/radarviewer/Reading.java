/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package radarviewer;

import java.util.Comparator;

/**
 *
 * @author Thusitha
 */
public class Reading implements Comparable<Reading>{
    private int degree;
    private double distance;
    
    public static final Comparator<Reading> readingComparator = new Comparator<Reading>() {

        @Override
        public int compare(Reading o1, Reading o2) {
            return o1.degree-o2.degree;
        }
    };

    public Reading(int degree, double distance) {
        this.degree = degree;
        this.distance = distance;
    }

    public int getDegree() {
        return degree;
    }

    public void setDegree(int degree) {
        this.degree = degree;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    @Override
    public int compareTo(Reading o) {
        return this.degree-o.degree;
    }
    
}
