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
 * Ensures that a <code>String</code> can be used as an Agora identifier; an
 * Agora identifier is a short, unique name that should embed nicely in pretty
 * URI's. The name must not need encoding when converted to RFC 3986
 * percent-encoded format and its absolute maximum length is 80 characters; in
 * other words, an Agora identifier may contain RFC 3986 2.3. Unreserved
 * Characters only.
 * 
 * @see <a href="http://tools.ietf.org/html/rfc3986#section-2.1">RFC 3986
 *      Uniform Resource Identifier (URI): Generic Syntax 2.1.
 *      Percent-Encoding</a>
 * @author Sakari A. Maaranen
 */
public final class IdentifierValidator extends PatternValidator {

    private static final IdentifierValidator INSTANCE = new IdentifierValidator();

    private IdentifierValidator() {
        super(false, true, 1, 80, "[\\w-.~]{1,80}");
    }

    /**
     * Returns the <code>IdentifierValidator</code> object.
     * 
     * @return the <code>IdentifierValidator</code> object.
     */
    public static IdentifierValidator getInstance() {
        return INSTANCE;
    }
}
