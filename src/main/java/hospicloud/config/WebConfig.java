package hospicloud.config;

import hospicloud.security.TechnicalLoggingInterceptor;
import hospicloud.security.TenantLoggingInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final TechnicalLoggingInterceptor technicalLoggingInterceptor;

    public WebConfig(TechnicalLoggingInterceptor technicalLoggingInterceptor) {
        this.technicalLoggingInterceptor = technicalLoggingInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new TenantLoggingInterceptor());
        registry.addInterceptor(technicalLoggingInterceptor);
    }
}