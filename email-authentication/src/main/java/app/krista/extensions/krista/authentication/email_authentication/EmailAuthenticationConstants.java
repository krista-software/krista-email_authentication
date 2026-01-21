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

import com.google.gson.Gson;

public final class EmailAuthenticationConstants {

    public static final String X_KRISTA_SESSION_ID = "X-Krista-Session-Id";
    public static final String X_KRISTA_ORIGINAL_URL = "X-Krista-Original-URI";

    public static final String SUPPORTED_DOMAINS = "Supported Domains";
    public static final String ALLOW_NEW_ACCOUNT_CREATION = "Allow New Account Creation";
    public static final String DEFAULT_ROLES_FOR_NEW_ACCOUNT = "Default Roles for New Accounts";
    public static final String DEFAULT_ROLE = "Krista Client User";
    public static final String USE_DEFAULT_MAIL_SERVER = "Use Default Mail Server";
    public static final String SENDER_EMAIL_ADDRESS = "Email Address of Sender";
    public static final String SMTP_ACCOUNT = "Sender Account";
    public static final String SMTP_PASSWORD = "Sender Password";
    public static final String SMTP_HOST = "SMTP Host";
    public static final String SMTP_PORT = "SMTP Port";
    public static final String AUTHENTICATION_TYPE = "Email Authentication";

    public static final Gson GSON = new Gson();

    private EmailAuthenticationConstants() {
    }

}
