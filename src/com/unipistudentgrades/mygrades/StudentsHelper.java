
package com.unipistudentgrades.mygrades;
import android.content.Context;
import java.util.Collections;
import android.util.Log;
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

public class StudentsHelper{

    public static final String LOGIN_PAGE = "https://students.unipi.gr/login.asp";
    public static final String GRADES_PAGE = "https://students.unipi.gr/stud_CResults.asp?studPg=1&mnuid=mnu3";
    public static final String ENCODING = "ISO-8859-7";
    //      public static final String GRADES_PAGE = "http://192.168.1.127:8000/koko.html";
    //      public static final String ENCODING = "UTF-8";
    public static final String LINE_PATTERN_STRING = "<tr height=\"25\" bgcolor=\"#fafafa\">.*?/tr>";
    public static final Pattern LINE_PATTERN = Pattern.compile(LINE_PATTERN_STRING);
    public static final String COLUMN_PATTERN_STRING = "<td.*?/td>";
    public static final Pattern COLUMN_PATTERN = Pattern.compile(COLUMN_PATTERN_STRING);
    public static final String HTML_TAG_PATTERN_STRING = "<.*?>";
    public static final Pattern HTML_TAG = Pattern.compile(HTML_TAG_PATTERN_STRING);
    public static final String NL = System.getProperty("line.separator");
    public static final String TAG ="StudentsHelper";
    public static final int NAME_POSITION = 1;
    public static final int GRADE_POSITION = 6;
    public static final int PERIOD_POSITION = 7;
    public String username = null;
    public String password = null;
    public List<Grade> grades = null;
    public int size=0;
    public Context context;

    public StudentsHelper(Context context){
        this.context = context;
    }

    public void init(String username, String password){
        this.username = username;
        this.password = password;
    }

    public boolean isEmpty(){
        return (size==0);
    }

    public void empty(){
        this.size=0;
    }

    public void updateGrades() {
        String table= null;
        try{
            table = this.getGradesPage();
        }
        catch (Exception e){
        }
        grades = new ArrayList<Grade>();

        if (table !=null){
            Matcher matcher = LINE_PATTERN.matcher(table);
            while (matcher.find()){
                String line = matcher.group();
                Matcher columnMatcher = COLUMN_PATTERN.matcher(line);
                String[] columns = {"","","","","","","","","","",""};
                int counter =0;
                while (columnMatcher.find()){
                    Matcher clean = HTML_TAG.matcher(columnMatcher.group());
                    String item = clean.replaceAll("");
                    columns[counter]=item;
                    counter= counter +1;
                }
                Grade grade = new Grade();
                grade.course_grade = columns[GRADE_POSITION];
                grade.course_name = columns[NAME_POSITION].substring(columns[NAME_POSITION].indexOf(')')+3);
                String[] lastfield = columns[PERIOD_POSITION].trim().split(" ");
                String period = lastfield[0];
                String year = lastfield[lastfield.length-1].replaceAll("\n", "").trim();
                grade.setPeriod(period.trim(),year.trim()); 
                grades.add(grade);

            }

            GradePeriodComparator comparator = new GradePeriodComparator();
            Collections.sort(grades, new GradePeriodComparator());
            if (grades.size() >0){
                int counter = 1;
                grades.add(0, new Grade(grades.get(0).human_period));
                String current_period = grades.get(1).period;
                Log.i(TAG, current_period);
                Grade nextGrade;
                while (counter < grades.size()){
                    nextGrade = grades.get(counter);
                    if (nextGrade.period.compareTo(current_period)!=0){
                        current_period = nextGrade.period;
                        grades.add(counter,new  Grade(nextGrade.human_period));
                        counter = counter +1;
                    }
                    counter = counter +1;
                }
            }


        }
        this.size = grades.size();
    }

    public List<Grade> getGrades(){
        if (this.isEmpty()){
            updateGrades();
        }
        Log.i(TAG, "size" + size);
        return this.grades;
    }


    public String getGradesPage() throws Exception{
        String page = null;
        try {
            //            HttpClient client = new DefaultHttpClient();
            HttpClient client = new MyHttpClient(this.context);

            HttpPost request = new HttpPost(LOGIN_PAGE);
            List <NameValuePair> nvps = new ArrayList <NameValuePair>();
            nvps.add(new BasicNameValuePair("userName", username));
            nvps.add(new BasicNameValuePair("pwd", password));
            nvps.add(new BasicNameValuePair("loginTrue", "login"));
            nvps.add(new BasicNameValuePair("submit1", "Είσοδος"));
            request.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
            HttpResponse response = client.execute(request);
            response.getEntity().consumeContent();
            response = client.execute(request);
            response.getEntity().consumeContent();
            HttpGet getrequest = new HttpGet(GRADES_PAGE);
            response = client.execute(getrequest);
            BufferedReader r = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), ENCODING));
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
