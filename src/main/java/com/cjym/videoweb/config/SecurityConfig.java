package com.cjym.videoweb.config;

import com.cjym.videoweb.provider.CustomAuthenticationProvider;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.savedrequest.NullRequestCache;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Resource
    private CustomAuthenticationProvider customAuthenticationProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                // 禁用 CSRF（JWT 认证无需）
        http.csrf(csrf -> csrf.disable())
                // 禁用请求缓存，避免内部 MvcRequestMatcher 触发 UrlPathHelper.PATH 报错
                .requestCache(cache -> cache.requestCache(new NullRequestCache()))
                // URL 授权规则（全部放行）
                .authorizeHttpRequests(authorize -> authorize
                        .anyRequest().permitAll()
                )
                // 表单登录配置
                .formLogin(form -> form
                        .loginPage("/login")               // 自定义登录页
                        .loginProcessingUrl("/login")      // 登录处理 URL
                        .defaultSuccessUrl("/index")        // 登录成功跳转
                        .permitAll()
                )
                // 登出配置
                .logout(logout -> logout
                        .logoutUrl("/logout")               // 登出 URL
                        .logoutSuccessUrl("/login")          // 登出后跳转
                        .permitAll()
                )
                // 异常处理
                .exceptionHandling(exception -> exception
                        .accessDeniedPage("/error/403")      // 未授权跳转
                );

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);
        authBuilder.authenticationProvider(customAuthenticationProvider);
        return authBuilder.build();
    }
}
