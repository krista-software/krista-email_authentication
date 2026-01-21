# Email Authentication Extension for Krista

1. [Overview](#overview)
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



## License

This project is licensed under the **GNU General Public License v3.0**.

```
Email Authentication Extension for Krista
Copyright (C) 2025 Krista Software

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
```

For the full license text, see the [LICENSE](LICENSE) file or visit https://www.gnu.org/licenses/gpl-3.0.html.
