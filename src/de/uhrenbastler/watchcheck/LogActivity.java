package de.uhrenbastler.watchcheck;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import de.uhrenbastler.watchcheck.data.Watch.Watches;


public class LogActivity extends Activity {
	
	List<String> watches = new ArrayList<String>();
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
  
        setContentView(R.layout.log);
        
        // Get all watches of from the content provider
        getAllWatchesFromDatabase();
        
        // Cancel button just closes the activity
        Button buttonCancel = (Button) findViewById(R.id.buttonLogCancel);
        buttonCancel.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                LogActivity.this.finish();
                
            }
        });
        
        ArrayAdapter<CharSequence> spinnerWatchesAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, watches);
        spinnerWatchesAdapter.add("New watch");
        Spinner spinnerWatches = (Spinner) findViewById( R.id.spinnerWatches );
        spinnerWatches.setAdapter( spinnerWatchesAdapter );
        spinnerWatches.setOnItemSelectedListener(new OnItemSelectedListener() {
        	 
			@Override
			// TODO: Hier wird auch "Selected watch new watch" selektiert!!"
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int position, long id) {
				if ( position > watches.size())
					Log.d("WatchCheck","Insert new watch");
				else
					Log.d("WatchCheck","Selected watch "+watches.get(position));
				
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
        });

        
        
        // OK button actually logs, displays an "OK" dialog, and after the dialog is acknowledged, closes
        // the activity
        Button buttonOk = (Button) findViewById(R.id.buttonLogOK);
        buttonOk.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                LogActivity.this.finish();       
            }
        });
        
    }

    
    // -> http://www.androidcompetencycenter.com/2009/01/basics-of-android-part-iv-android-content-providers/
    private void getAllWatchesFromDatabase() {
        Uri uriWatches = Watches.CONTENT_URI;
        String[] columns = new String[] { Watches.WATCH_ID, Watches.NAME, Watches.SERIAL };
        
        Cursor cur = managedQuery(uriWatches, columns, null, null, Watches.NAME);
        
        if ( cur.moveToFirst()) {
            Long id = null;
            String name = null;
            String serial = null;
            do {
                id = cur.getLong(cur.getColumnIndex(Watches.WATCH_ID));
                name = cur.getString(cur.getColumnIndex(Watches.NAME));
                serial = cur.getString(cur.getColumnIndex(Watches.SERIAL));
                
                Log.d("WatchCheck", "Found watch with id="+id+", name="+name+", serial="+serial);
                
                watches.add(name +(serial!=null?"("+serial+")":""));
            } while ( cur.moveToNext());
        }
        
        watches.add(getResources().getString(R.string.addWatch));
    }

}
