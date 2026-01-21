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

import java.util.regex.Pattern;

public final class EmailAddresses {

    public static final String DEFAULT_DOMAIN = "kristasoft.com";
    private static final Pattern EMAIL_ADDRESS_PATTERN =
            Pattern.compile("^[a-z0-9_+&*-]+(?:\\.[a-z0-9_+&*-]+)*@(?:[a-z0-9-]+\\.)+[a-z]{2,7}$");

    private EmailAddresses() {
    }

    public static boolean isValidEmailAddress(String emailAddress) {
        if (emailAddress == null || emailAddress.isBlank()) {
            return false;
        }
        return EMAIL_ADDRESS_PATTERN.matcher(normalize(emailAddress)).matches();
    }

    private static String normalize(String emailAddress) {
        return emailAddress.strip().toLowerCase();
    }

    public static String normalizeEmailAddress(String emailAddress) {
        if (!isValidEmailAddress(emailAddress)) {
            throw new IllegalArgumentException("Email address is not valid");
        }
        return normalize(emailAddress);
    }

    public static String getDomainName(String emailAddress) {
        return normalizeEmailAddress(emailAddress).split("@")[1];
    }

    public static String getLocalPart(String emailAddress) {
        return normalizeEmailAddress(emailAddress).split("@")[0];
    }

}
