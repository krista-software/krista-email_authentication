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

package app.krista.extensions.krista.authentication.email_authentication.utils;

import javax.ws.rs.core.HttpHeaders;
import app.krista.extension.request.protos.http.HttpRequest;
import com.kristasoft.common.PotentialCommonCode;

@PotentialCommonCode
public final class Cookies {

    private Cookies() {
    }

    public static String getCookie(HttpRequest httpRequest, String cookieName) {
        if (httpRequest == null || cookieName == null || cookieName.isBlank()) {
            return null;
        }
        String cookie = httpRequest.getHeader(HttpHeaders.COOKIE);
        if (cookie == null || !cookie.contains(cookieName)) {
            return null;
        }
        for (String cookieKeyValue : cookie.split(";")) {
            String strippedCookieKeyValue = cookieKeyValue.stripLeading();
            if (strippedCookieKeyValue.startsWith(cookieName + "=")) {
                return strippedCookieKeyValue.substring((cookieName + "=").length());
            }
        }
        return null;
    }

    public static String createCookie(String key, String value, boolean secure, boolean httpOnly) {
        return createCookie(key, value, null, secure, httpOnly);
    }

    public static String createCookie(String key, String value, String path, boolean secure, boolean httpOnly) {
        if (key == null || value == null || key.isBlank() || key.contains("=") || value.contains("=")) {
            throw new IllegalArgumentException("Can't have null values, blank key, or '=' in values");
        }
        StringBuilder builder = new StringBuilder();
        builder.append(key).append('=').append(value).append(';');
        if (path == null || path.isBlank()) {
            path = "/";
        }
        builder.append("path=").append(path).append(';');
        if (secure) {
            builder.append("SameSite=None;secure;");
        }
        if (httpOnly) {
            builder.append("HttpOnly;");
        }
        builder.setLength(builder.length() - 1);
        return builder.toString();
    }

}
