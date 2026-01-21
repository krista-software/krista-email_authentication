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

package app.krista.extensions.krista.authentication.email_authentication.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public final class DomainNames {

    private static final Pattern DOMAIN_NAME_PATTERN = Pattern.compile(
            "^((?!-)[A-Za-z0-9-]"
                    + "{1,63}(?<!-)\\.)"
                    + "+[A-Za-z]{2,6}");

    private DomainNames() {
    }

    public static List<String> normalizeDomainNames(List<String> domainNames) {
        if (domainNames == null || domainNames.isEmpty()) {
            return List.of();
        }
        Set<String> seen = new HashSet<>();
        List<String> normalized = new ArrayList<>();
        for (String item : domainNames) {
            String normalizedDomainName = normalizeDomainName(item);
            if (seen.add(normalizedDomainName)) {
                normalized.add(normalizedDomainName);
            }
        }
        return normalized;
    }

    public static String normalizeDomainName(String domain) {
        if (!isValidDomain(domain)) {
            throw new IllegalArgumentException("Invalid domain name!");
        }
        return normalize(domain);
    }

    private static String normalize(String domain) {
        return domain.toLowerCase().strip();
    }

    public static boolean isValidDomain(String domainName) {
        if (domainName == null || domainName.isBlank()) {
            return false;
        }
        return DOMAIN_NAME_PATTERN.matcher(normalize(domainName)).matches();
    }

}
