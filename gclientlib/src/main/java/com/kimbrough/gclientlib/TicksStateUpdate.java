package com.kimbrough.gclientlib;

/**
 * @author Rofaeil Ashaiaa
 *         Created on 04/02/18.
 */

/**
 * are we actually getting the expected ticks,
 * i.e. reflect whether we are currently experiencing the ticks at the rate we expect
 */

public enum TicksStateUpdate {
    //No ticks ever received in this session
    RED,
    //On receipt of a new tick while in green state then stay green (green is renewed)
    GREEN,
    // If no tick received in scaling factor * requested frequency then set to orange
    ORANGE
}
