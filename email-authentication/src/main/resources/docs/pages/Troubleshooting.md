# Troubleshooting

## Overview

This page provides solutions to common issues encountered when using the Email Authentication Extension. Issues are organized by category for easy navigation.

## Configuration Issues

### Issue: "Default mail server not yet supported"

**Symptoms**:
- Error message when saving configuration
- Cannot enable "Use Default Mail Server" option

**Cause**: The default mail server feature is not yet implemented.

**Resolution**:
1. Set "Use Default Mail Server" to `false`
2. Configure custom SMTP settings:
   - SMTP Host
   - SMTP Port
   - Email Address of Sender
   - Sender Account (if authentication required)
   - Sender Password (if authentication required)
3. Save the configuration

**Example Configuration**:
```
Use Default Mail Server: false
SMTP Host: smtp.gmail.com
SMTP Port: 465
Email Address of Sender: noreply@company.com
Sender Account: your-email@gmail.com
Sender Password: your-app-password
```

---

### Issue: Configuration validation fails

**Symptoms**:
- Cannot save extension configuration
- Error messages about missing or invalid fields

**Cause**: Required fields are missing or contain invalid values.

**Resolution**:
1. Verify all required fields are filled:
   - ✅ SMTP Host (not empty)
   - ✅ SMTP Port (numeric value)
   - ✅ Email Address of Sender (valid email format)
2. Check optional fields:
   - Sender Account and Password (if SMTP requires authentication)
   - Supported Domains (valid domain names, comma-separated)
   - Default Roles (must exist in workspace)
3. Ensure "Use Default Mail Server" is set to `false`
4. Save the configuration

**Validation Checklist**:
- [ ] SMTP Host is not empty
- [ ] SMTP Port is a number (e.g., 465, 587)
- [ ] Email Address of Sender is in format: user@domain.com
- [ ] Use Default Mail Server is false
- [ ] Default Roles exist in workspace (if specified)

---

### Issue: "Role not found" error

**Symptoms**:
- Error when saving configuration
- Message: "Role '[role name]' not found"

**Cause**: Specified default role doesn't exist in the workspace.

**Resolution**:
1. Navigate to **People → Roles** in workspace settings
2. Check if the role exists
3. If role doesn't exist:
   - Create the role with the exact name
   - Or update the "Default Roles for New Accounts" configuration to use an existing role
4. Role names are case-sensitive, ensure exact match
5. Save the configuration

**Example**:
```
Default Roles for New Accounts: Krista Client User

If "Krista Client User" doesn't exist:
1. Go to People → Roles
2. Create role named "Krista Client User" (exact match)
3. Return to extension configuration
4. Save
```

---

## Email Delivery Issues

### Issue: User doesn't receive verification email

**Symptoms**:
- User submits email address
- "Check your email" page is shown
- No email received

**Possible Causes**:
1. Email in spam/junk folder
2. SMTP configuration incorrect
3. Email address typo
4. SMTP server issues
5. Firewall blocking SMTP connections

**Resolution Steps**:

**Step 1: Check Spam Folder**
- Ask user to check spam/junk folder
- Add sender email to safe senders list

**Step 2: Verify SMTP Configuration**
```
1. Check SMTP Host is correct
2. Verify SMTP Port (465 for SSL, 587 for TLS)
3. Confirm Sender Account and Password are correct
4. For Gmail: Use App Password, not account password
```

**Step 3: Test SMTP Connection**
```bash
# Test SMTP connection using telnet
telnet smtp.gmail.com 465

# Or use openssl for SSL connection
openssl s_client -connect smtp.gmail.com:465
```

**Step 4: Check Firewall Rules**
```
1. Verify outbound connections allowed on SMTP port
2. Check if SMTP server is reachable from Krista server
3. Review firewall logs for blocked connections
```

**Step 5: Review SMTP Server Logs**
- Check SMTP server logs for errors
- Look for authentication failures
- Check for rate limiting or quota issues

**Step 6: Verify Sender Email**
- Ensure sender email is authorized to send from SMTP server
- Check SPF, DKIM, DMARC records
- Verify email domain is valid

---

### Issue: "Failed to send email" error

**Symptoms**:
- Error message when submitting email
- User doesn't receive verification link

**Cause**: SMTP server unreachable or configuration incorrect.

**Resolution**:

**For Gmail**:
```
1. Enable "Less secure app access" or use App Password
2. Create App Password:
   - Go to Google Account settings
   - Security → App passwords
   - Generate password for "Mail"
   - Use this password in extension configuration
3. SMTP Settings:
   - Host: smtp.gmail.com
   - Port: 465
   - Account: your-email@gmail.com
   - Password: 16-character app password
```

**For Office 365**:
```
1. SMTP Settings:
   - Host: smtp.office365.com
   - Port: 587
   - Account: your-email@company.com
   - Password: your-password
2. Ensure account has permission to send email
3. Check if MFA is enabled (may require app password)
```

**For Custom SMTP Server**:
```
1. Verify SMTP server is running
2. Check authentication requirements
3. Verify SSL/TLS configuration
4. Test connection from Krista server
```

---

## Authentication Issues

### Issue: "Invalid email format" error

**Symptoms**:
- Error when submitting email address
- Message: "Email address '[email]' is not valid."

**Cause**: Email doesn't match required format.

**Resolution**:
1. Ensure email is in format: `user@domain.com`
2. Email must contain:
   - Username part (before @)
   - @ symbol
   - Domain name
   - Top-level domain (2-7 characters)
3. Valid examples:
   - ✅ john.doe@company.com
   - ✅ user+tag@example.co.uk
   - ✅ first_last@sub.domain.com
4. Invalid examples:
   - ❌ invalid-email
   - ❌ @domain.com
   - ❌ user@
   - ❌ user@domain

**Email Validation Pattern**:
```
^[a-z0-9_+&*-]+(?:\.[a-z0-9_+&*-]+)*@(?:[a-z0-9-]+\.)+[a-z]{2,7}$
```

---

### Issue: "Domain not supported" error

**Symptoms**:
- Error when submitting email
- Message: "ALLOW_AUTO_PERSON_CREATION is not enabled and domain for email '[email]' is not supported in workspace."

**Cause**: Email domain is not in the allowed domains list.

**Resolution**:

**Option 1: Add Domain to Supported Domains**
```
1. Go to extension configuration
2. Find "Supported Domains" parameter
3. Add the domain (e.g., company.com)
4. Multiple domains: company.com, partner.com
5. Save configuration
```

**Option 2: Enable Auto Account Creation**
```
1. Go to extension configuration
2. Set "Allow New Account Creation" to true
3. Configure "Default Roles for New Accounts"
4. Save configuration
```

**Option 3: Add Domain to Workspace**
```
1. Go to workspace settings
2. Navigate to domain configuration
3. Add the email domain
4. Save workspace settings
```

**Domain Validation Logic**:
```
1. If workspace has domain restrictions:
   - Email domain must be in workspace domains
2. If invoker has supported domains:
   - Email domain must be in invoker domains
3. Both checks must pass
```

---

### Issue: "Account not found" error

**Symptoms**:
- Error when submitting email
- Message: "Account not found"

**Cause**: Account doesn't exist and automatic account creation is disabled.

**Resolution**:

**Option 1: Enable Auto Account Creation**
```
1. Go to extension configuration
2. Set "Allow New Account Creation" to true
3. Configure "Default Roles for New Accounts"
4. Add email domain to "Supported Domains" (if needed)
5. Save configuration
```

**Option 2: Manually Create Account**
```
1. Go to workspace settings
2. Navigate to People → Accounts
3. Click "Add Account"
4. Enter user's email address
5. Assign appropriate roles
6. Save account
```

---

## Verification Link Issues

### Issue: "Email verification link is expired"

**Symptoms**:
- Error when clicking verification link
- Message: "Email verification link is expired."

**Cause**: More than 30 minutes have passed since the link was generated.

**Resolution**:
1. Request a new verification link:
   - Go to login page
   - Enter email address
   - Click "Send Login Link"
2. Check email for new verification link
3. Click the link within 30 minutes
4. If emails are delayed, check SMTP configuration

**Prevention**:
- Click verification links promptly
- Check spam folder immediately
- Ensure email delivery is fast (check SMTP settings)

---

### Issue: "Email verification link is USED"

**Symptoms**:
- Error when clicking verification link
- Message: "Email verification link is USED."

**Cause**: The verification link has already been used for authentication.

**Resolution**:
1. If you're already logged in:
   - Navigate to the desired page
   - No need to click the link again
2. If you're not logged in:
   - Request a new verification link
   - Use the new link to authenticate

**Note**: Each verification link can only be used once for security reasons.

---

### Issue: "Email verification link is not found"

**Symptoms**:
- Error when clicking verification link
- Message: "Email verification link is not found."

**Possible Causes**:
1. Invalid or corrupted verification code
2. Link was manually removed
3. Server restart cleared session storage
4. Link was already used and removed

**Resolution**:
1. Request a new verification link
2. Ensure you're clicking the complete link from the email
3. Don't manually edit the verification code
4. Copy and paste the entire URL if clicking doesn't work

---

### Issue: Verification link doesn't redirect to original page

**Symptoms**:
- Authentication succeeds
- User is redirected to home page instead of original page

**Cause**: Original URI parameter is missing or incorrect.

**Resolution**:
1. Ensure `X-Krista-Original-URI` parameter is included in login URL
2. Check that the parameter is preserved through the authentication flow
3. Verify URL encoding is correct

**Example**:
```
Correct:
/authn/login?X-Krista-Original-URI=/dashboard

Incorrect:
/authn/login
```

---

## Session Issues

### Issue: Session not persisting across requests

**Symptoms**:
- User is authenticated but immediately logged out
- Session cookie not being saved

**Possible Causes**:
1. Browser blocking cookies
2. HTTPS not being used
3. Cookie domain mismatch
4. Cookie attributes incorrect

**Resolution**:

**Step 1: Enable Cookies in Browser**
```
Chrome:
1. Settings → Privacy and security → Cookies
2. Enable "Allow all cookies"

Firefox:
1. Settings → Privacy & Security
2. Set "Standard" or "Custom" (allow cookies)

Safari:
1. Preferences → Privacy
2. Uncheck "Block all cookies"
```

**Step 2: Verify HTTPS**
```
1. Ensure Krista appliance is accessed via HTTPS
2. Check SSL certificate is valid
3. Verify Secure flag is set on cookies
```

**Step 3: Check Cookie Attributes**
```
Expected cookie attributes:
- Name: X-Krista-Session-Id
- HttpOnly: true
- Secure: true
- Path: /
- SameSite: Lax or Strict
```

**Step 4: Check Browser Console**
```
1. Open browser developer tools (F12)
2. Go to Console tab
3. Look for cookie-related errors
4. Check Application/Storage tab for cookies
```

---

### Issue: Session expires too quickly

**Symptoms**:
- User is logged out after short period of inactivity
- Session doesn't last as long as expected

**Cause**: Session timeout is too short or session is being cleared.

**Resolution**:
1. Check session timeout configuration
2. Verify session storage is persistent
3. Ensure session cleanup isn't too aggressive
4. Consider implementing "remember me" functionality

---

## Browser-Specific Issues

### Issue: Authentication works in Chrome but not Safari

**Symptoms**:
- Authentication succeeds in Chrome
- Fails in Safari with same configuration

**Cause**: Safari has stricter cookie policies.

**Resolution**:
1. Ensure HTTPS is used
2. Verify SameSite cookie attribute is set correctly
3. Check Safari's "Prevent cross-site tracking" setting
4. Ensure cookies are first-party (same domain)

**Safari Settings**:
```
1. Safari → Preferences → Privacy
2. Uncheck "Prevent cross-site tracking" (for testing)
3. Uncheck "Block all cookies"
```

---

### Issue: Authentication fails in incognito/private mode

**Symptoms**:
- Authentication works in normal mode
- Fails in incognito/private browsing mode

**Cause**: Incognito mode blocks third-party cookies by default.

**Resolution**:
1. This is expected behavior for enhanced privacy
2. Users should use normal browsing mode for authentication
3. Ensure cookies are first-party (same domain as application)
4. Consider alternative authentication methods for incognito users

---

## Network Issues

### Issue: SMTP connection timeout

**Symptoms**:
- Long delay when sending verification email
- Eventually fails with timeout error

**Cause**: Network connectivity issues or firewall blocking SMTP port.

**Resolution**:

**Step 1: Test Network Connectivity**
```bash
# Test if SMTP server is reachable
ping smtp.gmail.com

# Test if SMTP port is open
telnet smtp.gmail.com 465
nc -zv smtp.gmail.com 465
```

**Step 2: Check Firewall Rules**
```
1. Verify outbound connections allowed on SMTP port
2. Check if SMTP server IP is whitelisted
3. Review firewall logs for blocked connections
4. Add firewall rule if needed
```

**Step 3: Try Alternative Port**
```
If port 465 is blocked, try port 587:
1. Update SMTP Port to 587
2. Use STARTTLS instead of SSL
3. Save configuration and test
```

**Step 4: Check SMTP Server Status**
```
1. Verify SMTP server is running
2. Check server status page
3. Review server logs for issues
4. Contact SMTP provider support
```

---

## Performance Issues

### Issue: Slow email delivery

**Symptoms**:
- Long delay between requesting link and receiving email
- Users complain about slow authentication

**Possible Causes**:
1. SMTP server is slow or overloaded
2. Network latency
3. Email queuing delays
4. Spam filtering delays

**Resolution**:

**Step 1: Check SMTP Server Performance**
```
1. Monitor SMTP server response times
2. Check server load and capacity
3. Review server logs for delays
4. Consider using faster SMTP provider
```

**Step 2: Optimize SMTP Configuration**
```
1. Use geographically closer SMTP server
2. Enable connection pooling
3. Reduce email size (minimal HTML)
4. Use dedicated SMTP service (SendGrid, Mailgun)
```

**Step 3: Monitor Email Delivery**
```
1. Track email delivery times
2. Set up alerts for slow delivery
3. Monitor bounce rates
4. Review spam filter logs
```

---

## Security Issues

### Issue: Verification links being flagged as spam

**Symptoms**:
- Verification emails go to spam folder
- Users don't receive emails in inbox

**Cause**: Email authentication records not configured or sender reputation issues.

**Resolution**:

**Step 1: Configure SPF Record**
```
Add SPF record to DNS:
v=spf1 include:_spf.google.com ~all

For custom SMTP:
v=spf1 ip4:YOUR_SMTP_IP ~all
```

**Step 2: Configure DKIM**
```
1. Generate DKIM key pair
2. Add DKIM public key to DNS
3. Configure SMTP server to sign emails with DKIM
```

**Step 3: Configure DMARC**
```
Add DMARC record to DNS:
v=DMARC1; p=quarantine; rua=mailto:dmarc@yourdomain.com
```

**Step 4: Improve Sender Reputation**
```
1. Use dedicated IP for sending
2. Warm up IP gradually
3. Monitor bounce rates
4. Remove invalid email addresses
5. Avoid spam trigger words in email content
```

---

## Diagnostic Tools

### Check SMTP Configuration

```bash
# Test SMTP connection with openssl
openssl s_client -connect smtp.gmail.com:465 -crlf

# Send test email
EHLO localhost
AUTH LOGIN
[base64 encoded username]
[base64 encoded password]
MAIL FROM: <sender@example.com>
RCPT TO: <recipient@example.com>
DATA
Subject: Test Email
Test message
.
QUIT
```

### Check Email Headers

```
1. Receive verification email
2. View email source/headers
3. Check for:
   - SPF: PASS
   - DKIM: PASS
   - DMARC: PASS
   - Received headers (delivery path)
   - Spam score
```

### Check Browser Cookies

```javascript
// Open browser console and run:
document.cookie

// Should show:
// X-Krista-Session-Id=...; path=/; secure; httponly
```

### Check Session Storage

```java
// In Krista extension code:
SessionManager sessionManager = ...;
String sessionId = "...";
Account account = sessionManager.getAccount(sessionId);
System.out.println("Account: " + (account != null ? account.getEmail() : "null"));
```

---

## Common Error Messages

| Error Message | Cause | Resolution |
|---------------|-------|------------|
| "Email address is required" | Email field is empty | Enter an email address |
| "Email address '[email]' is not valid." | Invalid email format | Use format: user@domain.com |
| "Domain not supported" | Email domain not allowed | Add domain to configuration or enable auto-creation |
| "Account not found" | Account doesn't exist | Enable auto-creation or create account manually |
| "Email verification link is expired." | Link older than 30 minutes | Request new verification link |
| "Email verification link is USED." | Link already used | Request new verification link |
| "Email verification link is not found." | Invalid verification code | Request new verification link |
| "Failed to send email" | SMTP configuration error | Check SMTP settings and connectivity |
| "SMTP authentication failed" | Invalid SMTP credentials | Verify sender account and password |
| "Default mail server not yet supported" | Use Default Mail Server is true | Set to false and configure custom SMTP |

---

## Getting Help

If you're still experiencing issues after trying these troubleshooting steps:

### 1. Collect Diagnostic Information

```
- Extension version
- Krista version
- Browser and version
- Operating system
- SMTP provider (Gmail, Office 365, custom)
- Error messages (exact text)
- Steps to reproduce
- Screenshots (if applicable)
```

### 2. Check Logs

```
- Krista application logs
- SMTP server logs
- Browser console logs
- Network traffic (browser developer tools)
```

### 3. Contact Support

```
- Email: support@krista.ai
- Include diagnostic information
- Attach relevant logs
- Describe issue in detail
```

## See Also

- [Extension Configuration](pages/ExtensionConfiguration.md) - Configuration guide
- [Authentication](pages/Authentication.md) - Authentication flow
- [API Endpoints](pages/APIEndpoints.md) - API documentation
- [Dependencies](pages/Dependencies.md) - Using as a dependency
