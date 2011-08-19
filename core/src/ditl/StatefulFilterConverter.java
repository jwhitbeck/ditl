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
package ditl;

import java.io.IOException;
import java.util.*;

public class StatefulFilterConverter<E,S> implements Converter {

	private StatefulWriter<E,S> _to;
	private StatefulReader<E,S> _from;
	private Matcher<E> event_matcher;
	private Matcher<S> state_matcher;
	
	public StatefulFilterConverter( StatefulWriter<E,S> to, StatefulReader<E,S> from, Matcher<E> eventMatcher, Matcher<S> stateMatcher ){
		_to = to;
		_from = from;
		event_matcher = eventMatcher;
		state_matcher = stateMatcher;
	}
	
	@Override
	public void close() throws IOException {
		_to.close();
	}

	@Override
	public void run() throws IOException {
		Trace trace = _from.trace();
		_from.seek(trace.minTime());
		
		Set<S> initState = new HashSet<S>();
		for ( S state : _from.referenceState() )
			if ( state_matcher.matches(state) )
				initState.add(state);
		_to.setInitState(trace.minTime(), initState);
		
		while ( _from.hasNext() ){
			List<E> events = _from.next();
			for ( E item : events )
				if ( event_matcher.matches(item) )
					_to.append(_from.time(), item);
		}
		_to.setProperty(Trace.ticsPerSecondKey, _from.trace().ticsPerSecond());
		_to.setProperty(Trace.minTimeKey, trace.minTime());
		_to.setProperty(Trace.maxTimeKey, trace.maxTime());
	}
}
