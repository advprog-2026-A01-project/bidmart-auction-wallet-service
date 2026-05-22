package id.ac.ui.cs.advprog.auctionwallet.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings({
    "PMD.UnitTestShouldIncludeAssert",
    "PMD.AvoidDuplicateLiterals",
    "PMD.UnitTestContainsTooManyAsserts",
    "PMD.UnitTestAssertionsShouldIncludeMessage"
})
class SecurityConfigTest {

    private SecurityConfig securityConfig;

    @BeforeEach
    void setUp() {
        JwtAuthenticationFilter mockFilter = Mockito.mock(JwtAuthenticationFilter.class);
        securityConfig = new SecurityConfig(mockFilter);
    }

    @Test
    void securityConfigCanBeInstantiatedWithMockFilter() {
        assertEquals(SecurityConfig.class, securityConfig.getClass());
    }
}
