package com.poly.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.poly.dao.ProductDAO;
import com.poly.entity.Product;

@Controller
@RequestMapping("/")
public class HomeController {

    @Autowired
    ProductDAO productDAO;

    @GetMapping
    public String home(Model model) {
        List<Product> products = productDAO.findAll();
        model.addAttribute("items", products);
        return "product/list";
    }

}
