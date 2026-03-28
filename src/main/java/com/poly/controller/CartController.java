package com.poly.controller;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.poly.dao.ProductDAO;
import com.poly.entity.Product;
import jakarta.servlet.http.HttpSession; // Dùng javax.servlet... nếu lỗi

@Controller
public class CartController {

    @Autowired
    private ProductDAO productDAO; // Inject DAO để truy vấn thông tin sản phẩm từ DB

    @GetMapping("/cart")
    public String viewCart(HttpSession session, Model model) {
        // 1. Lấy giỏ hàng từ Session
        @SuppressWarnings("unchecked")
        Map<Integer, Integer> cart = (Map<Integer, Integer>) session.getAttribute("cart");
        
        // 2. Khởi tạo danh sách để chứa thông tin chi tiết hiển thị ra màn hình
        List<CartItem> cartItems = new ArrayList<>();
        double totalAmount = 0; // Tổng tiền giỏ hàng

        if (cart != null && !cart.isEmpty()) {
            // Duyệt qua từng sản phẩm trong Session
            for (Map.Entry<Integer, Integer> entry : cart.entrySet()) {
                Integer productId = entry.getKey();
                Integer quantity = entry.getValue();
                
                // Lấy thông tin chi tiết của sản phẩm từ Database
                Product product = productDAO.findById(productId).orElse(null);
                
                if (product != null) {
                    // Gom sản phẩm và số lượng lại thành 1 đối tượng CartItem
                    CartItem item = new CartItem(product, quantity);
                    cartItems.add(item);
                    totalAmount += (product.getPrice() * quantity);
                }
            }
        }

        // 3. Đưa dữ liệu sang file HTML
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("totalAmount", totalAmount);

        return "cart/view"; // Trả về file giao diện: src/main/resources/templates/cart/view.html
    }
    
    // Tạo một class nội bộ (DTO) để gom nhóm Sản phẩm & Số lượng mang sang View cho tiện
    public static class CartItem {
        public Product product;
        public int quantity;
        public double subTotal;

        public CartItem(Product product, int quantity) {
            this.product = product;
            this.quantity = quantity;
            this.subTotal = product.getPrice() * quantity;
        }
    }
}