package com.poly.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.poly.dao.ProductDAO;
import com.poly.entity.Product;

@Controller
public class ProductController {

    @Autowired
    ProductDAO pdao;

    // Hiển thị danh sách sản phẩm (Có hỗ trợ lọc theo Category)
    @RequestMapping("/product/list")
    public String list(Model model, @RequestParam("cid") Optional<String> cid) {
        List<Product> list;
        if (cid.isPresent()) {
            list = pdao.findByCategoryId(cid.get());
        } else {
            list = pdao.findAll();
        }
        model.addAttribute("items", list);
        return "product/list"; // Trả về file list.html trong thư mục templates/product
    }

    // Hiển thị chi tiết 1 sản phẩm theo ID
    @RequestMapping("/product/detail/{id}")
    public String detail(Model model, @PathVariable("id") Integer id) {
        Product item = pdao.findById(id).get();
        model.addAttribute("item", item);
        return "product/detail"; // Trả về file detail.html trong thư mục templates/product
    }
}