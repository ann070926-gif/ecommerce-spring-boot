package com.poly.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.poly.dao.AccountDAO;
import com.poly.entity.Account;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private AccountDAO accountDAO;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("\n========== LOGIN DEBUG START ==========");
        System.out.println("=== Tìm user: " + username);
        
        try {
            Account account = accountDAO.findById(username)
                    .orElseThrow(() -> {
                        System.out.println("!!! User NOT FOUND: " + username);
                        return new UsernameNotFoundException("Không tìm thấy tài khoản: " + username);
                    });

            System.out.println("✓ Tìm thấy user: " + account.getUsername());
            System.out.println("  Password in DB: " + account.getPassword());
            
            if (account.getAuthorities() == null) {
                System.out.println("!!! Authorities is NULL - no roles assigned");
                return User.withUsername(account.getUsername())
                    .password("{noop}" + account.getPassword())
                    .roles("USER") // Default role
                    .build();
            }
            
            System.out.println("  Authorities count: " + account.getAuthorities().size());
            
            String[] roles = account.getAuthorities().stream()
                    .map(authority -> {
                        String roleId = authority.getRole().getId();
                        System.out.println("    - Role: " + roleId);
                        return roleId;
                    })
                    .toArray(String[]::new);
            
            if (roles.length == 0) {
                System.out.println("!!! WARNING: No roles found, assigning default USER role");
                roles = new String[]{"USER"};
            }
            
            System.out.println("✓ Final roles: " + java.util.Arrays.toString(roles));

            UserDetails user = User.withUsername(account.getUsername())
                    .password("{noop}" + account.getPassword())
                    .roles(roles)
                    .build();
            
            System.out.println("✓ UserDetails created successfully");
            System.out.println("========== LOGIN DEBUG END ==========\n");
            return user;
            
        } catch (UsernameNotFoundException e) {
            System.out.println("!!! UsernameNotFoundException: " + e.getMessage());
            System.out.println("========== LOGIN DEBUG END ==========\n");
            throw e;
        } catch (Exception e) {
            System.out.println("!!! UNEXPECTED ERROR: " + e.getClass().getName());
            System.out.println("!!! Message: " + e.getMessage());
            e.printStackTrace();
            System.out.println("========== LOGIN DEBUG END ==========\n");
            throw new UsernameNotFoundException("Lỗi khi tải tài khoản: " + e.getMessage(), e);
        }
    }
}