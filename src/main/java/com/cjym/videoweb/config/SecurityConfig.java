package com.cjym.videoweb.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // URL 授权规则
                .authorizeHttpRequests(authorize -> authorize
                        // 静态资源放行
                        .requestMatchers("/static/**", "/css/**", "/js/**", "/images/**").permitAll()
                        // 登录接口放行
                        .requestMatchers("/login").permitAll()
                        // 其他所有请求需要认证
                        .anyRequest().authenticated()
                )
                // 表单登录配置
                .formLogin(form -> form
                        .loginPage("/login")           // 自定义登录页
                        .defaultSuccessUrl("/index")    // 登录成功跳转
                        .permitAll()
                )
                // 登出配置
                .logout(logout -> logout
                        .logoutSuccessUrl("/login")     // 登出后跳转
                        .permitAll()
                )
                // 异常处理
                .exceptionHandling(exception -> exception
                        .accessDeniedPage("/error/403") // 未授权跳转
                );

        return http.build();
    }
}
