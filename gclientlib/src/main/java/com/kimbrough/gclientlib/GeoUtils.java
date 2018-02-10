package com.kimbrough.gclientlib;

/**
 * @author Rofaeil Ashaiaa
 *         Created on 26/01/18.
 */

public class GeoUtils {
    public static double MEAN_EARTH_RADIUS_KM_ACCURATE = 6371;

    public static double degreesToRadians(double degrees) {
        return degrees * Math.PI / 180;
    }

    /*    Haversine https://en.wikipedia.org/wiki/Haversine_formula
        formula:	a = sin²(Δφ/2) + cos φ1 ⋅ cos φ2 ⋅ sin²(Δλ/2)
        c = 2 ⋅ atan2( √a, √(1−a) )
        d = R ⋅ c
        where	φ is latitude, λ is longitude, R is earth’s radius (mean radius = 6,371km);
        note that angles need to be in radians
       */
    public static double haversineDistance_km(double lat1, double lon1, double lat2, double lon2) {

        double dLat = degreesToRadians(lat2 - lat1);
        double dLon = degreesToRadians(lon2 - lon1);

        lat1 = degreesToRadians(lat1);
        lat2 = degreesToRadians(lat2);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
               Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(lat1) * Math.cos(lat2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double d = (MEAN_EARTH_RADIUS_KM_ACCURATE * c);
        return d;
    }


    /*    Law of cosines:	d = acos( sin φ1 ⋅ sin φ2 + cos φ1 ⋅ cos φ2 ⋅ cos Δλ ) ⋅ R
    *
    * https://en.wikipedia.org/wiki/Great-circle_distance
    * this claims that, on 32bit systems you get cos() rounding problems for close points, and that haversine
    * is better for close points
    * */
    public static double lawOfCosinesDistance_km(double lat1, double lon1, double lat2, double lon2) {
        double φ1 = degreesToRadians(lat1);
        double φ2 = degreesToRadians(lat2);
        double Δλ = degreesToRadians(lon2 - lon1);
        double d = Math.acos(Math.sin(φ1) * Math.sin(φ2) + Math.cos(φ1) * Math.cos(φ2) * Math.cos(Δλ)) * MEAN_EARTH_RADIUS_KM_ACCURATE;

        return d;
    }

}