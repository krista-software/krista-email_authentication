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

import app.krista.extension.common.CommonUtils;
import org.junit.Test;

public class ValidationsTest {

    @Test
    public void validateDomains() {
        // Given
        String correctDomainInput = "kristasoft.com";

        // When
        CommonUtils.validateDomains(correctDomainInput);

    }

    @Test
    public void validateDomains_commaSeparatedInputs() {
        // Given
        String commaSeparatedInput = "kristasoft.com,gmail.com,antbrains.com";

        // When
        CommonUtils.validateDomains(commaSeparatedInput);

    }

    @Test(expected = java.lang.IllegalArgumentException.class)
    public void validateDomains_wrongInput() {
        // Given
        String wrongDomainInput = "dummy";

        // When
        CommonUtils.validateDomains(wrongDomainInput);
    }

}