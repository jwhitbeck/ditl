import ditl.*;
import ditl.Writer;
import ditl.graphs.*;
import java.util.*;
import java.io.*;

public class ImportStanford implements Converter {

    Map<Integer,Integer> mote_offsets = new HashMap<Integer,Integer>();
    Map<Integer,File> mote_files;
    Random rng = new Random();
    boolean use_rand;
    long tps = 1000;
    int granularity = 20; // seconds
    
    /* the following params are from 
     *  http://www.salathegroup.com/guide/school_2010.html
     */
    int middleTimeStep = 1350;
    int numberOfTimeSteps = 1620; // 9 hours
    int maxTimeStamp = middleTimeStep + ((numberOfTimeSteps-1)/2); // integer division!
    int minTimeStamp = maxTimeStamp - (numberOfTimeSteps-1);

    BeaconTrace _beacons;

    ImportStanford(BeaconTrace beacons, Map<Integer,File> moteFiles, boolean randomize){
    	_beacons = beacons;
    	use_rand = randomize;
    	mote_files = moteFiles;
    	for ( Integer id : moteFiles.keySet() )
    		mote_offsets.put(id, (use_rand)? rng.nextInt(granularity) : 0);
    }

    long getBeaconTime(Integer from, Integer global_time){
    	int offset = mote_offsets.get(from);
    	return (global_time * granularity + offset) * tps;
    }


    @Override
    public void convert() throws IOException {
    	Writer<Arc> beacon_writer = _beacons.getWriter();
		for ( Map.Entry<Integer,File> e : mote_files.entrySet() ){
		    Integer to = e.getKey();
		    File f = e.getValue();
		    BufferedReader reader = new BufferedReader(new FileReader(f));
		    String line;
		    while ( (line=reader.readLine())!=null ){
				String[] elems = line.split(" ");
				Integer from = Integer.parseInt(elems[0]);
				Integer time_stamp = Integer.parseInt(elems[4]);
				if ( time_stamp >= minTimeStamp && time_stamp <= maxTimeStamp ){
				    long time = getBeaconTime(from, time_stamp);
				    beacon_writer.queue(time, new Arc(from, to));
				}
		    }
		    reader.close();
		}
		beacon_writer.flush();
		beacon_writer.setProperty(Trace.timeUnitKey, "ms"); // tps = 1000
		beacon_writer.setProperty(BeaconTrace.beaconningPeriodKey, granularity*1000);
		beacon_writer.close();
    }

    public static void main(String[] args) throws Exception {
    	WritableStore store = WritableStore.open(new File(args[0]));
    	Map<Integer,File> moteFiles = new HashMap<Integer,File>();
    	for ( int i=1; i<args.length; ++i){
    		File f = new File(args[i]);
    		String fileName = f.getName();
    		String[] elems = fileName.split("-");
    		if ( elems[0].equals("node") ){
    			Integer id = Integer.parseInt(elems[1]);
    			moteFiles.put(id,f);
    		}
    	}
    	BeaconTrace beacons = store.newTrace("rand_beacons", BeaconTrace.class, true);
		new ImportStanford(beacons, moteFiles, true).convert();
		BeaconTrace beacons2 = store.newTrace(BeaconTrace.class.getAnnotation(Trace.Type.class).value(), BeaconTrace.class, true);
		new ImportStanford(beacons2, moteFiles, false).convert();
		
		store.close();
    }
}