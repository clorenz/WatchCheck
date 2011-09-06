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
        
        // Evtl. einen SimpleCursorAdapter (gemäß http://android-developers.de/tutorials-faqs/daten-in-eine-spinner-laden-402.html
        // verwenden??
        
        Uri uriWatches = Watches.CONTENT_URI;
        String[] columns = new String[] { Watches.WATCH_ID, Watches.NAME, Watches.SERIAL };
        
        Cursor cur = managedQuery(uriWatches, columns, null, null, Watches.NAME);
        SimpleCursorAdapter spinnerWatchesAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item,
        		cur, new String[] { Watches.NAME, Watches.SERIAL  }, new int[] {android.R.id.text1}); 

        
        //ArrayAdapter<CharSequence> spinnerWatchesAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, watches);
        Spinner spinnerWatches = (Spinner) findViewById( R.id.spinnerWatches );
        spinnerWatches.setAdapter( spinnerWatchesAdapter );

        
        // OK button actually logs, displays an "OK" dialog, and after the dialog is acknowledged, closes
        // the activity
        
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
                
                watches.add(name);
            } while ( cur.moveToNext());
        }
        
        watches.add(getResources().getString(R.string.addWatch));
    }

}
