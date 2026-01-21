[<span style = "color : blue ; text-decoration: none">< back<span>](/)

# Release Notes - Email Authentication Extension

## Version 3.4.6

- **Krista Service APIs Java**: 1.0.113
- **Java Version**: 21
- **Release Date**: January 2026
- **Developer** : Vrushali Gaikwad

---

## What's New

### Documentation Enhancements
* Comprehensive documentation added with 5 detailed pages:
  - **Extension Configuration**: Complete setup guide with SMTP configuration examples
  - **Authentication**: Detailed authentication flow and security features
  - **API Endpoints**: Full REST API documentation with examples
  - **Dependencies**: Integration guide for using extension as a dependency
  - **Troubleshooting**: Extensive troubleshooting guide with solutions

* Added interactive documentation UI with search functionality
* Added Mermaid sequence diagrams for authentication flow visualization
* Added code examples in Java, JavaScript, Python, and bash

### Features
* **Email-based authentication** - Secure login via email verification links (users don't need passwords)
* **Time-limited verification links** - Links expire after 30 minutes for security
* **One-time use links** - Each verification link can only be used once
* **Domain restrictions** - Support for workspace-level and invoker-level domain filtering
* **Automatic account provisioning** - Create new accounts automatically with configurable roles
* **Session management** - Secure session creation and validation
* **SMTP configuration** - Required SMTP credentials for sending emails (Gmail, Office 365, custom servers)
* **Customizable email templates** - HTML email templates for verification links

### Security Features
* Secret code generation using UUID for verification links
* Session-based authentication with secure cookies
* HttpOnly and Secure cookie flags
* Domain validation for email addresses
* Role-based access control for new accounts
* Workspace admin rights required for configuration

---

## Resolved Bugs

* Fixed documentation tab not showing in UI by adding `@StaticResource` annotation
* Fixed documentation path from `/authn/docs` to `static/docs`
* Fixed field labels: Removed "(optional)" from Sender Account and Sender Password (they are required)
* Corrected documentation terminology from "passwordless" to "email-based authentication"
* Updated all configuration parameters to reflect correct required/optional status
* Improved error handling for SMTP connection failures
* Enhanced email validation for domain restrictions

---

## Configuration Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| SUPPORTED_DOMAINS | Text | Yes | Comma-separated list of allowed email domains |
| ALLOW_NEW_ACCOUNT_CREATION | Switch | Yes | Enable automatic account creation |
| DEFAULT_ROLES_FOR_NEW_ACCOUNT | Text | Yes | Comma-separated list of default roles |
| USE_DEFAULT_MAIL_SERVER | Switch | Yes | Use default mail server (not yet supported) |
| SENDER_EMAIL_ADDRESS | Text | Yes | Email address for sending verification links |
| SMTP_ACCOUNT | Text | Yes | SMTP account username |
| SMTP_PASSWORD | Text | Yes | SMTP account password (secured) |
| SMTP_HOST | Text | Yes | SMTP server hostname |
| SMTP_PORT | Text | Yes | SMTP server port (465 or 587) |

---

## API Endpoints

* `GET /authn/login` - Display login page
* `POST /authn/login` - Send verification link to email
* `GET /authn/` - Verify email link and create session
* `POST /authn/logout` - Logout and invalidate session
* `GET /authn/waiting` - Display waiting page after email sent

---

## Dependencies

* **Java Mail API**: `com.sun.mail:javax.mail:1.6.2`
* **C3P0 Connection Pool**: `com.mchange:c3p0:0.9.5.2`
* **Krista APIs**: Latest version
* **HK2 Dependency Injection**: Latest version

---

## Known Limitations

* Default mail server option is not yet supported
* Session storage is in-memory (not persisted across server restarts)
* Email templates are not customizable through UI (requires code changes)
* No rate limiting on email sending (may be added in future versions)

---

## Upgrade Notes

### From Previous Versions

1. **Rebuild the extension** after updating
2. **Update invoker configuration** with new SMTP settings if needed
3. **Verify documentation** is accessible at `/authn/static/docs/`
4. **Test authentication flow** with a test email address

### Breaking Changes

* None in this version

---

## Migration Guide

If migrating from another authentication extension:

1. **Export existing accounts** from current authentication system
2. **Configure email authentication** with appropriate domain restrictions
3. **Set default roles** for new account creation
4. **Test with a pilot group** before full deployment
5. **Update client applications** to use new authentication endpoints

---

## Support & Resources

* **Documentation**: Available at `/authn/static/docs/` in the extension
* **Issue Tracker**: [Krista JIRA](https://antbrains.atlassian.net/)
* **Support Email**: support@krista.ai

---

## Future Enhancements

* Persistent session storage (database-backed)
* Customizable email templates through UI
* Rate limiting for email sending
* Multi-factor authentication support
* Email verification code (alternative to link)
* Session timeout configuration through UI
* Default mail server support
* Email delivery status tracking

---

## License

Copyright © 2026 Krista Software. All rights reserved.

