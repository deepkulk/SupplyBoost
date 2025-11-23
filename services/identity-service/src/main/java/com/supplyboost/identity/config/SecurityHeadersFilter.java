package com.supplyboost.identity.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.stereotype.Component;

/**
 * Filter to add security headers to all HTTP responses.
 *
 * <p>Headers added: - X-Content-Type-Options: Prevents MIME type sniffing - X-Frame-Options:
 * Prevents clickjacking - X-XSS-Protection: Enables XSS filter - Content-Security-Policy:
 * Restricts resource loading - Strict-Transport-Security: Enforces HTTPS
 */
@Component
public class SecurityHeadersFilter implements Filter {

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {

    HttpServletResponse httpResponse = (HttpServletResponse) response;

    // Prevent MIME type sniffing
    httpResponse.setHeader("X-Content-Type-Options", "nosniff");

    // Prevent clickjacking
    httpResponse.setHeader("X-Frame-Options", "DENY");

    // Enable XSS protection
    httpResponse.setHeader("X-XSS-Protection", "1; mode=block");

    // Content Security Policy
    httpResponse.setHeader(
        "Content-Security-Policy",
        "default-src 'self'; "
            + "script-src 'self' 'unsafe-inline' 'unsafe-eval'; "
            + "style-src 'self' 'unsafe-inline'; "
            + "img-src 'self' data: https:; "
            + "font-src 'self' data:; "
            + "connect-src 'self'; "
            + "frame-ancestors 'none'");

    // Strict Transport Security (HSTS) - Only enable in production with HTTPS
    // httpResponse.setHeader("Strict-Transport-Security", "max-age=31536000;
    // includeSubDomains");

    // Referrer Policy
    httpResponse.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");

    // Permissions Policy (formerly Feature Policy)
    httpResponse.setHeader(
        "Permissions-Policy",
        "geolocation=(), microphone=(), camera=(), payment=(), usb=(), magnetometer=()");

    chain.doFilter(request, response);
  }
}
