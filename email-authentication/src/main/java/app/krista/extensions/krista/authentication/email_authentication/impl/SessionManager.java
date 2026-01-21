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
import java.util.Objects;
import java.util.UUID;
import javax.inject.Inject;
import app.krista.extensions.util.KeyValueStore;
import org.jvnet.hk2.annotations.Service;

/**
 * This class creates session after verification of email secret code from verification link.
 * This class has nothing to do with Krista's legacy clientSessionId.
 */
@Service
public final class SessionManager {

    private final KeyValueStore keyValueStore;

    @Inject
    public SessionManager(KeyValueStore keyValueStore) {
        this.keyValueStore = keyValueStore;
    }

    /**
     * Create session for given accountId.
     * This method should be called after verification of email link.
     *
     * @param accountId
     * @return sessionId
     * @throws IOException
     */
    public String create(String accountId) throws IOException {
        String session = UUID.randomUUID().toString();
        keyValueStore.put(toKey(session), accountId);
        return session;
    }

    public String getAccountId(String sessionId) throws IOException {
        return keyValueStore.get(toKey(sessionId), String.class);
    }

    public void remove(String sessionId) throws IOException {
        if (sessionId == null || sessionId.isEmpty()) {
            throw new IllegalArgumentException("Missing session id.");
        }
        keyValueStore.remove(toKey(sessionId));
    }

    private String toKey(String secret) {
        Objects.requireNonNull(secret);
        return "session-" + secret;
    }

}
