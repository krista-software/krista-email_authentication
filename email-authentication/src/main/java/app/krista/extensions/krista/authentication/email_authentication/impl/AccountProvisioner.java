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

import java.util.*;
import javax.inject.Inject;
import app.krista.extension.authorization.AuthorizationException;
import app.krista.extension.common.CommonUtils;
import app.krista.extensions.krista.authentication.email_authentication.utils.EmailAddresses;
import app.krista.ksdk.accounts.Account;
import app.krista.ksdk.accounts.AccountManager;
import app.krista.ksdk.accounts.ModifiableAccount;
import app.krista.ksdk.authorization.ModifiableRole;
import app.krista.ksdk.authorization.Role;
import app.krista.ksdk.authorization.RoleManager;
import org.jvnet.hk2.annotations.Service;

/**
 * Create or fetch workspace account with given IdentificationToken.
 */
@Service
public final class AccountProvisioner {

    private final AccountManager accountManager;
    private final RoleManager roleManager;

    @Inject
    public AccountProvisioner(AccountManager accountManager, RoleManager roleManager) {
        this.accountManager = accountManager;
        this.roleManager = roleManager;
    }

    /**
     * Lookup account with given email and roleIds if not found create one.
     *
     * @param emailAddress
     * @param roleIds
     * @return Account
     */
    public Account provisionAccount(String emailAddress, List<String> roleIds) {
        ModifiableAccount modifiableAccount = accountManager.lookupAccount(emailAddress);
        List<String> roleNames = ensureHasAllRoles(modifiableAccount, roleIds, roleManager);
        if (modifiableAccount == null) {
            return accountManager.createAccount(EmailAddresses.getLocalPart(emailAddress),
                    EmailAddresses.normalizeEmailAddress(emailAddress),
                    new LinkedHashSet<>(roleNames), provisionUserAttributes(emailAddress));
        }
        return modifiableAccount.unmodifiable();
    }

    private List<String> ensureHasAllRoles(ModifiableAccount modifiableAccount, List<String> roles,
            RoleManager roleManager)
            throws AuthorizationException {
        List<String> allRoles = new ArrayList<>();
        Iterable<ModifiableRole> workspaceRoles = roleManager.getRoles();
        for (String accountRole : roles) {
            boolean roleExit = false;
            for (ModifiableRole workspaceRole : workspaceRoles) {
                if (Objects.equals(accountRole, workspaceRole.getName())) {
                    allRoles.add(workspaceRole.getRoleId());
                    roleExit = true;
                    break;
                }
            }
            if (!roleExit) {
                ModifiableRole role = roleManager.createRole(accountRole);
                allRoles.add(role.getRoleId());
            }
        }
        if (modifiableAccount != null) {
            for (Role role : modifiableAccount.getRoles()) {
                allRoles.add(role.getRoleId());
            }
            modifiableAccount.addRole(allRoles.toArray(String[]::new));
        }
        return allRoles;
    }

    private Map<String, Object> provisionUserAttributes(String email) {
        return
                Map.of("ORG", EmailAddresses.getDomainName(email), "KRISTA_SOURCE",
                        "EXTENSION_EMAIL_AUTHENTICATION", "KRISTA_LAST_LOGIN",
                        CommonUtils.getDate());
    }

}
