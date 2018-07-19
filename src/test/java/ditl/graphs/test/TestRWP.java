/*******************************************************************************
 * This file is part of DITL.                                                  *
 *                                                                             *
 * Copyright (C) 2011-2012 John Whitbeck <john@whitbeck.fr>                    *
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
package ditl.graphs.test;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.apache.commons.io.FileUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import ditl.cli.CLI;

@RunWith(Suite.class)
@SuiteClasses({
        TestRWP.Import.class,
        TestRWP.CompareExport.class,
        TestRWP.CompareAnalyze.class })
public class TestRWP {

    @BeforeClass
    public static void checkStore() throws IOException {
        File storeDir = new File(getStorePath());
        if (!storeDir.exists())
            storeDir.mkdirs();
    }

    public static class Import {

        @Test
        public void importAll() throws IOException {
            // 1. movement
            graphsCli("import-movement --force %s " + getResourcePath() + "/movement.ns2");
            // 2. edges
            graphsCli("movement-to-edges --force %s 20");
            // 3. presence
            graphsCli("movement-to-presence --force %s");
            // 4. connected components
            graphsCli("edges-to-ccs --groups ccs --force %s");
            // 5. groups
            graphsCli("import-groups --force %s [{members:[[0,3]]},{members:[4,6]},{members:[5,[7,9]]}]");
            // 6. resample with 1 second
            graphsCli("resample --force %s 1");
            // 7. calculate arcs from beacons
            graphsCli("beacons-to-arcs --force %s");
            // 8. calculate resampled edges
            graphsCli("arcs-to-edges --force --edges resampled %s");
            // 9. calculate group edges
            graphsCli("group-edges --force %s group_edges");
            // 10. buffer edges
            graphsCli("buffer-edges --force %s 5");
            // 11. Reachability
            graphsCli("reachability --force --edges resampled --prune-reused %s 1 1 5");
        }
    }

    public static class CompareExport {

        @Test
        public void compareEdges() throws IOException {
            graphsCliAndCompare("export-edges --dest-time-unit ms %s", "edges.crawdad");
        }

        @Test
        public void compareBufferedEdges() throws IOException {
            graphsCliAndCompare("export-edges --dest-time-unit ms --edges buffered_edges %s", "buffered_edges.crawdad");
        }

        @Test
        public void compareGroupEdges() throws IOException {
            graphsCliAndCompare("export-edges --dest-time-unit ms --edges group_edges %s", "group_edges.crawdad");
        }

        @Test
        public void compareArcs() throws IOException {
            graphsCliAndCompare("export-arcs --dest-time-unit ms %s", "arcs.crawdad");
        }

        @Test
        public void compareResampled() throws IOException {
            graphsCliAndCompare("export-edges --dest-time-unit ms --edges resampled %s", "resampled.crawdad");
        }

        @Test
        public void compareReachable() throws IOException {
            graphsCliAndCompare("export-arcs --dest-time-unit ms --arcs resampled_t1000_d5000 %s", "reachable.crawdad");
        }
    }

    public static class CompareAnalyze {

        @Test
        public void compareAnyContacts() throws IOException {
            graphsCliAndCompare("analyze --any-contacts %s", "any_contacts.report");
        }

        @Test
        public void compareClustering() throws IOException {
            graphsCliAndCompare("analyze --clustering %s", "clustering.report");
        }

        @Test
        public void compareContacts() throws IOException {
            graphsCliAndCompare("analyze --contacts %s", "contacts.report");
        }

        @Test
        public void compareFirstContactTime() throws IOException {
            graphsCliAndCompare("analyze --first-contact-time %s", "first_contact_time.report");
        }

        @Test
        public void compareGroupSize() throws IOException {
            graphsCliAndCompare("analyze --group-size %s", "group_size.report");
        }

        @Test
        public void compareCCSize() throws IOException {
            graphsCliAndCompare("analyze --group-size --groups ccs %s", "group_size.ccs.report");
        }

        @Test
        public void compareInterAnyContacts() throws IOException {
            graphsCliAndCompare("analyze --inter-any-contacts %s", "inter_any_contacts.report");
        }

        @Test
        public void compareInterContacts() throws IOException {
            graphsCliAndCompare("analyze --inter-contacts %s", "inter_contacts.report");
        }

        @Test
        public void compareNodeCount() throws IOException {
            graphsCliAndCompare("analyze --node-count %s", "node_count.report");
        }

        @Test
        public void compareNodeDegree() throws IOException {
            graphsCliAndCompare("analyze --node-degree %s", "node_degree.report");
        }

        @Test
        public void compareNumContacts() throws IOException {
            graphsCliAndCompare("analyze --num-contacts %s", "num_contacts.report");
        }

        @Test
        public void compareReachability() throws IOException {
            graphsCliAndCompare("analyze --reachability --arcs resampled_t1000_d5000 %s", "reachability.report");
        }
    }

    private static String getStorePath() {
        return "target/test-data/rwp";
    }

    private static void cli(String cmd) throws IOException {
        CLI.main(cmd.split(" "));
    }

    private static String getResourcePath() {
        return "src/test/resources/rwp";
    }

    private static void graphsCli(String fmtCmd) throws IOException {
        cli(String.format("graphs " + fmtCmd, getStorePath()));
    }

    private static void graphsCliToFile(String fmtCmd, String fileName) throws IOException {
        FileOutputStream fos = new FileOutputStream(new File(getStorePath() + "/" + fileName));
        System.setOut(new PrintStream(fos));
        graphsCli(fmtCmd);
        fos.close();
        System.setOut(System.out);
    }

    private static void graphsCliAndCompare(String fmtCmd, String fileName) throws IOException {
        graphsCliToFile(fmtCmd, fileName);
        compareWithResource(fileName);
    }

    private static void compareWithResource(String fileName) throws IOException {
        assertEquals(fileName + " differs!",
                FileUtils.readFileToString(new File(getStorePath() + "/" + fileName)),
                FileUtils.readFileToString(new File(getResourcePath() + "/" + fileName)));
    }

}
