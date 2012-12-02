package ditl.test;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import ditl.Reader;
import ditl.SeekMap;
import ditl.StateUpdater;
import ditl.StatefulReader;
import ditl.StatefulTrace;
import ditl.StatefulWriter;
import ditl.Trace;
import ditl.WritableStore;
import ditl.Writer;
import ditl.graphs.Arc;
import ditl.graphs.ArcEvent;
import ditl.graphs.ArcTrace;
import ditl.graphs.BeaconTrace;

public class TestReadWrite {

    @BeforeClass
    public static void openStore() throws IOException {
        File storeDir = new File(getStorePath());
        if (!storeDir.exists())
            storeDir.mkdirs();
        store = WritableStore.open(new File(getStorePath()));
    }

    @AfterClass
    public static void closeStore() throws IOException {
        store.close();
    }

    private static String getStorePath() {
        return "target/test-data/writer";
    }

    private static List<Arc> getEvents(int n) {
        int i = 0;
        int k = 0;
        List<Arc> arcs = new LinkedList<Arc>();
        while (i < n) {
            arcs.add(new Arc(k, k));
            if (++i == n)
                break;
            for (int j = 0; j < k; ++j) {
                arcs.add(new Arc(k, j));
                if (++i == n)
                    break;
                arcs.add(new Arc(j, k));
                if (++i == n)
                    break;
            }
            ++k;
        }
        return arcs;
    }

    private static class StatefulEventGenerator {
        final int n_max;
        final StateUpdater<ArcEvent, Arc> updater = new ArcTrace.Updater();
        final Random rng = new Random(0);

        StatefulEventGenerator(int n) {
            n_max = (int) Math.sqrt(n);
            updater.setState(getEvents(n));
        }

        ArcEvent getNextEvent() {
            Arc a = new Arc(rng.nextInt(n_max), rng.nextInt(n_max));
            ArcEvent aev = updater.states().contains(a) ?
                    new ArcEvent(a, ArcEvent.Type.DOWN) : new ArcEvent(a, ArcEvent.Type.UP);
            updater.handleEvent(0, aev);
            return aev;
        }

        List<ArcEvent> getNextEvents(int n) {
            List<ArcEvent> events = new LinkedList<ArcEvent>();
            for (int i = 0; i < n; ++i) {
                events.add(getNextEvent());
            }
            return events;
        }
    }

    private final static int[][] simpleTestCase = {
            { 0, 1000 }, { 1, 1000 }, { 3, 1000 },
            { 6, 1000 }, { 7, 1000 }, { 10, 1000 },
            { 16, 1000 }, { 17, 1000 }, { 20, 1000 },
            { 26, 1000 }, { 27, 1000 }, { 210, 1000 }
    };

    private final static int[][] stressTestCase = {
            { 0, 64 }, { 1, 64 << 5 }, { 3, 64 << 10 },
            { 5, 64 << 15 }
    };

    private final static long[][] seekOffsets = {
            { 100, 17823 }, { 200, 1097834 }, { 300, 2000000 }
    };

    private static WritableStore store;

    @Test
    public void testSeekMap() throws IOException {
        SeekMap.Writer writer = new SeekMap.Writer(new FileOutputStream(getStorePath() + "/index_test"));
        for (long[] so : seekOffsets) {
            writer.append(so[0], so[1]);
        }
        writer.close();

        SeekMap sm = SeekMap.open(new FileInputStream(getStorePath() + "/index_test"));
        for (long[] so : seekOffsets) {
            assertTrue(sm.getOffset(so[0]) == so[1]);
        }
    }

    @Test
    public void testReaderAndWriter() throws Exception {
        Trace<Arc> trace = store.newTrace("simple", BeaconTrace.class, true);
        Writer<Arc> writer = trace.getWriter();

        for (int[] param : simpleTestCase) {
            writer.handle(param[0], getEvents(param[1]));
        }
        writer.close();

        Reader<Arc> reader = trace.getReader();
        for (int[] param : simpleTestCase) {
            assertTrue(reader.hasNext());
            Iterator<Arc> ref_i = getEvents(param[1]).iterator();
            Iterator<Arc> i = reader.next().iterator();
            while (ref_i.hasNext()) {
                assertTrue(i.hasNext());
                assertTrue(ref_i.next().equals(i.next()));
            }
        }
    }

    @Test
    public void testSeek() throws Exception {
        Trace<Arc> trace = store.newTrace("seek", BeaconTrace.class, true);
        Writer<Arc> writer = trace.getWriter();
        for (int[] param : simpleTestCase) {
            writer.handle(param[0], getEvents(param[1]));
        }
        writer.close();

        Reader<Arc> reader = trace.getReader();
        Random rng = new Random(0);
        for (int i = 0; i < 100; i++) {
            int k = rng.nextInt(simpleTestCase.length);
            long seek_time = simpleTestCase[k][0];
            reader.seek(seek_time);
            assertTrue(reader.hasNext());
            Iterator<Arc> ref_i = getEvents(simpleTestCase[k][1]).iterator();
            Iterator<Arc> cmp_i = reader.next().iterator();
            while (ref_i.hasNext()) {
                assertTrue(cmp_i.hasNext());
                assertTrue(ref_i.next().equals(cmp_i.next()));
            }
        }

    }

    @Test
    public void stressWriter() throws Exception {
        Trace<Arc> trace = store.newTrace("stress", BeaconTrace.class, true);
        Writer<Arc> writer = trace.getWriter();

        for (int[] param : stressTestCase) {
            writer.handle(param[0], getEvents(param[1]));
        }

        writer.close();
    }

    @Test
    public void testStatefulReaderAndWriter() throws Exception {
        StatefulTrace<ArcEvent, Arc> trace = store.newTrace("stateful", ArcTrace.class, true);
        StatefulWriter<ArcEvent, Arc> writer = trace.getWriter();

        StatefulEventGenerator gen = new StatefulEventGenerator(1000);
        writer.setInitState(0, gen.updater.states());
        for (int[] param : simpleTestCase) {
            writer.handle(param[0], gen.getNextEvents(param[1]));
        }
        writer.close();

        // test events
        gen = new StatefulEventGenerator(1000);
        StatefulReader<ArcEvent, Arc> reader = trace.getReader();
        reader.seek(trace.minTime());
        for (int[] param : simpleTestCase) {
            assertTrue(reader.hasNext());
            assertTrue(reader.nextTime() == param[0]);
            Iterator<ArcEvent> cmp_events = reader.next().iterator();
            Iterator<ArcEvent> ref_events = gen.getNextEvents(param[1]).iterator();
            while (ref_events.hasNext()) {
                assertTrue(cmp_events.hasNext());
                ArcEvent c = cmp_events.next();
                ArcEvent r = ref_events.next();
                assertTrue(c.isUp() == r.isUp());
                assertTrue(c.arc().equals(r.arc()));
            }
        }
        reader.close();

        // test states
        gen = new StatefulEventGenerator(1000);
        reader = trace.getReader();
        reader.seek(trace.minTime());
        for (int[] param : simpleTestCase) {
            reader.seek(param[0]);
            assertTrue(reader.referenceState().size() == gen.updater.states().size());
            gen.getNextEvents(param[1]);
        }
        reader.close();
    }
}
