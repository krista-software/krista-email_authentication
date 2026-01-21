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

import app.krista.extension.authorization.AuthorizationException;
import app.krista.extension.request.ProtoRequest;
import app.krista.extension.request.protos.http.HttpRequest;
import app.krista.ksdk.authentication.SessionManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class ClientSessionUtil {

    private final static Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    @SuppressWarnings({"unchecked", "deprecation"})
    public static String getAuthenticatedAccountId(SessionManager sessionManager, ProtoRequest request)
            throws AuthorizationException, ExecutionException, InterruptedException {
        if (sessionManager == null || !(request instanceof HttpRequest)) {
            return null;
        }
        String requestCtx = ((HttpRequest) (request)).getHeaders().get("X-Krista-Context");
        if (requestCtx == null) {
            return null;
        }
        Map<String, ?> map =
                (Map<String, ?>) GSON.fromJson(URLDecoder.decode(requestCtx, StandardCharsets.UTF_8), Map.class);
        Object clientSessionId = map.get("clientSessionId");
        if (clientSessionId == null) {
            return null;
        }
        return sessionManager.lookupAccountId((String) clientSessionId);
    }

}
