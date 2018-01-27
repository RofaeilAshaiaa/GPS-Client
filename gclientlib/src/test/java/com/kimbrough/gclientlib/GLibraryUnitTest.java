package com.kimbrough.gclientlib;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Rofaeil Ashaiaa
 *         Created on 17/01/18.
 */

public class GLibraryUnitTest {


    @Test
    public void UtilsUnitTest() throws Exception {

        double result1 = Utils.distanceInKmBetweenEarthCoordinates(0, 0, 0, 0);
        assertEquals(0, result1, 0);
        double result2 = Utils.distanceInKmBetweenEarthCoordinates(51.5, 0, 38.8, -77.1);
        assertEquals(5918.185064088765, result2, 0);
    }
}