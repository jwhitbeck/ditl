/*******************************************************************************
 * This file is part of DITL.                                                  *
 *                                                                             *
 * Copyright (C) 2011-2012 John Whitbeck <john@whitbeck.fr>                    *
 *                                                                             *
 * DITL is free software: you can redistribute it and/or modify                *
 * it under the terms of the GNU General Public License as published by        *
 * the Free Software Foundation, either version 3 of the License, or           *
 * (at your option) any later version.                                         *
 *                                                                             *
 * DITL is distributed in the hope that it will be useful,                     *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of              *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the               *
 * GNU General Public License for more details.                                *
 *                                                                             *
 * You should have received a copy of the GNU General Public License           *
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.       *
 *******************************************************************************/
package ditl;

public final class Units {

	public static Long getTicsPerSecond(String timeUnit) {
		String unit = timeUnit.toLowerCase();
		if ( unit.equals("s") ){
			return 1L;
		} else if ( unit.equals("ms") ){
			return 1000L;
		} else if ( unit.equals("us") ){
			return 1000000L;
		} else if ( unit.equals("ns") ){
			return 1000000000L;
		}
		return null;
	}
	
	public static String toTimeUnit(long tps) {
		if ( tps == 1L ){
			return "s";
		} else if ( tps == 1000L ) {
			return "ms";
		} else if ( tps == 1000000L ){
			return "us";
		} else if ( tps == 1000000000L ){
			return "ns";
		}
		return null;
	}
}
