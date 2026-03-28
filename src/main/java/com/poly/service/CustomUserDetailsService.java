package com.poly.service;

import com.poly.dao.AccountDAO;
import com.poly.entity.Account;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private AccountDAO accountDAO;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = accountDAO.findById(username)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy tài khoản: " + username));

        String[] roles = account.getAuthorities().stream()
                .map(authority -> authority.getRole().getId())
                .toArray(String[]::new);

        // CHÚ Ý DÒNG NÀY: Phải có chữ {noop} nằm ngay trong ngoặc kép, viết liền với mật khẩu
        return User.withUsername(account.getUsername())
                .password("{noop}" + account.getPassword()) 
                .roles(roles)
                .build();
    }
}