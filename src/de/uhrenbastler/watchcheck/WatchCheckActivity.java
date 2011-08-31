package de.uhrenbastler.watchcheck;

import java.util.Calendar;
import java.util.GregorianCalendar;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;

public class WatchCheckActivity extends Activity {
    
    private Button checkButton;
    private TimePicker watchtimePicker;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.main);
        
        watchtimePicker = (TimePicker) findViewById(R.id.TimePicker1);
        watchtimePicker.setIs24HourView(true);
        
        this.checkButton = (Button) findViewById(R.id.buttonCheckTime);
        this.checkButton.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                GregorianCalendar referenceTime = new GregorianCalendar();
                
                Integer minute = watchtimePicker.getCurrentMinute();
                Integer hour = watchtimePicker.getCurrentHour();
                
                GregorianCalendar watchTime = new GregorianCalendar();
                watchTime.set(Calendar.HOUR_OF_DAY, hour);
                watchTime.set(Calendar.MINUTE, minute);
                watchTime.set(Calendar.SECOND,0);
                watchTime.set(Calendar.MILLISECOND,0);
                
                int deltaSeconds = (int)(watchTime.getTimeInMillis() - referenceTime.getTimeInMillis()) / 1000;
                
                TextView deltaTime = (TextView) findViewById(R.id.deltaTime);
                deltaTime.setText("Delta: "+(deltaSeconds>0?"+":deltaSeconds<0?"":"+-")+deltaSeconds+" sec");
                
            }
        });
        
    }
}