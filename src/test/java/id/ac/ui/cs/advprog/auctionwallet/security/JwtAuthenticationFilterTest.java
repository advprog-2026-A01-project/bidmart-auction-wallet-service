package id.ac.ui.cs.advprog.auctionwallet.security;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Unit tests for JwtAuthenticationFilter.
 *
 * We inject a mock JwtDecoder via ReflectionTestUtils to avoid needing a live JWKS server,
 * allowing us to test all code paths including the happy path where a valid JWT is decoded
 * and the security context is populated.
 */
@SuppressWarnings({
    "PMD.UnitTestShouldIncludeAssert",
    "PMD.AvoidDuplicateLiterals",
    "PMD.UnitTestContainsTooManyAsserts",
    "PMD.UnitTestAssertionsShouldIncludeMessage",
    "PMD.LawOfDemeter"
})
class JwtAuthenticationFilterTest {

    private static final String JWKS_URI = "http://localhost:8081/.well-known/jwks.json";

    private JwtAuthenticationFilter filterWithMockDecoder(JwtDecoder mockDecoder) {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(JWKS_URI);
        ReflectionTestUtils.setField(filter, "jwtDecoder", mockDecoder);
        return filter;
    }

    @Test
    void requestWithoutAuthorizationHeaderPassesThrough() throws Exception {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(JWKS_URI);

        MockHttpServletRequest request   = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilterInternal(request, response, chain);

        assertEquals(200, response.getStatus());
    }

    @Test
    void requestWithNonBearerAuthorizationHeaderPassesThrough() throws Exception {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(JWKS_URI);

        MockHttpServletRequest request   = new MockHttpServletRequest();
        request.addHeader("Authorization", "Basic dXNlcjpwYXNz");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilterInternal(request, response, chain);

        assertEquals(200, response.getStatus());
    }

    @Test
    void requestWithMalformedBearerTokenReturnsUnauthorized() throws Exception {
        JwtDecoder mockDecoder = Mockito.mock(JwtDecoder.class);
        when(mockDecoder.decode(anyString())).thenThrow(new JwtException("Malformed JWT"));

        JwtAuthenticationFilter filter = filterWithMockDecoder(mockDecoder);

        MockHttpServletRequest request   = new MockHttpServletRequest();
        request.setRequestURI("/api/wallet/me/info");
        request.addHeader("Authorization", "Bearer totally.invalid.token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilterInternal(request, response, chain);

        assertEquals(401, response.getStatus());
    }

    @Test
    void requestWithValidJwtSetsSecurityContext() throws Exception {
        Jwt validJwt = Jwt.withTokenValue("valid.token.value")
                .header("alg", "RS256")
                .claim("username", "user-123")
                .claim("sub", "user-123")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        JwtDecoder mockDecoder = Mockito.mock(JwtDecoder.class);
        when(mockDecoder.decode("valid.token.value")).thenReturn(validJwt);

        JwtAuthenticationFilter filter = filterWithMockDecoder(mockDecoder);

        SecurityContextHolder.clearContext();

        MockHttpServletRequest request   = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer valid.token.value");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilterInternal(request, response, chain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("user-123", SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        assertEquals(200, response.getStatus());
        SecurityContextHolder.clearContext();
    }

    @Test
    void requestWithValidJwtButNoUsernameClaim_doesNotSetAuthentication() throws Exception {
        Jwt jwtWithoutUsername = Jwt.withTokenValue("token.no.username")
                .header("alg", "RS256")
                .claim("sub", "user-456")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        JwtDecoder mockDecoder = Mockito.mock(JwtDecoder.class);
        when(mockDecoder.decode("token.no.username")).thenReturn(jwtWithoutUsername);

        JwtAuthenticationFilter filter = filterWithMockDecoder(mockDecoder);

        SecurityContextHolder.clearContext();

        MockHttpServletRequest request   = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token.no.username");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilterInternal(request, response, chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(200, response.getStatus());

        SecurityContextHolder.clearContext();
    }
}
