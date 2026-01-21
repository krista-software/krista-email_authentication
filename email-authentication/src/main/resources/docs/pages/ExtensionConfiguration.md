# Extension Configuration

## Overview

The Email Authentication Extension requires configuration of SMTP server settings and access control parameters. This page provides comprehensive guidance on setting up the extension for your Krista workspace.

## Configuration Parameters

The following table lists all configuration parameters available for the Email Authentication Extension:

| Parameter | Type | Required | Description | Example |
|-----------|------|----------|-------------|---------|
| **SMTP Host** | Text | Yes | The hostname or IP address of your SMTP mail server | `smtp.gmail.com` |
| **SMTP Port** | Text | Yes | The port number for SMTP communication | `465` (SSL), `587` (TLS) |
| **Email Address of Sender** | Text | Yes | The email address that appears as the sender of authentication emails | `noreply@company.com` |
| **Sender Account** | Text | Yes | The username/email for SMTP authentication | `your-email@gmail.com` |
| **Sender Password** | Text (Secured) | Yes | The password for SMTP authentication (encrypted) | `your-app-password` |
| **Supported Domains** | Text | Yes | Comma-separated list of allowed email domains (use `*` for all domains) | `company.com, partner.com` or `*` |
| **Allow New Account Creation** | Switch | Yes | Enable automatic creation of new user accounts | `true` or `false` |
| **Default Roles for New Accounts** | Text | Yes | Comma-separated list of roles assigned to new accounts | `Krista Client User` |
| **Use Default Mail Server** | Switch | Yes | Use Krista's default mail server (not yet supported) | `false` |

## Step-by-Step Setup

### Step 1: Access Extension Configuration

1. Log in to your Krista workspace as an administrator
2. Navigate to **Extensions** in the workspace settings
3. Click **Add Extension** or select the Email Authentication Extension
4. You will see the configuration form with all parameters

### Step 2: Configure Required SMTP Settings

#### SMTP Host
Enter the hostname of your SMTP server:
- **Gmail**: `smtp.gmail.com`
- **Office 365**: `smtp.office365.com`
- **Custom Server**: Your server's hostname or IP address

#### SMTP Port
Select the appropriate port based on your security requirements:
- **Port 465**: SSL/TLS encryption (recommended)
- **Port 587**: STARTTLS encryption
- **Port 25**: Unencrypted (not recommended for production)

#### Email Address of Sender
Enter the email address that will appear as the sender:
- Use a no-reply address like `noreply@yourcompany.com`
- Ensure this address is authorized to send from your SMTP server
- This address will be visible to users receiving authentication emails

### Step 3: Configure SMTP Authentication

SMTP authentication credentials are required for sending emails:

#### Sender Account
- Enter the username or email address for SMTP authentication
- For Gmail, use your full Gmail address
- For Office 365, use your full email address
- This is **required** for the extension to send emails

#### Sender Password
- Enter the password for SMTP authentication
- **For Gmail**: Use an [App Password](https://support.google.com/accounts/answer/185833), not your regular password
- **For Office 365**: Use your account password or app-specific password
- This field is encrypted and stored securely
- This is **required** for the extension to send emails

> **🔒 Security Note**: Passwords are encrypted in storage. Never share SMTP credentials.

### Step 4: Configure Domain Access Control

#### Supported Domains
Restrict authentication to specific email domains:
- Enter comma-separated domain names: `company.com, partner.com, contractor.com`
- Enter `*` to allow all domains
- Domain validation is case-insensitive
- Only users with email addresses from these domains can authenticate
- This is a **required** field

**Example**:
```
company.com, subsidiary.com
```

Or to allow all domains:
```
*
```

### Step 5: Configure Account Provisioning

#### Allow New Account Creation
Enable or disable automatic account creation:
- **Enabled (true)**: New users from allowed domains will have accounts created automatically
- **Disabled (false)**: Only existing users can authenticate

> **⚠️ Warning**: When disabled, users without existing accounts will receive an error.

#### Default Roles for New Accounts
Specify roles for newly created accounts (required):
- Enter comma-separated role names: `Krista Client User, Report Viewer`
- Roles must exist in the workspace before configuration
- Recommended default: `Krista Client User`
- All new accounts will receive these roles automatically
- This is a **required** field

**Example**:
```
Krista Client User, Basic User
```

> **📝 Note**: Create required roles in **People → Roles** before configuring this parameter.

### Step 6: Verify Configuration

After entering all parameters:

1. Click **Test Connection** (if available) to verify SMTP settings
2. Click **Save** to apply the configuration
3. The extension will validate your settings
4. If validation fails, review error messages and correct the configuration

## Configuration Examples

### Example 1: Gmail SMTP Configuration

```
SMTP Host: smtp.gmail.com
SMTP Port: 465
Email Address of Sender: noreply@yourcompany.com
Sender Account: your-email@gmail.com
Sender Password: your-16-character-app-password
Supported Domains: yourcompany.com
Allow New Account Creation: true
Default Roles for New Accounts: Krista Client User
Use Default Mail Server: false
```

### Example 2: Office 365 SMTP Configuration

```
SMTP Host: smtp.office365.com
SMTP Port: 587
Email Address of Sender: noreply@yourcompany.com
Sender Account: noreply@yourcompany.com
Sender Password: your-password
Supported Domains: yourcompany.com, partner.com
Allow New Account Creation: true
Default Roles for New Accounts: Krista Client User, Basic User
Use Default Mail Server: false
```

### Example 3: Custom SMTP Server

```
SMTP Host: mail.yourcompany.com
SMTP Port: 465
Email Address of Sender: krista@yourcompany.com
Sender Account: krista@yourcompany.com
Sender Password: secure-password
Supported Domains: yourcompany.com
Allow New Account Creation: false
Default Roles for New Accounts: Krista Client User
Use Default Mail Server: false
```

## Security Considerations

### SMTP Security Best Practices

1. **Always Use Encryption**
   - Use port 465 (SSL/TLS) or 587 (STARTTLS)
   - Never use port 25 (unencrypted) in production
   - Verify SSL/TLS certificates are valid

2. **Use App-Specific Passwords**
   - For Gmail, create an App Password instead of using your account password
   - For Office 365, consider using app-specific passwords
   - Rotate passwords periodically

3. **Secure Credential Storage**
   - SMTP passwords are encrypted in the Krista database
   - Use strong, unique passwords for SMTP accounts
   - Never share SMTP credentials

4. **Domain Restrictions**
   - Configure supported domains to limit access
   - Align with workspace domain settings
   - Consider both invoker-level and workspace-level restrictions

5. **Account Provisioning**
   - Only enable automatic account creation if needed
   - Carefully configure default roles
   - Review new accounts periodically

## Troubleshooting Configuration Issues

### Issue: "Default mail server not yet supported"

**Cause**: The "Use Default Mail Server" option is enabled.

**Resolution**:
1. Set "Use Default Mail Server" to `false`
2. Configure custom SMTP settings (host, port, credentials)
3. Save the configuration

### Issue: Configuration validation fails

**Cause**: Invalid or incomplete SMTP settings.

**Resolution**:
1. Verify all required fields are filled:
   - SMTP Host
   - SMTP Port
   - Email Address of Sender
2. Check SMTP credentials if authentication is required
3. Ensure SMTP server is accessible from your Krista instance
4. Test SMTP connection using a mail client

### Issue: Cannot save configuration

**Cause**: Missing required parameters or invalid values.

**Resolution**:
1. Review all required fields (marked with asterisk)
2. Verify email addresses are in valid format
3. Ensure port number is numeric
4. Check that specified roles exist in the workspace

### Issue: Roles not found

**Cause**: Specified default roles don't exist in the workspace.

**Resolution**:
1. Navigate to **People → Roles** in workspace settings
2. Create the required roles
3. Return to extension configuration
4. Enter the exact role names (case-sensitive)

## Advanced Configuration

### Multiple Domain Support

To support multiple email domains:

```
company.com, subsidiary.com, partner.com, contractor.com
```

- Separate domains with commas
- Spaces are automatically trimmed
- Domain matching is case-insensitive
- Subdomains must be explicitly listed

### Custom Role Assignment

To assign multiple roles to new accounts:

```
Krista Client User, Report Viewer, Dashboard User
```

- Separate role names with commas
- Role names are case-sensitive
- All roles must exist before configuration
- Roles are assigned immediately upon account creation

### Workspace Domain Integration

The extension respects workspace-level domain settings:

- If workspace has domain restrictions, they are enforced first
- Invoker-level domains (Supported Domains) provide additional filtering
- Both workspace and invoker domains must allow the email domain
- If "Allow New Account Creation" is disabled, workspace domains are checked

## Configuration Validation

The extension validates configuration when you save:

### SMTP Validation
- ✅ SMTP Host is not empty
- ✅ SMTP Port is numeric
- ✅ Email Address of Sender is valid email format
- ✅ If "Use Default Mail Server" is true, throws error (not supported)

### Domain Validation
- ✅ Supported Domains are valid domain names (if specified)
- ✅ No invalid characters in domain names

### Role Validation
- ✅ Default Roles exist in the workspace
- ✅ Role names are valid

## Next Steps

After configuring the extension:

1. **Test Authentication Flow** - See [Authentication](pages/Authentication.md)
2. **Review API Endpoints** - See [API Endpoints](pages/APIEndpoints.md)
3. **Integrate with Extensions** - See [Dependencies](pages/Dependencies.md)
4. **Troubleshoot Issues** - See [Troubleshooting](pages/Troubleshooting.md)

## See Also

- [Authentication](pages/Authentication.md) - Authentication flow and security
- [API Endpoints](pages/APIEndpoints.md) - REST API documentation
- [Dependencies](pages/Dependencies.md) - Using as a dependency
- [Troubleshooting](pages/Troubleshooting.md) - Common issues and solutions


