/*******************************************************************************
 * This file is part of DITL.                                                  *
 *                                                                             *
 * Copyright (C) 2011 John Whitbeck <john@whitbeck.fr>                         *
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
package ditl.graphs.cli;

import org.apache.commons.cli.*;

public final class ExternalFormat {
	
	public final static int NS2 = 0;
	public final static int ONE = 1;
	public final static int CRAWDAD = 2;
	
	public final static String[] labels = {
		"ns2",
		"one",
		"crawdad"
	};
	
	private Integer[] _fmts;
	private Integer cur_fmt;
	
	private final static String fmtOption = "format";
	
	public ExternalFormat(Integer...fmts){
		_fmts = fmts;
		cur_fmt = _fmts[0];
	}
	
	public void setOptions(Options options){
		StringBuffer buffer = new StringBuffer();
		for ( int i=0; i<_fmts.length; ++i){
			buffer.append(labels[_fmts[i]]);
			if ( i<_fmts.length-1)
				buffer.append(" | ");
		}
		if ( _fmts.length >= 2 )
			buffer.append(" (default: "+labels[cur_fmt]+")");
		options.addOption(null, fmtOption, true, buffer.toString());
	}
	
	public void parse(CommandLine cli){
		if ( cli.hasOption(fmtOption) ){
			String v = cli.getOptionValue(fmtOption);
			for ( int i=0; i<labels.length; ++i){
				if ( labels[i].equals(v.toLowerCase()))
					cur_fmt = i;
			}
		}
	}
	
	public boolean is(Integer fmt){
		return fmt.equals(cur_fmt);
	}
}