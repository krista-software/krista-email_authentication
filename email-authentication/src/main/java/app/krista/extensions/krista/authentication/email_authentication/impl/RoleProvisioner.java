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
import app.krista.extension.authorization.AuthorizationException;
import app.krista.extension.executor.Invoker;
import app.krista.extensions.krista.authentication.email_authentication.EmailAuthenticationConstants;
import app.krista.ksdk.authorization.ModifiableRole;
import app.krista.ksdk.authorization.RoleManager;
import org.jvnet.hk2.annotations.Service;

import static app.krista.extensions.krista.authentication.email_authentication.EmailAuthenticationConstants.*;

/**
 * Create or fetch workspace roles.
 */
@Service
public final class RoleProvisioner {

    private final Invoker invoker;
    private final RoleManager roleManager;

    @Inject
    public RoleProvisioner(@Named("self") Invoker invoker, RoleManager roleManager) {
        this.invoker = invoker;
        this.roleManager = roleManager;
    }

    /**
     * This method retrieves a role from the workspace that matches the 'Default Role Name'.
     * This method will create a new role in workspace if the matching role does not exist.
     *
     * @return ModifiableRole
     * @throws AuthorizationException
     */
    public ModifiableRole provisionDefaultRole() throws AuthorizationException {
        return getModifiableRole(getDefaultRoleName());
    }

    private ModifiableRole getModifiableRole(String roleName) {
        ModifiableRole modifiableRole = roleManager.lookupRole(roleName);
        if (null == modifiableRole) {
            roleManager.createRole(roleName);
        }
        return modifiableRole;
    }

    /**
     * Verify the invoker parameter values for @DEFAULT_ROLES_FOR_NEW_ACCOUNT if not present
     * returns @DEFAULT_ROLE
     *
     * @return String
     */
    private String getDefaultRoleName() {
        String defaultRole = DEFAULT_ROLE;
        Object defaultRoleObject =
                invoker.getAttributes().get(EmailAuthenticationConstants.DEFAULT_ROLES_FOR_NEW_ACCOUNT);
        if (defaultRoleObject instanceof String && !((String) defaultRoleObject).isBlank()) {
            defaultRole = (String) defaultRoleObject;
        }
        return defaultRole;
    }

}
