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

import static app.krista.extensions.krista.authentication.email_authentication.EmailAuthenticationConstants.*;

public final class VerificationLinkDetails {

    public enum State {
        GENERATED,
        USED,
    }

    private final String email;
    private final String secret;
    private final long expiry;
    private final String state;
    private final String sessionId;
    private final String accountId;

    public VerificationLinkDetails(String email, String code, long expiry, String state, String sessionId,
            String accountId) {
        this.email = email;
        this.secret = code;
        this.expiry = expiry;
        this.state = state;
        this.sessionId = sessionId;
        this.accountId = accountId;
    }

    public static VerificationLinkDetails fromJson(String json) {
        return GSON.fromJson(json, VerificationLinkDetails.class);
    }

    public String getEmail() {
        return email;
    }

    public String getSecret() {
        return secret;
    }

    public long getExpiry() {
        return expiry;
    }

    public String getState() {
        return state;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getAccountId() {
        return accountId;
    }

    public String toJson() {
        return GSON.toJson(this);
    }

}
