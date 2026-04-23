package com.poly.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.poly.dao.CategoryDAO;
import com.poly.entity.Category;

@Controller
@RequestMapping("/admin/category")
public class AdminCategoryController {

    @Autowired
    private CategoryDAO categoryDAO;

    // 1. Hiển thị danh sách tất cả danh mục
    @GetMapping("")
    public String index(Model model) {
        model.addAttribute("category", new Category());
        model.addAttribute("items", categoryDAO.findAll());
        return "admin/category";
    }

    // 2. Hiển thị thông tin 1 danh mục lên Form khi bấm nút "Edit"
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") String id, Model model) {
        Category category = categoryDAO.findById(id).orElse(new Category());
        model.addAttribute("category", category);
        model.addAttribute("items", categoryDAO.findAll());
        model.addAttribute("isEdit", true);
        return "admin/category";
    }

    // 3. Lưu danh mục (Thêm hoặc Sửa)
    @PostMapping("/save")
    public String save(@ModelAttribute("category") Category category) {
        categoryDAO.save(category);
        return "redirect:/admin/category";
    }

    // 4. Xóa danh mục
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable("id") String id) {
        categoryDAO.deleteById(id);
        return "redirect:/admin/category";
    }
}
