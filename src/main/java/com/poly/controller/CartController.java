package com.poly.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.poly.dao.CartDAO;
import com.poly.dao.CartItemDAO;
import com.poly.dao.ProductDAO;
import com.poly.entity.Cart;
import com.poly.entity.CartItem;
import com.poly.entity.Product;

import jakarta.servlet.http.HttpSession;

@Controller
public class CartController {

    @Autowired
    private CartDAO cartDAO;
    
    @Autowired
    private CartItemDAO cartItemDAO;
    
    @Autowired
    private ProductDAO productDAO;

    // Lấy username hiện tại (hỗ trợ cả OAuth2 và form login)
    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getName().equals("anonymousUser")) {
            return null;
        }

        // 1. Kiểm tra OAuth2 Login (Google)
        if (auth.getPrincipal() instanceof OAuth2User) {
            OAuth2User oauth2User = (OAuth2User) auth.getPrincipal();
            // Lấy email từ OAuth2
            String email = oauth2User.getAttribute("email");
            return email != null ? email : auth.getName();
        }

        // 2. Form Login thường (username/password)
        return auth.getName();
    }

    // Lấy hoặc tạo giỏ hàng cho user hiện tại
    private Cart getOrCreateCart() {
        String username = getCurrentUsername();
        if (username == null) {
            return null; // Chưa đăng nhập
        }
        
        Cart cart = cartDAO.findByUsername(username);
        if (cart == null) {
            cart = new Cart();
            cart.setUsername(username);
            cart.setCreateDate(new Date());
            cart.setUpdateDate(new Date());
            cartDAO.save(cart);
        }
        return cart;
    }

    @GetMapping("/cart")
    public String viewCart(Model model, HttpSession session) {
        List<CartItem> cartItems = new ArrayList<>();
        double totalAmount = 0;

        String username = getCurrentUsername();
        
        // Nếu user đã đăng nhập, lấy giỏ từ database
        if (username != null) {
            Cart cart = cartDAO.findByUsername(username);
            if (cart != null && cart.getItems() != null && !cart.getItems().isEmpty()) {
                cartItems = cart.getItems();
                for (CartItem item : cartItems) {
                    totalAmount += item.getSubTotal();
                }
            }
        } 
        // Nếu chưa đăng nhập, lấy từ session
        else {
            @SuppressWarnings("unchecked")
            Map<Integer, Integer> sessionCart = (Map<Integer, Integer>) session.getAttribute("cart");
            if (sessionCart != null && !sessionCart.isEmpty()) {
                // Duyệt qua từng sản phẩm trong session cart
                for (Map.Entry<Integer, Integer> entry : sessionCart.entrySet()) {
                    Product product = productDAO.findById(entry.getKey()).orElse(null);
                    if (product != null) {
                        CartItem item = new CartItem();
                        item.setProduct(product);
                        item.setQuantity(entry.getValue());
                        item.setPrice(product.getPrice());
                        cartItems.add(item);
                        totalAmount += product.getPrice() * entry.getValue();
                    }
                }
            }
        }

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("totalAmount", totalAmount);
        return "cart/view";
    }

    // Thêm sản phẩm vào giỏ hàng
    @GetMapping("/cart/add/{id}")
    public String addToCart(@PathVariable("id") Integer productId) {
        Cart cart = getOrCreateCart();
        
        if (cart == null) {
            return "redirect:/login"; // Chưa đăng nhập
        }

        Product product = productDAO.findById(productId).orElse(null);
        if (product == null) {
            return "redirect:/product/list";
        }

        // Kiểm tra xem sản phẩm đã có trong giỏ chưa
        CartItem existItem = cart.getItems().stream()
            .filter(item -> item.getProduct().getId().equals(productId))
            .findFirst()
            .orElse(null);

        if (existItem != null) {
            // Nếu đã có thì tăng số lượng
            existItem.setQuantity(existItem.getQuantity() + 1);
            cartItemDAO.save(existItem);
        } else {
            // Nếu chưa có thì thêm mới
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProduct(product);
            newItem.setQuantity(1);
            newItem.setPrice(product.getPrice());
            cartItemDAO.save(newItem);
        }

        cart.setUpdateDate(new Date());
        cartDAO.save(cart);

        return "redirect:/cart";
    }

    // Xóa 1 sản phẩm khỏi giỏ hàng
    @GetMapping("/cart/remove/{id}")
    public String removeFromCart(@PathVariable("id") String id, HttpSession session) {
        String username = getCurrentUsername();
        
        // Nếu user đã đăng nhập, xóa từ database
        if (username != null) {
            try {
                Long cartItemId = Long.parseLong(id);
                cartItemDAO.deleteById(cartItemId);
            } catch (NumberFormatException e) {
                // Handle error
            }
        } 
        // Nếu chưa đăng nhập, xóa từ session
        else {
            try {
                Integer productId = Integer.parseInt(id);
                @SuppressWarnings("unchecked")
                Map<Integer, Integer> sessionCart = (Map<Integer, Integer>) session.getAttribute("cart");
                if (sessionCart != null) {
                    sessionCart.remove(productId);
                    session.setAttribute("cart", sessionCart);
                }
            } catch (NumberFormatException e) {
                // Handle error
            }
        }
        
        return "redirect:/cart";
    }

    // Cập nhật số lượng sản phẩm trong giỏ hàng
    @GetMapping("/cart/update/{id}")
    public String updateCart(@PathVariable("id") String id, 
                            @RequestParam("quantity") Integer quantity,
                            HttpSession session) {
        String username = getCurrentUsername();
        
        // Nếu user đã đăng nhập, cập nhật từ database
        if (username != null) {
            try {
                Long cartItemId = Long.parseLong(id);
                CartItem item = cartItemDAO.findById(cartItemId).orElse(null);
                
                if (item != null) {
                    if (quantity > 0) {
                        item.setQuantity(quantity);
                        cartItemDAO.save(item);
                    } else {
                        cartItemDAO.deleteById(cartItemId);
                    }
                }
            } catch (NumberFormatException e) {
                // Handle error
            }
        } 
        // Nếu chưa đăng nhập, cập nhật từ session
        else {
            try {
                Integer productId = Integer.parseInt(id);
                @SuppressWarnings("unchecked")
                Map<Integer, Integer> sessionCart = (Map<Integer, Integer>) session.getAttribute("cart");
                
                if (sessionCart != null) {
                    if (quantity > 0) {
                        sessionCart.put(productId, quantity);
                    } else {
                        sessionCart.remove(productId);
                    }
                    session.setAttribute("cart", sessionCart);
                }
            } catch (NumberFormatException e) {
                // Handle error
            }
        }
        
        return "redirect:/cart";
    }

    // Xóa toàn bộ giỏ hàng
    @GetMapping("/cart/clear")
    public String clearCart(HttpSession session) {
        String username = getCurrentUsername();
        
        // Nếu user đã đăng nhập, xóa từ database
        if (username != null) {
            Cart cart = cartDAO.findByUsername(username);
            if (cart != null) {
                cartDAO.delete(cart);
            }
        } 
        // Nếu chưa đăng nhập, xóa session
        else {
            session.removeAttribute("cart");
        }
        
        return "redirect:/cart";
    }
}