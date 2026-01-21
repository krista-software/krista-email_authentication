# Email Authentication Extension

## Overview

The **Email Authentication Extension** provides secure email-based authentication for Krista applications through email verification links. This extension enables users to authenticate by entering their email address and clicking a verification link sent to their inbox, without requiring them to remember passwords.

When a user attempts to log in, they receive an email containing a time-limited verification link. Clicking this link completes the authentication process and grants access to the application. The extension requires SMTP server configuration and supports domain-based access control and automatic account provisioning.

## Key Features

✅ **Email-Based Authentication** - Users authenticate by clicking verification links sent to their email
✅ **No Password Required for Users** - Users don't need to remember passwords (SMTP credentials required for setup)
✅ **Time-Limited Verification Links** - Secure verification codes with 30-minute expiry
✅ **Configurable SMTP** - Support for any SMTP server (Gmail, Office 365, custom servers)
✅ **Domain-Based Access Control** - Restrict access to specific email domains
✅ **Automatic Account Provisioning** - Create new user accounts automatically
✅ **Role-Based Access** - Assign default roles to new accounts
✅ **Secure Session Management** - HTTP-only cookies with secure flags
✅ **One-Time Use Links** - Verification links can only be used once
✅ **SSL/TLS Support** - Encrypted email transmission

## Quick Start Guide

### 1. Configure SMTP Settings
Set up your SMTP server configuration with host, port, sender account, and password (all required).

### 2. Configure Domain Access
Specify which email domains are allowed to authenticate (required - use `*` for all domains).

### 3. Enable Account Provisioning
Choose whether to automatically create accounts for new users.

### 4. Set Default Roles
Define which roles new accounts should receive.

### 5. Test Authentication
Verify the authentication flow works correctly.

## Documentation Structure

### Getting Started
- **[Extension Configuration](pages/ExtensionConfiguration.md)** - Complete setup and configuration guide
- **[Authentication](pages/Authentication.md)** - Authentication flow and security details

### API Reference
- **[API Endpoints](pages/APIEndpoints.md)** - REST API documentation for all endpoints

### Additional Resources
- **[Dependencies](pages/Dependencies.md)** - How to use this extension as a dependency
- **[Troubleshooting](pages/Troubleshooting.md)** - Common issues and solutions
- **[Release Notes](pages/ReleaseNotes.md)** - Version history and changelog

## Support & Resources

- **Extension Version**: 3.4.6
- **Java Version**: 21
- **JAX-RS Base Path**: `/authn`
- **Documentation**: Available at `/authn/static/docs/`

## Authentication Flow

1. User enters email address on login page
2. System validates email and sends verification link
3. User clicks link in email
4. System verifies link and creates session
5. User is redirected to original requested URL

## Security Features

🔒 **Time-Limited Links** - 30-minute expiration
🔒 **One-Time Use** - Links cannot be reused
🔒 **State Validation** - Links validated against current state
🔒 **Domain Validation** - Email domains checked against allowed list
🔒 **Secure Cookies** - HTTP-only and secure flags enabled
🔒 **SSL/TLS** - Encrypted SMTP communication

---

For detailed information, please refer to the documentation pages listed above.

