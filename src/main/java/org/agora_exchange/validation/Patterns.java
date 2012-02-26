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

/**
 * @author Sakari A. Maaranen
 */
public interface Patterns {

    /**
     * Any amount or zero white space.
     */
    public static final String WS = "\\s*";

    /**
     * The decimal dot and at least one decimal digit.
     */
    public static final String DECIMAL_FRACTION = "\\.\\d+";

    public static final String DECIMAL_INTEGER = "(?:0|[1-9]\\d*)";

    /**
     * A decimal number that may contain a fraction part and may contain an
     * exponent part. Leading zero must be present if the value is within
     * interval (-1.0, 1.0).
     */
    public static final String DECIMAL = "-?" + DECIMAL_INTEGER + "(?:"
            + DECIMAL_FRACTION + ")?" + "(?:E-?" + DECIMAL_INTEGER + ")?";

    /**
     * The Unicode 'INFINITY' <code>\u221e</code> or the asterisk character.
     */
    public static final String INFINITY = "[\u221e\\*]";
}
