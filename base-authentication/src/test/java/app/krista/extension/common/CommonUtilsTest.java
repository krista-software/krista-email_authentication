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

package app.krista.extension.common;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import org.junit.Assert;
import org.junit.Test;

public class CommonUtilsTest {

    @Test
    public void getDate() {
        // Given
        Date currentDate = new Date();
        LocalDate localDate = currentDate.toInstant().atZone(ZoneId.of("GMT")).toLocalDate();
        int month = localDate.getMonthValue();
        int year = localDate.getYear();

        // When
        String date = CommonUtils.getDate();
        String[] dateIndex = date.split("-");
        System.out.println(date);
        // Then
        Assert.assertNotNull(date); //verify not null
        Assert.assertTrue(date.contains("T")); //verify time separator
        Assert.assertEquals(dateIndex[0], "" + year); //verify year
        Assert.assertEquals(dateIndex[1], month < 10 ? "0" + month : "" + month); //verify month
        Assert.assertTrue(dateIndex[2].endsWith("+0000")); //verify GMT time zone

    }

    @Test
    public void isValidDomain() {
        // Given
        String wrongDomainInput = "dummy";
        String correctDomainInput = "kristasoft.com";
        String nullInput = null;
        String emptyInput = "";
        String commaSeparatedInput = "gmail.com,kristasoft.com,antbrains.com";

        // When
        boolean wrongDomainOut = CommonUtils.isValidDomain(wrongDomainInput);
        boolean correctDomainOut = CommonUtils.isValidDomain(correctDomainInput);
        boolean nullOut = CommonUtils.isValidDomain(nullInput);
        boolean emptyOut = CommonUtils.isValidDomain(emptyInput);

        // Then
        Assert.assertFalse(wrongDomainOut); //verify time separator
        Assert.assertTrue(correctDomainOut); //verify time separator
        Assert.assertFalse(nullOut); //verify time separator
        Assert.assertFalse(emptyOut); //verify time separator
    }

    @Test
    public void isEmailDomainPresentInSupportedWorkspaceDomains() {
        // When
        boolean emptyEmailInputs =
                CommonUtils.isEmailDomainPresentInSupportedWorkspaceDomains("", "");
        boolean nullEmail =
                CommonUtils.isEmailDomainPresentInSupportedWorkspaceDomains(null, "");
        boolean wrongEmailInputs =
                CommonUtils.isEmailDomainPresentInSupportedWorkspaceDomains("abc@wrong.com", "example.com");
        boolean correctInputs =
                CommonUtils.isEmailDomainPresentInSupportedWorkspaceDomains("abc@example.com",
                        "example.com,kristasoft.com");

        // Then
        Assert.assertFalse(emptyEmailInputs); //verify time separator
        Assert.assertFalse(nullEmail); //verify time separator
        Assert.assertFalse(wrongEmailInputs); //verify time separator
        Assert.assertTrue(correctInputs); //verify time separator
    }

    @Test
    public void isValidEmail() {
        // Given
        String wrongEmail = "exa.ada";
        String correctInput = "abc@kristasoft.com";
        String nullInput = null;
        String emptyInput = "";

        // When
        boolean wrongEmailOut = CommonUtils.isValidEmail(wrongEmail);
        boolean correctEmailOut = CommonUtils.isValidEmail(correctInput);
        boolean nullOut = CommonUtils.isValidEmail(nullInput);
        boolean emptyOut = CommonUtils.isValidEmail(emptyInput);

        // Then
        Assert.assertFalse(wrongEmailOut); //verify time separator
        Assert.assertTrue(correctEmailOut); //verify time separator
        Assert.assertFalse(nullOut); //verify time separator
        Assert.assertFalse(emptyOut); //verify time separator

    }

    @Test(expected = IllegalArgumentException.class)
    public void test_Empty_ValidateDomains() {
        // Given
        String emptyInput = "";

        // When
        CommonUtils.validateDomains(emptyInput);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_Null_ValidateDomains() {
        // Given
        String nullInput = null;

        // When
        CommonUtils.validateDomains(nullInput);
    }

    @Test
    public void test_ValidateDomains() {
        // Given
        String valid = "example.com,kristasoft.com";

        // When
        CommonUtils.validateDomains(valid);
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateIfSupportedDomain() {
        CommonUtils.validateIfSupportedDomain(null, null, null);
    }

}