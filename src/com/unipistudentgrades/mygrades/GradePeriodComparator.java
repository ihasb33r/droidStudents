package com.unipistudentgrades.mygrades;
import java.util.Comparator;

public class GradePeriodComparator implements Comparator{
    public int compare(Object Grade1,Object  Grade2){

        String grade1period = ((Grade) Grade1).period;
        String grade2period = ((Grade) Grade2).period;

        return -grade1period.compareTo(grade2period);
    }
}
