/*
 * Agora Exchange for Online Managed Services
 *
 * Copyright (C) 2012 Sakari A. Maaranen
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.agora_exchange.validation;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;

import org.agora_exchange.conversion.DimensionalUnit;

import static org.agora_exchange.validation.Interval.BRACKET_LEFT;
import static org.agora_exchange.validation.Interval.BRACKET_RIGHT;
import static org.agora_exchange.validation.Patterns.WS;
import static org.agora_exchange.validation.Patterns.DECIMAL;
import static org.agora_exchange.validation.Patterns.INFINITY;

/**
 * Validates that a numeric value is within one of specified intervals. The unit
 * of measurement must be specified, unless specifying a dimensionless quantity.
 * <p>
 * Accepts both the ISO/IEC 80000-2 outwards-pointing bracket notation and the
 * parenthesis notation for an open endpoint, so all the following are equal:
 * <dl>
 * <dt>]a, b[</dt>
 * <dd>outwards-pointing bracket notation</dd>
 * <dt>(a, b)</dt>
 * <dd>parenthesis notation</dd>
 * <dt>]a, b)</dt>
 * <dt>(a, b[</dt>
 * <dd>mixing works, but should be avoided</dd>
 * </dl>
 * </p>
 * <p>
 * Accepts both the asterisk and the Unicode 'INFINITY' (U+221E) character
 * &#x221E; to denote an infinite endpoint. The minus or plus sign (to denote
 * negative or positive infinity) on either or both sides of the interval may be
 * omitted.
 * </p>
 * <p>
 * The unit of measurement is separated by a colon before the list of intervals.
 * See the rules for the unit of measurement in {@link DimensionalUnit}.
 * </p>
 * 
 * @author Sakari A. Maaranen
 */
public class IntervalsValidator extends AbstractValidator<String> {

    private static final String LEFT = "(?:-?" + INFINITY + "|" + DECIMAL
            + ")";
    private static final String RIGHT = "(?:\\+?" + INFINITY + "|"
            + DECIMAL + ")";

    private static final String CAPTURE_INTERVAL = "(" + BRACKET_LEFT + WS
            + LEFT + WS + "," + WS + RIGHT + WS + BRACKET_RIGHT + ")";

    private static final String CAPTURE_UNIT = "(?:" + "([^"
            + DimensionalUnit.CHARS_FORBIDDEN + "]+):" + ")?";

    /**
     * Note the use of reluctant quantifier.
     */
    private static final PatternValidator listValidator = new PatternValidator(
            CAPTURE_UNIT + WS + "(?:" + WS + CAPTURE_INTERVAL + ")+?");

    private static Double greatestDouble;
    private static Long greatestLong;

    /**
     * @param data
     * @param set
     *            adds the resulting intervals into this set.
     * @return the unit of measurement.
     */
    private static synchronized String parse(Object data,
            Set<QuantityInterval> set) {
        String string = listValidator.normalize(data);
        if (null == string) return null;
        Matcher matcher = listValidator.getPattern().matcher(string);

        QuantityInterval interval = null;

        // Checks the ordering of the list of intervals.
        greatestDouble = -Double.MAX_VALUE;
        greatestLong = Long.MIN_VALUE;

        String unit = null; // the unit of measurement

        while (matcher.find()) {
            if (null == unit) {
                unit = matcher.group(1);
                if (null != unit) {
                    unit = unit.trim();
                }
            }
            interval = new QuantityInterval(matcher.group(2), unit);
            if (set.add(interval)) ;
            else throw new IllegalArgumentException("Duplicate " + interval
                    + " in " + string + ".");

            if (endpointsInOrder(interval)) ;
            else throw new IllegalArgumentException("The interval "
                    + interval + " is not in order in " + string + ".");
        }
        return unit;
    }

    private static synchronized boolean endpointsInOrder(Interval interval) {
        return endpointInOrder(interval, true)
                && endpointInOrder(interval, false);
    }

    private static synchronized boolean endpointInOrder(Interval interval,
            boolean left) {
        String endpoint = left ? interval.getLeft() : interval.getRight();
        Long endpointLong;
        Double endpointDouble;

        if (null == endpoint) { // The endpoint is infinite.
            if (left) {
                endpointLong = Long.MIN_VALUE;
                endpointDouble = -Double.MAX_VALUE;
            } else {
                endpointLong = Long.MAX_VALUE;
                endpointDouble = Double.MAX_VALUE;
            }
        } else { // The endpoint is bounded.
            try {
                endpointLong = Long.valueOf(endpoint);
                endpointDouble = endpointLong.doubleValue();
            } catch (NumberFormatException e) {
                endpointLong = null;
                try {
                    endpointDouble = Double.valueOf(endpoint);
                } catch (NumberFormatException x) {
                    endpointDouble = null;
                    throw new IllegalArgumentException("The "
                            + (left ? "left" : "right") + " endpoint \""
                            + endpoint + "\" is not a number.", x);
                }
            }
        }
        if (null == endpointLong) {
            // Check order by the decimal value of the endpoint.
            if (endpointDouble < greatestDouble) return false;
            else {
                greatestDouble = endpointDouble;
                greatestLong = (endpointDouble > (double) Long.MAX_VALUE) ? Long.MAX_VALUE
                        : (endpointDouble > (double) Long.MIN_VALUE) ? endpointDouble.longValue()
                                : Long.MIN_VALUE;
            }
        } else {
            // Check order by the integer value of the endpoint.
            if (endpointLong < greatestLong) {
                return false;
            } else {
                greatestLong = endpointLong;
                greatestDouble = endpointLong.doubleValue();
            }
        }
        return true;
    }

    private final Set<QuantityInterval> intervals;
    private final DimensionalUnit dimensionalUnit;

    public IntervalsValidator(String intervals) {
        Set<QuantityInterval> set = new LinkedHashSet<QuantityInterval>();
        this.dimensionalUnit = new DimensionalUnit(parse(intervals, set));
        this.intervals = Collections.unmodifiableSet(set);
    }

    /**
     * Returns the unit of measurement. All values are normalised to this unit
     * when validated. For example, if the unit of measurement is bytes, then
     * the expression "1 kilobit" would be validated to "125 bytes".
     * 
     * @return
     */
    public DimensionalUnit getUnit() {
        return dimensionalUnit;
    }

    /**
     * @return <code>true</code> if and only if this validator specifies a unit
     *         of measurement.
     */
    public boolean hasUnit() {
        if (null == getUnit()) return false;
        if ("".equals(getUnit().toString())) {
            return false;
        }
        return true;
    }

    @Override
    public String normalize(Object data) {
        if (null == data) return null;
        String s = data.toString().trim();
        for (QuantityInterval interval : intervals) {
            if (interval.contains(s)) {
                return interval.normalise(s)
                        + (hasUnit() ? " " + getUnit() : "");
            }
        }
        throw new IllegalArgumentException("Not within a valid interval.");
    }
}
