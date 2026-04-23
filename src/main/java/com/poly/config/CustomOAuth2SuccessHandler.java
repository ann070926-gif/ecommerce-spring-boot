package com.poly.config;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.poly.dao.AccountDAO;
import com.poly.dao.AuthorityDAO;
import com.poly.dao.RoleDAO;
import com.poly.entity.Account;
import com.poly.entity.Authority;
import com.poly.entity.Role;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private AccountDAO accountDAO;
    
    @Autowired
    private RoleDAO roleDAO;
    
    @Autowired
    private AuthorityDAO authorityDAO;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        // Lấy thông tin từ OAuth2
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");

        // Kiểm tra xem email đã tồn tại trong DB chưa
        Account account = accountDAO.findById(email).orElse(null);

        if (account == null) {
            // Nếu chưa tồn tại, tạo tài khoản mới
            account = new Account();
            account.setUsername(email);
            account.setEmail(email);
            account.setFullname(name != null ? name : email);
            account.setPassword(""); // OAuth2 không cần password
            account.setPhoto(""); // Có thể lấy từ oauth2User.getAttribute("picture")
            account.setAddress("");
            account.setPhone("");
            
            // Lưu tài khoản
            accountDAO.save(account);

            // Cấp quyền mặc định là "USER"
            Role role = roleDAO.findById("USER").orElse(null);
            if (role != null) {
                Authority authority = new Authority();
                authority.setAccount(account);
                authority.setRole(role);
                authorityDAO.save(authority);
            }
        } else {
            // Nếu đã tồn tại, cập nhật fullname từ Google
            if (name != null && !name.equals(account.getFullname())) {
                account.setFullname(name);
                accountDAO.save(account);
            }
        }

        // Redirect về trang chủ
        response.sendRedirect("/");
    }
}
