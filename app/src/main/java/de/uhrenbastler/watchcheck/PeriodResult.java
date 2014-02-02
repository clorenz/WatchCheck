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

import java.util.GregorianCalendar;

public class PeriodResult {
	
	GregorianCalendar referenceStartTime;
	GregorianCalendar watchStartTime;
	GregorianCalendar referenceEndTime;
	GregorianCalendar watchEndTime;
	/**
	 * @return the referenceStartTime
	 */
	public GregorianCalendar getReferenceStartTime() {
		return referenceStartTime;
	}
	/**
	 * @param referenceStartTime the referenceStartTime to set
	 */
	public void setReferenceStartTime(GregorianCalendar referenceStartTime) {
		this.referenceStartTime = referenceStartTime;
	}
	/**
	 * @return the watchStartTime
	 */
	public GregorianCalendar getWatchStartTime() {
		return watchStartTime;
	}
	/**
	 * @param watchStartTime the watchStartTime to set
	 */
	public void setWatchStartTime(GregorianCalendar watchStartTime) {
		this.watchStartTime = watchStartTime;
	}
	/**
	 * @return the referenceEndTime
	 */
	public GregorianCalendar getReferenceEndTime() {
		return referenceEndTime;
	}
	/**
	 * @param referenceEndTime the referenceEndTime to set
	 */
	public void setReferenceEndTime(GregorianCalendar referenceEndTime) {
		this.referenceEndTime = referenceEndTime;
	}
	/**
	 * @return the watchEndTime
	 */
	public GregorianCalendar getWatchEndTime() {
		return watchEndTime;
	}
	/**
	 * @param watchEndTime the watchEndTime to set
	 */
	public void setWatchEndTime(GregorianCalendar watchEndTime) {
		this.watchEndTime = watchEndTime;
		
	}
	
	public double getAverageDeviation() {
		double periodInMillis = referenceEndTime.getTimeInMillis() - referenceStartTime.getTimeInMillis();
		double deviationInMillis = watchEndTime.getTimeInMillis() - watchStartTime.getTimeInMillis();
		
		if ( periodInMillis!=0)
			return ( (deviationInMillis * 86400) / periodInMillis ) / 1000;
		else
			return 0;
	}
	
	public void setWatchStartOffset(double deviation) {
		this.watchStartTime = new GregorianCalendar();
		this.watchStartTime.setTimeInMillis(referenceStartTime.getTimeInMillis() + (long)(1000*deviation));		
	}
	
	public void setWatchEndOffset(double deviation) {
		this.watchEndTime = new GregorianCalendar();
		this.watchEndTime.setTimeInMillis(referenceEndTime.getTimeInMillis() + (long)(1000*deviation));	
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "PeriodResult [referenceStartTime=" + referenceStartTime.getTime()
				+ "\n\t, watchStartTime=" + watchStartTime.getTime() + ",\n\t referenceEndTime="
				+ referenceEndTime.getTime() + "\n\t, watchEndTime=" + watchEndTime.getTime() + 
				"\n\t, averageDeviation=" + getAverageDeviation() +
				"]";
	}
	
	

}
