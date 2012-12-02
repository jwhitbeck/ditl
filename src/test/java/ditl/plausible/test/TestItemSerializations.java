package ditl.plausible.test;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Test;

import ditl.CodedBuffer;
import ditl.CodedInputStream;
import ditl.Item;
import ditl.graphs.Edge;
import ditl.plausible.WindowedEdge;
import ditl.plausible.WindowedEdgeEvent;
import ditl.plausible.WindowedEdgeEvent.Type;

public class TestItemSerializations {

    private static <I extends Item> I writeAndRead(I item, Item.Factory<I> factory) throws IOException {
        CodedBuffer buffer = new CodedBuffer();
        item.write(buffer);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        buffer.flush(os);
        return factory.fromBinaryStream(new CodedInputStream(new ByteArrayInputStream(os.toByteArray())));
    }

    @Test
    public void windowedEdges() throws IOException {
        Edge e = new Edge(1, 2);
        WindowedEdge we1 = new WindowedEdge(e);
        WindowedEdgeEvent wee1 = new WindowedEdgeEvent(e, Type.PREVDOWN, 0);
        WindowedEdgeEvent wee2 = new WindowedEdgeEvent(e, Type.PREVUP, 100);
        WindowedEdgeEvent wee3 = new WindowedEdgeEvent(e, Type.NEXTDOWN, 200);
        WindowedEdgeEvent wee4 = new WindowedEdgeEvent(e, Type.DOWN);
        we1.handleEvent(wee1);
        we1.handleEvent(wee2);
        we1.handleEvent(wee3);

        WindowedEdge we2 = writeAndRead(we1, new WindowedEdge.Factory());
        assertTrue(we2.edge().equals(we1.edge()));
        assertTrue(we2.minUpTime(150) == we1.minUpTime(150));
        assertTrue(we2.minDownTime(150) == we1.minDownTime(150));

        WindowedEdgeEvent wee5 = writeAndRead(wee2, new WindowedEdgeEvent.Factory());
        assertTrue(wee2.type() == wee5.type());
        assertTrue(wee2.edge().equals(wee5.edge()));

        WindowedEdgeEvent wee6 = writeAndRead(wee4, new WindowedEdgeEvent.Factory());
        assertTrue(wee4.type() == wee6.type());
        assertTrue(wee4.edge().equals(wee6.edge()));
    }
}
