package com.poly.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.poly.dao.CategoryDAO;
import com.poly.dao.ProductDAO;
import com.poly.entity.Product;

@Controller
@RequestMapping("/admin/product")
public class AdminProductController {

    @Autowired
    ProductDAO productDAO;

    @Autowired
    CategoryDAO categoryDAO;

    @GetMapping("")
    public String index(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("items", productDAO.findAll());
        model.addAttribute("categories", categoryDAO.findAll());
        return "admin/product";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Integer id, Model model) {
        Product product = productDAO.findById(id).orElse(new Product());
        model.addAttribute("product", product);
        model.addAttribute("items", productDAO.findAll());
        model.addAttribute("categories", categoryDAO.findAll());
        return "admin/product";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute("product") Product product,
            @RequestParam("imageFile") MultipartFile imageFile) {

        if (product.getAvailable() == null) {
            product.setAvailable(true);
        }

        if (!imageFile.isEmpty()) {
            try {
                String originalName = imageFile.getOriginalFilename();
                String fileName = new java.io.File(originalName).getName();

                String userDir = System.getProperty("user.dir");
                String staticDir = userDir + "/src/main/resources/static/images/";

                new java.io.File(staticDir).mkdirs();

                java.nio.file.Files.copy(
                        imageFile.getInputStream(),
                        java.nio.file.Paths.get(staticDir + fileName),
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING
                );

                product.setImage(fileName);

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            if (product.getId() != null) {
                Product existingProduct = productDAO.findById(product.getId()).orElse(null);
                if (existingProduct != null) {
                    product.setImage(existingProduct.getImage());
                }
            }
        }

        productDAO.save(product);
        return "redirect:/admin/product";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable("id") Integer id) {
        productDAO.deleteById(id);
        return "redirect:/admin/product";
    }
}
