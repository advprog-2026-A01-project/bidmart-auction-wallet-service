package id.ac.ui.cs.advprog.auctionwallet.wallet.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final GatewaySecretInterceptor gatewaySecretInterceptor;

    @Autowired
    public WebConfig(GatewaySecretInterceptor gatewaySecretInterceptor) {
        this.gatewaySecretInterceptor = gatewaySecretInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(gatewaySecretInterceptor)
                .addPathPatterns("/api/**");
    }
}
