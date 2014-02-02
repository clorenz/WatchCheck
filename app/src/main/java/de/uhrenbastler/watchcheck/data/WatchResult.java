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
package de.uhrenbastler.watchcheck.data;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents all results of one watch. It acts a a singleton.
 * @author clorenz
 * @created on 08.10.2011
 */
public class WatchResult {
		
	List<ResultPeriod> resultPeriods;
	static WatchResult instance = new WatchResult();
	
	private WatchResult() {
		resultPeriods = new ArrayList<ResultPeriod>();
	}
	
	public static WatchResult getInstance() {
		return instance;
	}

	public void clear() {
		resultPeriods.clear();
	}

	public ResultPeriod getResultPeriod(int period) {
		ResultPeriod resultPeriod = resultPeriods.get(period);
		
		return resultPeriod;
	}

	public void addLog(Log log) {
		ResultPeriod resultPeriod;
		
		android.util.Log.d("WatchCheck","LOG="+log);
		
		if ( log.isFlagReset()) { 
			// new period
			resultPeriod = new ResultPeriod();
		} else {
			// use previous one
			resultPeriod = resultPeriods.get(resultPeriods.size()-1);
		}
		
		resultPeriod.addLog(log);
		
		if ( log.isFlagReset()) {
			resultPeriods.add(resultPeriod);
		} else {
			resultPeriods.set(resultPeriods.size()-1, resultPeriod);
		}		
	}

	public int getNumberOfResultPeriods() {
		return resultPeriods.size();
	}
	
	

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "WatchResult [numberOfResultPeriods="+resultPeriods.size()+", resultPeriods=" + resultPeriods + "]";
	}
	
	

}
