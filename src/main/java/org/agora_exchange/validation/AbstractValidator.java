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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides a default implementation of the {@link #isValid} method and a
 * logging facility for derived implementations.
 * 
 * @author Sakari A. Maaranen
 * 
 * @param <T>
 *            the class of valid data objects for the derived implementation.
 */
public abstract class AbstractValidator<T> implements Validator<T> {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    /*
     * (non-Javadoc)
     * 
     * @see org.agora_exchange.util.DataValidator#isValid(java.lang.Object)
     */
    @Override
    public boolean isValid(final Object data) {
        try {
            normalize(data);
        } catch (IllegalArgumentException e) {
            // This is expected and should never need to be logged.
            return false;
        } catch (NullPointerException e) {
            /*
             * This is an indication of a bad implementation and therefore
             * logged at the debug level. Do not log this as a warning because
             * system administrators can't help it. Only developers can fix it.
             */
            logger.debug("Bad validator", e);
            return false;
        }
        return true;
    }
}
