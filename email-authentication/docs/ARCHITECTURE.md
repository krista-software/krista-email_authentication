# Email Authentication Extension - Architecture Documentation

## Table of Contents
1. [Overview](#overview)
2. [Architectural Layers](#architectural-layers)
3. [Component Architecture](#component-architecture)
4. [Data Flow](#data-flow)
5. [Performance Characteristics](#performance-characteristics)
6. [Error Scenarios](#error-scenarios)
7. [Security Architecture](#security-architecture)
8. [Deployment Architecture](#deployment-architecture)

---

## Overview

### Extension Metadata
- **Name**: Email Authentication Extension
- **Version**: 3.5.7
- **Java Version**: Java 21
- **Platform**: Krista 3.4.0+
- **JAX-RS Base Path**: `/authn`
- **Domain**: Authentication (Krista Ecosystem)

### Purpose
The Email Authentication Extension provides passwordless authentication for Krista applications through email verification links. Users authenticate by entering their email address and clicking a time-limited verification link sent to their inbox.

### Key Architectural Principles
- **Layered Architecture**: 5-layer separation of concerns
- **Stateless REST API**: No server-side state between requests (except sessions)
- **Dependency Injection**: HK2-based DI for loose coupling
- **Platform Integration**: Leverages Krista platform services
- **Security-First**: Multiple security layers and validations

---

## Architectural Layers

The extension follows a strict 5-layer architecture pattern:

```
┌─────────────────────────────────────────────────────────────┐
│                    Layer 1: Presentation                     │
│              (REST API / HTTP Request Handling)              │
├─────────────────────────────────────────────────────────────┤
│                  Layer 2: Business Logic                     │
│           (Authentication Logic & Orchestration)             │
├─────────────────────────────────────────────────────────────┤
│                    Layer 3: Service Layer                    │
│        (Session, Account, Email, Verification Services)      │
├─────────────────────────────────────────────────────────────┤
│                  Layer 4: Data Access Layer                  │
│              (Platform Services & Persistence)               │
├─────────────────────────────────────────────────────────────┤
│                    Layer 5: Utility Layer                    │
│            (Validation, Formatting, Common Utils)            │
└─────────────────────────────────────────────────────────────┘
```

### Layer 1: Presentation Layer

**Purpose**: Handle HTTP requests/responses and expose REST endpoints

**Components**:
- `EmailAuthenticationResource` - Main REST resource class
- `EmailAuthenticationApplication` - JAX-RS application configuration
- `EmailResponseFactory` - HTTP response creation with headers/cookies

**Responsibilities**:
- Route HTTP requests to appropriate handlers
- Parse request parameters (query, form, cookies)
- Create HTTP responses with proper status codes
- Set cookies and headers
- Handle content negotiation (HTML/JSON)

**Technologies**: JAX-RS 2.1, Servlet API 4.0

### Layer 2: Business Logic Layer

**Purpose**: Implement core authentication logic and orchestration

**Components**:
- `EmailService` - Core authentication service
- `EmailAuthenticationRequestAuthenticator` - Platform authenticator implementation
- `EmailInvokerAttributes` - Configuration model
- `EmailInvokerAttributesProvider` - Cached configuration provider

**Responsibilities**:
- Validate email addresses and domains
- Orchestrate authentication flow
- Provision accounts and roles
- Generate verification links
- Verify verification codes
- Create authenticated sessions
- Implement RequestAuthenticator interface

**Key Methods**:
- `sendLoginLink(String originalUrl, String email)` - Initiate authentication
- `verifySecretLink(String originalUrl, String code)` - Complete authentication
- `logout(String sessionId)` - Terminate session

### Layer 3: Service Layer

**Purpose**: Provide specialized services for specific concerns

**Components**:

#### Account Management
- `AccountProvisioner` - Account creation and lookup
- `RoleProvisioner` - Role creation and assignment

#### Session Management
- `SessionManager` - Session lifecycle management
- `VerificationLinkManager` - Verification link storage and retrieval

#### Email Services
- `EmailSender` - Asynchronous email delivery
- `MailSessionProvider` - SMTP session configuration
- `EmailConfiguration` - Email server configuration parser

#### Location Services
- `LocationProvider` - URL construction for verification links

**Responsibilities**:
- Manage session creation, retrieval, and deletion
- Store and retrieve verification link details
- Send emails asynchronously via SMTP
- Provision accounts with proper roles
- Generate verification URLs
- Interact with platform services (AccountManager, RoleManager)

**Key Patterns**:
- **Service Pattern**: Each service has single responsibility
- **Provider Pattern**: Configuration and session providers
- **Manager Pattern**: Lifecycle management for sessions and links

### Layer 4: Data Access Layer

**Purpose**: Persist and retrieve data using platform services

**Platform Services Used**:
- `KeyValueStore` - Persistent key-value storage for sessions and verification links
- `AccountManager` - Krista account management
- `RoleManager` - Krista role management
- `AuthorizationContext` - Current authorization context

**Data Stored**:
1. **Sessions**: `session-{uuid}` → `accountId`
2. **Verification Links**: `secret-{uuid}` → `VerificationLinkDetails` (JSON)

**Storage Characteristics**:
- No expiration mechanism (manual cleanup required)
- JSON serialization for complex objects
- String-based keys with prefixes

### Layer 5: Utility Layer

**Purpose**: Provide reusable utilities and validation

**Components**:
- `EmailAddresses` - Email validation and parsing
- `Cookies` - Cookie parsing and extraction
- `DomainNames` - Domain validation
- `Attributes` - Attribute extraction utilities
- `Resources` - Resource loading utilities

**Responsibilities**:
- Validate email addresses using regex
- Extract domain and local parts from emails
- Parse HTTP cookies
- Validate domain names against allowed list
- Load static resources

---

## Component Architecture

### Core Components Diagram

```
┌──────────────────────────────────────────────────────────────────┐
│                   EmailAuthenticationExtension                    │
│                    (Extension Entry Point)                        │
└────────────────────────┬─────────────────────────────────────────┘
                         │
         ┌───────────────┼───────────────┐
         │               │               │
         ▼               ▼               ▼
┌─────────────────┐ ┌─────────────┐ ┌──────────────────────┐
│ REST Resources  │ │ Authenticator│ │ Configuration        │
│ (JAX-RS)        │ │              │ │ Validation           │
└────────┬────────┘ └──────┬──────┘ └──────────────────────┘
         │                 │
         ▼                 ▼
┌──────────────────────────────────────┐
│         EmailService                  │
│    (Core Business Logic)              │
└───────────┬──────────────────────────┘
            │
    ┌───────┼───────┬──────────┬────────────┐
    ▼       ▼       ▼          ▼            ▼
┌─────┐ ┌─────┐ ┌──────┐ ┌─────────┐ ┌──────────┐
│Acct │ │Role │ │Email │ │Session  │ │Verif Link│
│Prov │ │Prov │ │Sender│ │Manager  │ │Manager   │
└──┬──┘ └──┬──┘ └───┬──┘ └────┬────┘ └────┬─────┘
   │       │        │         │            │
   └───────┴────────┴─────────┴────────────┘
                     │
                     ▼
            ┌────────────────┐
            │ KeyValueStore  │
            │ (Platform)     │
            └────────────────┘
```

### Dependency Injection Graph

```
EmailAuthenticationExtension
  ├─→ EmailAuthenticationRequestAuthenticator
  │     ├─→ SessionManager
  │     │     └─→ KeyValueStore
  │     └─→ EmailService
  │           ├─→ AccountProvisioner
  │           │     ├─→ AccountManager
  │           │     └─→ RoleManager
  │           ├─→ RoleProvisioner
  │           │     ├─→ Invoker
  │           │     └─→ RoleManager
  │           ├─→ SessionManager
  │           ├─→ VerificationLinkManager
  │           │     └─→ KeyValueStore
  │           ├─→ EmailInvokerAttributesProvider
  │           │     └─→ Invoker
  │           └─→ LocationProvider
  │                 └─→ RoutingInfo
  └─→ EmailInvokerAttributesProvider
```

---

## Data Flow

### Authentication Flow - Login Request

#### Sequence Diagram: Send Login Link

```
User Browser          REST API          EmailService       AccountProv.    VerifLinkMgr    EmailSender    KeyValueStore
     │                   │                    │                  │               │              │               │
     │  POST /login      │                    │                  │               │              │               │
     │  email=user@ex.com│                    │                  │               │              │               │
     ├──────────────────>│                    │                  │               │              │               │
     │                   │ sendLoginLink()    │                  │               │              │               │
     │                   ├───────────────────>│                  │               │              │               │
     │                   │                    │ validate email   │               │              │               │
     │                   │                    │ validate domain  │               │              │               │
     │                   │                    │                  │               │              │               │
     │                   │                    │ provisionAccount()│              │              │               │
     │                   │                    ├─────────────────>│               │              │               │
     │                   │                    │                  │ lookupAccount()│             │               │
     │                   │                    │                  │ createAccount()│             │               │
     │                   │                    │<─────────────────┤               │              │               │
     │                   │                    │ Account          │               │              │               │
     │                   │                    │                  │               │              │               │
     │                   │                    │ create(accountId)│               │              │               │
     │                   │                    ├──────────────────┼──────────────>│              │               │
     │                   │                    │                  │               │ put(session) │               │
     │                   │                    │                  │               ├─────────────>│               │
     │                   │                    │<─────────────────┼───────────────┤              │               │
     │                   │                    │ sessionId        │               │              │               │
     │                   │                    │                  │               │              │               │
     │                   │                    │ generate UUID    │               │              │               │
     │                   │                    │ create VerifLink │               │              │               │
     │                   │                    ├──────────────────┼──────────────>│              │               │
     │                   │                    │                  │               │ put(secret)  │               │
     │                   │                    │                  │               ├─────────────>│               │
     │                   │                    │                  │               │              │               │
     │                   │                    │ sendMessage()    │               │              │               │
     │                   │                    ├──────────────────┼───────────────┼─────────────>│               │
     │                   │                    │                  │               │              │ queue email   │
     │                   │                    │                  │               │              │ async send    │
     │                   │<───────────────────┤                  │               │              │               │
     │<──────────────────┤                    │                  │               │              │               │
     │  200 OK           │                    │                  │               │              │               │
     │  "Check email"    │                    │                  │               │              │               │
     │                   │                    │                  │               │              │               │
```

**Steps**:
1. User submits email address via POST /login
2. EmailService validates email format and domain
3. AccountProvisioner looks up or creates account
4. SessionManager creates temporary session
5. VerificationLinkManager stores verification details
6. EmailSender queues email with verification link
7. User receives "Check your email" response

#### Sequence Diagram: Verify Link and Authenticate

```
User Browser          REST API          EmailService       VerifLinkMgr    SessionManager    KeyValueStore
     │                   │                    │                  │               │               │
     │  Click link       │                    │                  │               │               │
     │  GET /?code=uuid  │                    │                  │               │               │
     ├──────────────────>│                    │                  │               │               │
     │                   │ verifySecretLink() │                  │               │               │
     │                   ├───────────────────>│                  │               │               │
     │                   │                    │ get(code)        │               │               │
     │                   │                    ├─────────────────>│               │               │
     │                   │                    │                  │ get(secret)   │               │
     │                   │                    │                  ├──────────────>│               │
     │                   │                    │<─────────────────┤               │               │
     │                   │                    │ VerifLinkDetails │               │               │
     │                   │                    │                  │               │               │
     │                   │                    │ validate expiry  │               │               │
     │                   │                    │ validate state   │               │               │
     │                   │                    │ validate domain  │               │               │
     │                   │                    │                  │               │               │
     │                   │                    │ create(accountId)│               │               │
     │                   │                    ├──────────────────┼──────────────>│               │
     │                   │                    │                  │               │ put(session)  │
     │                   │                    │                  │               ├──────────────>│
     │                   │                    │<─────────────────┼───────────────┤               │
     │                   │                    │ sessionId        │               │               │
     │                   │<───────────────────┤                  │               │               │
     │<──────────────────┤                    │                  │               │               │
     │  302 Redirect     │                    │                  │               │               │
     │  Set-Cookie:      │                    │                  │               │               │
     │  X-KRISTA-SESSION │                    │                  │               │               │
     │                   │                    │                  │               │               │
```

**Steps**:
1. User clicks verification link in email
2. EmailService retrieves verification details
3. Validates expiry time, state, and domain
4. SessionManager creates authenticated session
5. Response sets session cookie and redirects to original URL

### Authenticated Request Flow

```
User Browser          Authenticator      SessionManager    KeyValueStore    Platform
     │                      │                  │                │              │
     │  GET /api/resource   │                  │                │              │
     │  Cookie: session-id  │                  │                │              │
     ├─────────────────────>│                  │                │              │
     │                      │ getAccountId()   │                │              │
     │                      ├─────────────────>│                │              │
     │                      │                  │ get(sessionId) │              │
     │                      │                  ├───────────────>│              │
     │                      │<─────────────────┤                │              │
     │                      │ accountId        │                │              │
     │                      │                  │                │              │
     │                      │ setAuthContext() │                │              │
     │                      ├──────────────────┼────────────────┼─────────────>│
     │<─────────────────────┤                  │                │              │
     │  200 OK              │                  │                │              │
     │  Resource data       │                  │                │              │
```

**Steps**:
1. Browser sends request with session cookie
2. RequestAuthenticator extracts session ID from cookie
3. SessionManager retrieves account ID from KeyValueStore
4. Platform sets authorization context
5. Request proceeds with authenticated account

---

## Performance Characteristics

### Performance Metrics

| Operation | Latency | Throughput | Bottleneck |
|-----------|---------|------------|------------|
| Send Login Link | 200-500ms | 10-50 req/s | SMTP connection |
| Verify Link | 50-100ms | 100-500 req/s | KeyValueStore read |
| Authenticated Request | 10-20ms | 1000+ req/s | KeyValueStore read |
| Logout | 20-50ms | 500+ req/s | KeyValueStore delete |

### Scalability Characteristics

#### Horizontal Scalability
- **Stateless Design**: All state in KeyValueStore (shared across instances)
- **No In-Memory State**: Can scale horizontally without session affinity
- **Shared Storage**: KeyValueStore must support concurrent access

#### Vertical Scalability Limits
- **Email Sending**: Single-threaded executor per instance
- **SMTP Connection Pool**: Limited by mail server connections
- **Memory**: Minimal (no caching, no large objects)

### Performance Bottlenecks

#### 1. Email Sending (Critical)
**Issue**: Single-threaded email worker
```java
ExecutorService executorService = Executors.newSingleThreadExecutor();
```
**Impact**:
- Sequential email processing
- ~1-2 emails/second maximum
- Queue buildup under load

**Mitigation**:
- Use thread pool: `Executors.newFixedThreadPool(10)`
- Implement batch sending
- Use external email service (SendGrid, SES)
- Add email queue monitoring

#### 2. SMTP Connection Overhead
**Issue**: New connection per email batch
```java
try (Transport transport = session.getTransport()) {
    transport.connect();
    // send emails
}
```
**Impact**:
- 100-300ms connection overhead
- TLS handshake per batch
- Connection pool exhaustion

**Mitigation**:
- Keep connections alive longer
- Use connection pooling
- Implement connection reuse
- Consider SMTP relay service

#### 3. KeyValueStore Latency
**Issue**: Every request requires KeyValueStore lookup
**Impact**:
- 10-50ms per request
- Network round-trip to storage
- Potential storage bottleneck

**Mitigation**:
- Implement local caching (with TTL)
- Use faster storage backend
- Batch operations where possible
- Consider Redis or similar

#### 4. No Cleanup Mechanism
**Issue**: Sessions and verification links never expire automatically
**Impact**:
- Unbounded storage growth
- Memory/disk exhaustion over time
- Performance degradation

**Mitigation**:
- Implement background cleanup job
- Use TTL-based storage (Redis)
- Add manual cleanup endpoint
- Monitor storage size

### Resource Consumption

| Resource | Usage | Notes |
|----------|-------|-------|
| Memory | Low (~50MB) | No caching, minimal objects |
| CPU | Low | Simple validation logic |
| Network | Medium | SMTP connections, KeyValueStore |
| Storage | Growing | No automatic cleanup |
| Threads | 1 + request threads | Single email worker thread |

### Capacity Planning

**Assumptions**:
- Average email send time: 500ms
- Average verification link lifetime: 15 minutes
- Average session lifetime: 24 hours

**Capacity Estimates**:
- **Login requests**: 120/minute (2/second with single thread)
- **Concurrent sessions**: Limited by KeyValueStore capacity
- **Storage growth**: ~1KB per session + ~500 bytes per verification link

**Recommended Limits**:
- Max concurrent logins: 100/minute
- Max active sessions: 100,000
- Max verification links: 10,000 (with cleanup)

---

## Error Scenarios

### Error Handling Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Error Categories                          │
├─────────────────────────────────────────────────────────────┤
│  1. Validation Errors (400)                                  │
│  2. Authentication Errors (401)                              │
│  3. Configuration Errors (500)                               │
│  4. External Service Errors (502/503)                        │
│  5. Storage Errors (500)                                     │
└─────────────────────────────────────────────────────────────┘
```

### Detailed Error Scenarios

#### 1. Validation Errors (HTTP 400)

| Scenario | Trigger | Error Message | Recovery |
|----------|---------|---------------|----------|
| Invalid Email Format | `user@invalid` | "Email address 'user@invalid' is not valid." | User re-enters valid email |
| Missing Email | Empty email field | "Email address '' is not valid." | User provides email |
| Missing Original URL | No `originalUrl` param | "OriginalUrl parameter is missing." | Include URL in request |
| Unsupported Domain | `user@blocked.com` | "Email domain 'blocked.com' is not supported." | Use allowed domain |
| Invalid Domain Config | Malformed domain list | "Domain name 'invalid..com' is not valid." | Fix configuration |

**Code Location**: `EmailService.validate()`, `EmailAddresses.isValidEmailAddress()`

**Error Response**:
```html
<html>
  <body>
    <h1>Error</h1>
    <p>Email address 'invalid@email' is not valid.</p>
  </body>
</html>
```

#### 2. Authentication Errors (HTTP 401)

| Scenario | Trigger | Error Message | Recovery |
|----------|---------|---------------|----------|
| Expired Verification Link | Link older than 15 min | "Email verification link is expired." | Request new link |
| Already Used Link | Link clicked twice | "Email verification link is USED." | Request new link |
| Invalid Secret Code | Tampered URL | "Email verification link is not found." | Request new link |
| Secret Mismatch | Code doesn't match | "Email verification link is not matching." | Request new link |
| Invalid Session | Session not found | Returns `null` accountId | Re-authenticate |

**Code Location**: `EmailService.verifySecretLink()`, `SessionManager.getAccountId()`

**Error Flow**:
```
User clicks expired link
  → VerificationLinkManager.get(code)
  → Check expiry: verificationLinkDetails.getExpiry() < currentTime
  → VerificationLinkManager.remove(code)
  → throw IllegalArgumentException("Email verification link is expired.")
  → Display error page
```

#### 3. Configuration Errors (HTTP 500)

| Scenario | Trigger | Error Message | Recovery |
|----------|---------|---------------|----------|
| Default Mail Server | `useDefaultMailServer=true` | "Default mail server not yet supported" | Configure SMTP |
| Missing SMTP Config | No SMTP host/port | Connection failure | Configure SMTP |
| Invalid SMTP Credentials | Wrong password | Authentication failure | Fix credentials |
| Missing Sender Email | No sender address | Email send failure | Configure sender |
| Invalid Role Name | Non-existent role | Role creation | Auto-creates role |

**Code Location**: `EmailAuthenticationExtension.validateAttributes()`

**Validation Logic**:
```java
@InvokerRequest(InvokerRequest.Type.VALIDATE_ATTRIBUTES)
public void validateAttributes(Map<String, Object> attributes) {
    EmailConfiguration configuration = EmailConfiguration.parse(attributes);
    if (configuration.isUsingDefaultMailServer()) {
        throw new IllegalArgumentException("Default mail server not yet supported");
    }
}
```

#### 4. External Service Errors (SMTP)

| Scenario | Trigger | Exception | Impact |
|----------|---------|-----------|--------|
| SMTP Connection Failure | Network issue | `MessagingException` | Email not sent, silent failure |
| SMTP Authentication Failure | Wrong credentials | `AuthenticationFailedException` | Email not sent, silent failure |
| SMTP Send Failure | Server rejection | `SendFailedException` | Email not sent, silent failure |
| Mail Server Timeout | Slow server | `MessagingException` | Email delayed/failed |

**Code Location**: `EmailSender.Worker.sendMails()`

**Error Handling**:
```java
try {
    sendMails(emailWorkList.poll());
} catch (MessagingException cause) {
    // handle exception - CURRENTLY SILENT!
}
```

**⚠️ Critical Issue**: Email failures are silently swallowed. User sees "Check your email" but email never arrives.

**Recommended Fix**:
- Log errors
- Update verification link state to FAILED
- Implement retry mechanism
- Notify user of failure

#### 5. Storage Errors (KeyValueStore)

| Scenario | Trigger | Exception | Impact |
|----------|---------|-----------|--------|
| Storage Unavailable | Network/service down | `IOException` | Cannot create session |
| Storage Full | Disk space exhausted | `IOException` | Cannot store data |
| Serialization Error | Invalid JSON | `JsonSyntaxException` | Cannot parse data |
| Concurrent Modification | Race condition | Data inconsistency | Duplicate sessions |

**Code Location**: `SessionManager`, `VerificationLinkManager`

**Error Propagation**:
```java
public String create(String accountId) throws IOException {
    String session = UUID.randomUUID().toString();
    keyValueStore.put(toKey(session), accountId); // throws IOException
    return session;
}
```

### Error Recovery Strategies

#### User-Facing Errors
1. **Display HTML Error Page**: `EmailResponseFactory.create()`
2. **Provide Clear Message**: Explain what went wrong
3. **Offer Recovery Action**: "Request new link" button
4. **Log for Debugging**: Track error patterns

#### System Errors
1. **Graceful Degradation**: Return null instead of crashing
2. **Retry Logic**: For transient failures
3. **Circuit Breaker**: For external services
4. **Monitoring**: Alert on error rates

### Error Monitoring Recommendations

```
┌─────────────────────────────────────────────────────────────┐
│                  Monitoring Metrics                          │
├─────────────────────────────────────────────────────────────┤
│  • Email send failure rate                                   │
│  • Verification link expiry rate                             │
│  • Invalid session rate                                      │
│  • SMTP connection errors                                    │
│  • KeyValueStore errors                                      │
│  • Average time to verify link                               │
│  • Abandoned authentication rate                             │
└─────────────────────────────────────────────────────────────┘
```

---

## Security Architecture

### Security Layers

```
┌─────────────────────────────────────────────────────────────┐
│  Layer 1: Input Validation                                   │
│  • Email format validation (regex)                           │
│  • Domain whitelist enforcement                              │
│  • URL parameter sanitization                                │
└─────────────────────────────────────────────────────────────┘
                          ▼
┌─────────────────────────────────────────────────────────────┐
│  Layer 2: Cryptographic Security                             │
│  • UUID-based secrets (128-bit entropy)                      │
│  • TLS for SMTP connections                                  │
│  • Secure cookie attributes                                  │
└─────────────────────────────────────────────────────────────┘
                          ▼
┌─────────────────────────────────────────────────────────────┐
│  Layer 3: Temporal Security                                  │
│  • Time-limited verification links (15 min)                  │
│  • One-time use enforcement                                  │
│  • State tracking (GENERATED → USED)                         │
└─────────────────────────────────────────────────────────────┘
                          ▼
┌─────────────────────────────────────────────────────────────┐
│  Layer 4: Access Control                                     │
│  • Email ownership verification                              │
│  • Role-based authorization                                  │
│  • Session-based authentication                              │
└─────────────────────────────────────────────────────────────┘
```

### Security Mechanisms

#### 1. Email Validation
**Purpose**: Prevent injection and invalid inputs

**Implementation**:
```java
Pattern EMAIL_ADDRESS_PATTERN =
    Pattern.compile("^[a-z0-9_+&*-]+(?:\\.[a-z0-9_+&*-]+)*@(?:[a-z0-9-]+\\.)+[a-z]{2,7}$");
```

**Protections**:
- Prevents SQL injection (no special chars)
- Prevents XSS (no HTML tags)
- Ensures valid email format
- Case-insensitive normalization

**Limitations**:
- Doesn't validate email deliverability
- Doesn't prevent disposable emails
- Doesn't prevent typos

#### 2. Domain Whitelisting
**Purpose**: Restrict authentication to approved domains

**Configuration**:
```
SUPPORTED_DOMAINS = "example.com,company.org,trusted.net"
```

**Enforcement**:
```java
handleSupportedDomains(secretDetails, supportsNewAccountCreation);
```

**Bypass Conditions**:
- If `ALLOW_NEW_ACCOUNT_CREATION = true` and domain not in list → Error
- If `ALLOW_NEW_ACCOUNT_CREATION = false` → Domain check skipped

**Security Gap**: Configuration can disable domain restrictions entirely

#### 3. Verification Link Security

**Secret Generation**:
```java
String secret = UUID.randomUUID().toString(); // 128-bit random
```

**Properties**:
- **Entropy**: 128 bits (2^128 possible values)
- **Unpredictability**: Cryptographically secure random
- **Uniqueness**: Collision probability ~0

**Link Structure**:
```
https://workspace.krista.ai/authn/?code=550e8400-e29b-41d4-a716-446655440000&originalUrl=...
```

**Protections**:
- Cannot be guessed
- Cannot be brute-forced (15-minute window)
- One-time use only
- Expires after 15 minutes

**Vulnerabilities**:
- Link visible in email (plaintext)
- Link visible in browser history
- Link visible in server logs
- Link can be forwarded

#### 4. Time-Based Expiration

**Configuration**:
```java
private long getExpiryTime() {
    return System.currentTimeMillis() + (15 * 60 * 1000); // 15 minutes
}
```

**Validation**:
```java
if (verificationLinkDetails.getExpiry() < System.currentTimeMillis()) {
    verificationLinkManager.remove(secret);
    throw new IllegalArgumentException("Email verification link is expired.");
}
```

**Attack Mitigation**:
- Limits brute-force window
- Reduces replay attack window
- Forces re-authentication for old links

**Considerations**:
- Clock skew between servers
- User timezone differences
- Email delivery delays

#### 5. State Machine Protection

**States**:
```java
enum State {
    GENERATED,  // Link created, not yet used
    USED        // Link clicked and verified
}
```

**State Transition**:
```
GENERATED → (verify) → USED
USED → (verify) → ERROR
```

**Enforcement**:
```java
if (!state.equals(VerificationLinkDetails.State.GENERATED.name())) {
    throw new IllegalStateException("Email verification link is " + state + ".");
}
```

**Protection**: Prevents replay attacks (link reuse)

#### 6. Session Security

**Session Creation**:
```java
String session = UUID.randomUUID().toString(); // 128-bit random
```

**Cookie Attributes**:
```java
X-KRISTA-SESSION-ID={sessionId}; HttpOnly; Secure; SameSite=Strict
```

**Properties**:
- **HttpOnly**: Prevents JavaScript access (XSS protection)
- **Secure**: HTTPS only (prevents MITM)
- **SameSite=Strict**: Prevents CSRF

**Session Storage**:
```
session-{uuid} → accountId
```

**Vulnerabilities**:
- No session expiration (indefinite lifetime)
- No session invalidation on password change
- No concurrent session limits
- No session fixation protection

### Security Vulnerabilities & Mitigations

| Vulnerability | Severity | Impact | Mitigation |
|---------------|----------|--------|------------|
| **No Session Expiration** | HIGH | Sessions never expire | Implement TTL (24h) |
| **Silent Email Failures** | MEDIUM | User locked out | Log and notify failures |
| **No Rate Limiting** | HIGH | Email bombing, DoS | Implement rate limits |
| **Link in Plaintext Email** | MEDIUM | Email interception | Use HTTPS, short expiry |
| **No CSRF Protection** | LOW | Session hijacking | SameSite cookie (implemented) |
| **No Account Lockout** | MEDIUM | Brute force | Limit login attempts |
| **Unbounded Storage** | MEDIUM | DoS via storage | Implement cleanup |
| **No Audit Logging** | MEDIUM | No forensics | Add audit trail |

### Recommended Security Enhancements

1. **Implement Rate Limiting**
   ```java
   // Limit to 3 login attempts per email per 15 minutes
   RateLimiter.checkLimit(email, 3, Duration.ofMinutes(15));
   ```

2. **Add Session Expiration**
   ```java
   // Store expiry with session
   keyValueStore.put(toKey(session), accountId, TTL.hours(24));
   ```

3. **Implement Audit Logging**
   ```java
   auditLog.log("LOGIN_ATTEMPT", email, ipAddress, timestamp);
   auditLog.log("LOGIN_SUCCESS", accountId, ipAddress, timestamp);
   ```

4. **Add Email Delivery Confirmation**
   ```java
   // Update verification link state
   verificationLinkDetails.setState(State.SENT);
   // Or State.FAILED on error
   ```

5. **Implement Account Lockout**
   ```java
   // After 5 failed attempts in 1 hour
   if (failedAttempts.get(email) > 5) {
       throw new AccountLockedException();
   }
   ```

---

## Deployment Architecture

### Extension Deployment Model

```
┌──────────────────────────────────────────────────────────────┐
│                    Krista Platform                            │
│  ┌────────────────────────────────────────────────────────┐  │
│  │         Extension Container (HK2)                      │  │
│  │  ┌──────────────────────────────────────────────────┐  │  │
│  │  │   Email Authentication Extension                 │  │  │
│  │  │   • JAR: email-authentication-3.5.7.jar          │  │  │
│  │  │   • REST API: /authn/*                           │  │  │
│  │  │   • Static Resources: /authn/docs/*              │  │  │
│  │  └──────────────────────────────────────────────────┘  │  │
│  │                                                          │  │
│  │  Platform Services:                                     │  │
│  │  • KeyValueStore                                        │  │
│  │  • AccountManager                                       │  │
│  │  • RoleManager                                          │  │
│  │  • AuthorizationContext                                 │  │
│  └────────────────────────────────────────────────────────┘  │
└──────────────────────────────────────────────────────────────┘
                          │
                          ▼
┌──────────────────────────────────────────────────────────────┐
│                   External Services                           │
│  • SMTP Server (smtp.gmail.com, smtp.office365.com, etc.)    │
│  • Persistent Storage (for KeyValueStore)                    │
└──────────────────────────────────────────────────────────────┘
```

### Runtime Environment

**Requirements**:
- **Java**: 21+
- **Krista Platform**: 3.4.0+
- **SMTP Server**: Any standard SMTP server
- **Storage**: Persistent KeyValueStore backend

**Configuration Parameters**:
```
SUPPORTED_DOMAINS              = "example.com,company.org"
ALLOW_NEW_ACCOUNT_CREATION     = true
DEFAULT_ROLES_FOR_NEW_ACCOUNT  = "User,Viewer"
USE_DEFAULT_MAIL_SERVER        = false
SENDER_EMAIL_ADDRESS           = "noreply@example.com"
SMTP_ACCOUNT                   = "smtp-user@example.com"
SMTP_PASSWORD                  = "********" (secured)
SMTP_HOST                      = "smtp.gmail.com"
SMTP_PORT                      = "587"
```

### Multi-Instance Deployment

```
┌─────────────────────────────────────────────────────────────┐
│                      Load Balancer                           │
└────────────┬────────────────────────────┬───────────────────┘
             │                            │
             ▼                            ▼
┌─────────────────────┐      ┌─────────────────────┐
│  Krista Instance 1  │      │  Krista Instance 2  │
│  ┌───────────────┐  │      │  ┌───────────────┐  │
│  │ Email Auth    │  │      │  │ Email Auth    │  │
│  │ Extension     │  │      │  │ Extension     │  │
│  └───────┬───────┘  │      │  └───────┬───────┘  │
└──────────┼──────────┘      └──────────┼──────────┘
           │                            │
           └────────────┬───────────────┘
                        ▼
           ┌─────────────────────────┐
           │  Shared KeyValueStore   │
           │  (Redis, Database, etc) │
           └─────────────────────────┘
                        │
                        ▼
           ┌─────────────────────────┐
           │     SMTP Server         │
           └─────────────────────────┘
```

**Scaling Characteristics**:
- ✅ **Stateless**: Can scale horizontally
- ✅ **Shared Storage**: All instances use same KeyValueStore
- ⚠️ **Email Bottleneck**: Single-threaded email sender per instance
- ⚠️ **No Session Affinity Required**: Sessions stored externally

### Network Architecture

```
Internet
   │
   ▼
┌─────────────────────────────────────────────────────────────┐
│                    Firewall / WAF                            │
└────────────────────────────┬────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────┐
│                    Reverse Proxy (HTTPS)                     │
│                    • TLS Termination                         │
│                    • Rate Limiting                           │
└────────────────────────────┬────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────┐
│                    Krista Platform                           │
│                    • Email Auth Extension                    │
│                    • Port: 8080 (internal)                   │
└────────────────────────────┬────────────────────────────────┘
                             │
              ┌──────────────┼──────────────┐
              ▼              ▼              ▼
      ┌─────────────┐  ┌──────────┐  ┌──────────┐
      │KeyValueStore│  │  SMTP    │  │  Email   │
      │   (Redis)   │  │  Server  │  │  Client  │
      └─────────────┘  └──────────┘  └──────────┘
```

### Configuration Management

**Extension Configuration** (via Krista Studio):
```json
{
  "SUPPORTED_DOMAINS": "example.com,company.org",
  "ALLOW_NEW_ACCOUNT_CREATION": true,
  "DEFAULT_ROLES_FOR_NEW_ACCOUNT": "User",
  "USE_DEFAULT_MAIL_SERVER": false,
  "SENDER_EMAIL_ADDRESS": "noreply@example.com",
  "SMTP_ACCOUNT": "smtp-user@example.com",
  "SMTP_PASSWORD": "encrypted-password",
  "SMTP_HOST": "smtp.gmail.com",
  "SMTP_PORT": "587"
}
```

**SMTP Configuration** (JavaMail Properties):
```properties
mail.smtp.auth=true
mail.smtp.starttls.enable=true
mail.smtp.host=smtp.gmail.com
mail.smtp.port=587
mail.smtp.ssl.trust=smtp.gmail.com
```

### Monitoring & Observability

**Key Metrics to Monitor**:
```
Application Metrics:
  • Login requests per minute
  • Verification success rate
  • Email send success rate
  • Average verification time
  • Active sessions count
  • Verification links count

Performance Metrics:
  • Request latency (p50, p95, p99)
  • Email send latency
  • KeyValueStore latency
  • SMTP connection time

Error Metrics:
  • Email send failures
  • Expired link rate
  • Invalid session rate
  • SMTP errors
  • Storage errors

Resource Metrics:
  • Memory usage
  • Thread pool utilization
  • Storage size
  • Network bandwidth
```

**Logging Strategy**:
```java
// Recommended logging points
logger.info("Login attempt: email={}", email);
logger.info("Account provisioned: accountId={}", accountId);
logger.info("Verification link sent: email={}, expiresAt={}", email, expiry);
logger.info("Verification successful: accountId={}", accountId);
logger.warn("Verification failed: code={}, reason={}", code, reason);
logger.error("Email send failed: email={}, error={}", email, error);
```

### Disaster Recovery

**Backup Requirements**:
1. **KeyValueStore**: Regular backups of session and verification data
2. **Configuration**: Extension configuration backup
3. **Audit Logs**: Retain authentication logs

**Recovery Scenarios**:

| Scenario | Impact | Recovery |
|----------|--------|----------|
| KeyValueStore failure | All sessions lost | Users re-authenticate |
| SMTP server down | Cannot send emails | Queue emails, retry |
| Extension crash | Authentication unavailable | Restart extension |
| Configuration loss | Extension misconfigured | Restore from backup |

**High Availability**:
- Deploy multiple Krista instances
- Use replicated KeyValueStore (Redis Cluster)
- Configure SMTP failover servers
- Implement health checks

---

## Appendix: Complete Architecture Diagram

### System Context Diagram

```
                    ┌─────────────┐
                    │    User     │
                    │   Browser   │
                    └──────┬──────┘
                           │
                           │ HTTPS
                           │
                    ┌──────▼──────────────────────────────────┐
                    │   Krista Platform                       │
                    │                                         │
                    │  ┌───────────────────────────────────┐  │
                    │  │ Email Authentication Extension    │  │
                    │  │                                   │  │
                    │  │  ┌─────────────────────────────┐  │  │
                    │  │  │  REST API Layer             │  │  │
                    │  │  │  /authn/login               │  │  │
                    │  │  │  /authn/                    │  │  │
                    │  │  │  /authn/logout              │  │  │
                    │  │  └────────┬────────────────────┘  │  │
                    │  │           │                       │  │
                    │  │  ┌────────▼────────────────────┐  │  │
                    │  │  │  Business Logic Layer       │  │  │
                    │  │  │  • EmailService             │  │  │
                    │  │  │  • RequestAuthenticator     │  │  │
                    │  │  └────────┬────────────────────┘  │  │
                    │  │           │                       │  │
                    │  │  ┌────────▼────────────────────┐  │  │
                    │  │  │  Service Layer              │  │  │
                    │  │  │  • SessionManager           │  │  │
                    │  │  │  • AccountProvisioner       │  │  │
                    │  │  │  • VerificationLinkManager  │  │  │
                    │  │  │  • EmailSender              │  │  │
                    │  │  └────────┬────────────────────┘  │  │
                    │  └───────────┼─────────────────────┘  │
                    │              │                        │
                    │  ┌───────────▼─────────────────────┐  │
                    │  │  Platform Services              │  │
                    │  │  • KeyValueStore                │  │
                    │  │  • AccountManager               │  │
                    │  │  • RoleManager                  │  │
                    │  └───────────┬─────────────────────┘  │
                    └──────────────┼────────────────────────┘
                                   │
                    ┌──────────────┼──────────────┐
                    │              │              │
                    ▼              ▼              ▼
            ┌──────────────┐ ┌──────────┐ ┌────────────┐
            │ KeyValueStore│ │   SMTP   │ │   User's   │
            │   (Redis)    │ │  Server  │ │   Email    │
            └──────────────┘ └──────────┘ └────────────┘
```

### Data Flow Summary

**Login Flow**:
```
User → REST API → EmailService → AccountProvisioner → AccountManager
                              → VerificationLinkManager → KeyValueStore
                              → EmailSender → SMTP Server → User Email
```

**Verification Flow**:
```
User Email → Click Link → REST API → EmailService → VerificationLinkManager → KeyValueStore
                                                  → SessionManager → KeyValueStore
                                                  → Response (Set-Cookie)
```

**Authenticated Request Flow**:
```
User → REST API → RequestAuthenticator → SessionManager → KeyValueStore
                                       → Platform (set auth context)
                                       → Application Logic
```

---

## Summary

### Strengths
✅ Clean layered architecture with clear separation of concerns
✅ Passwordless authentication improves user experience
✅ Platform integration leverages existing services
✅ Stateless design enables horizontal scaling
✅ Dependency injection promotes testability
✅ Security-first approach with multiple validation layers

### Weaknesses
⚠️ Single-threaded email sending limits throughput
⚠️ No automatic cleanup of expired data
⚠️ Silent email failures create poor UX
⚠️ No session expiration creates security risk
⚠️ No rate limiting enables abuse
⚠️ Limited error handling and recovery

### Recommended Improvements
1. **Performance**: Multi-threaded email sending, connection pooling
2. **Reliability**: Retry logic, error notifications, health checks
3. **Security**: Session expiration, rate limiting, audit logging
4. **Operations**: Automated cleanup, monitoring, alerting
5. **UX**: Better error messages, email delivery confirmation

---

**Document Version**: 1.0
**Last Updated**: 2026-01-15
**Extension Version**: 3.5.7
**Author**: Architecture Documentation Team
- `sendLoginLink(String originalUrl, String email)` - Validates and sends verification email
- `verifySecretLink(String originalUrl, String code)` - Validates verification code and creates session
- `logout(String sessionId)` - Terminates authenticated session
- `getAuthenticatedAccountId(ProtoRequest)` - Returns account ID for authenticated requests

### Layer 3: Service Layer

**Purpose**: Provide specialized services for specific domains

**Components**:

#### Session Management
- `SessionManager` - Manages user sessions
  - `create(String accountId)` → String sessionId
  - `getAccountId(String sessionId)` → String accountId
  - `remove(String sessionId)` → void

#### Account & Role Provisioning
- `AccountProvisioner` - Creates/retrieves user accounts
  - `provisionAccount(String email, List<String> roleIds)` → Account
- `RoleProvisioner` - Provisions default roles
  - `provisionDefaultRole()` → ModifiableRole

#### Verification Link Management
- `VerificationLinkManager` - Manages verification link storage
  - `add(VerificationLinkDetails)` → void
  - `get(String code)` → VerificationLinkDetails
  - `remove(String code)` → void

#### Email Services
- `EmailSender` - Sends verification emails via SMTP
  - `sendMessage(String to, String subject, String body, String secret)` → void
- `MailSessionProvider` - Provides JavaMail session with SMTP config
  - `getSession()` → Session

**Data Models**:
- `VerificationLinkDetails` - Verification link state
  - email, secret, expiry, state (GENERATED/USED), sessionId, accountId

### Layer 4: Data Access Layer

**Purpose**: Interface with platform services and persistent storage

**Platform Services Used**:
- `KeyValueStore` - Persistent key-value storage
  - Stores sessions: `session-{sessionId}` → `{accountId}`
  - Stores verification links: `secret-{uuid}` → `{VerificationLinkDetails JSON}`
- `AccountManager` - Platform account operations
  - `lookupAccount(String email)` → ModifiableAccount
  - `createAccount(...)` → Account
- `RoleManager` - Platform role operations
  - `getRoles()` → Iterable<ModifiableRole>
  - `createRole(String name)` → ModifiableRole
  - `lookupRole(String name)` → ModifiableRole
- `AuthorizationContext` - Platform authorization service
  - `getAuthorizedAccount()` → Account

**Storage Patterns**:
```
KeyValueStore:
  ├── session-{uuid} → accountId (String)
  └── secret-{uuid} → VerificationLinkDetails (JSON)
```

### Layer 5: Utility Layer

**Purpose**: Provide reusable utility functions

**Components**:
- `EmailAddresses` - Email validation and parsing
  - Regex: `^[a-z0-9_+&*-]+(?:\.[a-z0-9_+&*-]+)*@(?:[a-z0-9-]+\.)+[a-z]{2,7}$`
  - `isValidEmailAddress(String)` → boolean
  - `getDomainName(String)` → String
  - `getLocalPart(String)` → String
- `DomainNames` - Domain validation and normalization
  - Regex: `^((?!-)[A-Za-z0-9-]{1,63}(?<!-)\.)+[A-Za-z]{2,6}`
  - `isValidDomain(String)` → boolean
  - `normalizeDomainNames(List<String>)` → List<String>
- `Cookies` - Cookie creation and parsing
  - `getCookie(HttpRequest, String name)` → String
  - `createCookie(String key, String value, ...)` → String
- `Resources` - HTML template loading
  - `getTranslatedResource(Class, String file, Map<String, String>)` → Response
- `Attributes` - Configuration parsing
  - `toList(String commaSeparated)` → List<String>
- `LocationProvider` - URL generation
  - `getLocation(String path, Map<String, String> params)` → String

---

## Component Architecture

### High-Level Component Diagram

```
┌──────────────────────────────────────────────────────────────────┐
│                         HTTP Client                               │
└────────────────────────┬─────────────────────────────────────────┘
                         │
                         ▼
┌──────────────────────────────────────────────────────────────────┐
│                  EmailAuthenticationResource                      │
│                    (REST Endpoints)                               │
│  GET  /authn/type                                                 │
│  GET  /authn/login                                                │
│  POST /authn/login                                                │
│  GET  /authn/                                                     │
│  GET  /authn/waiting                                              │
│  POST /authn/logout                                               │
└────────────────────────┬─────────────────────────────────────────┘
                         │
                         ▼
┌──────────────────────────────────────────────────────────────────┐
│                      EmailService                                 │
│                  (Business Logic)                                 │
│  • sendLoginLink()                                                │
│  • verifySecretLink()                                             │
│  • logout()                                                       │
└─────┬──────────┬──────────┬──────────┬──────────┬────────────────┘
      │          │          │          │          │
      ▼          ▼          ▼          ▼          ▼
┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────────┐
│ Session  │ │ Account  │ │Verif.Link│ │  Email   │ │   Response   │
│ Manager  │ │Provision.│ │ Manager  │ │  Sender  │ │   Factory    │
└────┬─────┘ └────┬─────┘ └────┬─────┘ └────┬─────┘ └──────────────┘
     │            │            │            │
     ▼            ▼            ▼            ▼
┌──────────────────────────────────────────────────────────────────┐
│                    Platform Services                              │
│  KeyValueStore  │  AccountManager  │  RoleManager  │  SMTP Server │
└──────────────────────────────────────────────────────────────────┘
```

