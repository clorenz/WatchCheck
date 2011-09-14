package de.uhrenbastler.watchcheck;

import java.text.DecimalFormat;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;


public class LogActivity extends Activity {
	
    protected static final String ATTR_DEVIATION = "attrDeviation";

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Bundle extras = getIntent().getExtras();
		if (extras == null) {
			Log.w("WatchCheck", "LogActivity called without data!!");
			return;
		}
		
		double deviation = extras.getDouble(ATTR_DEVIATION);
		Log.d("WatchCheck","deviation="+deviation);
        
        setContentView(R.layout.log); 
        
        TextView textDeviation = (TextView) findViewById(R.id.textViewDeviationValue);
        DecimalFormat df = new DecimalFormat("#.#");
        
        textDeviation.setText( (deviation>0?"+":deviation<0?"-":"+-") + df.format(Math.abs(deviation)) +" sec." );
        
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
        
        
        // OK button actually logs, displays an "OK" dialog, and after the dialog is acknowledged, closes
        // the activity
        
        
        // TODO: Save-Dioalog
        Button buttonSave = (Button) findViewById(R.id.buttonSave);
        buttonSave.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                LogActivity.this.finish();       
            }
        });
        
    }
}
