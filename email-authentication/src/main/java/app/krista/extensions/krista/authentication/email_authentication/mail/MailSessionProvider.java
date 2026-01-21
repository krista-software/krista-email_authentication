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

import java.util.Properties;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;

/**
 * Creates a javax.mail.Session object with given set of properties
 * with javax.mail.PasswordAuthentication authentication type.
 */
public class MailSessionProvider {

    private final Properties properties;
    private final String userName;
    private final String password;
    private static Session session;

    public MailSessionProvider(Properties properties, String userName, String password) {
        this.properties = properties;
        this.userName = userName;
        this.password = password;
    }

    public Session getSession() {
        if (session == null) {
            session = Session.getInstance(properties,
                    new javax.mail.Authenticator() {
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(userName, password);
                        }
                    });
        }
        return session;
    }

}
