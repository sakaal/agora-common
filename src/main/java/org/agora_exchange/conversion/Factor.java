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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An individual multiplicative term in an expression representing a product of
 * variables with integer exponents.
 * 
 * @see <a href="http://en.wikipedia.org/wiki/International_System_of_Units"
 *      >International System of Units</a>
 * 
 * @author Sakari A. Maaranen
 */
public class Factor implements Comparable<Factor> {

    public static final String[] LABELS_METRE = { "metres", "meters",
            "metre", "meter", "m" };
    public static final String[] LABELS_GRAM = { "grams", "gram", "g" };
    public static final String[] LABELS_SECOND = { "seconds", "second", "s" };
    public static final String[] LABELS_AMPERE = { "amperes", "ampere", "A" };
    public static final String[] LABELS_KELVIN = { "kelvins", "kelvin",
            "\u212A", "K" };
    public static final String[] LABELS_CANDELA = { "candelas", "candela",
            "cd" };
    public static final String[] LABELS_MOLE = { "moles", "mole", "mol" };
    public static final String[] LABELS_BYTE = { "bytes", "byte", "B" };
    public static final String[] LABELS_BIT = { "bits", "bit" };

    private static final String[][] LABELS = { LABELS_METRE, LABELS_GRAM,
            LABELS_SECOND, LABELS_AMPERE, LABELS_KELVIN, LABELS_CANDELA,
            LABELS_MOLE, LABELS_BYTE, LABELS_BIT };

    private static final String CAPTURE_SUPPORTED_LABEL;

    static {
        StringBuffer buf = new StringBuffer("(");
        for (String[] symbols : LABELS) {
            for (String symbol : symbols) {
                buf.append(symbol).append('|');
            }
        }
        buf.setCharAt(buf.length() - 1, ')');
        CAPTURE_SUPPORTED_LABEL = buf.toString();
    }

    public static final double YOCTO = 1E-24;
    public static final double ZEPTO = 1E-21;
    public static final double ATTO = 1E-18;
    public static final double FEMTO = 1E-15;
    public static final double PICO = 1E-12;
    public static final double NANO = 1E-9;
    public static final double MICRO = 1E-6;
    public static final double MILLI = 1E-3;
    public static final double CENTI = 1E-2;
    public static final double DECI = 1E-1;
    public static final int DECA = (int) 1E1;
    public static final int HECTO = (int) 1E2;
    public static final int KILO = (int) 1E3;
    public static final int KIBI = 1 << 10;
    public static final int MEGA = (int) 1E6;
    public static final int MEBI = 1 << 20;
    public static final long GIGA = (long) 1E9;
    public static final long GIBI = 1L << 30;
    public static final long TERA = (long) 1E12;
    public static final long TEBI = 1L << 40;
    public static final long PETA = (long) 1E15;
    public static final long PEBI = 1L << 50;
    public static final double EXA = 1E18;
    public static final double EXBI = 1L << 60;
    public static final double ZETTA = 1E21;
    public static final double ZEBI = (double) GIBI * TEBI;
    public static final double YOTTA = 1E24;
    public static final double YOBI = (double) TEBI * TEBI;

    /**
     * Contains the supported prefixes as factors of one; for example, the
     * Factor object for kilo is 1000/k that equals 1.
     */
    public static final List<Factor> PREFIXES_SI = Collections.unmodifiableList(Arrays.asList(new Factor[] {
            new Factor(YOCTO, "y", null, -1),
            new Factor(ZEPTO, "z", null, -1),
            new Factor(ATTO, "a", null, -1),
            new Factor(FEMTO, "f", null, -1),
            new Factor(PICO, "p", null, -1),
            new Factor(NANO, "n", null, -1),
            new Factor(MICRO, "\u00B5", null, -1),
            new Factor(MILLI, "m", null, -1),
            new Factor(CENTI, "c", null, -1),
            new Factor(DECI, "d", null, -1), new Factor(1, null, null, -1),
            new Factor(DECA, "da", null, -1),
            new Factor(HECTO, "h", null, -1),
            new Factor(KILO, "k", null, -1),
            new Factor(MEGA, "M", null, -1),
            new Factor(GIGA, "G", null, -1),
            new Factor(TERA, "T", null, -1),
            new Factor(PETA, "P", null, -1),
            new Factor(EXA, "E", null, -1),
            new Factor(ZETTA, "Z", null, -1),
            new Factor(YOTTA, "Y", null, -1) }));

    public static final List<Factor> PREFIXES_BINARY = Collections.unmodifiableList(Arrays.asList(new Factor[] {
            new Factor(1, null, null, -1),
            new Factor(KIBI, "Ki", null, -1),
            new Factor(MEBI, "Mi", null, -1),
            new Factor(GIBI, "Gi", null, -1),
            new Factor(TEBI, "Ti", null, -1),
            new Factor(PEBI, "Pi", null, -1),
            new Factor(EXBI, "Ei", null, -1),
            new Factor(ZEBI, "Zi", null, -1),
            new Factor(YOBI, "Yi", null, -1) }));

    /**
     * The character that is used for separating multiplicative terms.
     */
    private static final char CHAR_MULTI = '\u00B7';

    private static final String CAPTURE_EXP_PRE = "(square |cubic )?";

    /**
     * Longer prefixes must come before shorter ones, so that the longer ones
     * are matched first.
     */
    private static final String CAPTURE_PREFIX = "(yocto|zepto|atto|femto|pico|"
            + "nano|micro|milli|centi|deci|deca|hecto|kilo|mega|giga|tera|peta|"
            + "exa|zetta|yotta|kibi|mebi|tebi|pebi|exbi|zebi|yobi|"
            + "y|z|a|f|p|n|\u00B5|m|c|da|d|h|"
            + "Ki|k|Mi|M|Gi|G|Ti|T|Pi|P|Ei|E|Zi|Z|Yi|Y)?";

    private static final String EXP_CHARS = "\u2070\u00B9\u00B2\u00B3\u2074\u2075\u2076\u2077\u2078\u2079\u207B";

    private static final String LABEL_CHARS = "[^"
            + "\\[\\](){}\\s\u00A0\u2044/%\u2030\u00B7\u00D7\u22C5"
            + EXP_CHARS + ",.\\-+*:;" + "]+";

    private static final Pattern PREFIXED_UNKNOWN = Pattern.compile("^"
            + CAPTURE_PREFIX + "(" + LABEL_CHARS + ")$");

    private static final Pattern KNOWN_LABEL = Pattern.compile("^"
            + CAPTURE_PREFIX + CAPTURE_SUPPORTED_LABEL + "$");

    /**
     * The label may contain intermediate nonconsecutive ASCII space. We have to
     * match the label first (before matching the prefix), because otherwise
     * "metres" would produce "m" and "etres" (that would be 'milli-etres').
     */
    private static final String CAPTURE_PREFIXED_LABEL = "(" + LABEL_CHARS
            + "(?: " + LABEL_CHARS + ")*" + ")";

    private static final String CAPTURE_EXP_POST = "(\u207B?["
            + "\u2070\u00B9\u00B2\u00B3\u2074\u2075\u2076\u2077\u2078\u2079"
            + "]+| squared)?";

    private static final Pattern FACTOR = Pattern.compile("^"
            + CAPTURE_EXP_PRE + CAPTURE_PREFIXED_LABEL + CAPTURE_EXP_POST
            + "$");

    /**
     * Assuming that the given prefix is a valid SI or IEC 80000-13:2008 prefix,
     * converts it to a corresponding factor. Does not verify the prefix.
     * <p>
     * Note that the IEEE 754 standard double precision floating point format
     * supports a 52-bit fraction. Exact calculations using double precision
     * floating point factors together with 64-bit long integer values should
     * work up to peta binary scale (i.e. with values strictly less than 8192
     * peta binary). Factors of exa, exbi, zetta, zebi, yotta and yobi scale are
     * beyond 52 bits precision and may prove tricky when used with exact
     * integers; for example:
     * <ul>
     * <li>Rounding error causes <code>(long)((double)EXA+1)</code> to be
     * different from <code>(long)EXA+1</code>.</li>
     * <li>Overflow in the two's complement (signed) arithmetic causes
     * <code>8*(long)EXBI</code> to be negative&mdash;same as
     * <code>8192*(long)PEBI</code>.</li>
     * </ul>
     * </p>
     * 
     * @param prefix
     * @return
     */
    public static double parsePrefix(String prefix) {
        if (null == prefix || prefix.isEmpty()) return 1;
        switch (prefix.charAt(0)) {

        case 'k': // k, kilo, kibi
            return (prefix.endsWith("i")) ? KIBI : KILO;

        case 'm': // m, milli, micro, mega, mebi
            if (prefix.length() > 3) {
                switch (prefix.charAt(2)) {
                case 'g':
                    return MEGA;
                case 'l':
                    break;
                case 'c':
                    return MICRO;
                default:
                    return MEBI;
                }
            }
            return MILLI;

        case 'K': // Ki
            return KIBI;

        case 'M': // Mi, M
            return (prefix.length() > 1) ? MEBI : MEGA;

        case 'G': // Gi, G
            return (prefix.length() > 1) ? GIBI : GIGA;

        case 'T': // Ti, T
        case 't': // tera, tebi
            return (prefix.endsWith("i")) ? TEBI : TERA;

        case 'g': // giga
            return GIGA;

        case 'h': // h, hecto
            return HECTO;

        case 'c': // c, centi
            return CENTI;

        case 'd': // d, deci, da, deca
            return (prefix.endsWith("a")) ? DECA : DECI;

        case '\u00B5':
            return MICRO;

        case 'n': // n, nano
            return NANO;

        case 'p': // p, pico, peta, pebi
            if (prefix.length() > 3) {
                switch (prefix.charAt(2)) {
                case 't':
                    return PETA;
                case 'c':
                    break;
                default:
                    return PEBI;
                }
            }
            return PICO;

        case 'P': // Pi, P
            return (prefix.length() > 1) ? PEBI : PETA;

        case 'e': // exbi, exa
        case 'E': // Ei, E
            return (prefix.endsWith("i")) ? EXBI : EXA;

        case 'f': // f, femto
            return FEMTO;

        case 'a': // a, atto
            return ATTO;

        case 'Z': // Zi, Z
            return (prefix.length() > 1) ? ZEBI : ZETTA;

        case 'Y': // Yi, Y
            return (prefix.length() > 1) ? YOBI : YOTTA;

        case 'z': // z, zepto, zetta, zebi
            if (prefix.length() > 3) {
                switch (prefix.charAt(2)) {
                case 't':
                    return ZETTA;
                case 'p':
                    break;
                default:
                    return ZEBI;
                }
            }
            return ZEPTO;

        case 'y': // y, yocto, yotta, yobi
            if (prefix.length() > 3) {
                switch (prefix.charAt(2)) {
                case 't':
                    return YOTTA;
                case 'c':
                    break;
                default:
                    return YOBI;
                }
            }
            return YOCTO;

        default:
            throw new IllegalArgumentException("Unknown prefix: \""
                    + prefix + "\"");
        }
    }

    public static int parseExponent(String exp) {
        if (null == exp || exp.isEmpty()) return 1;
        if (exp.contains("square")) return 2;
        if (exp.startsWith("cubic")) return 3;
        int e = 0, p = 1, i = exp.length();
        while (true) {
            switch (exp.charAt(--i)) {
            case '\u207B':
                e *= -1;
                break;
            case '\u2070':
                break;
            case '\u00B9':
                e += 1 * p;
                break;
            case '\u00B2':
                e += 2 * p;
                break;
            case '\u00B3':
                e += 3 * p;
                break;
            case '\u2074':
                e += 4 * p;
                break;
            case '\u2075':
                e += 5 * p;
                break;
            case '\u2076':
                e += 6 * p;
                break;
            case '\u2077':
                e += 7 * p;
                break;
            case '\u2078':
                e += 8 * p;
                break;
            case '\u2079':
                e += 9 * p;
                break;
            }
            if (i > 0) p *= 10;
            else break;
        }
        return e;
    }

    public static String formatExponent(int exp) {
        StringBuffer buf = new StringBuffer(String.valueOf(exp));

        for (int i = 0; i < buf.length(); i++) {
            switch (buf.charAt(i)) {
            case '-':
                buf.setCharAt(i, '\u207B');
                break;
            case '0':
                buf.setCharAt(i, '\u2070');
                break;
            case '1':
                buf.setCharAt(i, '\u00B9');
                break;
            case '2':
                buf.setCharAt(i, '\u00B2');
                break;
            case '3':
                buf.setCharAt(i, '\u00B3');
                break;
            case '4':
                buf.setCharAt(i, '\u2074');
                break;
            case '5':
                buf.setCharAt(i, '\u2075');
                break;
            case '6':
                buf.setCharAt(i, '\u2076');
                break;
            case '7':
                buf.setCharAt(i, '\u2077');
                break;
            case '8':
                buf.setCharAt(i, '\u2078');
                break;
            case '9':
                buf.setCharAt(i, '\u2079');
                break;
            }
        }
        return buf.toString();
    }

    /**
     * Compares two factors according to their values only. Does not care about
     * the prefix, symbol or exponent.
     */
    public static final Comparator<Factor> COMPARATOR_VALUE = new Comparator<Factor>() {
        @Override
        public int compare(Factor a, Factor b) {
            final double f = a.getValue(), g = b.getValue();
            return (f < g) ? -1 : (f > g) ? 1 : 0;
        }
    };

    /**
     * Finds the most suitable prefix (returned as a <code>Factor</code> object)
     * for the given value when the prefix is raised to the given exponent.
     * 
     * @param value
     * @param exponent
     *            an exponent to apply to the prefix.
     * @param prefixes
     *            The list of prefixes to use must be sorted in ascending order
     *            according to the factor value.
     * @return
     */
    public static Factor forValue(double value, int exponent,
            List<Factor> prefixes) {
        final int i = Collections.binarySearch(prefixes, new Factor(
                Math.pow(value, (1d / exponent)), null, null, -1),
                COMPARATOR_VALUE);
        if (i > -1) { // exact match
            return prefixes.get(i);
        } else if (i < -1) { // between exact matches
            return prefixes.get(-(i + 2));
        } else { // below the smallest prefix
            return prefixes.get(0);
        }
    }

    /**
     * Creates a normalised copy of the given factor setting the value to 1 and
     * removing any prefix.
     * 
     * @param factor
     * @return
     */
    public static Factor normalise(Factor factor) {
        return new Factor(1, "", factor.getSymbol(), factor.getExponent());
    }

    /**
     * Creates a simplified copy of the given factor normalising the value to 1
     * and setting a prefix that would suit the given factor.
     * 
     * @param factor
     * @param prefixes
     * @return
     */
    public static Factor simplify(Factor factor, List<Factor> prefixes) {
        return new Factor(1, Factor.forValue(factor.getFactor(),
                factor.getExponent(), prefixes).getPrefix(),
                factor.getSymbol(), factor.getExponent());
    }

    /**
     * Combines occurrences of the same variable.
     * 
     * @return
     */
    private static Map<String, Factor> factorLabel(List<Factor> factors) {
        final Map<String, Factor> map = new LinkedHashMap<String, Factor>();
        for (Factor factor : factors) {
            if (map.containsKey(factor.getSymbol())) {
                Factor previous = map.get(factor.getSymbol());
                map.put(factor.getSymbol(), new Factor(factor, previous));
            } else {
                map.put(factor.getSymbol(), factor);
            }
        }
        return map;
    }

    /**
     * Returns the combined factor of all variables. Combines occurrences of the
     * same variable cancelling out any whose exponent is left at zero. Moves
     * any factors with negative exponents after the factors with positive
     * exponents, otherwise preserving their order.
     * 
     * @return
     */
    public static double simplify(List<Factor> factors,
            List<Factor> prefixes) {
        Map<String, Factor> map = factorLabel(factors);
        List<Factor> numerator = new ArrayList<Factor>();
        List<Factor> denominator = new ArrayList<Factor>();
        double value = 1;
        for (Factor factor : map.values()) {
            final Factor simplified = Factor.simplify(factor, prefixes);
            value *= factor.getFactor() / simplified.getFactor();
            if (factor.getExponent() > 0) numerator.add(simplified);
            else if (factor.getExponent() < 0) denominator.add(simplified);
            else ; // cancel out
        }
        factors.clear();
        factors.addAll(numerator);
        factors.addAll(denominator);
        return value;
    }

    public static double normalise(List<Factor> factors) {
        Map<String, Factor> map = factorLabel(factors);
        List<Factor> numerator = new ArrayList<Factor>();
        List<Factor> denominator = new ArrayList<Factor>();
        double value = 1;
        for (Factor factor : map.values()) {
            final Factor normalised = Factor.normalise(factor);
            value *= factor.getFactor();
            if (factor.getExponent() > 0) numerator.add(normalised);
            else if (factor.getExponent() < 0) denominator.add(normalised);
            else ; // cancel out
        }
        factors.clear();
        factors.addAll(numerator);
        factors.addAll(denominator);
        return value;
    }

    /**
     * Returns the supported synonyms of the given unit label.
     * 
     * @param label
     * @return
     */
    private static String[] getLabels(String label) {
        if (null == label) label = "";
        for (String[] symbols : LABELS) {
            for (String alias : symbols) {
                if (alias.equals(label)) return symbols;
            }
        }
        return new String[] { label };
    }

    /**
     * Returns the normal symbol for the given label.
     * 
     * @param label
     * @return
     */
    public static String getSymbol(String label) {
        final String[] labels = getLabels(label);
        return labels[labels.length - 1];
    }

    // INSTANCE

    private final double value;
    private final String prefix;
    private final String symbol;
    private final int exponent;

    /**
     * Raises the given factor to the given exponent.
     * 
     * @param factor
     * @param exponent
     *            a multiplier to the exponent, usually 1 or -1.
     */
    public Factor(Factor factor, int exponent) {
        this.value = factor.getValue();
        this.prefix = factor.getPrefix();
        this.symbol = factor.getSymbol();
        this.exponent = factor.getExponent() * exponent;
    }

    /**
     * @param expression
     *            an expression that may contain a prefix and an exponent.
     * @param exponent
     *            a multiplier to the exponent, usually 1 or -1.
     */
    public Factor(String expression, int exponent) {
        final Matcher factorRegex = FACTOR.matcher(expression);
        if (factorRegex.find()) {
            String exp = factorRegex.group(1);
            this.value = 1;

            String prefixedSymbol = factorRegex.group(2);

            Matcher symbolRegex = KNOWN_LABEL.matcher(prefixedSymbol);
            if (symbolRegex.find()) {
                String str = symbolRegex.group(1);
                this.prefix = (null == str) ? "" : str;
                this.symbol = getSymbol(symbolRegex.group(2));
            } else {
                symbolRegex = PREFIXED_UNKNOWN.matcher(prefixedSymbol);
                if (symbolRegex.find()) {
                    String pre = symbolRegex.group(1);
                    this.prefix = (null == pre) ? "" : pre;
                    this.symbol = symbolRegex.group(2);
                } else {
                    this.prefix = "";
                    this.symbol = prefixedSymbol;
                }
            }

            if (null == exp) {
                exp = factorRegex.group(3);
            } else throw new IllegalArgumentException(
                    "Duplicate exponent: \"" + expression + "\"");
            this.exponent = parseExponent(exp) * exponent;
        } else throw new IllegalArgumentException("Not a valid factor: \""
                + expression + "\"");
    }

    public Factor(Factor a, Factor b) {
        if (a.symbolEquals(b)) {
            this.value = a.getFactor() * b.getFactor();
            this.prefix = "";
            this.symbol = a.getSymbol();
            this.exponent = a.getExponent() + b.getExponent();
        } else throw new IllegalArgumentException("Different symbols: "
                + a.getSymbol() + CHAR_MULTI + b.getSymbol());
    }

    public Factor(Factor a, Factor b, List<Factor> prefixes) {
        if (a.symbolEquals(b)) {
            this.symbol = a.getSymbol();
            this.exponent = a.getExponent() + b.getExponent();
            double value = a.getFactor() * b.getFactor();
            final Factor prefix = Factor.forValue(value, exponent, prefixes);
            this.value = value / prefix.getFactor();
            this.prefix = prefix.getPrefix();
        } else throw new IllegalArgumentException("Different symbols: "
                + a.getSymbol() + CHAR_MULTI + b.getSymbol());
    }

    private Factor(double value, String prefix, String symbol, int exponent) {
        this.value = value;
        this.prefix = (null == prefix) ? "" : prefix;
        this.symbol = (null == symbol) ? "" : symbol;
        this.exponent = exponent;
    }

    public double getValue() {
        return value;
    }

    public String getPrefix() {
        return prefix;
    }

    /**
     * Returns the value multiplied by the exponentiated prefix.
     * 
     * @return
     */
    public double getFactor() {
        int e = getExponent();
        final double nominal;
        if (e < 0) {
            nominal = 1 / parsePrefix(getPrefix());
            e = -e;
        } else {
            nominal = parsePrefix(getPrefix());
        }
        double factor = 1;
        while (e-- > 0)
            factor *= nominal;
        return getValue() * factor;
    }

    public String getSymbol() {
        return symbol;
    }

    public int getExponent() {
        return exponent;
    }

    @Override
    public String toString() {
        return toString(1);
    }

    /**
     * Produces a <code>String</code> representation of a product of one or more
     * factors.
     * 
     * @param factors
     *            a list of multiplicative terms where all factors with negative
     *            exponents must appear after those with positive exponents.
     * @return
     */
    public static String toString(List<Factor> factors) {
        StringBuffer unit = new StringBuffer();
        Iterator<Factor> i = factors.iterator();
        boolean multiply = false;
        all: while (i.hasNext()) {
            Factor factor = i.next();
            if (factor.getExponent() < 0) {
                unit.append("/");
                do {
                    unit.append(factor.toString(-1));
                    if (i.hasNext()) factor = i.next();
                    else break all;
                    unit.append(CHAR_MULTI);
                } while (factor.getExponent() < 0);
                throw new IllegalStateException(
                        "Positive exponent after negative: " + factor);
            }
            if (multiply) unit.append(CHAR_MULTI);
            else multiply = true;
            unit.append(factor.toString(1));
        }
        return unit.toString();
    }

    /**
     * @param e
     *            multiplier for the exponent before converting to
     *            <code>String</code>; should be 1 or -1.
     * @return
     */
    String toString(int e) {
        int x = getExponent() * e;
        return ((1 == getValue()) ? "" : getValue() + " ") + getPrefix()
                + getSymbol() + ((1 == x) ? "" : formatExponent(x));
    }

    public boolean symbolEquals(Factor other) {
        return getSymbol().equals(other.getSymbol());
    }

    /**
     * Returns <code>true</code> if and only if both the symbol and the exponent
     * of the other factor are equal to those of this factor.
     * 
     * @param other
     * @return
     */
    public boolean dimensionEquals(Factor other) {
        return symbolEquals(other)
                && (getExponent() == other.getExponent());
    }

    @Override
    public int compareTo(Factor other) {
        if (dimensionEquals(other)) {
            final double f = getFactor(), g = other.getFactor();
            return (f < g) ? -1 : (f > g) ? 1 : 0;
        } else throw new ClassCastException(
                "Cannot compare different dimensions: " + getSymbol()
                        + formatExponent(getExponent()) + " <> "
                        + other.getSymbol()
                        + formatExponent(other.getExponent()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((symbol == null) ? 0 : symbol.hashCode());
        result = prime * result + exponent;
        final long temp = Double.doubleToLongBits(getFactor());
        result = prime * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Factor)) return false;
        Factor other = (Factor) obj;
        if (!dimensionEquals(other)) return false;
        return getFactor() == other.getFactor();
    }
}
