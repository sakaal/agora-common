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

public class DomainConverter extends XmlAdapter<String, Validator<String>>
{

    private static final transient Logger logger =
            LoggerFactory.getLogger(DomainConverter.class);

    public static Validator<String> parseTokenToValidator(String domain) {
        if (null == domain) return null;
        // TODO:
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
