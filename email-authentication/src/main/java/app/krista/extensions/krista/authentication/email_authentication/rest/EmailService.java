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
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import javax.inject.Inject;
import javax.inject.Named;
import javax.mail.MessagingException;
import javax.ws.rs.core.Response;
import app.krista.extension.common.CommonUtils;
import app.krista.extension.executor.Invoker;
import app.krista.extension.request.RoutingInfo;
import app.krista.extension.request.protos.http.HttpProtocol;
import app.krista.extensions.krista.authentication.email_authentication.EmailAuthenticationConstants;
import app.krista.extensions.krista.authentication.email_authentication.impl.*;
import app.krista.extensions.krista.authentication.email_authentication.mail.EmailSender;
import app.krista.extensions.krista.authentication.email_authentication.mail.MailSessionProvider;
import app.krista.extensions.krista.authentication.email_authentication.utils.EmailAddresses;
import app.krista.ksdk.accounts.Account;
import app.krista.ksdk.authentication.AuthenticationSettings;
import app.krista.ksdk.authorization.ModifiableRole;
import org.jvnet.hk2.annotations.Service;

import static app.krista.extensions.krista.authentication.email_authentication.EmailAuthenticationConstants.*;

@Service
public class EmailService {

    private final Invoker invoker;
    private final VerificationLinkManager verificationLinkManager;
    private final SessionManager sessionManager;
    private final LocationProvider locationProvider;
    private final EmailInvokerAttributesProvider invokerAttributesProvider;
    private final AccountProvisioner accountProvisioner;
    private final RoleProvisioner roleProvisioner;
    private final AuthenticationSettings authenticationSettings;

    @Inject
    public EmailService(@Named("self") Invoker invoker,
            VerificationLinkManager emailAuthenticationStore,
            SessionManager sessionManager,
            LocationProvider locationProvider,
            EmailInvokerAttributesProvider invokerAttributesProvider,
            AccountProvisioner accountProvisioner,
            RoleProvisioner roleProvisioner,
            AuthenticationSettings authenticationSettings) {
        this.verificationLinkManager = emailAuthenticationStore;
        this.invoker = invoker;
        this.sessionManager = sessionManager;
        this.accountProvisioner = accountProvisioner;
        this.roleProvisioner = roleProvisioner;
        this.invokerAttributesProvider = invokerAttributesProvider;
        this.locationProvider = locationProvider;
        this.authenticationSettings = authenticationSettings;
    }

    /**
     * This method validates email address and originalUrl input parameters
     * and show error html page in case of errors otherwise end email with secret link.
     *
     * @param originalUrl
     * @param email
     * @return Response
     * @throws IOException
     */
    Response sendLoginLink(String originalUrl, String email) throws IOException {
        validate(originalUrl == null || originalUrl.isBlank(), "OriginalUrl parameter is missing.");
        validate(!EmailAddresses.isValidEmailAddress(email), "Email address '" + email + "' is not valid.");
        String roles = invoker.getAttributes().get(DEFAULT_ROLES_FOR_NEW_ACCOUNT).toString();
        Account account = accountProvisioner.provisionAccount(email, List.of(roles));
        Response loginPageResponse =
                EmailResponseFactory.create(account, invokerAttributesProvider.getAttributes(), email, originalUrl);
        if (loginPageResponse != null) {
            return loginPageResponse;
        }
        if (account == null) {
            account = provisionNewAccount(email);
        }
        assert account != null;
        String sessionId = sessionManager.create(account.getAccountId());
        sendLoginLink(email, sessionId, originalUrl, account.getAccountId());
        return EmailResponseFactory.create(locationProvider, sessionId);
    }

    Response verifySecretLink(String originalUrl, String code) throws IOException {
        VerificationLinkDetails secretDetails = verifySecretLink(code);
        handleSupportedDomains(secretDetails, invokerAttributesProvider.getAttributes().supportsNewAccountCreation());
        String sessionId = sessionManager.create(secretDetails.getAccountId());
        return EmailResponseFactory.create(originalUrl, Map.of("Set-Cookie", sessionId));
    }

    private void handleSupportedDomains(VerificationLinkDetails secretDetails, boolean supportsNewAccountCreation) {
        List<String> supportedDomainsForWorkspace = authenticationSettings.getSupportedDomains();
        String supportedDomain = String.join(",", supportedDomainsForWorkspace);
        if (!supportsNewAccountCreation) {
            boolean isEmailDomainPresent =
                    CommonUtils.isEmailDomainPresentInSupportedWorkspaceDomains(secretDetails.getEmail(),
                            supportedDomain);
            if (!isEmailDomainPresent) {
                throw new IllegalArgumentException(
                        "ALLOW_AUTO_PERSON_CREATION is not enabled and domain for email '" + secretDetails.getEmail() +
                                "' is not supported in workspace.");
            }
        }
        Object invokerDomains = invoker.getAttributes().get(EmailAuthenticationConstants.SUPPORTED_DOMAINS);
        if (invokerDomains != null) {
            CommonUtils.validateIfSupportedDomain(secretDetails.getEmail(), supportedDomain, invokerDomains);
        }
    }

    private Account provisionNewAccount(String email) {
        ModifiableRole modifiableRole = roleProvisioner.provisionDefaultRole();
        return accountProvisioner.provisionAccount(email, List.of(modifiableRole.getRoleId()));
    }

    private void sendLoginLink(String email, String sessionId, String originalUrl, String accountId) {
        try {
            EmailAddresses.isValidEmailAddress(email);
            String secret = UUID.randomUUID().toString();
            verificationLinkManager.add(new VerificationLinkDetails(email, secret, getExpiryTime(),
                    VerificationLinkDetails.State.GENERATED.toString(), sessionId, accountId));
            Properties properties = getProperties();
            String accountEmail = invoker.getAttributes().get(SMTP_ACCOUNT).toString();
            String accountPassword = invoker.getAttributes().get(SMTP_PASSWORD).toString();
            MailSessionProvider mailSessionProvider =
                    new MailSessionProvider(properties, accountEmail, accountPassword);
            new EmailSender(mailSessionProvider).sendMessage(email, "Email Authentication Link",
                    invoker.getRoutingInfo().getRoutingURL(
                            HttpProtocol.PROTOCOL_NAME, RoutingInfo.Type.APPLIANCE)
                            + "/authn/?code=" + secret + "&" + EmailAuthenticationConstants.X_KRISTA_ORIGINAL_URL +
                            "=" + originalUrl, secret);
        } catch (IOException | MessagingException cause) {
            cause.printStackTrace();
            throw new IllegalStateException(cause);
        }
    }

    private Properties getProperties() {
        Properties properties = new Properties();
        properties.put("mail.smtp.host", invoker.getAttributes().get(SMTP_HOST));
        properties.put("mail.smtp.port", invoker.getAttributes().get(SMTP_PORT));
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.socketFactory.port", "465");
        properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        properties.put("mail.smtp.ssl.checkserveridentity", "true");
        return properties;
    }

    private VerificationLinkDetails verifySecretLink(String secret) {
        try {
            VerificationLinkDetails verificationLinkDetails = verificationLinkManager.get(secret);
            validate(verificationLinkDetails == null, "Email verification link is not found.");
            if (verificationLinkDetails.getExpiry() < System.currentTimeMillis()) {
                verificationLinkManager.remove(secret);
                throw new IllegalArgumentException("Email verification link is expired.");
            }
            String state = verificationLinkDetails.getState();
            if (!state.equals(VerificationLinkDetails.State.GENERATED.name())) {
                throw new IllegalStateException("Email verification link is " + state + ".");
            }
            String originalSecret = verificationLinkDetails.getSecret();
            if (!secret.equals(originalSecret)) {
                throw new IllegalStateException("Email verification link is not matching.");
            }
            return new VerificationLinkDetails(verificationLinkDetails.getEmail(), secret,
                    verificationLinkDetails.getExpiry(),
                    VerificationLinkDetails.State.USED.name(), verificationLinkDetails.getSessionId(),
                    verificationLinkDetails.getAccountId());
        } catch (IOException cause) {
            cause.printStackTrace();
            throw new IllegalStateException("Failed to verify email secret.", cause);
        }
    }

    public void logout(String sessionId) throws IOException {
        sessionManager.remove(sessionId);
    }

    private void validate(boolean isError, String message) {
        if (isError) {
            throw new IllegalArgumentException(message);
        }
    }

    private long getExpiryTime() {
        return System.currentTimeMillis() + (30 * 60 * 1000);
    }

}
