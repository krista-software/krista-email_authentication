# Dependencies

## Overview

The Email Authentication Extension can be used as a dependency in other Krista extensions to provide authentication services. This page explains how to add the extension as a dependency and use its authentication capabilities in your custom extensions.

## Adding as a Dependency

### Step 1: Add Extension Dependency

In your extension's configuration, add the Email Authentication Extension as a dependency:

1. Navigate to your extension's settings
2. Go to the **Dependencies** section
3. Click **Add Dependency**
4. Select **Email Authentication Extension**
5. Save the configuration

### Step 2: Configure Extension

Ensure the Email Authentication Extension is properly configured with:
- SMTP server settings
- Sender email address
- Domain restrictions (optional)
- Account provisioning settings (optional)

See [Extension Configuration](pages/ExtensionConfiguration.md) for detailed setup instructions.

## Using Authentication in Your Extension

### Session Validation

To validate if a user is authenticated, check for the session cookie:

```java
import ai.krista.appliance.session.SessionManager;
import ai.krista.appliance.account.Account;

public class MyExtension {
    
    private final SessionManager sessionManager;
    
    public boolean isAuthenticated(HttpServletRequest request) {
        // Get session ID from cookie
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return false;
        }
        
        String sessionId = null;
        for (Cookie cookie : cookies) {
            if ("X-Krista-Session-Id".equals(cookie.getName())) {
                sessionId = cookie.getValue();
                break;
            }
        }
        
        if (sessionId == null) {
            return false;
        }
        
        // Validate session
        Account account = sessionManager.getAccount(sessionId);
        return account != null;
    }
    
    public Account getAuthenticatedAccount(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        
        String sessionId = null;
        for (Cookie cookie : cookies) {
            if ("X-Krista-Session-Id".equals(cookie.getName())) {
                sessionId = cookie.getValue();
                break;
            }
        }
        
        if (sessionId == null) {
            return null;
        }
        
        return sessionManager.getAccount(sessionId);
    }
}
```

### Protecting Endpoints

To protect your extension's endpoints, redirect unauthenticated users to the login page:

```java
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class MyExtension {
    
    public void handleRequest(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        if (!isAuthenticated(request)) {
            // Redirect to login page with original URL
            String originalUri = request.getRequestURI();
            String loginUrl = "/authn/login?X-Krista-Original-URI=" + 
                URLEncoder.encode(originalUri, "UTF-8");
            response.sendRedirect(loginUrl);
            return;
        }
        
        // User is authenticated, proceed with request
        Account account = getAuthenticatedAccount(request);
        // ... handle request
    }
}
```

### Custom Authentication Logic

You can implement custom authentication logic that integrates with the Email Authentication Extension:

```java
public class CustomAuthExtension {
    
    private final SessionManager sessionManager;
    private final AccountManager accountManager;
    
    public void authenticateWithCustomLogic(String email, HttpServletResponse response) 
            throws Exception {
        
        // Custom validation logic
        if (!isEmailAllowed(email)) {
            throw new SecurityException("Email not allowed");
        }
        
        // Get or create account
        Account account = accountManager.getAccountByEmail(email);
        if (account == null) {
            account = createAccount(email);
        }
        
        // Create session
        String sessionId = UUID.randomUUID().toString();
        sessionManager.createSession(sessionId, account.getId());
        
        // Set session cookie
        Cookie cookie = new Cookie("X-Krista-Session-Id", sessionId);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        response.addCookie(cookie);
    }
    
    private boolean isEmailAllowed(String email) {
        // Custom logic to validate email
        return true;
    }
    
    private Account createAccount(String email) {
        // Custom account creation logic
        return accountManager.createAccount(email);
    }
}
```

## Integration Patterns

### Pattern 1: Authentication Gateway

Use the Email Authentication Extension as a gateway for all requests:

```java
public class AuthenticationGateway implements Filter {
    
    private final SessionManager sessionManager;
    private final List<String> publicPaths = Arrays.asList("/authn/", "/public/");
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String path = httpRequest.getRequestURI();
        
        // Allow public paths
        if (isPublicPath(path)) {
            chain.doFilter(request, response);
            return;
        }
        
        // Check authentication
        if (!isAuthenticated(httpRequest)) {
            String originalUri = httpRequest.getRequestURI();
            String loginUrl = "/authn/login?X-Krista-Original-URI=" + 
                URLEncoder.encode(originalUri, "UTF-8");
            httpResponse.sendRedirect(loginUrl);
            return;
        }
        
        // User is authenticated
        chain.doFilter(request, response);
    }
    
    private boolean isPublicPath(String path) {
        return publicPaths.stream().anyMatch(path::startsWith);
    }
    
    private boolean isAuthenticated(HttpServletRequest request) {
        // Check session cookie
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return false;
        
        for (Cookie cookie : cookies) {
            if ("X-Krista-Session-Id".equals(cookie.getName())) {
                String sessionId = cookie.getValue();
                return sessionManager.getAccount(sessionId) != null;
            }
        }
        return false;
    }
}
```

### Pattern 2: Role-Based Access Control

Combine email authentication with role-based access control:

```java
public class RoleBasedAuthExtension {
    
    private final SessionManager sessionManager;
    private final AccountManager accountManager;
    
    public boolean hasRole(HttpServletRequest request, String roleName) {
        Account account = getAuthenticatedAccount(request);
        if (account == null) {
            return false;
        }
        
        return account.getRoles().stream()
            .anyMatch(role -> role.getName().equals(roleName));
    }
    
    public void requireRole(HttpServletRequest request, HttpServletResponse response, 
                           String roleName) throws IOException {
        
        if (!isAuthenticated(request)) {
            redirectToLogin(request, response);
            return;
        }
        
        if (!hasRole(request, roleName)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, 
                "You don't have permission to access this resource");
        }
    }
    
    private Account getAuthenticatedAccount(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;

        for (Cookie cookie : cookies) {
            if ("X-Krista-Session-Id".equals(cookie.getName())) {
                return sessionManager.getAccount(cookie.getValue());
            }
        }
        return null;
    }
}
```

### Pattern 3: API Authentication

Use email authentication for REST API endpoints:

```java
public class ApiAuthExtension {

    private final SessionManager sessionManager;

    @GET
    @Path("/api/data")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getData(@Context HttpServletRequest request) {

        // Validate authentication
        Account account = getAuthenticatedAccount(request);
        if (account == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                .entity("{\"error\": \"Authentication required\"}")
                .build();
        }

        // Return data
        String data = fetchDataForAccount(account);
        return Response.ok(data).build();
    }

    private Account getAuthenticatedAccount(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;

        for (Cookie cookie : cookies) {
            if ("X-Krista-Session-Id".equals(cookie.getName())) {
                return sessionManager.getAccount(cookie.getValue());
            }
        }
        return null;
    }

    private String fetchDataForAccount(Account account) {
        // Fetch data specific to the authenticated account
        return "{\"data\": \"example\"}";
    }
}
```

## Common Use Cases

### Use Case 1: Protecting a Dashboard

```java
public class DashboardExtension {

    private final SessionManager sessionManager;

    @GET
    @Path("/dashboard")
    @Produces(MediaType.TEXT_HTML)
    public Response showDashboard(@Context HttpServletRequest request,
                                  @Context HttpServletResponse response)
            throws IOException {

        // Check authentication
        Account account = getAuthenticatedAccount(request);
        if (account == null) {
            String loginUrl = "/authn/login?X-Krista-Original-URI=/dashboard";
            response.sendRedirect(loginUrl);
            return null;
        }

        // Render dashboard for authenticated user
        String html = renderDashboard(account);
        return Response.ok(html).build();
    }

    private String renderDashboard(Account account) {
        return "<html><body><h1>Welcome, " + account.getEmail() + "</h1></body></html>";
    }
}
```

### Use Case 2: User Profile Management

```java
public class ProfileExtension {

    private final SessionManager sessionManager;
    private final AccountManager accountManager;

    @GET
    @Path("/profile")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProfile(@Context HttpServletRequest request) {

        Account account = getAuthenticatedAccount(request);
        if (account == null) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        // Return user profile
        Map<String, Object> profile = new HashMap<>();
        profile.put("email", account.getEmail());
        profile.put("roles", account.getRoles().stream()
            .map(Role::getName)
            .collect(Collectors.toList()));

        return Response.ok(profile).build();
    }

    @PUT
    @Path("/profile")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateProfile(@Context HttpServletRequest request,
                                  Map<String, Object> updates) {

        Account account = getAuthenticatedAccount(request);
        if (account == null) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        // Update profile
        accountManager.updateAccount(account.getId(), updates);

        return Response.ok().build();
    }
}
```

### Use Case 3: Multi-Tenant Application

```java
public class MultiTenantExtension {

    private final SessionManager sessionManager;
    private final WorkspaceManager workspaceManager;

    @GET
    @Path("/tenant/{tenantId}/data")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTenantData(@PathParam("tenantId") String tenantId,
                                  @Context HttpServletRequest request) {

        // Validate authentication
        Account account = getAuthenticatedAccount(request);
        if (account == null) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        // Validate tenant access
        if (!hasAccessToTenant(account, tenantId)) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        // Return tenant data
        String data = fetchTenantData(tenantId);
        return Response.ok(data).build();
    }

    private boolean hasAccessToTenant(Account account, String tenantId) {
        // Check if account has access to the tenant
        return workspaceManager.hasAccess(account.getId(), tenantId);
    }
}
```

## Best Practices

### 1. Always Validate Sessions

Never trust client-provided session IDs without validation:

```java
public Account getAuthenticatedAccount(HttpServletRequest request) {
    String sessionId = getSessionIdFromCookie(request);
    if (sessionId == null) {
        return null;
    }

    // Always validate session with SessionManager
    Account account = sessionManager.getAccount(sessionId);
    if (account == null) {
        // Session is invalid or expired
        return null;
    }

    return account;
}
```

### 2. Use HTTPS Only

Always enforce HTTPS for authenticated requests:

```java
public void handleRequest(HttpServletRequest request, HttpServletResponse response)
        throws IOException {

    if (!request.isSecure()) {
        response.sendError(HttpServletResponse.SC_FORBIDDEN,
            "HTTPS required for authenticated requests");
        return;
    }

    // Proceed with authenticated request
}
```

### 3. Implement Proper Error Handling

Provide clear error messages for authentication failures:

```java
public Response handleAuthenticatedRequest(HttpServletRequest request) {
    try {
        Account account = getAuthenticatedAccount(request);
        if (account == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                .entity("{\"error\": \"Authentication required. Please log in.\"}")
                .build();
        }

        // Process request
        return Response.ok().build();

    } catch (Exception e) {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
            .entity("{\"error\": \"An error occurred processing your request.\"}")
            .build();
    }
}
```

### 4. Log Authentication Events

Log authentication events for auditing and security monitoring:

```java
public void handleLogin(Account account) {
    logger.info("User logged in: email={}, accountId={}, timestamp={}",
        account.getEmail(),
        account.getId(),
        Instant.now());
}

public void handleLogout(Account account) {
    logger.info("User logged out: email={}, accountId={}, timestamp={}",
        account.getEmail(),
        account.getId(),
        Instant.now());
}

public void handleAuthenticationFailure(String email, String reason) {
    logger.warn("Authentication failed: email={}, reason={}, timestamp={}",
        email,
        reason,
        Instant.now());
}
```

### 5. Implement Session Timeout

Implement session timeout for inactive users:

```java
public class SessionTimeoutManager {

    private final Map<String, Long> lastActivityMap = new ConcurrentHashMap<>();
    private final long timeoutMillis = 30 * 60 * 1000; // 30 minutes

    public void recordActivity(String sessionId) {
        lastActivityMap.put(sessionId, System.currentTimeMillis());
    }

    public boolean isSessionActive(String sessionId) {
        Long lastActivity = lastActivityMap.get(sessionId);
        if (lastActivity == null) {
            return false;
        }

        long inactiveTime = System.currentTimeMillis() - lastActivity;
        return inactiveTime < timeoutMillis;
    }

    public void cleanupExpiredSessions() {
        long now = System.currentTimeMillis();
        lastActivityMap.entrySet().removeIf(entry ->
            now - entry.getValue() > timeoutMillis);
    }
}
```

## Troubleshooting

### Issue: Session not persisting across requests

**Cause**: Cookie not being sent or stored properly

**Resolution**:
1. Verify HTTPS is being used
2. Check cookie attributes (HttpOnly, Secure, Path)
3. Verify browser is accepting cookies
4. Check for cookie domain mismatch

### Issue: Authentication works but user data not available

**Cause**: Session is valid but account lookup fails

**Resolution**:
1. Verify SessionManager is properly injected
2. Check account still exists in database
3. Verify workspace membership
4. Check account status (active vs. disabled)

### Issue: Redirect loop after login

**Cause**: Authentication check redirecting to login page repeatedly

**Resolution**:
1. Ensure `/authn/` paths are excluded from authentication checks
2. Verify session cookie is being set correctly
3. Check for conflicting authentication filters
4. Verify original URI parameter is preserved

## Maven Dependencies

If you're building a custom extension, add these dependencies to your `pom.xml`:

```xml
<dependencies>
    <!-- Krista Core -->
    <dependency>
        <groupId>ai.krista</groupId>
        <artifactId>krista-core</artifactId>
        <version>${krista.version}</version>
    </dependency>

    <!-- Servlet API -->
    <dependency>
        <groupId>javax.servlet</groupId>
        <artifactId>javax.servlet-api</artifactId>
        <version>4.0.1</version>
        <scope>provided</scope>
    </dependency>

    <!-- JAX-RS API (for REST endpoints) -->
    <dependency>
        <groupId>javax.ws.rs</groupId>
        <artifactId>javax.ws.rs-api</artifactId>
        <version>2.1.1</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

## See Also

- [Extension Configuration](pages/ExtensionConfiguration.md) - Configure the extension
- [Authentication](pages/Authentication.md) - Authentication flow and security
- [API Endpoints](pages/APIEndpoints.md) - REST API documentation
- [Troubleshooting](pages/Troubleshooting.md) - Common issues and solutions
```


