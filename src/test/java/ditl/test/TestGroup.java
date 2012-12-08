package ditl.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.junit.Test;

import ditl.Groups;
import ditl.IdMap;

public class TestGroup {
    @Test
    public void jsonToGroup() {
        IdMap idmap = new IdMap(JSONObject.fromObject("{\"a\":13}"));

        Set<Integer> group = Groups.parse(JSONArray.fromObject("[[1,3],5,[8,11],\"a\"]"), idmap);
        for (int i : new int[] { 1, 2, 3, 5, 8, 9, 10, 11, 13 }) {
            assertTrue(group.contains(i));
        }
        for (int i : new int[] { 0, 4, 6, 7, 12 }) {
            assertFalse(group.contains(i));
        }
    }

    @Test
    public void groupToJson() {
        Set<Integer> group = Groups.parse(JSONArray.fromObject("[[1,3],5,[8,11],14]"));
        assertTrue(group.equals(Groups.parse(Groups.toJSON(group))));
    }
}
