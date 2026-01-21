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

package app.krista.extension.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class CreateSessionResponseParser {

    private final static Gson GSON_JSON_MAPPER = new GsonBuilder().create();

    public AuthenticationResponse parse(Map<String, Object> createResponse, String email, String extraResponse) {
        validateCreateResponse(createResponse);
        Object accountObj = createResponse.get("account");
        Object personObj = createResponse.get("person");
        Map<?, ?> accountMap = GSON_JSON_MAPPER.fromJson(GSON_JSON_MAPPER.toJson(accountObj), Map.class);
        validateAccount(accountMap);
        String clientSessionId = (String) createResponse.get("clientSessionId");
        String personId = (String) ((Map<?, ?>) accountMap.get("personId")).get("id");
        String name = (String) accountMap.get("name");
        String accountId = (String) ((Map<?, ?>) accountMap.get("localId")).get("id");
        List<String> roles = getRoles(accountMap);
        String inboxId = (String) ((Map<?, ?>) accountMap.get("inboxId")).get("id");
        boolean isWorkspaceAdmin = (Boolean) accountMap.get("isWorkspaceAdmin");
        boolean isApplianceManager = (Boolean) accountMap.get("isApplianceManager");
        String iconUrl = getIconUrl(personObj);
        String kristaAccountId = (String) createResponse.get("kristaAccountId");
        return new AuthenticationResponse(clientSessionId, name, iconUrl, accountId, kristaAccountId, personId, roles,
                inboxId,
                isWorkspaceAdmin, isApplianceManager, Map.of("email", email), Map.of("auth", extraResponse));
    }

    private void validateAccount(Map<?, ?> accountMap) {
        if (accountMap == null || accountMap.isEmpty()) {
            throw new IllegalArgumentException("Account information is empty.");
        }
        List<String> missingKeys = new ArrayList<>();
        for (String requiredKey : Set.of("personId", "localId", "roles", "inboxId")) {
            if (!accountMap.containsKey(requiredKey)) {
                missingKeys.add(requiredKey);
            }
        }
        if (!missingKeys.isEmpty()) {
            throw new IllegalArgumentException("Account information missing data for few properties. " + missingKeys);
        }
    }

    private void validateCreateResponse(Map<String, Object> createResponse) {
        if (createResponse == null) {
            throw new IllegalArgumentException("Create client session response is null.");
        }
        List<String> missingKeys = new ArrayList<>();
        for (String requiredKey : Set.of("clientSessionId", "person", "account")) {
            if (!createResponse.containsKey(requiredKey)) {
                missingKeys.add(requiredKey);
            }
        }
        if (!missingKeys.isEmpty()) {
            throw new IllegalArgumentException("Missing required keys from create session response. " + missingKeys);
        }
    }

    private List<String> getRoles(Map<?, ?> accountMap) {
        List<?> roles = (List<?>) accountMap.get("roles");
        List<String> rolesString = new ArrayList<>();
        for (Object object : roles) {
            Map<?, ?> roleMap = GSON_JSON_MAPPER.fromJson(GSON_JSON_MAPPER.toJson(object), Map.class);
            Object id = roleMap.get("id");
            rolesString.add((String) id);
        }
        return rolesString;
    }

    private String getIconUrl(Object personObj) {
        Map<?, ?> personMap = GSON_JSON_MAPPER.fromJson(GSON_JSON_MAPPER.toJson(personObj), Map.class);
        String iconUrl = "";
        if (personMap != null) {
            iconUrl = personMap.get("icon") == null ? "" : (String) ((Map<?, ?>) personMap.get("icon")).get("uri");
        }
        return iconUrl;
    }

}
