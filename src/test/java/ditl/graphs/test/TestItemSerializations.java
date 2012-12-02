package ditl.graphs.test;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import ditl.CodedBuffer;
import ditl.CodedInputStream;
import ditl.Item;
import ditl.graphs.Arc;
import ditl.graphs.ArcEvent;
import ditl.graphs.Edge;
import ditl.graphs.EdgeEvent;
import ditl.graphs.Group;
import ditl.graphs.GroupEvent;
import ditl.graphs.Movement;
import ditl.graphs.MovementEvent;
import ditl.graphs.Point;
import ditl.graphs.Presence;
import ditl.graphs.PresenceEvent;

public class TestItemSerializations {

    private static <I extends Item> I writeAndRead(I item, Item.Factory<I> factory) throws IOException {
        CodedBuffer buffer = new CodedBuffer();
        item.write(buffer);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        buffer.flush(os);
        return factory.fromBinaryStream(new CodedInputStream(new ByteArrayInputStream(os.toByteArray())));
    }

    @Test
    public void presence() throws IOException {
        Presence p1 = new Presence(1);
        Presence p2 = writeAndRead(p1, new Presence.Factory());
        assertTrue(p1.id().equals(p2.id()));
    }

    @Test
    public void presenceEvent() throws IOException {
        PresenceEvent p1 = new PresenceEvent(1, PresenceEvent.Type.IN);
        PresenceEvent p2 = writeAndRead(p1, new PresenceEvent.Factory());
        assertTrue(p1.id().equals(p2.id()));
        assertTrue(p1.isIn() == p2.isIn());
    }

    @Test
    public void arc() throws IOException {
        Arc a = new Arc(1, 2);
        Arc b = writeAndRead(a, new Arc.Factory());
        assertTrue(a.equals(b));
    }

    @Test
    public void arcEvent() throws IOException {
        ArcEvent ae = new ArcEvent(1, 2, ArcEvent.Type.UP);
        ArcEvent be = writeAndRead(ae, new ArcEvent.Factory());
        assertTrue(ae.from().equals(be.from()));
        assertTrue(ae.to().equals(be.to()));
        assertTrue(ae.isUp() == be.isUp());
    }

    @Test
    public void edge() throws IOException {
        Edge e = new Edge(1, 2);
        Edge f = writeAndRead(e, new Edge.Factory());
        assertTrue(e.equals(f));
    }

    @Test
    public void edgeEvent() throws IOException {
        EdgeEvent ee = new EdgeEvent(-1, 2, EdgeEvent.Type.UP);
        EdgeEvent ef = writeAndRead(ee, new EdgeEvent.Factory());
        assertTrue(ee.id1().equals(ef.id1()));
        assertTrue(ee.id2().equals(ef.id2()));
        assertTrue(ee.isUp() == ef.isUp());
    }

    @Test
    public void group() throws IOException {
        Set<Integer> members = new HashSet<Integer>(Arrays.asList(1, 5, 8));
        Group g = new Group(1, members);
        Group h = writeAndRead(g, new Group.Factory());
        assertTrue(g.gid().equals(h.gid()));
        assertTrue(h.members().equals(g.members()));
    }

    @Test
    public void groupEvent() throws IOException {
        Set<Integer> members = new HashSet<Integer>(Arrays.asList(1, 5, 8));
        GroupEvent ge1 = new GroupEvent(1, GroupEvent.Type.NEW);
        GroupEvent ge2 = writeAndRead(ge1, new GroupEvent.Factory());
        assertTrue(ge1.gid().equals(ge2.gid()));
        assertTrue(ge1.type() == ge2.type());

        ge1 = new GroupEvent(1, GroupEvent.Type.JOIN, members);
        ge2 = writeAndRead(ge1, new GroupEvent.Factory());
        assertTrue(ge1.gid().equals(ge2.gid()));
        assertTrue(ge1.type() == ge2.type());
        assertTrue(ge1.members().equals(ge2.members()));
    }

    @Test
    public void movement() throws IOException {
        Movement m1 = new Movement(1, new Point(4.5, 9.7));
        Movement m2 = writeAndRead(m1, new Movement.Factory());
        assertTrue(m1.from().x == m2.from().x);
        assertTrue(m1.from().y == m2.from().y);
        assertTrue(m1.id().equals(m2.id()));

        m1 = new Movement(2, new Point(3.1, 2.3), 0, new Point(4.5, 9.7), 0.2);
        m2 = writeAndRead(m1, new Movement.Factory());
        assertTrue(m1.from().x == m2.from().x);
        assertTrue(m1.from().y == m2.from().y);
        assertTrue(m1.id().equals(m2.id()));
        assertTrue(m1.to().x == m2.to().x);
        assertTrue(m1.to().y == m2.to().y);
        assertTrue(m1.positionAtTime(100).x == m2.positionAtTime(100).x);
        assertTrue(m1.positionAtTime(100).y == m2.positionAtTime(100).y);
    }

    @Test
    public void movementEvent() throws IOException {
        MovementEvent m1 = new MovementEvent(1);
        MovementEvent m2 = writeAndRead(m1, new MovementEvent.Factory());
        assertTrue(m1.id() == m2.id());
        assertTrue(m1.type() == m2.type());

        m1 = new MovementEvent(1, new Point(1, 2));
        m2 = writeAndRead(m1, new MovementEvent.Factory());
        assertTrue(m1.id() == m2.id());
        assertTrue(m1.dest().x == m2.dest().x);
        assertTrue(m1.dest().y == m2.dest().y);
        assertTrue(m1.type() == m2.type());

        m1 = new MovementEvent(1, 2.0, new Point(1, 2));
        m2 = writeAndRead(m1, new MovementEvent.Factory());
        assertTrue(m1.id() == m2.id());
        assertTrue(m1.dest().x == m2.dest().x);
        assertTrue(m1.dest().y == m2.dest().y);
        assertTrue(m1.type() == m2.type());
    }
}
