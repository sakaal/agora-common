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
package org.agora_exchange.conversion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <ul>
 * <li>Multiple terms must be separated by a multiplication operator or a
 * division operator.</li>
 * <li>No more than one division operator is allowed. More than one
 * multiplicative terms following the division operator must be parenthesised.</li>
 * <li>Exponents must be expressed using the Unicode superscript digits, the
 * superscript minus symbol, the words "square" or "cubic" before the label, or
 * the word "squared" after the label. If any of the words is used, it must be
 * separated by a single space.</li>
 * <li>Any whitespace other than intermediate nonconsecutive ASCII space
 * characters (in unit labels) is prohibited.</li>
 * <li>Kelvins must be used instead of &#xB0;C, &#xB0;F or &#xB0;R for
 * expressing absolute or relative temperatures. Please use kelvins even for
 * everyday purposes like describing weather etc.</li>
 * </ul>
 * TODO:
 * <ul>
 * <li>Percent and permil</li>
 * <li>The degree symbol &#xB0; when not followed by a letter is &pi;/180
 * radians. The prime symbol (or a single quote) after a number is understood as
 * 1/60 of a degree. The double prime symbol (or a double quote) after a number
 * is 1/3600 of a degree.</li>
 * </ul>
 * 
 * @author Sakari A. Maaranen
 */
public class DimensionalUnit {

    /**
     * Symbols for SI derived units formed by multiplication are joined with a
     * centre dot or a non-breaking space. This implementation also allows the
     * Unicode multiplication sign, the dot operator, and the asterisk.
     */
    public static final String CHARS_MULTIPLICATION = ""
            + "\u00A0\u00B7\u00D7\u22C5*";

    /**
     * Division operators supported are the (forward) slash, Unicode division
     * sign, fraction slash (solidus) and division slash.
     */
    public static final String CHARS_DIVISION = "/\u00F7\u2044\u2215";

    private static final Pattern MULTIPLICATIVE_TERMS = Pattern.compile("["
            + CHARS_MULTIPLICATION + "]");

    public static final String CHARS_FORBIDDEN = "\\t\\n\\x0B\\f\\r"
            + "!\"&`+,\\-.:;<=>?@[\\\\]^'{|}~";

    private static final String NO_DIV = "[^" + CHARS_DIVISION
            + CHARS_FORBIDDEN + "]";

    private static final String NO_OP = "[^" + CHARS_MULTIPLICATION
            + CHARS_DIVISION + CHARS_FORBIDDEN + "()]";

    private static final String CAPTURE_NUMERATOR = "(" + NO_DIV + "*+)";

    private static final String CAPTURE_DENOMINATOR = "(?:\\((" + NO_DIV
            + "+)\\)|(" + NO_OP + "+))";

    private static final String CAPTURE_FRACTION = "^" + CAPTURE_NUMERATOR
            + "(?:" + "[" + CHARS_DIVISION + "]" + CAPTURE_DENOMINATOR
            + ")?$";

    private static final Pattern FRACTION = Pattern.compile(CAPTURE_FRACTION);

    // INSTANCE

    private final List<Factor> factors = new ArrayList<Factor>();

    private final double value;

    private static void parse(String expression, List<Factor> factors) {
        Matcher m = FRACTION.matcher(expression);
        String numerator, denominator;
        if (m.find()) {
            numerator = m.group(1);
            denominator = m.group(2);
            if (null == denominator) denominator = m.group(3);
        } else throw new IllegalArgumentException("Invalid expression: \""
                + expression + "\"");

        for (String term : MULTIPLICATIVE_TERMS.split(numerator)) {
            factors.add(new Factor(term, 1));
        }
        if (null != denominator) {
            for (String term : MULTIPLICATIVE_TERMS.split(denominator)) {
                factors.add(new Factor(term, -1));
            }
        }
    }

    /**
     * Creates a new dimensional unit guessing whether to use binary or decimal
     * SI prefixes. If any factor within the expression has a binary prefix,
     * then those will be assumed for the returned unit.
     * 
     * @param expression
     */
    public DimensionalUnit(String expression) {
        if ((null == expression) || expression.isEmpty()) {
            this.value = 1;
            return;
        }
        parse(expression, factors);
        boolean binary = false;
        for (Factor f : factors) {
            String used = f.getPrefix();
            if (null == used || "".equals(used)) {

            } else {
                for (Factor p : Factor.PREFIXES_BINARY) {
                    String prefix = p.getPrefix();
                    if (null == prefix || "".equals(prefix)) {
                        // skip
                    } else if (used.startsWith(prefix)) {
                        binary = true;
                        break;
                    }
                }
            }
        }
        this.value = Factor.simplify(factors,
                binary ? Factor.PREFIXES_BINARY : Factor.PREFIXES_SI);
    }

    public DimensionalUnit(String expression, List<Factor> prefixes) {
        if ((null == expression) || expression.isEmpty()) {
            this.value = 1;
            return;
        }
        parse(expression, factors);
        this.value = Factor.simplify(factors, prefixes);
    }

    public double getValue() {
        return value;
    }

    /**
     * Returns the list of factors of this dimensional unit.
     * 
     * @return an unmodifiable list.
     */
    public List<Factor> getFactors() {
        return Collections.unmodifiableList(factors);
    }

    /**
     * Returns the scalar conversion rate that should be applied to a value of
     * this unit to convert it into a value of a given unit.
     * 
     * @param other
     * @return
     * @throws java.lang.IllegalArgumentException
     *             if the result is not scalar, i.e. the units have different
     *             dimensions.
     */
    public double to(DimensionalUnit other) {
        List<Factor> all = new ArrayList<Factor>(factors);
        for (Factor factor : other.getFactors()) {
            all.add(new Factor(factor, -1));
        }
        double ratio = Factor.normalise(all);
        if (all.isEmpty()) return ratio;
        throw new IllegalArgumentException("Conversion from " + this
                + " to " + other + " has nonscalar dimension: "
                + Factor.toString(all));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        final long temp = Double.doubleToLongBits(value);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        for (Factor factor : factors) {
            /*
             * The ordering of factors must not affect the hash code. Otherwise
             * the hash code would not be consistent with equals. The Factor
             * class hashCode() method should be robust (and unlikely to return
             * -1, 0 or 1), so just multiplying the return values should work
             * reasonably well.
             */
            result *= factor.hashCode();
        }
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        try {
            DimensionalUnit other;
            if (obj instanceof DimensionalUnit) {
                if (this == obj) return true;
                other = (DimensionalUnit) obj;
            } else return false;
            return 1d == this.to(other);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public String toString() {
        return ((1 == getValue()) ? "" : getValue() + " ")
                + Factor.toString(getFactors());
    }
}
