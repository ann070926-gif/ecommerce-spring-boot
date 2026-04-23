package com.poly.rest;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.poly.dao.CartDAO;
import com.poly.dao.CartItemDAO;
import com.poly.dao.ProductDAO;
import com.poly.entity.Cart;
import com.poly.entity.CartItem;
import com.poly.entity.Product;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/cart")
public class CartRestController {

    @Autowired
    private CartDAO cartDAO;

    @Autowired
    private CartItemDAO cartItemDAO;

    @Autowired
    private ProductDAO productDAO;

    // Lấy username hiện tại (hỗ trợ OAuth2 và form login)
    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return null;
        }
        if (auth.getPrincipal() instanceof OAuth2User) {
            OAuth2User oauth2User = (OAuth2User) auth.getPrincipal();
            String email = oauth2User.getAttribute("email");
            return email != null ? email : auth.getName();
        }
        return auth.getName();
    }

    /**
     * Thêm sản phẩm vào giỏ hàng:
     * - Đã login  → lưu DB (persistent, không mất khi tắt server)
     * - Chưa login → lưu Session (mất khi tắt server, còn khi reload trang)
     */
    @PostMapping("/add/{productId}")
    public ResponseEntity<?> addToCart(@PathVariable("productId") Integer productId,
                                       HttpSession session) {
        try {
            String username = getCurrentUsername();
            Map<String, Object> response = new HashMap<>();

            // ── TRƯỜNG HỢP ĐÃ ĐĂNG NHẬP → lưu DB ──────────────────────────────
            if (username != null) {
                Product product = productDAO.findById(productId).orElse(null);
                if (product == null) {
                    return ResponseEntity.badRequest().body("Sản phẩm không tồn tại");
                }

                // Lấy hoặc tạo Cart cho user
                Cart cart = cartDAO.findByUsername(username);
                if (cart == null) {
                    cart = new Cart();
                    cart.setUsername(username);
                    cart.setCreateDate(new Date());
                    cart.setUpdateDate(new Date());
                    cart = cartDAO.save(cart);
                }

                // Kiểm tra sản phẩm đã có trong giỏ chưa
                final Cart finalCart = cart;
                CartItem existItem = cart.getItems() == null ? null :
                    cart.getItems().stream()
                        .filter(item -> item.getProduct().getId().equals(productId))
                        .findFirst()
                        .orElse(null);

                if (existItem != null) {
                    existItem.setQuantity(existItem.getQuantity() + 1);
                    cartItemDAO.save(existItem);
                } else {
                    CartItem newItem = new CartItem();
                    newItem.setCart(finalCart);
                    newItem.setProduct(product);
                    newItem.setQuantity(1);
                    newItem.setPrice(product.getPrice());
                    cartItemDAO.save(newItem);
                }

                cart.setUpdateDate(new Date());
                cartDAO.save(cart);

                // Reload để lấy số lượng mới nhất
                Cart updatedCart = cartDAO.findByUsername(username);
                int totalItems = updatedCart.getItems() != null ? updatedCart.getItems().size() : 0;

                response.put("status", "success");
                response.put("totalItems", totalItems);
                response.put("storage", "db");
                return ResponseEntity.ok(response);
            }

            // ── TRƯỜNG HỢP CHƯA ĐĂNG NHẬP → lưu Session ────────────────────────
            @SuppressWarnings("unchecked")
            Map<Integer, Integer> cart = (Map<Integer, Integer>) session.getAttribute("cart");
            if (cart == null) {
                cart = new HashMap<>();
            }

            cart.put(productId, cart.getOrDefault(productId, 0) + 1);
            session.setAttribute("cart", cart);

            response.put("status", "success");
            response.put("totalItems", cart.size());
            response.put("storage", "session");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Lỗi khi thêm vào giỏ hàng: " + e.getMessage());
        }
    }

    /**
     * Trả về số lượng sản phẩm trong giỏ (dùng cho badge trên header)
     * - Đã login  → đếm từ DB
     * - Chưa login → đếm từ Session
     */
    @GetMapping("/count")
    public ResponseEntity<?> getCartCount(HttpSession session) {
        String username = getCurrentUsername();
        Map<String, Object> response = new HashMap<>();

        if (username != null) {
            Cart cart = cartDAO.findByUsername(username);
            int count = (cart != null && cart.getItems() != null) ? cart.getItems().size() : 0;
            response.put("totalItems", count);
        } else {
            @SuppressWarnings("unchecked")
            Map<Integer, Integer> cart = (Map<Integer, Integer>) session.getAttribute("cart");
            response.put("totalItems", cart != null ? cart.size() : 0);
        }

        return ResponseEntity.ok(response);
    }
}