/* ------------------------------------------------------------------------- *
$Source:$
$Author:$
$Date: $
$Revision: $

(C) 2006 Christoph Lorenz, <mail@christophlorenz.de>
All rights reserved.

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

** ------------------------------------------------------------------------- */
package de.uhrenbastler.watchcheck;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import de.uhrenbastler.watchcheck.data.Log;
import de.uhrenbastler.watchcheck.data.Log.Logs;
import de.uhrenbastler.watchcheck.data.Result;
import de.uhrenbastler.watchcheck.data.WatchResult;
import de.uhrenbastler.watchcheck.db.WatchCheckDBHelper;
import de.uhrenbastler.watchcheck.ui.MainActivity;


public class ResultsActivity extends Activity {
	
	ListView listView;
	int currentPeriod=0;
	SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy\nHH:mm:ss");
	
	
	/**
	 * Since onResume() is called after onCreate, we must here
	 * initialize the listAdapter and populate it with data!
	 */
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
		populateListAdapter();
		
		writeHeader();
		writeAverage();
	}
	
	
	private void writeHeader() {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		
		int selectedWatchId = preferences.getInt(MainActivity.PREFERENCE_CURRENT_WATCH, -1);
		
		TextView resultWatchNameView = (TextView) findViewById(R.id.watchModel);
		resultWatchNameView.setText(WatchCheckDBHelper.getWatchFromDatabase(selectedWatchId, getContentResolver()).getAsTitleString());
		
		TextView headerView = (TextView) findViewById(R.id.textViewResultsHeader);
		String header = String.format(getResources().getString(R.string.resultHeader),
				(currentPeriod+1), 
				WatchResult.getInstance().getNumberOfResultPeriods(),
				WatchResult.getInstance().getResultPeriod(currentPeriod).getFormattedStartDate(),
				WatchResult.getInstance().getResultPeriod(currentPeriod).getFormattedEndDate()
		);
		headerView.setText(header);
		
		((Button) findViewById(R.id.resultPrevious)).setEnabled(currentPeriod>0);
		((Button) findViewById(R.id.resultPrevious)).setBackgroundResource(currentPeriod==0?0:android.R.drawable.ic_media_rew);
		((Button) findViewById(R.id.resultPrevious)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if ( currentPeriod>0 ) {
					currentPeriod--;
					onResume();
				}
			}
		});
		
		((Button) findViewById(R.id.resultNext)).setEnabled(currentPeriod<WatchResult.getInstance().getNumberOfResultPeriods()-1);
		((Button) findViewById(R.id.resultNext)).setBackgroundResource(currentPeriod<WatchResult.getInstance().getNumberOfResultPeriods()-1?android.R.drawable.ic_media_ff:0);
		((Button) findViewById(R.id.resultNext)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if ( currentPeriod<WatchResult.getInstance().getNumberOfResultPeriods() ) {
					currentPeriod++;
					onResume();
				}
			}
		});
		
		android.util.Log.d("WatchCheck","RESULT="+WatchResult.getInstance());
	}


	private void writeAverage() {
		TextView average = (TextView) findViewById(R.id.textViewAverageResult);
		
		DecimalFormat resultFormat = new DecimalFormat("+#.#;-#.#");
		
		double averageDeviation = WatchResult.getInstance().getResultPeriod(currentPeriod).getAverageDailyDeviation();
		average.setText(String.format(getResources().getString(R.string.averageResult),
				averageDeviation!=0.0d?resultFormat.format(averageDeviation):"+-0"));
		
	}


	private void populateListAdapter() {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		
		int selectedWatchId = preferences.getInt(MainActivity.PREFERENCE_CURRENT_WATCH, -1);
        
		getLogsForCurrentWatchFromDatabase(selectedWatchId);
		
		if ( currentPeriod >= WatchResult.getInstance().getNumberOfResultPeriods() )
			currentPeriod = WatchResult.getInstance().getNumberOfResultPeriods()-1;
		
		final ListAdapter listAdapter = new ResultsAdapter(this, R.layout.result_row, 
				WatchResult.getInstance().getResultPeriod(currentPeriod).getResults());
		
		listView.setAdapter(listAdapter);
		
		// Jump to the last entry
		listView.post(new Runnable() {
	        @Override
	        public void run() {
	        	listView.setSelection(listAdapter.getCount() - 1);
	        }
	    });

	}
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.result);

		listView = (ListView) findViewById(R.id.resultListView);
		
		currentPeriod=99999;
	}
	
	
	
	private void getLogsForCurrentWatchFromDatabase(int currentWatchId) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
		
		Uri uriLogs = Logs.CONTENT_URI;
		String[] columns = new String[] { Logs._ID, Logs.WATCH_ID, Logs.MODUS, Logs.LOCAL_TIMESTAMP,
				Logs.NTP_DIFF, Logs.DEVIATION, Logs.FLAG_RESET, Logs.POSITION, Logs.TEMPERATURE,
				Logs.COMMENT};
		
		Cursor cur=null;
		
		GregorianCalendar previousTimestamp=null;
		double previousDeviation=0;
		Double previousNtpDiff=null;
		
		WatchResult.getInstance().clear();
		
		try {
			cur = managedQuery(uriLogs, columns, Logs.WATCH_ID+"="+currentWatchId, null, Logs.LOCAL_TIMESTAMP+" asc");

			PeriodResult periodResult=null;
			
			if (cur.moveToFirst()) {
				do {
					Log log = new Log();
					log.setId(cur.getLong(cur.getColumnIndex(Logs._ID)));
					log.setWatchId(currentWatchId);
					log.setModus(cur.getString(cur.getColumnIndex(Logs.MODUS)));
					log.setNtpDiff(cur.getDouble(cur.getColumnIndex(Logs.NTP_DIFF)));
					log.setDeviation(cur.getFloat(cur.getColumnIndex(Logs.DEVIATION)));
					log.setFlagReset("1".equals(cur.getString(cur.getColumnIndex(Logs.FLAG_RESET))) ||
							cur.isFirst());
					log.setPosition(cur.getString(cur.getColumnIndex(Logs.POSITION)));
					log.setTemperature(cur.getInt(cur.getColumnIndex(Logs.TEMPERATURE)));
					log.setComment(cur.getString(cur.getColumnIndex(Logs.COMMENT)));
					
					try {
						GregorianCalendar localTimestamp = new GregorianCalendar();
						localTimestamp.setTime(dateFormat.parse(cur.getString(cur.getColumnIndex(Logs.LOCAL_TIMESTAMP))));
						log.setLocalTimestamp(localTimestamp);
						
						if ( log.isFlagReset()) {
							//if ( !cur.isFirst())
							//	periodResults.add(periodResult);

							previousTimestamp = null;
							previousDeviation = 0;
							previousNtpDiff=null;
							periodResult = new PeriodResult();
							periodResult.setReferenceStartTime(localTimestamp);
							periodResult.setWatchStartOffset(log.getDeviation());
						}
						
						if ( previousTimestamp!=null) {
							// verbrauchte Zeit berechnen
							long timeDiffInMillis = localTimestamp.getTimeInMillis() - previousTimestamp.getTimeInMillis();
							// Differenz der Stände berechnen
							double deviation = log.getDeviation() - previousDeviation;
							
							if ( previousNtpDiff!=null && log.isNtpMode() )
								log.setNtpCorrectionFactor( (86400000d *(log.getNtpDiff()-previousNtpDiff)) / timeDiffInMillis) ;
							
							// auf 24h hochrechnen
							log.setDailyDeviation( (86400000d * deviation) / timeDiffInMillis );
							periodResult.setReferenceEndTime(localTimestamp);
							periodResult.setWatchEndOffset(log.getDeviation());
						}
						
						previousTimestamp = localTimestamp;
						previousDeviation = log.getDeviation();
						
						if ( log.isNtpMode())
							previousNtpDiff = log.getNtpDiff();
						else
							previousNtpDiff = null;
						
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						android.util.Log.e("WatchCheck",e.getMessage());
					}

					WatchResult.getInstance().addLog(log);
				} while (cur.moveToNext());
				
				//if ( !cur.isFirst())
				//	periodResults.add(periodResult);
			}

		} finally {
			if ( cur !=null )
				cur.close();
		}
		
		//android.util.Log.d("WatchCheck", periodResults.toString());
	}
	
	
	private class ResultsAdapter extends ArrayAdapter<Result> {
		
		private List<Result> results;

		public ResultsAdapter(Context context, int textViewResourceId,
				List<Result> results) {
			super(context, textViewResourceId, results);
			this.results = results;
		}
		
		@Override
        public View getView(int position, View convertView, ViewGroup parent) {
                View v = convertView;
                if (v == null) {
                    LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    v = vi.inflate(R.layout.result_row, null);
                }
                Result l = results.get(position);
                
                if (l != null) {
                	
                		/**
                		 * 
                		 * Vorgehen für die Zwischenergebnisse
                		TableRow currentRow = (TableRow) v.findViewById(R.id.tableRowResult);
                		currentRow.removeAllViews();
                		currentRow.addView(bli bla blubb)
                		**/
                	
                		TextView timestampView = (TextView) v.findViewById(R.id.textViewResultTimestamp);
                		
                		timestampView.setText(sdf.format(l.getTimestamp())
                			+((l.getNtpDeviation()!=0)?"\n["+
                					new DecimalFormat("+0.00s/d;-0.00s/d").format(l.getNtpDeviation())+"]":"")	
                			);
                		timestampView.setGravity(Gravity.CENTER);
                		timestampView.setTypeface(l.isNtpPrecision()?Typeface.DEFAULT_BOLD:Typeface.DEFAULT);
                	
                		TextView offsetView= (TextView) v.findViewById(R.id.textViewResultOffset);
                		offsetView.setText(new DecimalFormat("+0.0s;-0.0s").format(l.getOffset()));
                		offsetView.setGravity(Gravity.LEFT);
                		offsetView.setTypeface( (l.isNtpPrecision())?Typeface.DEFAULT_BOLD:Typeface.DEFAULT);
                		
                		
	                	TextView dailyDeviationView= (TextView) v.findViewById(R.id.textViewResultDailyDeviation);
	                	if ( !l.isPeriodStart()) {
	                        dailyDeviationView.setText(new DecimalFormat("+0.0s/d;-0.0s/d").format(l.getDailyDeviation()));
	                	} else {
	                		dailyDeviationView.setText("-");
	                	}
	                    dailyDeviationView.setGravity(Gravity.LEFT);   
                }
                
                return v;
        }
		
	}

}
