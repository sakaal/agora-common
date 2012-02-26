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

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.agora_exchange.validation.Patterns.DECIMAL_INTEGER;
import static org.agora_exchange.validation.Patterns.WS;

public class DomainConverter extends XmlAdapter<String, Validator<String>>
{
    private static final transient Logger logger =
            LoggerFactory.getLogger(DomainConverter.class);
    /**
     * The minimum and maximum length of a pattern can be given as: { min , max
     * }
     */
    private static final String MIN_MAX = "\\{" + WS + DECIMAL_INTEGER + WS
            + "," + WS + DECIMAL_INTEGER + WS + "\\}";

    /**
     * A pattern can have any of these modifiers:
     * <dl>
     * <dt>null</dt>
     * <d-d>A <code>null</code> value is accepted: It's checked before trying to
     * trim, check the length of, or match the pattern.</dd>
     * <dt>trim</dt>
     * <dd>The value will be trimmed before the length is checked and the
     * pattern matched.</dd>
     * <dt>{ min , max }</dt>
     * <dd>The minimum and maximum length of the value will be checked before
     * the pattern is matched.</dd>
     * </dl>
     */
    private static final String MODIFIER = "(?:null|trim|" + MIN_MAX + ")";

    /**
     * A regex to match zero, one or many modifiers.
     */
    private static final String PATTERN_MODS = "(?:" + WS + MODIFIER + "(?:"
            + WS + "," + WS + MODIFIER + ")*" + ")?";

    private static final Pattern modifiers = Pattern.compile(PATTERN_MODS);

    public static Validator<String> parseTokenToValidator(String domain) {
        if (null == domain) return null;
        if (domain.startsWith("pattern:")) {

            final int modsAt = domain.indexOf(':') + 1;
            final int separator = domain.indexOf(':', modsAt);
            final String mods = domain.substring(modsAt, separator);
            final String pattern = domain.substring(separator + 1).trim();

            if (modifiers.matcher(mods).matches()) {
                logger.warn("TODO: The pattern modifiers \"" + mods
                        + "\" are valid, but not observed for domain: "
                        + domain);
            } else {
                throw new IllegalArgumentException(
                        "Pattern modifiers' syntax error: \"" + mods
                                + "\" does not match the regular expression \""
                                + modifiers.toString() + "\".");
            }
            return new PatternValidator(pattern);
        } else if (domain.startsWith("interval:")) {
            final String intervals =
                    domain.substring(domain.indexOf(':') + 1).trim();
            return new IntervalsValidator(intervals);
        }
        logger.warn("No DataValidator for domain=\"" + domain + "\".");
        return null;
    }

    public static String printValidatorToToken(Validator<String> validator) {
        // TODO:
        return "";
    }

    @Override
    public String marshal(Validator<String> v) throws Exception {
        return printValidatorToToken(v);
    }

    @Override
    public Validator<String> unmarshal(String v) throws Exception {
        return parseTokenToValidator(v);
    }
}
