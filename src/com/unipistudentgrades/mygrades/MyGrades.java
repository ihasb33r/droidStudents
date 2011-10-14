package com.unipistudentgrades.mygrades;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Intent;
import android.view.MenuInflater;
import android.preference.PreferenceManager;
import android.content.SharedPreferences;


import java.util.Comparator;
import java.util.Collections;
import java.io.*;
import java.lang.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.apache.http.protocol.HTTP;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class MyGrades extends ListActivity
{


    public static List<Grade> grades = null;
    private String username;
    private String password;

    @Override
        public void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.main);
        }

    @Override
        public void onStart(){
            super.onStart();
        }

    @Override
        public void onResume(){
            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
            username  = sharedPrefs.getString("unipi_username", null);
            password  = sharedPrefs.getString("unipi_password", null);
            if (username == null || password == null){
              showPreferences();
            }
            else{

                updateGrades(username, password);
            }
            super.onResume();
        }



    @Override
        public boolean onCreateOptionsMenu(Menu menu) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.unipi_menu, menu);
            return true;

        }

    @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.preferences:
                    showPreferences();
                    return true;
                case R.id.update:
                    this.grades = null;
                    updateGrades(username, password);
                    return true;
                default:
                    return super.onOptionsItemSelected(item);
            }
        }

    public boolean updateGrades(String username, String password){

        if (grades == null){
            grades =new ArrayList(); 
            String page = null;
            try{
                page = getGradesPage(username, password);
            }
            catch(Exception e){
            }

            try{
                if (page!=null){
                    grades = getGrades(page);
                }
                else{
                }
            }
            catch(Exception e){
            }
        }
        setListAdapter(new GradeAdapter(this, grades));
        return true;

    }
    public boolean showPreferences(){
        Intent intent = new Intent(this, Preferences.class);
        startActivity(intent);
        return true;

    }


    public List<Grade> getGrades( String page) {
        List<Grade> grades = new ArrayList<Grade>();
        String table = page;
        Pattern linepattern = Pattern.compile("<tr height=\"25\" bgcolor=\"#fafafa\">.*?/tr>");
        Matcher matcher = linepattern.matcher(table);
        String NL = System.getProperty("line.separator");
        Pattern columnpattern = Pattern.compile("<td.*?/td>");
        Pattern removehtml = Pattern.compile("<.*?>");
        while (matcher.find()){
            String line = matcher.group();
            Matcher columnmatcher = columnpattern.matcher(line);
            int counter =0;
            String[] columns = {"","","","","","","","","","",""};
            while (columnmatcher.find()){
                Matcher clean = removehtml.matcher(columnmatcher.group());
                String item = clean.replaceAll("");
                columns[counter]=item;
                counter= counter +1;
            }
            Grade grade = new Grade();
            grade.course_grade = columns[6];
            grade.course_name = columns[1].substring(columns[1].indexOf(')')+3);
            String[] lastfield = columns[7].trim().split(" ");
            String period = lastfield[0];
            String year = lastfield[lastfield.length-1].replaceAll("\n", "").trim();
            grade.setPeriod(period.trim(),year.trim()); 
            grades.add(grade);

        }

        GradePeriodComparator comparator = new GradePeriodComparator();
        Collections.sort(grades, new GradePeriodComparator());
        return grades;  
    }





    public String getGradesPage(String username, String password) throws Exception {
        String page = null;
        try {
            HttpClient client = new DefaultHttpClient();

            HttpPost request = new HttpPost("http://students.unipi.gr/login.asp");
            List <NameValuePair> nvps = new ArrayList <NameValuePair>();
            nvps.add(new BasicNameValuePair("userName", username));
            nvps.add(new BasicNameValuePair("pwd", password));
            nvps.add(new BasicNameValuePair("loginTrue", "login"));
            nvps.add(new BasicNameValuePair("submit1", "Είσοδος"));
            request.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
            HttpResponse response = client.execute(request);
            response = client.execute(request);
            HttpGet getrequest = new HttpGet("http://students.unipi.gr/stud_CResults.asp?studPg=1&mnuid=mnu3");
            response = client.execute(getrequest);
            BufferedReader r = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "ISO-8859-7"));
            StringBuilder total = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) {
                total.append(line);
            }
            page  =total.toString(); 
            return page;
        } finally {
        }
    }
}
class GradeAdapter extends ArrayAdapter {
    private final Activity activity;
    private final List grades;

    public GradeAdapter(Activity activity, List objects) {
        super(activity, R.layout.grade_list_item , objects);
        this.activity = activity;
        this.grades = objects;
    }

    @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View rowView = convertView;
            GradeView gView = null;

            if(rowView == null)
            {
                // Get a new instance of the row layout view
                LayoutInflater inflater = activity.getLayoutInflater();
                rowView = inflater.inflate(R.layout.grade_list_item, null);

                // Hold the view objects in an object,
                // so they don't need to be re-fetched
                gView = new GradeView();
                gView.course_name = (TextView) rowView.findViewById(R.id.course_name);
                gView.course_grade = (TextView) rowView.findViewById(R.id.course_grade);

                // Cache the view objects in the tag,
                // so they can be re-accessed later
                rowView.setTag(gView);
            } else {
                gView = (GradeView) rowView.getTag();
            }

            // Transfer the stock data from the data object
            // to the view objects
            Grade grade = (Grade) grades.get(position);
            gView.course_name.setText(grade.course_name);
            gView.course_grade.setText(grade.course_grade);

            return rowView;
        }
    @Override
        public int getCount(){
            return (this.grades!=null) ? this.grades.size() : 0;
        }

    protected static class GradeView {
        protected TextView course_name;
        protected TextView course_grade;
    }
}
class Grade{

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

class GradePeriodComparator implements Comparator{
    public int compare(Object Grade1,Object  Grade2){

        String grade1period = ((Grade) Grade1).period;
        String grade2period = ((Grade) Grade2).period;

        return -grade1period.compareTo(grade2period);
    }
}
