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

import java.util.Map;
import javax.inject.Inject;
import app.krista.extension.authorization.RequestAuthenticator;
import app.krista.extension.impl.anno.*;
import app.krista.extensions.krista.authentication.email_authentication.impl.EmailInvokerAttributesProvider;
import app.krista.extensions.krista.authentication.email_authentication.impl.LocationProvider;
import app.krista.extensions.krista.authentication.email_authentication.impl.SessionManager;
import app.krista.extensions.krista.authentication.email_authentication.mail.EmailConfiguration;
import app.krista.ksdk.context.AuthorizationContext;

@Field(name = EmailAuthenticationConstants.SUPPORTED_DOMAINS, type = "Text") // Studio doesn't support Text[]
@Field(name = EmailAuthenticationConstants.ALLOW_NEW_ACCOUNT_CREATION, type = "Switch")
@Field(name = EmailAuthenticationConstants.DEFAULT_ROLES_FOR_NEW_ACCOUNT, type = "Text") // not Text[] support
@Field(name = EmailAuthenticationConstants.USE_DEFAULT_MAIL_SERVER, type = "Switch")
@Field(name = EmailAuthenticationConstants.SENDER_EMAIL_ADDRESS, type = "Text")
@Field(name = EmailAuthenticationConstants.SMTP_ACCOUNT, type = "Text")
@Field(name = EmailAuthenticationConstants.SMTP_PASSWORD, type = "Text", attributes = @Attribute(name = "isSecured", value = "true"))
@Field(name = EmailAuthenticationConstants.SMTP_HOST, type = "Text")
@Field(name = EmailAuthenticationConstants.SMTP_PORT, type = "Text")
@Java(version = Java.Version.JAVA_21)
@StaticResource(path = "docs", file = "docs")
@Extension(version = "3.5.7", name = "Email Authentication",
        implementingDomainIds = "catEntryDomain_db053e8f-a194-4dde-aa6f-701ef7a6b3a7", jaxrsId = "authn",
        requireWorkspaceAdminRights = true)
@Domain(id = "catEntryDomain_db053e8f-a194-4dde-aa6f-701ef7a6b3a7",
        name = "Authentication",
        ecosystemId = "catEntryEcosystem_d3b05047-07b0-4b06-95a3-9fb8f7f608d9",
        ecosystemName = "Krista",
        ecosystemVersion = "3e7e09ed-688f-41fa-ab7c-ff879e750011")
public class EmailAuthenticationExtension {

    private static final Map<String, String> CUSTOM_TABS = Map.of("Documentation", "static/docs");

    private final RequestAuthenticator requestAuthenticator;
    private EmailInvokerAttributesProvider provider;

    @Inject
    public EmailAuthenticationExtension(LocationProvider locationProvider,
            AuthorizationContext authorizationContext, SessionManager sessionManager,
            EmailInvokerAttributesProvider provider) {
        this(new EmailAuthenticationRequestAuthenticator(locationProvider, authorizationContext, sessionManager));
        this.provider = provider;
    }

    public EmailAuthenticationExtension(RequestAuthenticator requestAuthenticator) {
        this.requestAuthenticator = requestAuthenticator;
    }

    @InvokerRequest(InvokerRequest.Type.AUTHENTICATOR)
    public RequestAuthenticator getRequestAuthenticator() {
        return requestAuthenticator;
    }

    @InvokerRequest(InvokerRequest.Type.CUSTOM_TABS)
    public Map<String, String> getCustomTabs() {
        return CUSTOM_TABS;
    }

    @InvokerRequest(InvokerRequest.Type.VALIDATE_ATTRIBUTES)
    public void validateAttributes(Map<String, Object> attributes) {
        // test if the values are correct:
        // do we find the roles?
        EmailConfiguration configuration = EmailConfiguration.parse(attributes);
        if (configuration.isUsingDefaultMailServer()) {
            throw new IllegalArgumentException("Default mail server not yet supported");
        }
    }

    @InvokerRequest(InvokerRequest.Type.INVOKER_UPDATED)
    public void invokerUpdated(Map<String, Object> old, Map<String, Object> newA) {
        provider.updateAttributes();
    }

    @InvokerRequest(InvokerRequest.Type.TEST_CONNECTION)
    public void testConnection() {
        // test if we can connect to the smtp
    }

}
