import ditl.*;
import ditl.Writer;
import ditl.graphs.*;
import java.util.*;
import java.io.*;

public class ImportStanford implements Converter {

    Map<Integer,Integer> mote_offsets = new HashMap<Integer,Integer>();
    Map<Integer,File> mote_files;
    Random rng = new Random();
    long tps = 1000;
    int granularity = 20; // seconds
    
    /* the following params are from 
     *  http://www.salathegroup.com/guide/school_2010.html
     */
    int middleTimeStep = 1350;
    int numberOfTimeSteps = 1620; // 9 hours
    int maxTimeStamp = middleTimeStep + ((numberOfTimeSteps-1)/2); // integer division!
    int minTimeStamp = maxTimeStamp - (numberOfTimeSteps-1);

    Writer<Edge> beacon_writer;

    ImportStanford(Writer<Edge> beaconWriter, Map<Integer,File> moteFiles){
	beacon_writer=beaconWriter;
	mote_files = moteFiles;
	for ( Integer id : moteFiles.keySet() ){
	    mote_offsets.put(id, rng.nextInt(granularity));
	}
    }

    long getBeaconTime(Integer from, Integer global_time){
	int offset = mote_offsets.get(from);
	return (global_time * granularity + offset) * tps;
    }


    public void run() throws IOException {
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
		    beacon_writer.queue(time, new Edge(from, to));
		}
	    }
	    reader.close();
	}
	beacon_writer.flush();
    }

    public void close() throws IOException {
	beacon_writer.setProperty(Trace.ticsPerSecondKey, tps);
	beacon_writer.close();
    }

    public static void main(String[] args) throws IOException {
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
	Writer<Edge> beaconWriter = new GraphStore(store).getBeaconsWriter(GraphStore.defaultBeaconsName);
	Converter converter = new ImportStanford(beaconWriter, moteFiles);
	converter.run();
	converter.close();

	store.close();
    }
}