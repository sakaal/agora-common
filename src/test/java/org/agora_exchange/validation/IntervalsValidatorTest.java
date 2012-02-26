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

import static org.junit.Assert.*;

/**
 * @author Sakari A. Maaranen
 */
public class IntervalsValidatorTest {

    IntervalsValidator validator;

    @Test
    public void testSingle() {
        validator = new IntervalsValidator("kg:[0,1]");

        assertTrue(validator.isValid("0 kg"));
        assertTrue(validator.isValid(1 / 2 + "kg"));
        assertTrue(validator.isValid(1.0000000000000000 + "kg"));

        // The precision is highest near zero.
        assertFalse(validator.isValid(-0.000000000000000000000000000000001));

        // Near 1.0d this is about the best possible precision (16 decimals).
        assertFalse(validator.isValid(1.0000000000000002));
    }

    @Test
    public void testSingleNegative() {
        validator = new IntervalsValidator("m/s : [-2,-1]");
        assertTrue(validator.isValid("-2m/s"));
        assertTrue(validator.isValid(-1 + "m/s"));
        assertFalse(validator.isValid(-1));
        assertFalse(validator.isValid(-2.000000001 + "m/s"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSinglePositiveInvalid() {
        validator = new IntervalsValidator("s [0,+1]");
    }

    @Test
    public void testSingleWithWhiteSpace() {
        validator = new IntervalsValidator(
                "\tm/s:\t(\n\t\t0 ,\n\t\t1\n\t)   ");
    }

    @Test
    public void testUnbounded() {
        validator = new IntervalsValidator("]-\u221e,+\u221e[");
        validator = new IntervalsValidator("(-*,+*)");
        validator = new IntervalsValidator("(-*,*)");
        validator = new IntervalsValidator("(*,+*)");
        validator = new IntervalsValidator("(*,*)");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInfiniteLeftEndpointClosed() {
        validator = new IntervalsValidator("[-\u221e,+\u221e[");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInfiniteRightEndpointClosed() {
        validator = new IntervalsValidator("]-\u221e,+\u221e]");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnboundedRightInvalid1() {
        validator = new IntervalsValidator("(*,-*)");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnboundedRightInvalid2() {
        validator = new IntervalsValidator("(*,+)");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnboundedLeftInvalid1() {
        validator = new IntervalsValidator("(+*,*)");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnboundedLeftInvalid2() {
        validator = new IntervalsValidator("(-,*)");
    }

    @Test
    public void testThree() {
        validator = new IntervalsValidator("K: (*,-5) [0,12) ]15, 120[");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testThreeOverlap() {
        validator = new IntervalsValidator("mol: (*,-5) [-6,12) ]15, 120[");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInfiniteOverlapFirst() {
        validator = new IntervalsValidator("(*,*) [0,1]");
    }

    @Test
    public void testZeroPointDecimal() {
        validator = new IntervalsValidator(
                "[-0.0000000000001,0.9) (0.9,1.0]");
        assertFalse(validator.isValid(-0.0000000000002));
        assertTrue(validator.isValid(-0.0000000000001));
        assertTrue(validator.isValid(-0.0000000000000));
        assertFalse(validator.isValid(0.9));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTinyOverlap() {
        validator = new IntervalsValidator(
                "[-1,0] [-0.000000000000000000000000000000000000000001,1.0]");
    }

    @Test
    public void testDecimalSINormalization() {
        validator = new IntervalsValidator("Mbps: (0, 10000000]");
        assertEquals("250000 Mbps", validator.normalize("0.25 Tbps"));
    }

    @Test
    public void testBinaryNormalization() {
        validator = new IntervalsValidator("MiB: (0, 2560]");
        assertEquals("2560 MiB", validator.normalize("2.5 GiB"));
    }
}
