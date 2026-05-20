package id.ac.ui.cs.advprog.auctionwallet.wallet.grpc;

import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;
import org.springframework.beans.factory.annotation.Value;

@GrpcGlobalServerInterceptor
public class GrpcServerSecretInterceptor implements ServerInterceptor {

    static final Metadata.Key<String> GATEWAY_SECRET_KEY =
            Metadata.Key.of("x-gateway-secret", Metadata.ASCII_STRING_MARSHALLER);

    @Value("${gateway.secret:local-dev-gateway-secret}")
    private String configuredSecret;

    @Override
    @SuppressWarnings("PMD.LawOfDemeter")
    public <I, O> ServerCall.Listener<I> interceptCall(
            ServerCall<I, O> call,
            Metadata headers,
            ServerCallHandler<I, O> next) {

        String incomingSecret = headers.get(GATEWAY_SECRET_KEY);
        if (incomingSecret == null || !incomingSecret.equals(configuredSecret)) {
            call.close(
                    Status.UNAUTHENTICATED.withDescription("Invalid or missing x-gateway-secret"),
                    new Metadata());
            return new ServerCall.Listener<>() { };
        }
        return next.startCall(call, headers);
    }
}
