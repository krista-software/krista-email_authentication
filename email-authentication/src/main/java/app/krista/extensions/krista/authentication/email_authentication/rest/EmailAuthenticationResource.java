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
import java.util.Map;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import app.krista.extensions.krista.authentication.email_authentication.EmailAuthenticationConstants;
import app.krista.extensions.krista.authentication.email_authentication.utils.Resources;

import static app.krista.extensions.krista.authentication.email_authentication.EmailAuthenticationConstants.*;

/**
 * The API resource class which manages overall
 * flow like login, logout, verify secret of email authentication extension.
 */
@Path("/")
public class EmailAuthenticationResource {

    private final EmailService emailService;

    @Inject
    public EmailAuthenticationResource(EmailService emailService) {
        this.emailService = emailService;
    }

    /**
     * Returns type of the authentication extension.
     */
    @GET
    @Path("/type")
    public String getAuthType() {
        return AUTHENTICATION_TYPE;
    }

    @GET
    @Path("/login")
    @Produces(MediaType.TEXT_HTML)
    public Response getLoginPage(@Context HttpHeaders headers,
            @QueryParam(EmailAuthenticationConstants.X_KRISTA_ORIGINAL_URL) String originalUrl) throws IOException {
        return EmailResponseFactory.create(originalUrl, Map.of("__error", ""));
    }

    @POST
    @Path("/login")
    @Produces(MediaType.APPLICATION_JSON)
    public Response sendLoginLink(@FormParam("originalUrl") String originalUrl, @FormParam("email") String email)
            throws IOException {
        return emailService.sendLoginLink(originalUrl, email);
    }

    /**
     * Returns waiting page after email is send for verification to the user.
     *
     * @return html page
     */
    @GET
    @Path("/waiting")
    public Response getWaitingPage(@CookieParam(X_KRISTA_SESSION_ID) String sessionId) throws IOException {
        return Resources.getTranslatedResource(getClass(), "waiting.html",
                Map.of("__sessionId", sessionId));
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response verifySecretLink(@Context HttpHeaders headers,
            @QueryParam(EmailAuthenticationConstants.X_KRISTA_ORIGINAL_URL) String originalUrl,
            @QueryParam("code") String code) {
        try {
            return emailService.verifySecretLink(originalUrl, code);
        } catch (Exception cause) {
            throw new IllegalStateException("Failed to verify email.", cause);
        }
    }

    @POST
    @Path("/logout")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void logout(@CookieParam(X_KRISTA_SESSION_ID) String sessionId) throws IOException {
        emailService.logout(sessionId);
    }

}
