package id.ac.ui.cs.advprog.auctionwallet.wallet.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Component
public class GatewaySecretInterceptor implements HandlerInterceptor {

    @Value("${gateway.secret:local-dev-gateway-secret}")
    private String configuredGatewaySecret;

    @Override
    @SuppressWarnings("PMD.LawOfDemeter")
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String gatewaySecretHeader = request.getHeader("X-Gateway-Secret");

        if (!isValidSecret(gatewaySecretHeader)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Unauthorized: Invalid or missing X-Gateway-Secret");
            return false;
        }
        return true;
    }
    
    private boolean isValidSecret(String incoming) {
        if (incoming == null) {
            return false;
        }
        byte[] incomingBytes  = incoming.getBytes(StandardCharsets.UTF_8);
        byte[] configuredBytes = configuredGatewaySecret.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(incomingBytes, configuredBytes);
    }
}
