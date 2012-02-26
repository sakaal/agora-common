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

import java.util.regex.Pattern;

/**
 * Ensures that a <code>String</code> matches a given regular expression.
 * 
 * @see java.util.regex.Pattern
 * @author Sakari A. Maaranen
 */
public class PatternValidator extends AbstractValidator<String> {

    /**
     * Defines an arbitrary limit for sanity checking the maximum supported
     * length of 1000000 characters.
     */
    public static final int MAX_SUPPORTED_LENGTH = 1000000;

    private final boolean nullValid;
    private final boolean trimming;
    private final int minLength;
    private final int maxLength;
    private final Pattern pattern;

    /**
     * @param nullValid
     *            Whether this validator should accept <code>null</code> as
     *            valid. The default is <code>true</code>.
     * @param trimming
     *            Whether to remove leading and trailing white space from the
     *            string (before checking its length). The default is
     *            <code>true</code>.
     * @param minLength
     *            the minimum length of a valid value (checked before matching).
     *            The default is zero (0).
     * @param maxLength
     *            the maximum length of a valid value (checked before matching).
     *            The default is {@link #MAX_SUPPORTED_LENGTH}.
     * @param regex
     *            a regular expression (as specified by {@link Pattern}) to
     *            match valid values.
     * @throws java.util.regex.PatternSyntaxException
     *             if the expression's syntax is invalid.
     */
    public PatternValidator(boolean nullValid, boolean trimming,
            int minLength, int maxLength, String regex) {
        if (minLength < 0 || minLength > maxLength) {
            throw new IllegalArgumentException("Invalid minimum length "
                    + minLength + " while the maximum was " + maxLength
                    + ".");
        }
        if (maxLength > MAX_SUPPORTED_LENGTH) {
            throw new IllegalArgumentException("Maximum length "
                    + maxLength + " is greater than the supported "
                    + MAX_SUPPORTED_LENGTH + ".");
        }
        this.nullValid = nullValid;
        this.trimming = trimming;
        this.minLength = minLength;
        this.maxLength = maxLength;
        this.pattern = Pattern.compile(regex);
        logger.trace("Created a new " + toString() + ".");
    }

    /**
     * @see #PatternValidator(boolean, boolean, int, int, String)
     */
    public PatternValidator(boolean nullValid, boolean trimming,
            int maxLength, String regex) {
        this(nullValid, trimming, 0, maxLength, regex);
    }

    /**
     * @see #PatternValidator(boolean, boolean, int, int, String)
     */
    public PatternValidator(boolean nullValid, boolean trimming,
            String regex) {
        this(nullValid, trimming, 0, MAX_SUPPORTED_LENGTH, regex);
    }

    /**
     * @see #PatternValidator(boolean, boolean, int, int, String)
     */
    public PatternValidator(boolean nullValid, int minLength,
            int maxLength, String regex) {
        this(nullValid, true, minLength, maxLength, regex);
    }

    /**
     * @see #PatternValidator(boolean, boolean, int, int, String)
     */
    public PatternValidator(boolean nullValid, int maxLength, String regex) {
        this(nullValid, true, 0, maxLength, regex);
    }

    /**
     * @see #PatternValidator(boolean, boolean, int, int, String)
     */
    public PatternValidator(boolean nullValid, String regex) {
        this(nullValid, true, 0, MAX_SUPPORTED_LENGTH, regex);
    }

    /**
     * @see #PatternValidator(boolean, boolean, int, int, String)
     */
    public PatternValidator(int minLength, int maxLength, String regex) {
        this(true, true, minLength, maxLength, regex);
    }

    /**
     * @see #PatternValidator(boolean, boolean, int, int, String)
     */
    public PatternValidator(int maxLength, String regex) {
        this(true, true, 0, maxLength, regex);
    }

    /**
     * @see #PatternValidator(boolean, boolean, int, int, String)
     */
    public PatternValidator(String regex) {
        this(true, true, 0, MAX_SUPPORTED_LENGTH, regex);
    }

    @Override
    public String normalize(Object data) {
        if (null == data) {
            if (nullValid) return null;
            throw new IllegalArgumentException("The data was null.");
        }
        String string = data.toString();
        if (trimming) string = string.trim();
        final int len = string.length();
        if (len < minLength) {
            throw new IllegalArgumentException("The string length of "
                    + len + " is below the minimum of " + minLength + ".");
        }
        if (len > maxLength) {
            throw new IllegalArgumentException("The string length of "
                    + len + " is above the maximum of " + maxLength + ".");
        }
        if (pattern.matcher(string).matches()) {
            return string;
        } else {
            throw new IllegalArgumentException("The string \"" + string
                    + "\" does not match the regular expression \""
                    + pattern.toString() + "\".");
        }
    }

    public boolean isNullValid() {
        return nullValid;
    }

    public boolean isTrimming() {
        return trimming;
    }

    public int getMinLength() {
        return minLength;
    }

    public int getMaxLength() {
        return maxLength;
    }

    public Pattern getPattern() {
        return pattern;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(nullValid=" + nullValid
                + ",trimming=" + trimming + ",minLength=" + minLength
                + ",maxLength=" + maxLength + ",pattern=\"" + pattern
                + "\")";
    }
}
