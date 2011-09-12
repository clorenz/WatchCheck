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
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import de.uhrenbastler.watchcheck.data.Watch.Watches;


public class LogActivity extends Activity {
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.log); 
        
        Spinner positionSpinner = (Spinner) findViewById(R.id.logSpinnerPosition); 
        ArrayAdapter<?> positionAdapter = ArrayAdapter.createFromResource( this,
        		R.array.positions,android.R.layout.simple_spinner_item); 
        positionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        positionSpinner.setAdapter(positionAdapter); 
        
        Spinner temperatureSpinner = (Spinner) findViewById(R.id.logSpinnerTemperature); 
        ArrayAdapter<?> temperatureAdapter = ArrayAdapter.createFromResource( this,
        		R.array.temperatures,android.R.layout.simple_spinner_item); 
        temperatureAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        temperatureSpinner.setAdapter(temperatureAdapter); 
        
        
        // Cancel button just closes the activity
        Button buttonCancel = (Button) findViewById(R.id.buttonLogCancel);
        buttonCancel.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                LogActivity.this.finish();
                
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
}
