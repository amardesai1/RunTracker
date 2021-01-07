package com.example.psyad9.runtracker;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cursoradapter.widget.SimpleCursorAdapter;

import static com.example.psyad9.runtracker.Contract.*;


public class RunList extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_runs);
        sortbyDate(null);
    }

    //two methods that call the list loading method, each with a different sorting method
    public void sortbyDistance(View view)
    {
        loadlist(null,"distance ASC");
    }
    public void sortbyDate(View view)
    {
        loadlist(null,"_id ASC");
    }


    //list loading method which makes a cursor which can access the content provider to get a list of runs and display in the listview using an adapter in a defined order
    public void loadlist(View view, String s) {
        ListView lv = findViewById(R.id.runslist);
        String[] projection = new String[] {DATABASE_RUN_NAME, DATABASE_ID, DATABASE_DATE, DATABASE_START_TIME, DATABASE_LENGTH,DATABASE_COORDS, DATABASE_DISTANCE, DATABASE_WEATHER, DATABASE_NOTES, DATABASE_RATING};
        String colsToDisplay [] = new String[] {DATABASE_RUN_NAME};
        int[] colResIds = new int[] {R.id.value1,};
        final Cursor cursor = getContentResolver().query(RECORDS_URI, projection, null, null, s);
        SimpleCursorAdapter dataAdapter = new SimpleCursorAdapter(this, R.layout.itemlist, cursor, colsToDisplay, colResIds, 0);
        lv.setAdapter(dataAdapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> dataAdapter, View myView, int position, long id) {
                cursor.moveToPosition(position);
                int recordid =   cursor.getInt(cursor.getColumnIndex(DATABASE_ID));
                onViewRecipe(recordid);


            }
        });
    }


    //method called by pressing any list items which opens the run info activity page
    //passes the run id attribute of the selected run through a bundle to display in the activity
    public void onViewRecipe(int id)
    {
        Intent intent = new Intent(this, ViewRun.class);

        intent.putExtra(DATABASE_ID, id) ;
        startActivity(intent);
    }


    //method that refreshes the run list after returning to the activity, so that it reflects any recipes newly created
    @Override
    protected void onResume() {
        super.onResume();
        sortbyDate(null);
    }
}

