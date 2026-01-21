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

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import javax.ws.rs.core.Response;
import com.kristasoft.common.PotentialCommonCode;

@PotentialCommonCode
public final class Resources {

    private Resources() {
    }

    public static Response getTranslatedResource(Class<?> clazz, String path, Map<String, String> translations)
            throws IOException {
        return getTranslatedResource(clazz.getClassLoader(), path, translations);
    }

    public static Response getTranslatedResource(ClassLoader classLoader, String path,
            Map<String, String> translations) throws IOException {
        InputStream resourceAsStream = classLoader.getResourceAsStream(path);
        assert resourceAsStream != null;
        String page = new String(resourceAsStream.readAllBytes(), StandardCharsets.UTF_8);
        for (Map.Entry<String, String> entry : translations.entrySet()) {
            page = page.replace(entry.getKey(), entry.getValue());
        }
        return Response.ok(200).entity(page).build();
    }

}
