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

import java.io.IOException;
import javax.inject.Inject;
import app.krista.extensions.util.KeyValueStore;
import org.jvnet.hk2.annotations.Service;

/**
 * Manage persistence storage of secret send with email verification link.
 */
@Service
public final class VerificationLinkManager {

    private final KeyValueStore keyValueStore;

    @Inject
    public VerificationLinkManager(KeyValueStore keyValueStore) {
        this.keyValueStore = keyValueStore;
    }

    public VerificationLinkDetails get(String code) throws IOException {
        String value = keyValueStore.get(code, String.class);
        return value == null ? null : VerificationLinkDetails.fromJson(value);
    }

    public void add(VerificationLinkDetails verificationLinkDetails) throws IOException {
        keyValueStore.put(toKey(verificationLinkDetails.getSecret()), verificationLinkDetails.toJson());
    }

    public void remove(String code) throws IOException {
        keyValueStore.remove(toKey(code));
    }

    private String toKey(String secret) {
        return "secret-" + secret;
    }

}
