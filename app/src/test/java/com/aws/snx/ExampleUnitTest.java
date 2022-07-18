package com.aws.snx;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        System.out.println("test");
        MainActivity activity = new MainActivity();
        activity.testPublish();
        assertEquals(4, 2 + 2);
    }


}