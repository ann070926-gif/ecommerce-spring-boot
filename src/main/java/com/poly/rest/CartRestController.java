package com.poly.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession; // Dùng javax.servlet.http.HttpSession nếu bạn xài Spring Boot 2
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
public class CartRestController {

    @PostMapping("/add/{productId}")
    public ResponseEntity<?> addToCart(@PathVariable("productId") Integer productId, HttpSession session) {
        try {
            // 1. Lấy giỏ hàng từ Session (nếu chưa có thì tạo một Map mới)
            // Cấu trúc: Map<Mã sản phẩm, Số lượng>
            @SuppressWarnings("unchecked")
            Map<Integer, Integer> cart = (Map<Integer, Integer>) session.getAttribute("cart");
            if (cart == null) {
                cart = new HashMap<>();
            }

            // 2. Thêm sản phẩm vào giỏ hoặc tăng số lượng lên 1 nếu đã tồn tại
            cart.put(productId, cart.getOrDefault(productId, 0) + 1);

            // 3. Cập nhật lại giỏ hàng vào Session
            session.setAttribute("cart", cart);

            // 4. Trả về phản hồi dạng JSON báo thành công
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("totalItems", cart.size()); // Tổng số loại sản phẩm trong giỏ

            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Lỗi khi thêm vào giỏ hàng");
        }
    }
}