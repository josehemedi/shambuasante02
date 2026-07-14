package hospicloud.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import hospicloud.exceptions.ApiError;
import hospicloud.security.JwtAuthenticationFilter;
import hospicloud.security.PlanFeatureEnforcementFilter;
import hospicloud.security.TenantResolverFilter;
import hospicloud.security.TenantSubscriptionEnforcementFilter;
import hospicloud.services.SaasPlanService;
import hospicloud.services.TenantSubscriptionAccessService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           JwtAuthenticationFilter jwtAuthenticationFilter,
                                           TenantResolverFilter tenantResolverFilter,
                                           TenantSubscriptionEnforcementFilter tenantSubscriptionEnforcementFilter,
                                           PlanFeatureEnforcementFilter planFeatureEnforcementFilter,
                                           ObjectMapper objectMapper) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> {})
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(ex -> ex
                    .authenticationEntryPoint((request, response, authException) -> {
                        response.setStatus(HttpStatus.UNAUTHORIZED.value());
                        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                        objectMapper.writeValue(response.getOutputStream(), new ApiError(
                                HttpStatus.UNAUTHORIZED.value(),
                                "Unauthorized",
                                "Authentification requise. Veuillez vous reconnecter.",
                                request.getRequestURI()));
                    })
                    .accessDeniedHandler((request, response, accessDeniedException) -> {
                        response.setStatus(HttpStatus.FORBIDDEN.value());
                        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                        objectMapper.writeValue(response.getOutputStream(), new ApiError(
                                HttpStatus.FORBIDDEN.value(),
                                "Forbidden",
                                "Accès refusé pour votre rôle ou votre session.",
                                request.getRequestURI()));
                    }))
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterAfter(tenantResolverFilter, JwtAuthenticationFilter.class)
            .addFilterAfter(tenantSubscriptionEnforcementFilter, TenantResolverFilter.class)
            .addFilterAfter(planFeatureEnforcementFilter, TenantSubscriptionEnforcementFilter.class)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.POST, "/api/auth/logout").authenticated()
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                .requestMatchers("/ws/**").permitAll()
                .requestMatchers("/api/async/**").authenticated()

                .requestMatchers("/api/tenant-admin/**").hasRole("TENANT_ADMIN")

                .requestMatchers("/api/tenant/cashier/**").hasAnyRole("CAISSIER", "TENANT_ADMIN")

                .requestMatchers("/api/monitoring/**").hasRole("SUPER_ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/audit/**").hasRole("SUPER_ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/support/tickets").hasAnyRole(
                        "TENANT_ADMIN", "MEDECIN", "RECEPTION", "CAISSIER", "LABORANTIN")
                .requestMatchers(HttpMethod.GET, "/api/support/tickets").hasAnyRole(
                        "SUPER_ADMIN", "TENANT_ADMIN", "MEDECIN", "RECEPTION", "CAISSIER", "LABORANTIN")
                .requestMatchers(HttpMethod.PATCH, "/api/support/tickets/**").hasRole("SUPER_ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/dashboard/stats").hasRole("SUPER_ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/dashboard/mrr-series").hasRole("SUPER_ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/dashboard/plan-distribution").hasRole("SUPER_ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/dashboard/tenants").hasRole("SUPER_ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/subscriptions/**").hasRole("SUPER_ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/dashboard", "/api/dashboard/medecin", "/api/dashboard/medecin/pdf").hasRole("MEDECIN")
                .requestMatchers(HttpMethod.GET, "/api/workspace/medecin").hasRole("MEDECIN")
                .requestMatchers(HttpMethod.GET, "/api/hopitaux/**").hasRole("SUPER_ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/hopitaux/**").hasRole("SUPER_ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/hopitaux/**").hasRole("SUPER_ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/hopitaux/**").hasRole("SUPER_ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/hopitaux/**").hasRole("SUPER_ADMIN")

                .requestMatchers("/api/v1/patients/me/**").hasRole("PATIENT")
                .requestMatchers("/api/v1/reception/**").hasAnyRole("RECEPTION", "TENANT_ADMIN")
                .requestMatchers("/api/v1/lab/**").hasAnyRole("LABORANTIN", "TENANT_ADMIN")

                .requestMatchers("/api/patients/**").hasAnyRole("TENANT_ADMIN", "MEDECIN", "RECEPTION")
                .requestMatchers(HttpMethod.GET, "/api/medecins/**").hasAnyRole("TENANT_ADMIN", "MEDECIN", "RECEPTION")
                .requestMatchers("/api/medecins/**").hasAnyRole("TENANT_ADMIN", "MEDECIN")
                .requestMatchers("/api/medecins/patients/**").hasAnyRole("TENANT_ADMIN", "MEDECIN")
                .requestMatchers("/api/medecin/consultations/**").hasRole("MEDECIN")
                .requestMatchers("/api/medecin/laboratoire/**").hasRole("MEDECIN")
                .requestMatchers("/api/medecin/file-attente/**").hasRole("MEDECIN")

                .requestMatchers(HttpMethod.GET, "/api/rendezvous/jour", "/api/rendezvous/medecin", "/api/rendezvous/medecin/historique", "/api/rendezvous/disponibilite").hasAnyRole("TENANT_ADMIN", "MEDECIN")
                .requestMatchers(HttpMethod.GET, "/api/rendezvous").hasAnyRole("TENANT_ADMIN", "MEDECIN", "RECEPTION")
                .requestMatchers(HttpMethod.GET, "/api/rendezvous/*").hasAnyRole("TENANT_ADMIN", "MEDECIN", "RECEPTION", "PATIENT")
                .requestMatchers(HttpMethod.POST, "/api/rendezvous").hasAnyRole("TENANT_ADMIN", "MEDECIN", "RECEPTION")
                .requestMatchers(HttpMethod.PUT, "/api/rendezvous/**").hasAnyRole("TENANT_ADMIN", "MEDECIN", "RECEPTION")
                .requestMatchers(HttpMethod.PATCH, "/api/rendezvous/**").hasAnyRole("TENANT_ADMIN", "MEDECIN", "RECEPTION")

                .requestMatchers(HttpMethod.POST, "/api/consultations/teleconsultation/token")
                    .hasAnyRole("MEDECIN", "PATIENT")
                .requestMatchers(HttpMethod.GET, "/api/consultations/teleconsultation/*/messages")
                    .hasAnyRole("MEDECIN", "PATIENT")
                .requestMatchers(HttpMethod.POST, "/api/consultations/teleconsultation/*/messages")
                    .hasAnyRole("MEDECIN", "PATIENT")
                .requestMatchers(HttpMethod.POST, "/api/consultations/teleconsultation/*/messages/read")
                    .hasAnyRole("MEDECIN", "PATIENT")
                .requestMatchers("/api/consultations/**").hasAnyRole("TENANT_ADMIN", "MEDECIN")
                .requestMatchers(HttpMethod.GET, "/api/ordonnances/**").hasAnyRole("TENANT_ADMIN", "MEDECIN", "PATIENT")
                .requestMatchers(HttpMethod.POST, "/api/ordonnances/**").hasAnyRole("TENANT_ADMIN", "MEDECIN")
                .requestMatchers(HttpMethod.PUT, "/api/ordonnances/**").hasAnyRole("TENANT_ADMIN", "MEDECIN")
                .requestMatchers(HttpMethod.PATCH, "/api/ordonnances/**").hasAnyRole("TENANT_ADMIN", "MEDECIN")
                .requestMatchers(HttpMethod.DELETE, "/api/ordonnances/**").hasAnyRole("TENANT_ADMIN", "MEDECIN")

                .requestMatchers(HttpMethod.GET, "/api/v1/factures/**").hasAnyRole("TENANT_ADMIN", "CAISSIER")
                .requestMatchers(HttpMethod.POST, "/api/v1/factures/**").hasAnyRole("TENANT_ADMIN", "CAISSIER")
                .requestMatchers(HttpMethod.PUT, "/api/v1/factures/**").hasAnyRole("TENANT_ADMIN", "CAISSIER")
                .requestMatchers(HttpMethod.PATCH, "/api/v1/factures/**").hasAnyRole("TENANT_ADMIN", "CAISSIER")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/factures/**").hasRole("TENANT_ADMIN")

                .requestMatchers("/api/societes/**").hasAnyRole("TENANT_ADMIN", "MEDECIN", "RECEPTION")
                .requestMatchers("/api/antecedents/**").hasAnyRole("TENANT_ADMIN", "MEDECIN", "RECEPTION")
                .requestMatchers("/api/v1/horaires-travail/**").hasAnyRole("TENANT_ADMIN", "MEDECIN", "RECEPTION")
                .requestMatchers("/api/v1/discharge-notes/**").hasAnyRole("TENANT_ADMIN", "MEDECIN", "RECEPTION")

                .requestMatchers(HttpMethod.GET, "/api/v1/sortie/patient/*/contexte").hasAnyRole("TENANT_ADMIN", "MEDECIN")
                .requestMatchers(HttpMethod.POST, "/api/v1/sortie/autoriser").hasAnyRole("TENANT_ADMIN", "MEDECIN")
                .requestMatchers(HttpMethod.GET, "/api/v1/sortie/pretes").hasAnyRole("TENANT_ADMIN", "RECEPTION")
                .requestMatchers(HttpMethod.POST, "/api/v1/sortie/*/delivrer").hasAnyRole("TENANT_ADMIN", "RECEPTION")

                .requestMatchers("/api/reports/**").hasAnyRole("TENANT_ADMIN", "LABORANTIN")

                .requestMatchers("/api/ai/**").hasAnyRole("SUPER_ADMIN", "TENANT_ADMIN", "MEDECIN")

                .requestMatchers("/api/archives/**").hasAnyRole(
                        "ARCHIVISTE", "TENANT_ADMIN", "MEDECIN", "RECEPTION")

                .anyRequest().authenticated()
            );

        return http.build();
    }

    @Bean
    public TenantResolverFilter tenantResolverFilter() {
        return new TenantResolverFilter();
    }

    @Bean
    public TenantSubscriptionEnforcementFilter tenantSubscriptionEnforcementFilter(
            TenantSubscriptionAccessService tenantSubscriptionAccessService,
            ObjectMapper objectMapper) {
        return new TenantSubscriptionEnforcementFilter(tenantSubscriptionAccessService, objectMapper);
    }

    @Bean
    public PlanFeatureEnforcementFilter planFeatureEnforcementFilter(
            SaasPlanService saasPlanService,
            ObjectMapper objectMapper) {
        return new PlanFeatureEnforcementFilter(saasPlanService, objectMapper);
    }
}
