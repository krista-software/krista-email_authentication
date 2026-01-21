/*
 * Email Authentication Extension for Krista
 * Copyright (C) 2025 Krista Software
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>. 
 */

package com.krista.extensions.krista.authentication.email.impl;

import app.krista.extensions.krista.authentication.email_authentication.utils.DomainNames;
import org.junit.Assert;
import org.junit.Test;

public class DomainNamesTest {

    @Test
    public void testIsValidDomain() {
        Assert.assertTrue(DomainNames.isValidDomain("kristasoft.com"));
    }

    @Test
    public void testIsValidDomain_invalidDomain_Dots() {
        Assert.assertFalse(DomainNames.isValidDomain(".."));
    }

    @Test
    public void testIsValidDomain_invalidDomain_InvalidCharacter() {
        Assert.assertFalse(DomainNames.isValidDomain("a%b.com"));
    }

    @Test
    public void testIsValidDomain_invalidDomain_Empty() {
        Assert.assertFalse(DomainNames.isValidDomain(""));
    }

}
