package id.ac.ui.cs.advprog.auctionwallet.wallet.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class GatewaySecretInterceptor implements HandlerInterceptor {

    @Value("${gateway.secret:local-dev-gateway-secret}")
    private String configuredGatewaySecret;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String gatewaySecretHeader = request.getHeader("X-Gateway-Secret");

        if (gatewaySecretHeader == null || !gatewaySecretHeader.equals(configuredGatewaySecret)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Unauthorized: Invalid or missing X-Gateway-Secret");
            return false;
        }
        return true;
    }
}
