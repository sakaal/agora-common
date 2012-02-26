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

import static org.agora_exchange.validation.Patterns.DECIMAL;
import static org.agora_exchange.validation.Patterns.INFINITY;
import static org.agora_exchange.validation.Patterns.WS;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Sakari A. Maaranen
 */
public class Interval {

    static final String BRACKET_LEFT = "[\\[\\](]";
    static final String BRACKET_RIGHT = "[\\[\\])]";

    private static final String CAPTURE_LEFT = "(-?" + INFINITY + "|"
            + DECIMAL + ")";
    private static final String CAPTURE_RIGHT = "(\\+?" + INFINITY + "|"
            + DECIMAL + ")";

    private static final String INTERVAL = BRACKET_LEFT + WS + CAPTURE_LEFT
            + WS + "," + WS + CAPTURE_RIGHT + WS + BRACKET_RIGHT;

    private static final Pattern PATTERN_INTERVAL = Pattern.compile(INTERVAL);
    private static final Pattern PATTERN_INFINITY = Pattern.compile("[+-]?"
            + INFINITY);

    private final String left;
    private final boolean leftOpen;
    private final String right;
    private final boolean rightOpen;

    private transient String representation = null;

    public Interval(boolean leftOpen, String left, String right,
            boolean rightOpen) {
        this.leftOpen = leftOpen;
        this.left = left;
        this.right = right;
        this.rightOpen = rightOpen;
    }

    private static String endpoint(String endpoint) {
        return PATTERN_INFINITY.matcher(endpoint).matches() ? null
                : endpoint;
    }

    public Interval(String expression) {
        Matcher matcher = PATTERN_INTERVAL.matcher(expression);
        if (matcher.matches()) {
            left = endpoint(matcher.group(1));
            right = endpoint(matcher.group(2));
        } else {
            throw new IllegalArgumentException("Not a valid interval: \""
                    + expression + "\"");
        }
        char bracket;
        switch (bracket = expression.charAt(0)) {
        case '[': // The left endpoint is closed.
            if (isLeftBounded()) {
                leftOpen = false;
            } else throw new IllegalArgumentException(
                    "Ambiguous left endpoint is infinite and closed: "
                            + expression);
            break;
        case ']': // The left endpoint is open.
        case '(':
            leftOpen = true;
            break;
        default:
            throw new IllegalArgumentException("Unsupported left bracket '"
                    + bracket + "'.");
        }
        switch (bracket = expression.charAt(expression.length() - 1)) {
        case ']': // The right endpoint is closed.
            if (isRightBounded()) {
                rightOpen = false;
            } else throw new IllegalArgumentException(
                    "Ambiguous right endpoint is infinite and closed: "
                            + expression);
            break;
        case '[': // The right endpoint is open.
        case ')':
            rightOpen = true;
            break;
        default:
            throw new IllegalArgumentException(
                    "Unsupported right bracket '" + expression + "'.");
        }
    }

    public String getLeft() {
        return left;
    }

    public boolean isLeftBounded() {
        return null != left;
    }

    public boolean isLeftOpen() {
        return leftOpen;
    }

    public String getRight() {
        return right;
    }

    public boolean isRightBounded() {
        return null != right;
    }

    public boolean isRightOpen() {
        return rightOpen;
    }

    public boolean isBounded() {
        return isLeftBounded() && isRightBounded();
    }

    public boolean isHalfBounded() {
        return isLeftBounded() ^ isRightBounded();
    }

    @Override
    public String toString() {
        if (null == representation) {
            representation = (isLeftOpen() ? "(" : "[")
                    + (isLeftBounded() ? getLeft() : "-\u221e") + ", "
                    + (isRightBounded() ? getRight() : "+\u221e")
                    + (isRightOpen() ? ")" : "]");
        }
        return representation;
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
        Interval other = (Interval) obj;

        return toString().equals(other.toString());
    }

    static Number toNumber(String value) {
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException e) {
            try {
                return Double.valueOf(value);
            } catch (NumberFormatException x) {
                throw new IllegalArgumentException("The value \"" + value
                        + "\" is not a number.", x);
            }
        }
    }

    /**
     * Returns <code>true</code> if and only if the given object's
     * <code>String</code> representation can be interpret as an integer or a
     * floating point value that falls within this interval.
     * 
     * @param obj
     * @return
     */
    public boolean contains(Object obj) {
        if (null == obj) return false;
        Number value;
        if (obj instanceof Number) {
            value = (Number) obj;
        } else {
            value = toNumber(obj.toString());
        }
        Number endpoint;

        if (isLeftBounded()) {
            endpoint = toNumber(getLeft());

            if (isLeftOpen() ? value.doubleValue() > endpoint.doubleValue()
                    : value.doubleValue() >= endpoint.doubleValue()) ;
            else return false;
        }
        if (isRightBounded()) {
            endpoint = toNumber(getRight());

            if (isRightOpen() ? value.doubleValue() < endpoint.doubleValue()
                    : value.doubleValue() <= endpoint.doubleValue()) ;
            else return false;
        }
        return true;
    }
}
