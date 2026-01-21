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

package app.krista.extensions.krista.authentication.email_authentication;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.inject.Inject;
import javax.ws.rs.core.HttpHeaders;
import app.krista.extension.authorization.MustAuthenticateException;
import app.krista.extension.authorization.MustAuthorizeException;
import app.krista.extension.authorization.RequestAuthenticator;
import app.krista.extension.request.ProtoRequest;
import app.krista.extension.request.ProtoResponse;
import app.krista.extension.request.protos.http.HttpProtocol;
import app.krista.extension.request.protos.http.HttpRequest;
import app.krista.extension.request.protos.http.HttpResponse;
import app.krista.extensions.krista.authentication.email_authentication.impl.LocationProvider;
import app.krista.extensions.krista.authentication.email_authentication.impl.SessionManager;
import app.krista.extensions.krista.authentication.email_authentication.utils.Cookies;
import app.krista.ksdk.context.AuthorizationContext;
import app.krista.model.field.NamedField;

public class EmailAuthenticationRequestAuthenticator implements RequestAuthenticator {

    private final LocationProvider locationProvider;
    private final AuthorizationContext authorizationContext;
    private final SessionManager sessionManager;

    @Inject
    public EmailAuthenticationRequestAuthenticator(LocationProvider locationProvider,
            AuthorizationContext authorizationContext,
            SessionManager sessionManager) {
        this.locationProvider = locationProvider;
        this.authorizationContext = authorizationContext;
        this.sessionManager = sessionManager;
    }

    @Override
    public String getScheme() {
        return "Email";
    }

    @Override
    public Set<String> getSupportedProtocols() {
        return Set.of(HttpProtocol.PROTOCOL_NAME);
    }

    @Override
    public String getAuthenticatedAccountId(ProtoRequest protoRequest) {
        if (!(protoRequest instanceof HttpRequest)) {
            return null;
        }
        HttpRequest httpRequest = (HttpRequest) protoRequest;
        try {
            String sessionId = Cookies.getCookie(httpRequest, EmailAuthenticationConstants.X_KRISTA_SESSION_ID);
            String accountId;
            if (sessionId != null) {
                accountId = sessionManager.getAccountId(sessionId);
            } else {
                accountId = handleLoginRequest(httpRequest);
            }
            return accountId;
        } catch (IOException cause) {
            System.err.println(cause.getMessage());
            return null;
        }
    }

    @Override
    public boolean setServiceAuthorization(String s) {
        return false;
    }

    @Override
    public Map<String, NamedField> getAttributeFields() {
        return null;
    }

    @Override
    public ProtoResponse getMustAuthenticateResponse(MustAuthenticateException cause, ProtoRequest request) {
        String originalUrl = ((HttpRequest) request).getHeader(EmailAuthenticationConstants.X_KRISTA_ORIGINAL_URL);
        return new HttpResponse(302,
                Map.of(HttpHeaders.LOCATION, locationProvider.getLocation("/authn/login",
                        Map.of(EmailAuthenticationConstants.X_KRISTA_ORIGINAL_URL, originalUrl))),
                new ByteArrayInputStream(new byte[0]));
    }

    @Override
    public AuthorizationResponse getMustAuthenticateResponse(MustAuthenticateException cause) {
        return null;
    }

    @Override
    public ProtoResponse getMustAuthorizeResponse(MustAuthorizeException cause, ProtoRequest request) {
        return null;
    }

    @Override
    public AuthorizationResponse getMustAuthorizeResponse(MustAuthorizeException cause) {
        return null;
    }

    private String handleLoginRequest(HttpRequest httpRequest) {
        if (Objects.equals("/login", httpRequest.getUri().getPath())) {
            return authorizationContext.getAuthorizedAccount().getAccountId();
        }
        return null;
    }

}
