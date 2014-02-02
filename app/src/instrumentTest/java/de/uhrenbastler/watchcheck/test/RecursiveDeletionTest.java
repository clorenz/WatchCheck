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
package de.uhrenbastler.watchcheck.test;

import android.content.ContentValues;
import android.net.Uri;
import android.test.ActivityInstrumentationTestCase2;
import de.uhrenbastler.watchcheck.SelectWatchActivity;
import de.uhrenbastler.watchcheck.data.Watch.Watches;

// TODO: Add watch, add log, delete watch, check, that log is deleted in database!
public class RecursiveDeletionTest extends ActivityInstrumentationTestCase2<SelectWatchActivity> {

	SelectWatchActivity mActivity;
	
	public RecursiveDeletionTest(String pkg,
			Class<SelectWatchActivity> activityClass) {
		super("de.uhrenbastler.watchcheck", SelectWatchActivity.class);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see android.test.ActivityInstrumentationTestCase2#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		// TODO Auto-generated method stub
		super.setUp();
		mActivity = this.getActivity();
	}
	
	
	public void testDeletionDeletesInDatabase() {
		Uri foo = insertWatchIntoDatabase();
		System.out.println("foo="+foo);
	}
	
	
	// -------------------------------------
	/**
	 * Inserts a new watch into the database - no GUI involved!
	 * @return
	 */
	private Uri insertWatchIntoDatabase() {
		ContentValues values = new ContentValues();
		values.put(Watches.NAME, "TestWatch");
		values.put(Watches.SERIAL, "123");
		values.put(Watches.COMMENT, "Only for testing");
		
		Uri result = mActivity.getContentResolver().insert(Watches.CONTENT_URI, values);
		
		return result;
	}
	
	

}
