package com.poly.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.poly.dao.AccountDAO;
import com.poly.entity.Account;

@Controller
public class AccountController {

    @Autowired
    private AccountDAO accountDAO;

    // Lấy username hiện tại (hỗ trợ cả OAuth2 và form login)
    private String getCurrentUsername() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
                return null;
            }

            // 1. Kiểm tra OAuth2 Login (Google)
            if (auth.getPrincipal() instanceof OAuth2User) {
                OAuth2User oauth2User = (OAuth2User) auth.getPrincipal();
                String email = oauth2User.getAttribute("email");
                return email != null ? email : auth.getName();
            }

            // 2. Form Login thường
            return auth.getName();
        } catch (Exception e) {
            System.err.println("Error in getCurrentUsername: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // Hiển thị trang hồ sơ cá nhân
    @GetMapping("/tai-khoan/ho-so")
    public String showProfile(Model model) {
        String username = getCurrentUsername();

        if (username == null) {
            return "redirect:/login";
        }

        Account account = accountDAO.findById(username).orElse(null);
        if (account == null) {
            return "redirect:/login";
        }

        model.addAttribute("account", account);
        return "security/profile";
    }

    // Lấy thông tin tài khoản đang đăng nhập (trả về JSON cho modal)
    @GetMapping("/api/account/profile")
    @ResponseBody
    public ResponseEntity<?> getProfile() {
        String username = getCurrentUsername();

        if (username == null) {
            System.out.println("DEBUG: getCurrentUsername() returned null");
            return ResponseEntity.status(401).body("Chưa đăng nhập");
        }

        System.out.println("DEBUG: getCurrentUsername() = " + username);
        
        Account account = accountDAO.findById(username).orElse(null);
        if (account == null) {
            System.out.println("DEBUG: Account not found for username: " + username);
            return ResponseEntity.notFound().build();
        }

        System.out.println("DEBUG: Account found. Fullname = " + account.getFullname());
        
        Map<String, String> data = new HashMap<>();
        data.put("username", account.getUsername());
        data.put("fullname", account.getFullname() != null ? account.getFullname() : "");
        data.put("email",    account.getEmail()    != null ? account.getEmail()    : "");
        data.put("phone",    account.getPhone()    != null ? account.getPhone()    : "");
        data.put("address",  account.getAddress()  != null ? account.getAddress()  : "");
        data.put("photo",    account.getPhoto()     != null ? account.getPhoto()     : "");

        return ResponseEntity.ok(data);
    }

    // Cập nhật hồ sơ tài khoản (nhận form POST, trả về JSON)
    @PostMapping("/tai-khoan/ho-so")
    @ResponseBody
    public ResponseEntity<?> updateProfile(
            @RequestParam(value = "fullname", defaultValue = "") String fullname,
            @RequestParam(value = "phone",    defaultValue = "") String phone,
            @RequestParam(value = "address",  defaultValue = "") String address) {

        String username = getCurrentUsername();

        if (username == null) {
            return ResponseEntity.status(401).body(Map.of("status", "error", "message", "Chưa đăng nhập"));
        }

        // Validate đơn giản
        if (fullname.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("status", "error", "message", "Họ và tên không được để trống"));
        }

        Account account = accountDAO.findById(username).orElse(null);
        if (account == null) {
            return ResponseEntity.notFound().build();
        }

        account.setFullname(fullname.trim());
        account.setPhone(phone.trim());
        account.setAddress(address.trim());
        accountDAO.save(account);

        return ResponseEntity.ok(Map.of(
                "status",   "success",
                "message",  "Cập nhật hồ sơ thành công!",
                "fullname", account.getFullname(),
                "phone",    account.getPhone(),
                "address",  account.getAddress()
        ));
    }
}