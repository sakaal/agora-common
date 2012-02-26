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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.agora_exchange.conversion.DimensionalUnit;

import static org.agora_exchange.validation.Patterns.DECIMAL;
import static org.agora_exchange.validation.Patterns.WS;

/**
 * An interval that also specifies its unit of measurement.
 * 
 * @author Sakari A. Maaranen
 */
public class QuantityInterval extends Interval {

    private static final Pattern QUANTITY = Pattern.compile("(" + DECIMAL
            + ")" + WS + "([^" + DimensionalUnit.CHARS_FORBIDDEN + "]*)");

    private final DimensionalUnit unit;

    /**
     * Creates a new interval of plain quantities; the unit of measurement is 1
     * that is denoted by an empty string.
     * 
     * @param leftOpen
     * @param left
     * @param right
     * @param rightOpen
     */
    public QuantityInterval(boolean leftOpen, String left, String right,
            boolean rightOpen) {
        this(leftOpen, left, right, rightOpen, null);
    }

    /**
     * Creates a new interval of quantities with the specified unit of
     * measurement. If the given unit is <code>"1"</code>, it will be interpret
     * as an empty string that means the same.
     * 
     * @param leftOpen
     * @param left
     * @param right
     * @param rightOpen
     * @param unit
     */
    public QuantityInterval(boolean leftOpen, String left, String right,
            boolean rightOpen, String unit) {
        super(leftOpen, left, right, rightOpen);
        this.unit = parseUnit(unit);
        if (null == unit) {
            throw new IllegalArgumentException(
                    "The unit of measurement must be specified"
                            + " (even if it's 1 or empty).");
        }
    }

    private static DimensionalUnit parseUnit(String unit) {
        return new DimensionalUnit(unit);
    }

    public QuantityInterval(String interval, String unit) {
        super(interval);
        this.unit = parseUnit(unit);
    }

    /**
     * Returns the unit of measurement.
     * 
     * @return
     */
    public DimensionalUnit getUnitOfMeasurement() {
        return unit;
    }

    @Override
    public String toString() {
        return super.toString() + this.getUnitOfMeasurement();
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (null == obj) return false;

        if (getClass().equals(obj.getClass())) ;
        else return false;
        QuantityInterval other = (QuantityInterval) obj;

        return toString().equals(other.toString());
    }

    /**
     * Returns <code>true</code> if and only if the given object's
     * <code>String</code> representation can be interpret as an integer or a
     * floating point value that falls within this interval.
     * 
     * @param obj
     * @return
     */
    @Override
    public boolean contains(Object obj) {
        if (null == obj) return false;
        String rep = obj.toString();
        Matcher m = QUANTITY.matcher(rep);
        if (m.find()) {
            obj = m.group(1);
            String label = m.group(2);
            DimensionalUnit given = new DimensionalUnit(label);
            double ratio = given.to(getUnitOfMeasurement());
            if (ratio != 1) obj = Double.valueOf((String) obj) * ratio;
        } else throw new IllegalArgumentException(
                "The unit of measurement must be compatible to: "
                        + getUnitOfMeasurement());
        return super.contains(obj);
    }

    public Number normalise(Object obj) {
        String rep = obj.toString();
        Matcher m = QUANTITY.matcher(rep);
        if (m.find()) {
            String value = m.group(1);
            String label = m.group(2);
            DimensionalUnit given = new DimensionalUnit(label);
            double ratio = given.to(getUnitOfMeasurement());
            Double v = Double.valueOf(value) * ratio;

            // Return an integer or a floating point value as needed.
            if (v.equals(Math.ceil(v)) && v <= Long.MAX_VALUE
                    && v >= Long.MIN_VALUE) {
                return v.longValue();
            } else {
                return v;
            }
        } else throw new IllegalArgumentException(
                "The unit of measurement must be compatible to: "
                        + getUnitOfMeasurement());
    }
}
