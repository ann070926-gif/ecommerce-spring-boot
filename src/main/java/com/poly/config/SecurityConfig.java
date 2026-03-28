package com.poly.config;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.poly.service.CustomUserDetailsService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authBuilder.userDetailsService(customUserDetailsService)
                   .passwordEncoder(NoOpPasswordEncoder.getInstance());
        return authBuilder.build();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        
        // 1. Tắt CSRF để API Giỏ hàng không bị chặn
        http.csrf(csrf -> csrf.disable());

        // 2. Cấu hình phân quyền đường dẫn
        http.authorizeHttpRequests(auth -> auth
            // Bắt buộc đăng nhập mới được vào các trang này
            .requestMatchers("/checkout/**", "/order/**", "/tai-khoan/**").authenticated()
            // Bắt buộc có quyền ADMIN (hoặc DIRE) mới được vào trang quản trị
            .requestMatchers("/admin/**").hasAnyRole("ADMIN", "DIRE")
            // Các đường dẫn khác (trang chủ, xem sản phẩm, giỏ hàng) ai cũng được vào
            .anyRequest().permitAll() 
        );

        // 3. Cấu hình Form Đăng nhập
        http.formLogin(form -> form
            .loginPage("/login") // Đường dẫn đến trang giao diện đăng nhập của bạn
            .loginProcessingUrl("/login") // URL khi form submit (Spring Security tự xử lý)
            .defaultSuccessUrl("/", true) // Đăng nhập thành công -> Về trang chủ
            .failureUrl("/login?error=true") // Đăng nhập sai -> Về lại trang login báo lỗi
        );

        // 4. Cấu hình Đăng xuất
        http.logout(logout -> logout
            .logoutUrl("/logout")
            .logoutSuccessUrl("/") // Đăng xuất xong về trang chủ
        );

        return http.build();
    }
}