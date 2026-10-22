package shop.le_paysan.backend.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import shop.le_paysan.backend.auth.AuthRateLimitFilter;
import shop.le_paysan.backend.auth.JwtAuthFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final AuthRateLimitFilter authRateLimitFilter;

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter, AuthRateLimitFilter authRateLimitFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.authRateLimitFilter = authRateLimitFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config, UserDetailsService userDetailsService, PasswordEncoder passwordEncoder)
            throws Exception {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return new org.springframework.security.authentication.ProviderManager(provider);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(allowedOrigins.split(",")));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/error").permitAll()
                        // Doc API (springdoc) : utile pour le dev mobile, desactivee en prod via
                        // springdoc.api-docs.enabled=false (cf. application-prod.yml).
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**")
                        .permitAll()
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/api/v1/contact").permitAll()
                        // Appele par Fedapay (jamais par le frontend) : authentifie par signature HMAC,
                        // pas par JWT (cf. PaymentService.verifySignature). Volontairement hors /api/v1,
                        // cf. commentaire sur PaymentController.webhook().
                        .requestMatchers("/api/payments/fedapay/webhook").permitAll()
                        // Page de redirection pour le retour de paiement mobile, cf.
                        // PaymentController.mobileReturnRedirect() - meme logique que le webhook ci-dessus.
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/payments/mobile-return")
                        .permitAll()
                        // Photos produit et logo/couverture boutique : publiques (catalogue et fiches
                        // boutique visibles sans connexion). Documents KYC (shop-kyc, account-recovery,
                        // security-selfies) restent authentifies.
                        .requestMatchers(
                                org.springframework.http.HttpMethod.GET,
                                "/api/v1/files/products/**",
                                "/api/v1/files/shop-public/**")
                        .permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET,
                                "/api/v1/products/**", "/api/v1/categories/**")
                        .permitAll()
                        // Uniquement les routes de lecture publique de /api/shops (liste + fiche par id) :
                        // /api/shops/me* reste authentifie (ne doit pas matcher ici).
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/v1/shops", "/api/v1/shops/{id:[0-9]+}")
                        .permitAll()
                        // Avis publics (note moyenne + liste) : lecture ouverte, soumission authentifiee
                        // (verification achat livre faite dans ReviewService).
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/v1/shops/*/avis/**")
                        .permitAll()
                        // Bareme de livraison public (affiche sur la fiche produit/boutique) : lecture
                        // ouverte, modification reservee au vendeur (cf. ShopDeliveryTierController).
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/v1/shops/*/paliers-livraison")
                        .permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/v1/shops/*/villes-livraison")
                        .permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(authRateLimitFilter, JwtAuthFilter.class);
        return http.build();
    }
}
