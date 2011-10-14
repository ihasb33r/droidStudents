package com.unipistudentgrades.mygrades;

public class Grade{

    public String course_grade;
    public String course_name;
    public String period;


    public Grade(){
    }
    public Grade(String name, String value){
        this.course_grade = value;
        this.course_name = name;
        this.period = "";
    }

    public void setPeriod(String period, String year){
        String examperiod = "0";

        if (period.charAt(0)=='Ι'){
            examperiod = "2";
        }
        else if (period.charAt(0)=='Φ'){
            examperiod = "1";
        }
        else if (period.charAt(0)=='Σ'){
            examperiod = "3";
        }
        else{
            examperiod = "0";
            year="0000";
        }
        this.period = year.substring(0,4)+examperiod;

    }

}

