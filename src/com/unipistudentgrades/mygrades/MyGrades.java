package com.unipistudentgrades.mygrades;

import 	android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
import android.graphics.Color;

import java.util.ArrayList;
import java.util.List;
import android.app.ListActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Intent;
import android.view.MenuInflater;
import android.preference.PreferenceManager;
import android.content.SharedPreferences;

import android.util.Log;
import android.app.Activity;
import android.app.ProgressDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class MyGrades extends ListActivity
{
//    private static final StudentsHelper studentsHelper = new StudentsHelper() ;
    private String username;
    private String password;



    private class UpdateGrades extends AsyncTask<Void, Void, List<Grade>> {

        public ProgressDialog dialog;
        protected List<Grade> doInBackground(Void... pips) {
            StudentsHelper studentsHelper = new StudentsHelper(getApplicationContext());
            studentsHelper.init(username, password);

            try{
            List <Grade> grades = studentsHelper.getGrades();
            return grades;
            }
            catch (Exception e){
                Log.i("MYGRADES", e.getMessage());
            }
            return null;
        }

        protected void onPreExecute(){

        dialog = ProgressDialog.show(MyGrades.this, "", 
            "Fetching new grades. Please wait...", true);
        }
        protected void onPostExecute(List<Grade> grades) {
        dialog.dismiss();
        if (grades.size() ==0){
        Toast toast = Toast.makeText(MyGrades.this, "No grades found", 5);
        toast.show();
        }
        else{
        setListAdapter(new GradeAdapter(MyGrades.this, grades));
        }
        }
    }




    @Override
        public void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.main);
            //            studentsHelper = new StudentsHelper();
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
                updateGrades();
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
//                    studentsHelper.empty();
                    updateGrades();
                    return true;
                default:
                    return super.onOptionsItemSelected(item);
            }
        }

    public boolean updateGrades(){
        try{
        UpdateGrades task = new UpdateGrades();
        task.execute();
        }
        catch(Exception e){
        }
        return true;
    }

    public boolean showPreferences(){
        Intent intent = new Intent(this, Preferences.class);
        startActivity(intent);
        return true;

    }

}

class GradeAdapter extends ArrayAdapter {
    private final Activity activity;
    private final List grades;
    private final int light = (new Color()).rgb(30,30,30);
    private final int dark = (new Color()).rgb(00,00,00);

    public GradeAdapter(Activity activity, List objects) {
        super(activity, R.layout.grade_list_item , objects);
        this.activity = activity;
        this.grades = objects;
    }

    @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View rowView = convertView;
            Grade grade = (Grade) grades.get(position);
                Log.i("--------------MYGRADES---------------", Integer.toString(position));
                Log.i("--------------MYGRADES---------------", grade.course_name);
            if (grade.course_grade.compareTo("-99")!=0){

                GradeView gView = null;

    //            if(rowView == null)
     //           {
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
      //              rowView.setTag(gView);
       //         } else {
        //            try{
         //           gView = (GradeView) rowView.getTag();
          //          }
           //         catch(Exception e){
            //        }
            //    }

                // Transfer the stock data from the data object
                // to the view objects

                gView.course_name.setText(grade.course_name);
                gView.course_grade.setText(grade.course_grade);
            }
            else {
                HeaderView hView = null;

//                if(rowView == null)
 //               {
                    // Get a new instance of the row layout view
                    LayoutInflater inflater = activity.getLayoutInflater();
                    rowView = inflater.inflate(R.layout.header, null);

                    // Hold the view objects in an object,
                    // so they don't need to be re-fetched
                    hView = new HeaderView();
                    hView.header = (TextView) rowView.findViewById(R.id.headerview);

                    // Cache the view objects in the tag,
                    // so they can be re-accessed later
//                    rowView.setTag(hView);
 //               } else {
  //                  hView = (HeaderView) rowView.getTag();
   //             }

                // Transfer the stock data from the data object
                // to the view objects
                hView.header.setText(grade.human_period);

            }

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

    protected static class HeaderView {
        protected TextView spacerview;
        protected TextView header;
    }
}
