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

package app.krista.extensions.krista.authentication.email_authentication.mail;

import java.util.Map;
import java.util.Objects;
import app.krista.extension.executor.Invoker;
import app.krista.extensions.krista.authentication.email_authentication.EmailAuthenticationConstants;

public class EmailConfiguration {

    private final boolean useDefaultMailServer;
    private final String senderEmailAddress;
    private final String smtpAccount;
    private final String smtpPassword;
    private final String smtpHost;
    private final int smtpPort;
    private final Protocol protocol;

    public EmailConfiguration(boolean useDefaultMailServer, String senderEmailAddress, String senderAccount,
            String senderPassword,
            String smtpHost, int smtpPort, Protocol protocol) {
        this.useDefaultMailServer = useDefaultMailServer;
        this.senderEmailAddress = senderEmailAddress;
        this.smtpAccount = senderAccount;
        this.smtpPassword = senderPassword;
        this.smtpHost = smtpHost;
        this.smtpPort = smtpPort;
        this.protocol = protocol;
    }

    public static EmailConfiguration parse(Invoker invoker) {
        Objects.requireNonNull(invoker);
        return parse(invoker.getAttributes());
    }

    public static EmailConfiguration parse(Map<String, Object> attributes) {
        Objects.requireNonNull(attributes);
        Object defaultMailCheck = attributes.get(EmailAuthenticationConstants.USE_DEFAULT_MAIL_SERVER);
        boolean useDefaultMailServer = Boolean.parseBoolean(defaultMailCheck.toString());
        String senderEmailAddress = (String) attributes.get(EmailAuthenticationConstants.SENDER_EMAIL_ADDRESS);
        String smtpAccount = (String) attributes.get(EmailAuthenticationConstants.SMTP_ACCOUNT);
        if (smtpAccount == null || smtpAccount.isBlank()) {
            smtpAccount = senderEmailAddress;
        }
        String smtpPassword = (String) attributes.get(EmailAuthenticationConstants.SMTP_PASSWORD);
        String smtpHost = (String) attributes.get(EmailAuthenticationConstants.SMTP_HOST);
        int smtpPort = Integer.parseInt((String) attributes.get(EmailAuthenticationConstants.SMTP_PORT));
        Protocol protocol;
        switch (smtpPort) {
            case 25:
                protocol = Protocol.SMTP;
                break;
            case 587:
                protocol = Protocol.START_TLS;
                break;
            case 465:
            default:
                protocol = Protocol.SMTPS;
        }

        return new EmailConfiguration(useDefaultMailServer, senderEmailAddress, smtpAccount, smtpPassword, smtpHost,
                smtpPort, protocol);
    }

    public boolean isUsingDefaultMailServer() {
        return useDefaultMailServer;
    }

    public String getSenderEmailAddress() {
        return senderEmailAddress;
    }

    public String getSmtpAccount() {
        return smtpAccount;
    }

    public String getSmtpPassword() {
        return smtpPassword;
    }

    public String getSmtpHost() {
        return smtpHost;
    }

    public int getSmtpPort() {
        return smtpPort;
    }

    public Protocol getProtocol() {
        return protocol;
    }

    public enum Protocol {
        START_TLS,
        SMTP,
        SMTPS
    }

}
