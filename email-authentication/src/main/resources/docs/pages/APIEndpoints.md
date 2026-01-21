# API Endpoints

## Overview

The Email Authentication Extension provides REST API endpoints for email-based authentication via email verification links. This page documents all available endpoints, request/response formats, and usage examples.

## Base URL

All endpoints are relative to the Krista appliance URL:

```
https://[appliance-url]/authn/
```

## Endpoints

### 1. Login Page

**Endpoint**: `GET /authn/login`

**Description**: Displays the login page where users can enter their email address to receive a verification link.

**Request Parameters**:

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `X-Krista-Original-URI` | Query | No | The original URL the user was trying to access. User will be redirected here after successful authentication. |

**Response**: HTML page with email input form

**Example**:
```
GET /authn/login?X-Krista-Original-URI=/dashboard
```

**Response Body**:
```html
<!DOCTYPE html>
<html>
<head>
    <title>Email Authentication</title>
</head>
<body>
    <h1>Login</h1>
    <form method="POST" action="/authn/login">
        <input type="email" name="email" placeholder="Enter your email" required>
        <button type="submit">Send Login Link</button>
    </form>
</body>
</html>
```

---

### 2. Send Verification Link

**Endpoint**: `POST /authn/login`

**Description**: Validates the email address, creates or retrieves the user account, generates a verification link, and sends it via email.

**Request Parameters**:

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `email` | Form Data | Yes | The user's email address |
| `X-Krista-Original-URI` | Query | No | The original URL to redirect to after authentication |

**Request Headers**:
```
Content-Type: application/x-www-form-urlencoded
```

**Request Body**:
```
email=user@example.com
```

**Success Response**:

- **Status Code**: `200 OK`
- **Content-Type**: `text/html`
- **Body**: HTML page instructing user to check their email

**Example Success Response**:
```html
<!DOCTYPE html>
<html>
<head>
    <title>Check Your Email</title>
</head>
<body>
    <h1>Check Your Email</h1>
    <p>We've sent a verification link to user@example.com</p>
    <p>Click the link in the email to complete your login.</p>
</body>
</html>
```

**Error Responses**:

| Status Code | Error Message | Cause |
|-------------|---------------|-------|
| `400 Bad Request` | "Email address is required" | Email parameter is missing or empty |
| `400 Bad Request` | "Email address '[email]' is not valid." | Email format is invalid |
| `403 Forbidden` | "ALLOW_AUTO_PERSON_CREATION is not enabled and domain for email '[email]' is not supported in workspace." | Email domain not allowed |
| `404 Not Found` | "Account not found" | Account doesn't exist and auto-creation is disabled |
| `500 Internal Server Error` | "Failed to send email" | SMTP configuration error or email delivery failure |

**Example Error Response**:
```json
{
  "error": "Email address 'invalid-email' is not valid.",
  "status": 400
}
```

**cURL Example**:
```bash
curl -X POST https://your-appliance.com/authn/login \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "email=user@example.com"
```

---

### 3. Verify Email Link

**Endpoint**: `GET /authn/`

**Description**: Verifies the email verification link, validates the secret code, creates an authenticated session, and redirects the user to the original URL.

**Request Parameters**:

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `code` | Query | Yes | The secret verification code from the email link |
| `X-Krista-Original-URI` | Query | No | The original URL to redirect to after authentication |

**Request Headers**: None required

**Success Response**:

- **Status Code**: `302 Found` (Redirect)
- **Location Header**: Original URL or default dashboard
- **Set-Cookie**: `X-Krista-Session-Id=[session-id]; HttpOnly; Secure; Path=/`

**Example**:
```
GET /authn/?code=a1b2c3d4-e5f6-7890-abcd-ef1234567890&X-Krista-Original-URI=/dashboard
```

**Response Headers**:
```
HTTP/1.1 302 Found
Location: /dashboard
Set-Cookie: X-Krista-Session-Id=session-12345; HttpOnly; Secure; Path=/
```

**Error Responses**:

| Status Code | Error Message | Cause |
|-------------|---------------|-------|
| `400 Bad Request` | "Verification code is required" | Code parameter is missing |
| `404 Not Found` | "Email verification link is not found." | Invalid or removed verification code |
| `410 Gone` | "Email verification link is expired." | Link is older than 30 minutes |
| `409 Conflict` | "Email verification link is USED." | Link has already been used |
| `403 Forbidden` | "Domain not supported" | Email domain validation failed |

**Example Error Response**:
```json
{
  "error": "Email verification link is expired.",
  "status": 410
}
```

**cURL Example**:
```bash
curl -X GET "https://your-appliance.com/authn/?code=a1b2c3d4-e5f6-7890-abcd-ef1234567890" \
  -L -c cookies.txt
```

---

### 4. Logout

**Endpoint**: `GET /authn/logout`

**Description**: Terminates the current authenticated session and clears the session cookie.

**Request Parameters**: None

**Request Headers**:
```
Cookie: X-Krista-Session-Id=[session-id]
```

**Success Response**:

- **Status Code**: `302 Found` (Redirect)
- **Location Header**: `/authn/login`
- **Set-Cookie**: `X-Krista-Session-Id=; Max-Age=0; Path=/` (clears cookie)

**Example**:
```
GET /authn/logout
```

**Response Headers**:
```
HTTP/1.1 302 Found
Location: /authn/login
Set-Cookie: X-Krista-Session-Id=; Max-Age=0; Path=/
```

**cURL Example**:
```bash
curl -X GET https://your-appliance.com/authn/logout \
  -b cookies.txt \
  -L
```

---

## Authentication Flow Example

Here's a complete example of the authentication flow using the API endpoints:

### Step 1: User Requests Login

```bash
# User navigates to login page
curl -X GET "https://your-appliance.com/authn/login?X-Krista-Original-URI=/dashboard"
```

**Response**: HTML login form

### Step 2: User Submits Email

```bash
# User submits email address
curl -X POST https://your-appliance.com/authn/login \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "email=user@example.com"
```

**Response**: "Check your email" page

### Step 3: System Sends Email

The system sends an email to `user@example.com` with content:

```
Subject: Email Authentication Link

Click the link below to log in:

https://your-appliance.com/authn/?code=a1b2c3d4-e5f6-7890-abcd-ef1234567890&X-Krista-Original-URI=/dashboard

This link will expire in 30 minutes.
```

### Step 4: User Clicks Verification Link

```bash
# User clicks link in email
curl -X GET "https://your-appliance.com/authn/?code=a1b2c3d4-e5f6-7890-abcd-ef1234567890&X-Krista-Original-URI=/dashboard" \
  -L -c cookies.txt
```

**Response**: Redirect to `/dashboard` with session cookie set

### Step 5: User Accesses Protected Resource

```bash
# User accesses protected resource with session cookie
curl -X GET https://your-appliance.com/dashboard \
  -b cookies.txt
```

**Response**: Dashboard content (authenticated)

### Step 6: User Logs Out

```bash
# User logs out
curl -X GET https://your-appliance.com/authn/logout \
  -b cookies.txt \
  -L
```

**Response**: Redirect to login page, session cookie cleared

---

## Request/Response Examples

### Example 1: Successful Login Flow

**Request**:
```http
POST /authn/login HTTP/1.1
Host: your-appliance.com
Content-Type: application/x-www-form-urlencoded

email=john.doe@company.com
```

**Response**:
```http
HTTP/1.1 200 OK
Content-Type: text/html

<!DOCTYPE html>
<html>
<head><title>Check Your Email</title></head>
<body>
    <h1>Check Your Email</h1>
    <p>We've sent a verification link to john.doe@company.com</p>
</body>
</html>
```

**Email Sent**:
```
To: john.doe@company.com
From: noreply@company.com
Subject: Email Authentication Link

Click the link below to log in:

https://your-appliance.com/authn/?code=550e8400-e29b-41d4-a716-446655440000

This link will expire in 30 minutes.
```

**Verification Request**:
```http
GET /authn/?code=550e8400-e29b-41d4-a716-446655440000 HTTP/1.1
Host: your-appliance.com
```

**Verification Response**:
```http
HTTP/1.1 302 Found
Location: /
Set-Cookie: X-Krista-Session-Id=abc123def456; HttpOnly; Secure; Path=/
```

### Example 2: Invalid Email Format

**Request**:
```http
POST /authn/login HTTP/1.1
Host: your-appliance.com
Content-Type: application/x-www-form-urlencoded

email=invalid-email
```

**Response**:
```http
HTTP/1.1 400 Bad Request
Content-Type: application/json

{
  "error": "Email address 'invalid-email' is not valid.",
  "status": 400
}
```

### Example 3: Domain Not Supported

**Request**:
```http
POST /authn/login HTTP/1.1
Host: your-appliance.com
Content-Type: application/x-www-form-urlencoded

email=user@unauthorized-domain.com
```

**Response**:
```http
HTTP/1.1 403 Forbidden
Content-Type: application/json

{
  "error": "ALLOW_AUTO_PERSON_CREATION is not enabled and domain for email 'user@unauthorized-domain.com' is not supported in workspace.",
  "status": 403
}
```

### Example 4: Expired Verification Link

**Request**:
```http
GET /authn/?code=expired-code-12345 HTTP/1.1
Host: your-appliance.com
```

**Response**:
```http
HTTP/1.1 410 Gone
Content-Type: application/json

{
  "error": "Email verification link is expired.",
  "status": 410
}
```

### Example 5: Link Already Used

**Request**:
```http
GET /authn/?code=used-code-67890 HTTP/1.1
Host: your-appliance.com
```

**Response**:
```http
HTTP/1.1 409 Conflict
Content-Type: application/json

{
  "error": "Email verification link is USED.",
  "status": 409
}
```

---

## Integration Examples

### JavaScript/Fetch API

```javascript
// Send login request
async function sendLoginLink(email) {
  const formData = new URLSearchParams();
  formData.append('email', email);

  const response = await fetch('/authn/login', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    },
    body: formData
  });

  if (response.ok) {
    console.log('Verification link sent to', email);
  } else {
    const error = await response.json();
    console.error('Error:', error.error);
  }
}

// Logout
async function logout() {
  const response = await fetch('/authn/logout', {
    method: 'GET',
    credentials: 'include'
  });

  if (response.ok) {
    window.location.href = '/authn/login';
  }
}
```

### Python/Requests

```python
import requests

# Send login request
def send_login_link(email):
    url = 'https://your-appliance.com/authn/login'
    data = {'email': email}

    response = requests.post(url, data=data)

    if response.status_code == 200:
        print(f'Verification link sent to {email}')
    else:
        print(f'Error: {response.json()["error"]}')

# Verify link
def verify_link(code):
    url = f'https://your-appliance.com/authn/?code={code}'

    session = requests.Session()
    response = session.get(url, allow_redirects=True)

    if response.status_code == 200:
        print('Authentication successful')
        return session
    else:
        print(f'Error: {response.json()["error"]}')
        return None

# Logout
def logout(session):
    url = 'https://your-appliance.com/authn/logout'
    response = session.get(url)

    if response.status_code == 200:
        print('Logged out successfully')
```

### Java/HttpClient

```java
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class EmailAuthClient {

    private static final String BASE_URL = "https://your-appliance.com";
    private final HttpClient client = HttpClient.newHttpClient();

    public void sendLoginLink(String email) throws Exception {
        String formData = "email=" + email;

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/authn/login"))
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString(formData))
            .build();

        HttpResponse<String> response = client.send(request,
            HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            System.out.println("Verification link sent to " + email);
        } else {
            System.err.println("Error: " + response.body());
        }
    }

    public void logout() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/authn/logout"))
            .GET()
            .build();

        HttpResponse<String> response = client.send(request,
            HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 302) {
            System.out.println("Logged out successfully");
        }
    }
}
```

---

## Error Handling Best Practices

### 1. Validate Email Before Submission

```javascript
function isValidEmail(email) {
  const regex = /^[a-z0-9_+&*-]+(?:\.[a-z0-9_+&*-]+)*@(?:[a-z0-9-]+\.)+[a-z]{2,7}$/i;
  return regex.test(email);
}

async function sendLoginLink(email) {
  if (!isValidEmail(email)) {
    alert('Please enter a valid email address');
    return;
  }

  // Proceed with API call
}
```

### 2. Handle Network Errors

```javascript
async function sendLoginLink(email) {
  try {
    const response = await fetch('/authn/login', {
      method: 'POST',
      body: new URLSearchParams({ email })
    });

    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.error);
    }

    // Success
  } catch (error) {
    if (error instanceof TypeError) {
      alert('Network error. Please check your connection.');
    } else {
      alert(error.message);
    }
  }
}
```

### 3. Provide User Feedback

```javascript
async function sendLoginLink(email) {
  const button = document.getElementById('submit-button');
  button.disabled = true;
  button.textContent = 'Sending...';

  try {
    const response = await fetch('/authn/login', {
      method: 'POST',
      body: new URLSearchParams({ email })
    });

    if (response.ok) {
      showMessage('Check your email for the verification link', 'success');
    } else {
      const error = await response.json();
      showMessage(error.error, 'error');
    }
  } finally {
    button.disabled = false;
    button.textContent = 'Send Login Link';
  }
}
```

---

## Security Considerations

### 1. Always Use HTTPS

All API endpoints must be accessed over HTTPS to protect:
- Email addresses in transit
- Verification codes in URLs
- Session cookies

### 2. Validate Verification Codes

- Verification codes are single-use
- Codes expire after 30 minutes
- Codes are cryptographically random (UUID)

### 3. Secure Session Cookies

Session cookies have the following security attributes:
- `HttpOnly`: Prevents JavaScript access
- `Secure`: Only sent over HTTPS
- `SameSite`: Protects against CSRF
- `Path=/`: Limited to root path

### 4. Rate Limiting

Consider implementing rate limiting to prevent:
- Email flooding attacks
- Brute force verification code attempts
- Account enumeration

### 5. Domain Validation

Always configure domain restrictions to limit authentication to trusted email domains.

---

## See Also

- [Extension Configuration](pages/ExtensionConfiguration.md) - Configure SMTP and domain settings
- [Authentication](pages/Authentication.md) - Authentication flow and security
- [Dependencies](pages/Dependencies.md) - Using as a dependency
- [Troubleshooting](pages/Troubleshooting.md) - Common issues and solutions


