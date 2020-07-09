package de.testbefund.testbefundapi.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

import static org.springframework.security.config.Customizer.withDefaults;

@TestConfiguration
@EnableWebSecurity
@Order(value = 99)
public class TestSecurityConfig extends SecurityConfig {

    @Value("${testbefund.user}")
    private String masterUser;

    @Value("${testbefund.pass}")
    private String masterPass;

    @Value("${testbefund.allowed-origins}")
    private List<String> allowedOrigins;

    @Value("${testbefund.allowed-headers}")
    private List<String> allowedHeaders;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.cors(withDefaults())
                .authorizeRequests()
                .antMatchers(HttpMethod.POST, "/v1/test/container")
                .authenticated()
                .antMatchers(HttpMethod.GET, "/v1/test/auth")
                .authenticated()
                .antMatchers("/v1/test/**")
                .permitAll()
                .antMatchers("/client/**")
                .authenticated()
                .and()
                .csrf().disable()
                .httpBasic();
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

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication()
                .withUser(masterUser)
                .password(passwordEncoder().encode(masterPass))
                .authorities("ROLE_USER");
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
