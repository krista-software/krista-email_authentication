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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import app.krista.extension.authorization.AuthorizationException;
import app.krista.ksdk.accounts.AccountManager;
import app.krista.ksdk.authentication.AuthenticationSettings;

public final class CommonUtils {

    public static boolean isValidDomain(String domainName) {

        // If the string is empty
        // return false
        if (domainName == null || domainName.isEmpty()) {
            return false;
        }

        // Regex to check valid domain name.
        String regex = "^((?!-)[A-Za-z0-9-]"
                + "{1,63}(?<!-)\\.)"
                + "+[A-Za-z]{2,6}";

        // Compile the ReGex
        Pattern p
                = Pattern.compile(regex);

        // Pattern class contains matcher()
        // method to find the matching
        // between the given string and
        // regular expression.
        Matcher m = p.matcher(domainName);

        // Return if the string
        // matched the ReGex
        return m.matches();
    }

    public static boolean isEmailDomainPresentInSupportedWorkspaceDomains(String email, String allDomains) {
        if (email == null || email.isBlank()) {
            return false;
        }
        if (allDomains == null || allDomains.isEmpty() || allDomains.equalsIgnoreCase("All")) {
            return true;
        } else {
            String[] splitDomains = allDomains.split(",");
            String[] splitEmail = email.split("@");
            for (String domain : splitDomains) {
                if (domain.contains(splitEmail[1])) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void addSupportedDomainsToWorkspace(String email, List<String> supportedDomainsForWorkspace,
            AuthenticationSettings authenticationSettings)
            throws AuthorizationException {
        if (email != null && supportedDomainsForWorkspace != null && authenticationSettings != null) {
            String[] splitEmail = email.split("@");
            if (splitEmail.length > 1 && !supportedDomainsForWorkspace.contains(splitEmail[1])) {
                authenticationSettings.addSupportedDomains(splitEmail[1]);
            }
        }
    }

    public static String getDate() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss Z");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return dateFormat.format(Calendar.getInstance().getTime());
    }

    public static boolean isValidEmail(String email) {
        //https://www.geeksforgeeks.org/check-email-address-valid-not-java/
        if (email == null) {
            return false;
        }
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\." +
                "[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                "A-Z]{2,7}$";
        Pattern pat = Pattern.compile(emailRegex);
        return pat.matcher(email).matches();
    }

    public static void validateDomains(Object supportedDomains) {
        if (!(supportedDomains instanceof String)) {
            throw new IllegalArgumentException("Input is not string type:" + supportedDomains);
        }
        if (((String) supportedDomains).isEmpty()) {
            throw new IllegalArgumentException("Supported domains list is empty.");
        }
        String supportedDomainsString = (String) supportedDomains;
        String[] domains = supportedDomainsString.split(",");
        if (domains.length == 0) {
            throw new IllegalArgumentException("List of domain is empty.");
        }
        for (String domain : domains) {
            String trimmedDomain = domain.trim();
            if (!trimmedDomain.equalsIgnoreCase("all") && !CommonUtils.isValidDomain(trimmedDomain)) {
                throw new IllegalArgumentException("Invalid domain name:" + domain);
            }
        }
    }

    public static void validateInputParams(Map<String, Object> invokerAttributes, Set<String> requiredInputs,
            Object supportedDomains) {
        if (invokerAttributes == null || invokerAttributes.isEmpty()) {
            throw new IllegalArgumentException("Invoker attributes are not found.");
        }
        if (requiredInputs == null || requiredInputs.isEmpty()) {
            throw new IllegalArgumentException("Required inputs are not found.");
        }
        Set<String> missingRequiredParams = new HashSet<>();
        for (String requiredParam : requiredInputs) {
            if (!invokerAttributes.containsKey(requiredParam) || invokerAttributes.get(requiredParam) == null ||
                    invokerAttributes.get(requiredParam).toString().isEmpty()) {
                missingRequiredParams.add(requiredParam);
            }
        }
        if (!missingRequiredParams.isEmpty()) {
            throw new IllegalArgumentException("Missing required invoker params :" + missingRequiredParams);
        }
        validateDomains(supportedDomains);
    }

    public static void validateIfSupportedDomain(String email, String allDomains, Object supportedDomains) {
        if (!isValidEmail(email)) {
            throw new IllegalArgumentException("Not valid email address: " + email);
        }
        if (!supportedDomains.toString().isEmpty()) {
            boolean isSupportedFromExtension = CommonUtils
                    .isEmailDomainPresentInSupportedWorkspaceDomains(email,
                            supportedDomains.toString());
            boolean isSupportedFromWorkSpace =
                    isEmailDomainPresentInSupportedWorkspaceDomains(email,
                            allDomains);
            if (!isSupportedFromExtension && !isSupportedFromWorkSpace) {
                throw new IllegalArgumentException(
                        "Domain " + email.substring(email.indexOf("@") + 1) + " is not supported.");
            }
        }
    }

    public static void validateAutoUserCreation(String workspaceId, String email, boolean allowAutoUserCreationBool,
            String supportedDomain, AccountManager accountManager) throws AuthorizationException {
        if (workspaceId == null || workspaceId.isEmpty()) {
            throw new IllegalArgumentException("WorkspaceId not found.");
        }
        if (!isValidEmail(email)) {
            throw new IllegalArgumentException("Not valid email address: " + email);
        }
        if (!allowAutoUserCreationBool) {
            boolean isEmailDomainPresent =
                    isEmailDomainPresentInSupportedWorkspaceDomains(email, supportedDomain);
            if (!isEmailDomainPresent) {
                throw new IllegalArgumentException(
                        "ALLOW_AUTO_PERSON_CREATION is not enabled and domain for email " + email +
                                " is not supported in workspace.");
            }

            if (accountManager.lookupAccount(email) == null) {
                throw new IllegalArgumentException(
                        "ALLOW_AUTO_PERSON_CREATION is not enabled. Can't add new user with email :" + email);
            }
        }
    }

}
