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

	private StatefulTrace<E,S> _to;
	private StatefulTrace<E,S> _from;
	private Matcher<E> event_matcher;
	private Matcher<S> state_matcher;
	
	public StatefulFilterConverter( StatefulTrace<E,S> to, StatefulTrace<E,S> from, Matcher<E> eventMatcher, Matcher<S> stateMatcher ){
		_to = to;
		_from = from;
		event_matcher = eventMatcher;
		state_matcher = stateMatcher;
	}
	
	@Override
	public void convert() throws IOException {
		StatefulReader<E,S> reader = _from.getReader();
		StatefulWriter<E,S> writer = _to.getWriter(_from.snapshotInterval());
		
		reader.seek(_from.minTime());
		Set<S> initState = new HashSet<S>();
		for ( S state : reader.referenceState() )
			if ( state_matcher.matches(state) )
				initState.add(state);
		writer.setInitState(_from.minTime(), initState);
		
		while ( reader.hasNext() ){
			List<E> events = reader.next();
			for ( E item : events )
				if ( event_matcher.matches(item) )
					writer.append(reader.time(), item);
		}
		writer.setPropertiesFromTrace(_from);
		writer.close();
		reader.close();
	}
}
