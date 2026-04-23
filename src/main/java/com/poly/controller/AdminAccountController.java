package com.poly.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.poly.dao.AccountDAO;
import com.poly.entity.Account;

@Controller
@RequestMapping("/admin/account")
public class AdminAccountController {

    @Autowired
    private AccountDAO accountDAO;

    // 1. Hiển thị danh sách tất cả tài khoản
    @GetMapping("")
    public String index(Model model) {
        model.addAttribute("account", new Account());
        model.addAttribute("items", accountDAO.findAll());
        return "admin/account";
    }

    // 2. Hiển thị thông tin 1 tài khoản lên Form khi bấm nút "Edit"
    @GetMapping("/edit/{username}")
    public String edit(@PathVariable("username") String username, Model model) {
        Account account = accountDAO.findById(username).orElse(new Account());
        model.addAttribute("account", account);
        model.addAttribute("items", accountDAO.findAll());
        model.addAttribute("isEdit", true);
        return "admin/account";
    }

    // 3. Lưu tài khoản (Thêm hoặc Sửa)
    @PostMapping("/save")
    public String save(@ModelAttribute("account") Account account) {
        accountDAO.save(account);
        return "redirect:/admin/account";
    }

    // 4. Xóa tài khoản
    @GetMapping("/delete/{username}")
    public String delete(@PathVariable("username") String username) {
        accountDAO.deleteById(username);
        return "redirect:/admin/account";
    }
}
