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

import java.io.*;
import ditl.plausible.*;
import ditl.plausible.constraints.*;
import ditl.plausible.forces.*;
import ditl.*;
import ditl.graphs.*;

public class InferInfocom06 {
    
    static WritableStore store;
    static GraphStore gStore;
    static Trace links;
    static Trace windowed_links;
    static Trace presence;
    static Trace movement;
    static Trace static_movement;

    public static void main(String[] args) throws IOException {
	store = WritableStore.open(new File(args[0]));
	gStore = new GraphStore(store);

	links = store.getTrace(GraphStore.defaultLinksName);
	presence = store.getTrace(GraphStore.defaultPresenceName);
	static_movement = store.getTrace(args[1]);

	calculate_windowed_links();
	windowed_links = store.getTrace(WindowedLinkConverter.defaultWindowedLinksName);

	infer_movement();

	store.deleteTrace(windowed_links);

	store.close();
    }

    public static void calculate_windowed_links() throws IOException {
	long window = 1000 * links.ticsPerSecond();

	StatefulReader<LinkEvent,Link> linksReader = 
	    store.getStatefulReader(links, LinkEvent.factory(), Link.factory(), 
				    new LinkUpdater(), 0, window);
	
	StatefulWriter<WindowedLinkEvent,WindowedLink> windowed_writer = 
	    store.getStatefulWriter(WindowedLinkConverter.defaultWindowedLinksName, 
				       new WindowedLinkUpdater(), links.snapshotInterval());

	WindowedLinkConverter converter =
	    new WindowedLinkConverter(windowed_writer, linksReader, window);

	converter.run();
	converter.close();
    }

    public static void infer_movement() throws IOException {
	double width = 500;
	double height = 500;
	double border = 10;
	double tube_width = 10;
	double static_thresh = 0.1;
		
	StatefulReader<PresenceEvent,Presence> presenceReader = gStore.getPresenceReader(presence);
	StatefulReader<LinkEvent,Link> linksReader = gStore.getLinkReader(links);
	
	StatefulReader<WindowedLinkEvent,WindowedLink> windowReader = 
	    store.getStatefulReader(windowed_links, WindowedLinkEvent.factory(), 
				    WindowedLink.factory(), new WindowedLinkUpdater());
	
	StatefulReader<MovementEvent,Movement> staticReader = gStore.getMovementReader(static_movement);

	StatefulWriter<MovementEvent,Movement> movementWriter = 
	    gStore.getMovementWriter(GraphStore.defaultMovementName, links.snapshotInterval());
		
		
	PlausibleMobilityConverter plausible = 
	    new PlausibleMobilityConverter( movementWriter, presenceReader, 
					    linksReader, windowReader, staticReader, 
					    width, height, border, tube_width, static_thresh);

	for ( int i=1; i<=20; ++i)
	    plausible.markKnownMovement(new Integer[]{i});
		
	float vmax = 10;
	AnticipatedForce atf = new AnticipatedForce();
	plausible.addGlobalForce(atf);
	plausible.addGlobalConstraint(new MaxSpeedConstraint(vmax));

	plausible.addGlobalForce(new WellForce(width,height));
	plausible.addGlobalForce(new DampForce());
	
	plausible.setUpdateInterval(5);

	plausible.run();
	plausible.close();
    }
}
