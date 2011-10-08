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

import java.util.Date;

/**
 * This class represents a single result
 * @author clorenz
 * @created on 08.10.2011
 */
public class Result {
	
	Date timestamp;
	boolean ntpPrecision=false;
	double ntpDeviation;
	double offset;
	double dailyDeviation;
	boolean periodStart=false;
	
	
	
	public Result(boolean periodStart, Date timestamp, boolean ntpPrecision, double ntpDeviation,
			double offset, double dailyDeviation) {
		super();
		this.periodStart = periodStart;
		this.timestamp = timestamp;
		this.ntpPrecision = ntpPrecision;
		this.ntpDeviation = ntpDeviation;
		this.offset = offset;
		this.dailyDeviation = dailyDeviation;
	}
	/**
	 * @return the timestamp
	 */
	public Date getTimestamp() {
		return timestamp;
	}
	/**
	 * @return the ntpPrecision
	 */
	public boolean isNtpPrecision() {
		return ntpPrecision;
	}
	/**
	 * @return the ntpDeviation
	 */
	public double getNtpDeviation() {
		return ntpDeviation;
	}
	/**
	 * @return the offset
	 */
	public double getOffset() {
		return offset;
	}
	/**
	 * @return the dailyDeviation
	 */
	public double getDailyDeviation() {
		return dailyDeviation;
	}
	
	
	public boolean isPeriodStart() {
		return periodStart;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "\n\t\tResult [timestamp=" + timestamp + ", ntpPrecision="
				+ ntpPrecision + ", ntpDeviation=" + ntpDeviation + ", offset="
				+ offset + ", dailyDeviation=" + dailyDeviation
				+ ", periodStart=" + periodStart + "]";
	}
	
	

}
