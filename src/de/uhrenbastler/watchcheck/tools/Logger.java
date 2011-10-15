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
package de.uhrenbastler.watchcheck.tools;

import android.util.Log;

/**
 * Wrapper class for consistent logging
 * @author clorenz
 * @created on 14.10.2011
 */
public class Logger {
	
	public static final String TAG="WatchCheck";
	
	public static void debug(String message) {
		Log.d(TAG, message);
	}
	
	public static void info(String message) {
		Log.i(TAG, message);
	}
	
	public static void warn(String message) {
		Log.w(TAG, message);
	}
	
	public static void error(String message, Throwable t) {
		Log.e(TAG, message, t);
	}

}
