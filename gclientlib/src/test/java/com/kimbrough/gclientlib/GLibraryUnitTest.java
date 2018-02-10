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

        double result1 = GeoUtils.haversineDistance_km(0, 0, 0, 0);
        assertEquals(0, result1, 0);
        double result2 = GeoUtils.haversineDistance_km(51.5, 0, 38.8, -77.1);
        assertEquals(5918.185064088765, result2, 0);
    }

    @Test
    public void haversineTestsWithKnownDistance() throws Exception {

        // https://www.freemaptools.com/measure-distance.htm
        // this website only reports actual distance in m to 2dp, which means that the below accuracy scores could
        // even be under estimating the accuracy of the haversine
        // this pair is 10.13m apart and the measurement accuracy is within 145cm
        double lat1 = 0.07291719317436218; double lon1 = 51.47735121632144;
        double lat2 = 0.0728969273041002; double lon2 = 51.47726109959;
        double expected_distance_metres = 10.13; // actual 10.270774860991297
        double result1 = 1000.0*GeoUtils.haversineDistance_km(lat1, lon1, lat2, lon2);
        assertEquals(expected_distance_metres, result1, 0.145);

        // at 5.76m apart the measurement is 5.766821636640382
        lat1 = 0.07290378212928772; lon1 = 51.47732699342683;
        lat2=0.07289960951311514; lon2=51.477275299239956;
        expected_distance_metres = 5.76;
        result1 = 1000.0*GeoUtils.haversineDistance_km(lat1, lon1, lat2, lon2);
        assertEquals(expected_distance_metres, result1, 0.01);

        // with a 2.04m distance you can get to 1cm accuracy (probably an element of luck here) actual: 2.039749550583753
        lat1=0.07290109992027283; lon1=51.477293582516566;
        lat2=0.07289960951311514; lon2=51.477275299239956;
        expected_distance_metres = 2.04;

        result1 = 1000.0*GeoUtils.haversineDistance_km(lat1, lon1, lat2, lon2);
        assertEquals(expected_distance_metres, result1, 0.001);

        // 0.73m known distance gives 0.7346915763409498 in return as the result
        lat1=0.0729004293680191; lon1=51.47728481214858;
        lat2=0.072899944789242; lon2=51.47727822269677;
        expected_distance_metres = 0.73;

        result1 = 1000.0*GeoUtils.haversineDistance_km(lat1, lon1, lat2, lon2);
        assertEquals(expected_distance_metres, result1, 0.005);

    }
}