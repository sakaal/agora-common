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

import org.junit.Test;
import org.agora_exchange.conversion.DimensionalUnit;
import org.agora_exchange.conversion.Factor;

import static org.junit.Assert.*;

/**
 * @author Sakari A. Maaranen
 */
public class DimensionalUnitTest {

    @Test
    public void testKmh() {
        DimensionalUnit kmph = new DimensionalUnit("kilometres/h");
        assertEquals("km/h", kmph.toString());
        assertEquals(1, kmph.getValue(), 0);

        assertEquals(kmph, new DimensionalUnit("km/h"));

        DimensionalUnit metresPerHour = new DimensionalUnit("m/h");
        assertEquals(1, metresPerHour.getValue(), 0);
        assertEquals(metresPerHour, new DimensionalUnit("metre/h"));

        assertEquals(1d / 1000, metresPerHour.to(kmph), 0);
        assertEquals(1000, kmph.to(metresPerHour), 0);
    }

    @Test
    public void testKibibytes() {
        DimensionalUnit kibiB = new DimensionalUnit(
                "kibibytes²·kibibytes⁻¹", Factor.PREFIXES_BINARY);
        assertEquals("KiB", kibiB.toString());
        assertEquals(1, kibiB.getValue(), 0);

        DimensionalUnit kB = new DimensionalUnit("kB");
        assertEquals(kB, new DimensionalUnit("kilobytes"));
        assertEquals(1, kB.getValue(), 0);

        assertEquals(false, kibiB.equals(kB));
        assertEquals(1.024, kibiB.to(kB), 0);
        assertEquals(1 / 1.024, kB.to(kibiB), 0);
    }

    @Test
    public void testDerived() {
        DimensionalUnit weber = new DimensionalUnit(
                "metres²·seconds⁻²·kilogram·ampere⁻¹");
        assertEquals("m²·kg/s²·A", weber.toString());

        DimensionalUnit weber2 = new DimensionalUnit(
                "A⁻¹·second⁻²/(kg⁻¹·meter⁻²)");
        assertEquals("kg·m²/A·s²", weber2.toString());

        assertEquals(weber, weber2);
        assertEquals(weber.hashCode(), weber2.hashCode());

        DimensionalUnit kiloweber = new DimensionalUnit(
                "A⁻¹·second⁻²/(Mg⁻¹·meter⁻²)");

        assertEquals("Mg·m²/A·s²", kiloweber.toString());

        assertEquals(1d / 1000, weber.to(kiloweber), 0);
        assertEquals(1000, kiloweber.to(weber), 0);
    }
}
