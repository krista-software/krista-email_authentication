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

package app.krista.extensions.krista.authentication.email_authentication.impl;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import app.krista.extension.executor.Invoker;
import app.krista.extensions.krista.authentication.email_authentication.EmailAuthenticationConstants;
import app.krista.extensions.krista.authentication.email_authentication.mail.EmailConfiguration;
import app.krista.extensions.krista.authentication.email_authentication.utils.Attributes;
import app.krista.extensions.krista.authentication.email_authentication.utils.DomainNames;

public final class EmailInvokerAttributes {

    private final EmailConfiguration emailConfiguration;
    private final List<String> supportedDomains;
    private final boolean newAccountCreation;
    private final List<String> newAccountRoles;

    private EmailInvokerAttributes(EmailConfiguration emailConfiguration, List<String> supportedDomains,
            boolean newAccountCreation, List<String> newAccountRoles) {
        this.emailConfiguration = emailConfiguration;
        this.supportedDomains = supportedDomains;
        this.newAccountCreation = newAccountCreation;
        this.newAccountRoles = newAccountRoles;
    }

    public static EmailInvokerAttributes parse(Invoker invoker) {
        Objects.requireNonNull(invoker);
        return parse(invoker.getAttributes());
    }

    public static EmailInvokerAttributes parse(Map<String, Object> attributes) {
        Objects.requireNonNull(attributes);
        return new EmailInvokerAttributes(
                EmailConfiguration.parse(attributes),
                parseSupportedDomains(attributes),
                parseNewAccountCreation(attributes),
                parseNewAccountRoles(attributes));
    }

    private static List<String> parseNewAccountRoles(Map<String, Object> attributes) {
        String roles = (String) attributes.get(EmailAuthenticationConstants.DEFAULT_ROLES_FOR_NEW_ACCOUNT);
        if (roles == null || roles.isBlank()) {
            return List.of();
        }
        return Attributes.toList(roles);
    }

    private static boolean parseNewAccountCreation(Map<String, Object> attributes) {
        Object allowAutoUser = attributes.get(EmailAuthenticationConstants.ALLOW_NEW_ACCOUNT_CREATION);
        return Boolean.parseBoolean(allowAutoUser.toString());
    }

    private static List<String> parseSupportedDomains(Map<String, Object> attributes) {
        String supportedDomains = (String) attributes.get(EmailAuthenticationConstants.SUPPORTED_DOMAINS);
        if (supportedDomains == null || supportedDomains.isBlank()) {
            return List.of();
        }
        return DomainNames.normalizeDomainNames(Attributes.toList(supportedDomains));
    }

    public EmailConfiguration getEmailConfiguration() {
        return emailConfiguration;
    }

    public boolean supportsAllDomains() {
        return supportedDomains.isEmpty();
    }

    public List<String> getSupportedDomains() {
        return supportedDomains;
    }

    public boolean supportsNewAccountCreation() {
        return newAccountCreation;
    }

    public List<String> getNewAccountRoles() {
        return newAccountRoles;
    }

}
