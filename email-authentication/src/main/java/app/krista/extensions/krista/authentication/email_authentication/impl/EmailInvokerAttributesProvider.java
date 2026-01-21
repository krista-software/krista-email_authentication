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

import javax.inject.Inject;
import javax.inject.Named;
import app.krista.extension.executor.Invoker;
import org.jvnet.hk2.annotations.Service;

@Service
public class EmailInvokerAttributesProvider {

    private final Invoker invoker;
    private EmailInvokerAttributes attributes;

    @Inject
    public EmailInvokerAttributesProvider(@Named("self") Invoker invoker) {
        this.invoker = invoker;
    }

    public EmailInvokerAttributes getAttributes() {
        if (attributes == null) {
            load();
        }
        return attributes;
    }

    public synchronized void updateAttributes() {
        load();
    }

    private void load() {
        attributes = EmailInvokerAttributes.parse(invoker);
    }

}
