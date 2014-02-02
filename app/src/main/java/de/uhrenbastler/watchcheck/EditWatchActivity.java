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

import android.app.Activity;
import android.content.ContentValues;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import de.uhrenbastler.watchcheck.data.Watch;
import de.uhrenbastler.watchcheck.data.Watch.Watches;
import de.uhrenbastler.watchcheck.db.WatchCheckDBHelper;

public class EditWatchActivity extends Activity {

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.editwatch);
		
		Bundle extras = getIntent().getExtras();
		if (extras == null) {
			Log.w("WatchCheck", "EditWatchActivity called without data!!");
			return;
		}
		
		final int watchId = extras.getInt(Watches._ID);
		
		// Retrieve watch and fill fields
		Watch watch = WatchCheckDBHelper.getWatchFromDatabase(watchId, this.getContentResolver());
		Log.d("WatchCheck", "Editing watch with id="+watchId+"="+watch);
		
		final EditText model = (EditText) findViewById(R.id.editTextModel);
		model.setText(watch.getName());
		
		final EditText serial = (EditText) findViewById(R.id.editTextSerial);
		serial.setText(watch.getSerial());
		
		final EditText remarks = (EditText) findViewById(R.id.editTextRemarks);
		remarks.setText(watch.getComment());
		
		Button btnOk = (Button) findViewById(R.id.buttonOk);
		Button btnCancel = (Button) findViewById(R.id.buttonCancel);
		
		btnCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                EditWatchActivity.this.finish();
            }
        });
		
		btnOk.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// Daten in die DB liegen
				ContentValues values = new ContentValues();
				values.put(Watches.NAME, model.getEditableText().toString());
				values.put(Watches.SERIAL, serial.getEditableText().toString());
				values.put(Watches.COMMENT, remarks.getEditableText().toString());
				
				int updatedRecords = getContentResolver().update(Watches.CONTENT_URI, values, Watches._ID+"="+watchId, null);
				
				Log.d("WatchCheck","Updated "+updatedRecords+" watches with id="+watchId+" and values="+values);

				EditWatchActivity.this.finish();			
			}
		});
	}
	
	

}

