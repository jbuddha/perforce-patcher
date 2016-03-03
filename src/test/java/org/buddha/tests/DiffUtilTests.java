package org.buddha.tests;

import difflib.DiffUtils;
import difflib.Patch;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

/**
 *
 * @author jbuddha
 */
public class DiffUtilTests {

    @Test
    public void checkOnFirstLineRemoved() {
        List<String> old = new ArrayList<>();

        old.add("line 1");
        old.add("line 2");
        old.add("line 3");
        old.add("line 4");

        List<String> revised = new ArrayList<>();

        revised.add("line 2");
        revised.add("line 2");
        revised.add("line 3");
        revised.add("line 4");
        revised.add("line 5");

        Patch<String> diff = DiffUtils.diff(old, revised);
        List<String> generateUnifiedDiff = DiffUtils.generateUnifiedDiff("file.txt", "file.txt", old, diff, 10);
        for (String l : generateUnifiedDiff) {
            System.out.println(l);
        }
    }
}
