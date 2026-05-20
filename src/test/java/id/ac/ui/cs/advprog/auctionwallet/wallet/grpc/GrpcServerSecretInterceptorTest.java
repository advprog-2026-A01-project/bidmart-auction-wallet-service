package id.ac.ui.cs.advprog.auctionwallet.wallet.grpc;

import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({
    "PMD.UnitTestShouldIncludeAssert",
    "PMD.UnitTestContainsTooManyAsserts",
    "PMD.UnitTestAssertionsShouldIncludeMessage",
    "PMD.LawOfDemeter"
})
class GrpcServerSecretInterceptorTest {

    private GrpcServerSecretInterceptor interceptor;

    @Mock
    private ServerCall<String, String> serverCall;

    @Mock
    private ServerCallHandler<String, String> serverCallHandler;

    @Mock
    private ServerCall.Listener<String> listener;

    private static final String SECRET_VALUE = "test-secret";

    @BeforeEach
    void setUp() {
        interceptor = new GrpcServerSecretInterceptor();
        ReflectionTestUtils.setField(interceptor, "configuredSecret", SECRET_VALUE);
    }

    @Test
    void testInterceptCallWithValidSecretProceeds() {
        Metadata headers = new Metadata();
        headers.put(GrpcServerSecretInterceptor.GATEWAY_SECRET_KEY, SECRET_VALUE);

        when(serverCallHandler.startCall(serverCall, headers)).thenReturn(listener);

        ServerCall.Listener<String> result = interceptor.interceptCall(serverCall, headers, serverCallHandler);

        assertEquals(listener, result);
        verify(serverCallHandler).startCall(serverCall, headers);
    }

    @Test
    void testInterceptCallWithMissingSecretClosesCall() {
        Metadata headers = new Metadata();

        ServerCall.Listener<String> result = interceptor.interceptCall(serverCall, headers, serverCallHandler);

        assertNotNull(result);

        ArgumentCaptor<Status> statusCaptor = ArgumentCaptor.forClass(Status.class);
        verify(serverCall).close(statusCaptor.capture(), any(Metadata.class));

        assertEquals(Status.UNAUTHENTICATED.getCode(), statusCaptor.getValue().getCode());
        assertEquals("Invalid or missing x-gateway-secret", statusCaptor.getValue().getDescription());
    }

    @Test
    void testInterceptCallWithInvalidSecretClosesCall() {
        Metadata headers = new Metadata();
        headers.put(GrpcServerSecretInterceptor.GATEWAY_SECRET_KEY, "wrong-secret");

        ServerCall.Listener<String> result = interceptor.interceptCall(serverCall, headers, serverCallHandler);

        assertNotNull(result);

        ArgumentCaptor<Status> statusCaptor = ArgumentCaptor.forClass(Status.class);
        verify(serverCall).close(statusCaptor.capture(), any(Metadata.class));

        assertEquals(Status.UNAUTHENTICATED.getCode(), statusCaptor.getValue().getCode());
        assertEquals("Invalid or missing x-gateway-secret", statusCaptor.getValue().getDescription());
    }
}
