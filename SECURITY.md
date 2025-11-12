# Security Policy

## Supported Versions
Security updates are provided on the `main` branch.

## Reporting a Vulnerability
If you believe you have found a security vulnerability, please:

1. DO NOT open a public issue.
2. Email the maintainers at: security@example.org (replace with actual contact)
3. Include steps to reproduce, potential impact, and any suggested mitigations.

We aim to acknowledge reports within 72 hours.

## Scope
This app runs locally and communicates only with Android devices over ADB. It does not transmit data over the internet. Vulnerabilities of interest include:
- Command injection
- Privilege escalation
- Unsafe file operations
- Tampering with restore scripts
- Supply chain issues in platform-tools update

Thank you for helping keep the project and users safe.
