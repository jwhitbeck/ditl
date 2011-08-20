import java.io.*;
import plausible.*;
import plausible.constraints.*;
import plausible.forces.*;
import ditl.*;
import ditl.graphs.*;

public class InferStanford {
    
    static WritableStore store;
    static GraphStore gStore;
    static Trace links;
    static Trace windowed_links;
    static Trace presence;
    static Trace movement;

    public static void main(String[] args) throws IOException {
	store = WritableStore.open(new File(args[0]));
	gStore = new GraphStore(store);

	links = store.getTrace(GraphStore.defaultLinksName);
	presence = store.getTrace(GraphStore.defaultPresenceName);

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
	double width = 1000;
	double height = 1000;
	double border = 10;
	double tube_width = 10;
	double static_thresh = 0.1;
		
	StatefulReader<PresenceEvent,Presence> presenceReader = gStore.getPresenceReader(presence);
	StatefulReader<LinkEvent,Link> linksReader = gStore.getLinkReader(links);
	
	StatefulReader<WindowedLinkEvent,WindowedLink> windowReader = 
	    store.getStatefulReader(windowed_links, WindowedLinkEvent.factory(), 
				    WindowedLink.factory(), new WindowedLinkUpdater());
	
	StatefulWriter<MovementEvent,Movement> movementWriter = 
	    gStore.getMovementWriter(GraphStore.defaultMovementName, links.snapshotInterval());
		
		
	PlausibleMobilityConverter plausible = 
	    new PlausibleMobilityConverter( movementWriter, presenceReader, 
					    linksReader, windowReader, null, 
					    width, height, border, tube_width, static_thresh);
		
	float vmax = 10;
	AnticipatedForce atf = new AnticipatedForce();
	atf.setTau(200);
	plausible.addGlobalForce(atf);
	plausible.addGlobalConstraint(new MaxSpeedConstraint(vmax));

	plausible.addGlobalForce(new WellForce(width,height));
	plausible.addGlobalForce(new DampForce());
	
	plausible.setUpdateInterval(1);
	plausible.setNSteps(10);

	plausible.run();
	plausible.close();
    }
}