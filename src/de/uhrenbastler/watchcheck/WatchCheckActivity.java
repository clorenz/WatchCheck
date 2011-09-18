package de.uhrenbastler.watchcheck;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;
import de.uhrenbastler.watchcheck.db.WatchCheckDBHelper;
import de.uhrenbastler.watchcheck.ntp.NtpMessage;

/**
 * In dieser Activity wird der Stand der Uhr ermittelt
 * @author clorenz
 * @created on 10.09.2011
 */
public class WatchCheckActivity extends Activity {
    
    private Button checkButton;
    private TimePicker watchtimePicker;
    private double ntpDelta=0;
    private double deviation=0;
    private int selectedWatchId = -1;
    private boolean modeNtp=false;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
  
        setContentView(R.layout.watchcheck);
        
        watchtimePicker = (TimePicker) findViewById(R.id.TimePicker1);
        watchtimePicker.setIs24HourView(true);
                
        try {
            ntpDelta = getNtpDelta();
            
            DecimalFormat df = new DecimalFormat("#.##");
            String deviationString = df.format(ntpDelta);
            if ( ntpDelta>0)
            	deviationString = "+"+deviationString;
            getParent().setTitle(getResources().getString(R.string.app_name) + " [NTP; deviation="+deviationString+"sec]");
            
            modeNtp=true;
            
        } catch ( Exception e) {
            Log.e("WatchCheck", e.getMessage());
        }
        
        checkButton = (Button) findViewById(R.id.buttonCheckTime);
        this.checkButton.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                GregorianCalendar referenceTime = new GregorianCalendar();			// NTP-Zeit
                
                GregorianCalendar localTime = new GregorianCalendar();
                localTime.setTimeInMillis(referenceTime.getTimeInMillis());			// Handy-Zeit
                
                
                referenceTime.add(Calendar.MILLISECOND, (int)(1000 * ntpDelta));
               
                Integer minute = watchtimePicker.getCurrentMinute();
                Integer hour = watchtimePicker.getCurrentHour();
                
                GregorianCalendar watchTime = new GregorianCalendar();
                watchTime.set(Calendar.HOUR_OF_DAY, hour);
                watchTime.set(Calendar.MINUTE, minute);
                watchTime.set(Calendar.SECOND,0);
                watchTime.set(Calendar.MILLISECOND,0);
                
                // Precision: 1/10 sec
                deviation = (float)(watchTime.getTimeInMillis() - referenceTime.getTimeInMillis()) / 1000;
                
                Intent logIntent = new Intent(WatchCheckActivity.this, LogActivity.class);
                logIntent.putExtra(LogActivity.ATTR_DEVIATION, deviation);
                logIntent.putExtra(LogActivity.ATTR_WATCH_ID, selectedWatchId);
                logIntent.putExtra(LogActivity.ATTR_MODE_NTP, modeNtp);
                logIntent.putExtra(LogActivity.ATTR_LOCAL_TIME, localTime);		// Handy-Zeit
                logIntent.putExtra(LogActivity.ATTR_NTP_TIME, modeNtp?referenceTime:null);
                
                startActivity(logIntent);
            }
        });
    }
    
    
    @Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		
		selectedWatchId = preferences.getInt(MainActivity.PREFERENCE_CURRENT_WATCH, -1);
        
        TextView watchToCheck = (TextView) findViewById(R.id.watchModel);
        
        watchToCheck.setText(WatchCheckDBHelper.getWatchFromDatabase(selectedWatchId, getContentResolver()).getAsTitleString());
        
        GregorianCalendar now = new GregorianCalendar();
        now.add(Calendar.MINUTE, 1);
        
        watchtimePicker.setCurrentHour(now.get(Calendar.HOUR_OF_DAY));
        watchtimePicker.setCurrentMinute(now.get(Calendar.MINUTE));
    }

		

    protected double getNtpDelta() throws IOException {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if ( networkInfo!=null && networkInfo.isConnected()) {       
            DatagramSocket socket = new DatagramSocket();
            socket.setSoTimeout(2000);
            InetAddress address = InetAddress.getByName("europe.pool.ntp.org");
            byte[] buf = new NtpMessage().toByteArray();
            DatagramPacket packet =
                new DatagramPacket(buf, buf.length, address, 123);
            
            // Set the transmit timestamp *just* before sending the packet
            // ToDo: Does this actually improve performance or not?
            NtpMessage.encodeTimestamp(packet.getData(), 40,
                (System.currentTimeMillis()/1000.0) + 2208988800.0);
            
            socket.send(packet);
            
            
            // Get response
            Log.i("WatchCheck","NTP request sent, waiting for response...\n");
            packet = new DatagramPacket(buf, buf.length);
            socket.receive(packet);
            
            // Immediately record the incoming timestamp
            double destinationTimestamp =
                (System.currentTimeMillis()/1000.0) + 2208988800.0;
            
            
            // Process response
            NtpMessage msg = new NtpMessage(packet.getData());
            
            // Corrected, according to RFC2030 errata
            double roundTripDelay = (destinationTimestamp-msg.originateTimestamp) -
                (msg.transmitTimestamp-msg.receiveTimestamp);
                
            double localClockOffset =
                ((msg.receiveTimestamp - msg.originateTimestamp) +
                (msg.transmitTimestamp - destinationTimestamp)) / 2;
            
            
            // Display response
            Log.i("WatchCheck",msg.toString());
            
            Log.i("WatchCheck","Dest. timestamp:     " +
                NtpMessage.timestampToString(destinationTimestamp));
            
            Log.i("WatchCheck","Round-trip delay: " +
                new DecimalFormat("0.00").format(roundTripDelay*1000) + " ms");
            
            Log.i("WatchCheck","Local clock offset: " +
                new DecimalFormat("0.00").format(localClockOffset*1000) + " ms");
            
            socket.close();
    
            return localClockOffset;
            
        } else {
            return 0;
        }
    }
}