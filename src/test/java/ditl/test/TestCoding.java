package ditl.test;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Random;

import org.junit.Test;

import ditl.CodedBuffer;
import ditl.CodedInputStream;

public class TestCoding {

    private final static Random rng = new Random(0);
    private final static int N = 1000;

    private final static int[] randomIntegers = new int[N];
    private final static int[] randomSIntegers = new int[N];
    private final static long[] randomLongs = new long[N];
    private final static long[] randomSLongs = new long[N];
    private final static double[] randomDoubles = new double[N];

    static {
        for (int i = 0; i < N; ++i) {
            int n = rng.nextInt();
            randomIntegers[i] = n < 0 ? -n : n;
            randomSIntegers[i] = n;
            long l = rng.nextLong();
            randomLongs[i] = l < 0 ? -l : l;
            randomSLongs[i] = l;
            randomDoubles[i] = rng.nextDouble();
        }
    }

    @Test
    public void testIntegers() throws IOException {
        ByteArrayOutputStream bis = new ByteArrayOutputStream();
        CodedBuffer cb = new CodedBuffer();
        for (int i : randomIntegers) {
            cb.writeInt(i);
        }
        cb.flush(bis);
        CodedInputStream cis = new CodedInputStream(new ByteArrayInputStream(bis.toByteArray()));
        for (int i : randomIntegers) {
            assertTrue(cis.readInt() == i);
        }
    }

    @Test
    public void testSignedIntegers() throws IOException {
        ByteArrayOutputStream bis = new ByteArrayOutputStream();
        CodedBuffer cb = new CodedBuffer();
        for (int i : randomSIntegers) {
            cb.writeSInt(i);
        }
        cb.flush(bis);
        CodedInputStream cis = new CodedInputStream(new ByteArrayInputStream(bis.toByteArray()));
        for (int i : randomSIntegers) {
            assertTrue(cis.readSInt() == i);
        }
    }

    @Test
    public void testLongs() throws IOException {
        ByteArrayOutputStream bis = new ByteArrayOutputStream();
        CodedBuffer cb = new CodedBuffer();
        for (long i : randomLongs) {
            cb.writeLong(i);
        }
        cb.flush(bis);
        CodedInputStream cis = new CodedInputStream(new ByteArrayInputStream(bis.toByteArray()));
        for (long i : randomLongs) {
            assertTrue(cis.readLong() == i);
        }
    }

    @Test
    public void testSignedLongs() throws IOException {
        ByteArrayOutputStream bis = new ByteArrayOutputStream();
        CodedBuffer cb = new CodedBuffer();
        for (long i : randomSLongs) {
            cb.writeSLong(i);
        }
        cb.flush(bis);
        CodedInputStream cis = new CodedInputStream(new ByteArrayInputStream(bis.toByteArray()));
        for (long i : randomSLongs) {
            assertTrue(cis.readSLong() == i);
        }
    }

    @Test
    public void testDoubles() throws IOException {
        ByteArrayOutputStream bis = new ByteArrayOutputStream();
        CodedBuffer cb = new CodedBuffer();
        for (double d : randomDoubles) {
            cb.writeDouble(d);
        }
        cb.flush(bis);
        CodedInputStream cis = new CodedInputStream(new ByteArrayInputStream(bis.toByteArray()));
        for (double d : randomDoubles) {
            assertTrue(cis.readDouble() == d);
        }
    }

}
