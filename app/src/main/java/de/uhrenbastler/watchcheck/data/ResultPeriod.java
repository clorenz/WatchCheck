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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This class represents a full measuring period
 * @author clorenz
 * @created on 08.10.2011
 */
public class ResultPeriod {
	
	SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
	
	List<Result> results;
	Date timestampStart;
	Date timestampEnd;
	double averageDailyDeviation;
	
	public ResultPeriod() {
		results = new ArrayList<Result>();
	}
	
	public void addResult(Result result) {
		if ( timestampStart==null || result.getTimestamp().getTime() < timestampStart.getTime())
			timestampStart = result.getTimestamp();
		if ( timestampEnd==null || result.getTimestamp().getTime() > timestampEnd.getTime())
			timestampEnd = result.getTimestamp();
		
		results.add(result);
	}
	
	public double getAverageDailyDeviation() {
		double sum=0d;
		
		for ( Result result : results)
			sum += result.getDailyDeviation();
		
		if ( results.size()>0 )
			return ( sum / ((double)results.size()) );
		else
			return 0;
	}

	public List<Result> getResults() {
		return results;
	}

	public void addLog(Log log) {
		Date timestamp = log.getLocalTimestamp().getTime();
		boolean ntpPrecision = log.isNtpMode();
		double ntpDeviation = log.getNtpCorrectionFactor();			// s/d
		double offset = log.getDeviation() - log.getNtpCorrectionFactor();
		double dailyDeviation = log.getDailyDeviation();			// s/d
		
		Result result = new Result(results.isEmpty(),timestamp, ntpPrecision, ntpDeviation, offset, dailyDeviation);
		addResult(result);
	}

	public String getFormattedStartDate() {
		return sdf.format(timestampStart);
	}

	public String getFormattedEndDate() {
		return sdf.format(timestampEnd);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ResultPeriod [results=" + results
				+ ",\n\ttimestampStart=" + timestampStart + ",\n\ttimestampEnd="
				+ timestampEnd + ",\n\taverageDailyDeviation="
				+ averageDailyDeviation + ",\n\tgetAverageDailyDeviation()="
				+ getAverageDailyDeviation() + ",\n\tgetFormattedStartDate()="
				+ getFormattedStartDate() + ",\n\tgetFormattedEndDate()="
				+ getFormattedEndDate() + "]";
	}
	
	

}
