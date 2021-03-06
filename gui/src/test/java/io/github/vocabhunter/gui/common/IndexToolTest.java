/*
 * Open Source Software published under the Apache Licence, Version 2.0.
 */

package io.github.vocabhunter.gui.common;

import io.github.vocabhunter.analysis.core.VocabHunterException;
import org.junit.Assert;
import org.junit.Test;

import static io.github.vocabhunter.analysis.core.CoreTool.listOf;

public class IndexToolTest {
    private static final int SIZE = 10;

    private static final int REQUESTED = 5;

    private static final int ONE_BELOW = REQUESTED - 1;

    private static final int TWO_BELOW = REQUESTED - 2;

    private static final int THREE_BELOW = REQUESTED - 3;

    private static final int ONE_ABOVE = REQUESTED + 1;

    private static final int TWO_ABOVE = REQUESTED + 2;

    private static final int FIRST = 0;

    private static final int LAST = SIZE - 1;

    private static final int BEFORE_FIRST = FIRST - 1;

    private static final int AFTER_LAST = LAST + 1;

    @Test(expected = VocabHunterException.class)
    public void testNoMatch() throws Exception {
        findClosest();
    }

    @Test
    public void testSimpleMatch() throws Exception {
        validate(REQUESTED, REQUESTED);
    }

    @Test
    public void testOneBelow() throws Exception {
        validate(ONE_BELOW, ONE_BELOW, ONE_ABOVE);
    }

    @Test
    public void testOneAbove() throws Exception {
        validate(ONE_ABOVE, TWO_BELOW, ONE_ABOVE);
    }

    @Test
    public void testTwoBelow() throws Exception {
        validate(TWO_BELOW, TWO_BELOW, TWO_ABOVE);
    }

    @Test
    public void testTwoAbove() throws Exception {
        validate(TWO_ABOVE, THREE_BELOW, TWO_ABOVE);
    }

    @Test
    public void testFirst() throws Exception {
        validate(FIRST, FIRST);
    }

    @Test
    public void testLast() throws Exception {
        validate(LAST, LAST);
    }

    @Test(expected = VocabHunterException.class)
    public void testBeforeFirst() throws Exception {
        findClosest(BEFORE_FIRST);
    }

    @Test(expected = VocabHunterException.class)
    public void testAfterLast() throws Exception {
        findClosest(AFTER_LAST);
    }

    private void validate(final int expected, final Integer... accepted) {
        int result = findClosest(accepted);

        Assert.assertEquals(expected, result);
    }

    private int findClosest(final Integer... accepted) {
        return IndexTool.findClosest(REQUESTED, SIZE, listOf(accepted)::contains);
    }
}
