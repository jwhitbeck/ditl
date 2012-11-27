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
package ditl.graphs.cli;

import java.util.EnumSet;
import java.util.Iterator;

import org.apache.commons.cli.*;

public enum ExternalFormat {
	
	NS2("ns2"),
	ONE("one"),
	CRAWDAD("crawdad");
		
	private final String _label;
	
	private ExternalFormat(String label){
		_label = label;
	}
	
	public final static class CLIParser {
		
		private final static String fmtOption = "format";
		private final ExternalFormat default_fmt;
		private final EnumSet<ExternalFormat> fmt_options;
		
		public CLIParser(ExternalFormat defaultFormat, ExternalFormat...otherFormats){
			fmt_options = EnumSet.of(defaultFormat, otherFormats);
			default_fmt = defaultFormat;
		}
		
		public void setOptions(Options options){
			StringBuffer buffer = new StringBuffer();
			Iterator<ExternalFormat> i = fmt_options.iterator();
			while ( i.hasNext() ){
				buffer.append(i.next()._label);
				if ( i.hasNext() ){
					buffer.append(" | ");
				}
			}
			if ( default_fmt != null )
				buffer.append(" (default: "+default_fmt._label+")");
			options.addOption(null, fmtOption, true, buffer.toString());
		}
		
		public ExternalFormat parse(CommandLine cli) throws ParseException{
			if ( cli.hasOption(fmtOption) ){
				String v = cli.getOptionValue(fmtOption);
				for ( ExternalFormat fmt : ExternalFormat.values()){
					if ( fmt._label.equals(v.toLowerCase()))
						return fmt;
				}
				throw new ParseException("Invalid format '+v+'");
			}
			return default_fmt;
		}
	}
}