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
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import de.uhrenbastler.watchcheck.data.Watch.Watches;
import de.uhrenbastler.watchcheck.ntp.NtpMessage;

public class WatchCheckActivity extends Activity {
    
    private Button checkButton;
    private Button logButton;
    private TimePicker watchtimePicker;
    private double ntpDelta=0;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
  
        setContentView(R.layout.watchcheck);
        
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		
		int selectedWatchId = preferences.getInt(MainActivity.PREFERENCE_CURRENT_WATCH, -1);
        
        TextView watchToCheck = (TextView) findViewById(R.id.watchModel);
        
        watchToCheck.setText(getWatchFromDatabase(selectedWatchId));

        Toast toast = Toast.makeText(WatchCheckActivity.this, "Trying to get NTP time...", Toast.LENGTH_SHORT);
        toast.show();
        
        watchtimePicker = (TimePicker) findViewById(R.id.TimePicker1);
        watchtimePicker.setIs24HourView(true);
        
        TextView modeView = (TextView) findViewById(R.id.Mode);
        
               
        try {
            ntpDelta = getNtpDelta();
            
            String ntpMode = getResources().getString(R.string.modeNtp);
            ntpMode = ntpMode.replaceFirst("\\%s", ""+ntpDelta);
            modeView.setText(ntpMode);
        } catch ( Exception e) {
            Log.e("WatchCheck", e.getMessage());
            modeView.setText(R.string.modeLocal);
        }
        
        checkButton = (Button) findViewById(R.id.buttonCheckTime);
        this.checkButton.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                GregorianCalendar referenceTime = new GregorianCalendar();
                referenceTime.add(Calendar.SECOND, -1 * (int) ntpDelta);
               
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
                
                Button buttonLog = (Button) findViewById(R.id.buttonLog);
                buttonLog.setClickable(true); buttonLog.setEnabled(true);
                
            }
        });
        
        
        logButton = (Button) findViewById(R.id.buttonLog);
        logButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Intent logIntent = new Intent(WatchCheckActivity.this, LogActivity.class);
                startActivity(logIntent);
            }
            
        });
        
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
    
    
    private String getWatchFromDatabase(int id) {
    	
    	Uri selectedWatch = Uri.withAppendedPath(Watches.CONTENT_URI, ""+id);
    	String[] columns = new String[] { Watches.WATCH_ID, Watches.NAME, Watches.SERIAL };
    	Cursor cur = managedQuery(selectedWatch, columns, null, null, null);
    	
		if (cur.moveToFirst()) {
			String name = null;
			String serial = null;
			do {
				name = cur.getString(cur.getColumnIndex(Watches.NAME));
				serial = cur.getString(cur.getColumnIndex(Watches.SERIAL));

				return name+"("+serial+")";
			} while (cur.moveToNext());
		}
		
		return "?";
    }
}