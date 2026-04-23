package com.poly.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import com.poly.dao.AccountDAO;
import com.poly.dao.AuthorityDAO;
import com.poly.dao.RoleDAO;
import com.poly.entity.Account;
import com.poly.entity.Authority;
import com.poly.entity.Role;

@Controller
public class AuthController {

    @Autowired
    AccountDAO accountDAO;
    
    @Autowired
    RoleDAO roleDAO;
    
    @Autowired
    AuthorityDAO authorityDAO;

    // 1. Hiển thị trang Đăng nhập (Đã làm)
    @GetMapping("/login")
    public String login() {
        return "security/login"; 
    }

    // 2. Hiển thị trang Đăng ký
    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("account", new Account()); // Truyền một object rỗng sang form
        return "security/register";
    }

    // 3. Xử lý khi bấm nút Đăng ký
    @PostMapping("/register")
    public String processRegister(Account account, Model model) {
        // Kiểm tra xem tên đăng nhập đã tồn tại chưa
        if(accountDAO.existsById(account.getUsername())) {
            model.addAttribute("error", "Tên đăng nhập đã tồn tại!");
            return "security/register";
        }

        try {
            // A. Lưu mật khẩu plain text (không mã hóa)
            // account.setPhoto("default.png"); // Gán ảnh mặc định nếu cần
            
            // Lưu tài khoản vào bảng Accounts
            accountDAO.save(account);

            // B. Cấp quyền mặc định là "USER" cho tài khoản mới
            Role role = roleDAO.findById("USER").orElse(null); // Giả sử trong DB có role ID là 'USER'
            if (role != null) {
                Authority authority = new Authority();
                authority.setAccount(account);
                authority.setRole(role);
                authorityDAO.save(authority);
            }

            model.addAttribute("message", "Đăng ký thành công! Vui lòng đăng nhập.");
            return "security/login"; // Chuyển về trang đăng nhập
            
        } catch (Exception e) {
            model.addAttribute("error", "Có lỗi xảy ra, vui lòng thử lại!");
            return "security/register";
        }
    }
}