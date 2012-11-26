package ditl.plausible.cli;

import java.io.IOException;
import java.util.*;

import org.apache.commons.cli.*;

import ditl.Store.*;
import ditl.WritableStore.AlreadyExistsException;
import ditl.cli.ConvertApp;
import ditl.graphs.*;
import ditl.graphs.cli.GraphOptions;
import ditl.plausible.*;
import ditl.plausible.constraints.*;
import ditl.plausible.forces.*;

public class InferMobility extends ConvertApp {
	
	private GraphOptions graph_options = new GraphOptions(GraphOptions.PRESENCE, GraphOptions.EDGES, GraphOptions.MOVEMENT);
	private String windowedEdgesOption = "windowed-edges";
	private String windowed_edges_name;
	private String knownMovementOption = "known-movement";
	private String known_movement_name;
	private String constraintsOption = "constraints";
	private String constraints;
	
	private double width;
	private double height;
	private String vmaxOption = "vmax";
	private double vmax;
	private String kOption = "K";
	private double K;
	private String alphaOption = "alpha";
	private double alpha;
	private String tauOption = "tau";
	private double tau;
	private String epsilonOption = "epsilon";
	private double epsilon;
	private String lambdaOption = "lambda";
	private double lambda;
	private String rangeOption = "range";
	private double range;
	private String cutoffOption = "cutoff";
	private double cutoff;
	private String dampOption = "damp";
	private double D;
	private String borderOption = "border";
	private double border;
	private String tubeWidthOption = "tube-width";
	private double tube_width;
	private String staticThreshOption = "static-thresh";
	private double static_thresh;
	private String nStepsOption = "n-steps";
	private int n_steps;
	private String updateIntervalOption = "update-interval";
	private long update_interval;
	private String warmTimeOption = "warm-time";
	private long warm_time;
	private String noOverlapOption = "no-overlap";
	private boolean overlap;
	private String knownNodesOption = "known-nodes";
	private Integer[] known_nodes;
	
	public final static String PKG_NAME = "plausible";
	public final static String CMD_NAME = "infer";
	public final static String CMD_ALIAS = "i";
	
	@Override
	protected void initOptions() {
		super.initOptions();
		graph_options.setOptions(options);
		options.addOption(null, windowedEdgesOption, true, "name of windowed edge trace (default: "+WindowedEdgeTrace.defaultName+")");
		options.addOption(null, constraintsOption, true, "list of constraints (default: empty)");
		options.addOption(null, vmaxOption, true, "max node speed (default: "+AnticipatedForce.defaultVmax+")");
		options.addOption(null, kOption, true, "the Hooke parameter (default: "+AnticipatedForce.defaultK+")");
		options.addOption(null, lambdaOption, true, "spring equilibrium length in the Hooke force (default: "+AnticipatedForce.defaultLambda+")");
		options.addOption(null, alphaOption, true, "the Coulomb exponent (default: "+AnticipatedForce.defaultAlpha+")");
		options.addOption(null, epsilonOption, true, "guard to prevent denom from going to zero in coulomb force (default: "+AnticipatedForce.defaultEpsilon+")");
		options.addOption(null, tauOption, true, "time at which new events start to have a significant effect (default: "+AnticipatedForce.defaultTau+")");
		options.addOption(null, cutoffOption, true, "the distance beyond which the repulsive force goes to zero (default: "+AnticipatedForce.defaultCutoff+")");
		options.addOption(null, rangeOption, true, "transmission range (default: "+AnticipatedForce.defaultRange+")");
		options.addOption(null, staticThreshOption, true, "distance threshold for deciding whether a node is static or not (default: "+PlausibleMobilityConverter.defaultStaticThresh+")");
		options.addOption(null, tubeWidthOption, true, "width of the 'tube' for approximating straight lines (default: "+PlausibleMobilityConverter.defaultTubeWidth+")");
		options.addOption(null, borderOption, true, "border around the inferred mobility area (default: "+PlausibleMobilityConverter.defaultBorder+")");
		options.addOption(null, nStepsOption, true, "number of intermediate points between successive updates (default "+PlausibleMobilityConverter.defaultNSteps+")");
		options.addOption(null, updateIntervalOption, true, "time between printing new positions (default "+PlausibleMobilityConverter.defaultUpdateInterval+")");
		options.addOption(null, warmTimeOption, true, "time in seconds of initial warming from random positions (default "+PlausibleMobilityConverter.defaultWarmTime+")");
		options.addOption(null, dampOption, true, "damp force constant (default "+DampForce.defaultD+")");
		options.addOption(null, knownMovementOption, true, "name of known movement trace");
		options.addOption(null, noOverlapOption, true, "do not use overlap");
		options.addOption(null, knownNodesOption, true, "mark nodes with known movement");
	}
	
	@Override
	protected void parseArgs(CommandLine cli, String[] args)
			throws ParseException, ArrayIndexOutOfBoundsException,
			HelpException {
		super.parseArgs(cli, args);
		graph_options.parse(cli);
		windowed_edges_name = cli.getOptionValue(windowedEdgesOption, WindowedEdgeTrace.defaultName);
		width = Double.parseDouble(args[1]);
		height = Double.parseDouble(args[2]);
		vmax = Double.parseDouble(cli.getOptionValue(vmaxOption, String.valueOf(AnticipatedForce.defaultVmax)));
		K = Double.parseDouble(cli.getOptionValue(kOption, String.valueOf(AnticipatedForce.defaultK)));
		alpha = Double.parseDouble(cli.getOptionValue(alphaOption, String.valueOf(AnticipatedForce.defaultAlpha)));
		tau = Double.parseDouble(cli.getOptionValue(tauOption, String.valueOf(AnticipatedForce.defaultTau)));
		epsilon = Double.parseDouble(cli.getOptionValue(epsilonOption, String.valueOf(AnticipatedForce.defaultEpsilon)));
		lambda = Double.parseDouble(cli.getOptionValue(lambdaOption, String.valueOf(AnticipatedForce.defaultLambda)));
		range = Double.parseDouble(cli.getOptionValue(rangeOption, String.valueOf(AnticipatedForce.defaultRange)));
		cutoff = Double.parseDouble(cli.getOptionValue(cutoffOption, String.valueOf(AnticipatedForce.defaultCutoff)));
		D = Double.parseDouble(cli.getOptionValue(dampOption, String.valueOf(DampForce.defaultD)));
		border = Double.parseDouble(cli.getOptionValue(borderOption, String.valueOf(PlausibleMobilityConverter.defaultBorder)));
		tube_width = Double.parseDouble(cli.getOptionValue(tubeWidthOption, String.valueOf(PlausibleMobilityConverter.defaultTubeWidth)));
		static_thresh = Double.parseDouble(cli.getOptionValue(staticThreshOption, String.valueOf(PlausibleMobilityConverter.defaultStaticThresh)));
		n_steps = Integer.parseInt(cli.getOptionValue(nStepsOption, String.valueOf(PlausibleMobilityConverter.defaultNSteps)));
		update_interval = Long.parseLong(cli.getOptionValue(updateIntervalOption, String.valueOf(PlausibleMobilityConverter.defaultUpdateInterval)));
		warm_time = Long.parseLong(cli.getOptionValue(warmTimeOption, String.valueOf(PlausibleMobilityConverter.defaultWarmTime)));
		constraints = cli.getOptionValue(constraintsOption);
		known_movement_name = cli.getOptionValue(knownMovementOption);
		overlap = !cli.hasOption(noOverlapOption);
		if ( cli.hasOption(knownNodesOption) ) 
			known_nodes = parseKnownNodes(cli.getOptionValue(knownNodesOption));
	}

	@Override
	protected void run() throws IOException, NoSuchTraceException,
			AlreadyExistsException, LoadTraceException {
		
		PresenceTrace presence = (PresenceTrace)orig_store.getTrace(graph_options.get(GraphOptions.PRESENCE));
		EdgeTrace edges = (EdgeTrace)orig_store.getTrace(graph_options.get(GraphOptions.EDGES));
		WindowedEdgeTrace windowed_edges = (WindowedEdgeTrace)orig_store.getTrace(windowed_edges_name);
		MovementTrace known_movement = null;
		if ( known_movement_name != null ) 
			known_movement = (MovementTrace)orig_store.getTrace(known_movement_name);
		MovementTrace movement = (MovementTrace)dest_store.newTrace(graph_options.get(GraphOptions.MOVEMENT), MovementTrace.type, force);
		
		long tps = presence.ticsPerSecond();
		update_interval *= tps;
		warm_time *= tps;
		
		PlausibleMobilityConverter plausible = new PlausibleMobilityConverter(
				movement, presence, edges, windowed_edges, known_movement, 
				width, height, tube_width, static_thresh,
				n_steps, update_interval, warm_time, overlap);
		
		if ( known_nodes != null )
			plausible.markKnownMovement(known_nodes);

		plausible.addGlobalForce( new AnticipatedForce(
				K, alpha, vmax, 
				range, epsilon, tau, cutoff, lambda, tps));

		plausible.addGlobalForce( new WellForce(width, height) );
		plausible.addGlobalForce( new DampForce(D) );
		
		plausible.addGlobalConstraint(new BoxConstraint(width, height, border));
		plausible.addGlobalConstraint(new MaxSpeedConstraint(vmax) );
		
		if ( constraints != null)
			parseConstraintString(plausible, constraints);
		
		plausible.convert();
	}
	
	@Override
	protected String getUsageString(){
		return "[OPTIONS] STORE WIDTH HEIGHT";
	}
	
	private final class Elem {
		String cmd;
		Map<String,String> map = new HashMap<String,String>();
		Elem(String cmd_string){
			String[] elems = cmd_string.split(",");
			cmd = elems[0];
			for ( int i=1; i<elems.length; ++i ){
				String[] kv = elems[i].split("@");
				map.put(kv[0].toLowerCase(), kv[1]);
			}
		}
		Integer getInt(String key){ return Integer.parseInt(map.get(key.toLowerCase()));}
		Double getDouble(String key){ return Double.parseDouble(map.get(key.toLowerCase()));}
		boolean hasKey(String key){ return map.containsKey(key.toLowerCase()); }
	}
	
	private Elem[] stringToElems(String s){
		String[] cmds = s.split(":");
		Elem[] elems = new Elem[cmds.length];
		for(int i=0; i<cmds.length; ++i)
			elems[i] = new Elem(cmds[i]);
		return elems;
	}
	
	private void parseConstraintString(PlausibleMobilityConverter pmc, String s){
		for ( Elem elem : stringToElems(s) ){
			Constraint c = null;
			if ( elem.cmd.equals("Box") ){
				if ( elem.hasKey("width") && elem.hasKey("height") && elem.hasKey("border")){
					double w = elem.getDouble("width");
					double h = elem.getDouble("height");
					double b = elem.getDouble("border");
					c = new BoxConstraint(w,h,b);
				}
			} else if ( elem.cmd.equals("Horizontal") ){
				if ( elem.hasKey("height") )
					c = new HorizontalConstraint(elem.getDouble("height") );
			} else if ( elem.cmd.equals("Vertical") ){
				if ( elem.hasKey("width") )
					c = new VerticalConstraint(elem.getDouble("width") );
			} else if ( elem.cmd.equals("LeftOutlier") ){
				if ( elem.hasKey("node"))
					c = new LeftOutlierConstraint();
			} else if ( elem.cmd.equals("RightOutlier") ){
				if ( elem.hasKey("node"))
					c = new RightOutlierConstraint();
			}
			if ( c != null ){
				if ( elem.hasKey("node") ){
					Integer id = elem.getInt("node");
					pmc.addNodeConstraint(id, c);
				} else {
					pmc.addGlobalConstraint(c);
				}
			}
		}
	}

	private Integer[] parseKnownNodes(String s){
		List<Integer> list = new LinkedList<Integer>();
		String[] ranges = s.split(":");
		int j = 0;
		while ( j < ranges.length ){
			String[] bounds = ranges[j].split("-");
			Integer n;
			if ( bounds.length == 1 ) {
				n = Integer.parseInt(bounds[0]);
				list.add(n);
			} else {
				for ( n = Integer.parseInt(bounds[0]); n<=Integer.parseInt(bounds[1]); ++n)
					list.add(n);
			}
			++j;
		}
		return list.toArray(new Integer[]{});
	}
}
