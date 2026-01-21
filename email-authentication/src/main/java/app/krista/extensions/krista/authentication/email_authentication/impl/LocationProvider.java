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

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import app.krista.extension.executor.Invoker;
import app.krista.extension.request.RoutingInfo;
import app.krista.extension.request.protos.http.HttpProtocol;
import org.jvnet.hk2.annotations.Service;

@Service
public class LocationProvider {

    private final String baseUrl;

    @Inject
    public LocationProvider(@Named("self") Invoker invoker) {
        this(getBaseUrl(invoker));
    }

    private LocationProvider(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    private static String getBaseUrl(Invoker invoker) {
        String url = invoker.getRoutingInfo().getRoutingURL(HttpProtocol.PROTOCOL_NAME, RoutingInfo.Type.APPLIANCE);
        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        return url;
    }

    public String getLocation(String path) {
        return getLocation(path, Map.of());
    }

    public String getLocation(String path, Map<String, String> queryParameters) {
        StringBuilder builder = new StringBuilder(baseUrl);
        if (path != null && !path.isBlank()) {
            if (!path.startsWith("/")) {
                builder.append('/');
            }
            builder.append(path);
        }
        if (queryParameters != null && !queryParameters.isEmpty()) {
            builder.append('?');
            for (Map.Entry<String, String> entry : queryParameters.entrySet()) {
                builder.append(urlEncode(entry.getKey()))
                        .append('=')
                        .append(urlEncode(entry.getValue()))
                        .append('&');
            }
            builder.setLength(builder.length() - 1);
        }
        return builder.toString();
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

}
