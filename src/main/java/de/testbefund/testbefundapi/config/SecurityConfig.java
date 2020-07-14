package de.testbefund.testbefundapi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity()
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Value("${testbefund.allowed-origins}")
    private List<String> allowedOrigins;

    @Value("${testbefund.allowed-headers}")
    private List<String> allowedHeaders;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.cors(withDefaults())
                .authorizeRequests()
                .antMatchers(HttpMethod.POST, "/v1/testing/container")
                    .authenticated()
                .antMatchers(HttpMethod.GET, "/v1/testing/auth")
                    .authenticated()
                .antMatchers("/organization/**")
                    .authenticated()
                .antMatchers("/v1/testing/**")
                    .permitAll()
                .antMatchers("/v1/finding/**")
                    .permitAll()
                .antMatchers("/error")
                    .permitAll()
                .and()
                    .csrf()
                    .disable()
                    .oauth2ResourceServer()
                    .jwt();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(allowedOrigins);
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(allowedHeaders);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
