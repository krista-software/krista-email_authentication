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

package app.krista.extensions.krista.authentication.email_authentication.rest;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import app.krista.extensions.krista.authentication.email_authentication.EmailAuthenticationConstants;
import app.krista.extensions.krista.authentication.email_authentication.impl.EmailInvokerAttributes;
import app.krista.extensions.krista.authentication.email_authentication.impl.LocationProvider;
import app.krista.extensions.krista.authentication.email_authentication.utils.Cookies;
import app.krista.extensions.krista.authentication.email_authentication.utils.EmailAddresses;
import app.krista.extensions.krista.authentication.email_authentication.utils.Resources;
import app.krista.ksdk.accounts.Account;

/**
 * Create javax.ws.rs.core.Response object based on various conditions.
 */
public final class EmailResponseFactory {

    /**
     * Validates EmailInvokerAttributes and returns javax.ws.rs.core.Response.
     *
     * @param account
     * @param attributes
     * @param email
     * @param originalUrl
     * @return Response
     * @throws IOException
     */
    static Response create(Account account, EmailInvokerAttributes attributes, String email, String originalUrl)
            throws IOException {
        Response response = null;
        if (!attributes.supportsAllDomains()
                && !attributes.getSupportedDomains().contains(EmailAddresses.getDomainName(email))) {
            response = create(originalUrl, Map.of("__error", "Your domain is not supported: " + email));
        }
        if (account == null) {
            if (!attributes.supportsNewAccountCreation()) {
                System.out.println("Access denied for " + email + " because the account does not exist");
                response = create(originalUrl, Map.of("__error", "Login failure for: " + email));
            }
        }
        return response;
    }

    /**
     * Returns javax.ws.rs.core.Response with login page if properties contain "__error" key
     * Returns javax.ws.rs.core.Response with 302 redirection to 'originalUri' page if properties contain "Set-Cookie"
     * else returns null.
     *
     * @param originalUrl
     * @param properties
     * @return Response
     * @throws IOException
     */
    static Response create(String originalUrl, Map<String, String> properties) throws IOException {
        if (properties.containsKey("__error")) {
            return Resources.getTranslatedResource(EmailResponseFactory.class, "login.html",
                    Map.of("__originalUrl", originalUrl, "__error", properties.get("__error")));
        }
        if (properties.containsKey("Set-Cookie")) {
            URI originalUri = URI.create(originalUrl);
            return Response.status(302)
                    .header("Set-Cookie", properties.get("Set-Cookie"))
                    .header(HttpHeaders.LOCATION, originalUri)
                    .build();
        }
        return null;
    }

    /**
     * Returns response by setting EmailAuthenticationConstants.X_KRISTA_SESSION_ID cookie
     * with email waiting page redirection.
     *
     * @param locationProvider
     * @param sessionId
     * @return Response
     */

    //TODO: Remove cookies form here.
    static Response create(LocationProvider locationProvider, String sessionId) {
        return Response.status(302)
                .header(HttpHeaders.LOCATION, URI.create(locationProvider.getLocation("/authn/waiting")))
                .cookie(NewCookie.valueOf(
                        Cookies.createCookie(EmailAuthenticationConstants.X_KRISTA_SESSION_ID, sessionId, true, true)))
                .build();
    }

}
