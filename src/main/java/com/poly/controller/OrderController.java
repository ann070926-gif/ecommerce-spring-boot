package com.poly.controller;

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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.poly.dao.AccountDAO;
import com.poly.dao.CartDAO;
import com.poly.dao.OrderDAO;
import com.poly.dao.OrderDetailDAO;
import com.poly.dao.ProductDAO;
import com.poly.entity.Account;
import com.poly.entity.Cart;
import com.poly.entity.CartItem;
import com.poly.entity.Order;
import com.poly.entity.OrderDetail;
import com.poly.entity.Product;

import jakarta.servlet.http.HttpSession;

@Controller
public class OrderController {

    @Autowired private OrderDAO orderDAO;
    @Autowired private OrderDetailDAO orderDetailDAO;
    @Autowired private ProductDAO productDAO;
    @Autowired private AccountDAO accountDAO;
    @Autowired private CartDAO cartDAO;

    // ── Helper: lấy username hiện tại (hỗ trợ OAuth2 + form login) ──────────
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

    // ── GET /checkout ─────────────────────────────────────────────────────────
    @GetMapping("/checkout")
    public String checkout(HttpSession session, Model model) {
        String username = getCurrentUsername();
        double totalAmount = 0;

        if (username != null) {
            // Đã login → đọc giỏ từ DB
            Cart cart = cartDAO.findByUsername(username);
            if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
                return "redirect:/cart";
            }
            for (CartItem item : cart.getItems()) {
                totalAmount += item.getSubTotal();
            }
        } else {
            // Chưa login → đọc giỏ từ Session
            @SuppressWarnings("unchecked")
            Map<Integer, Integer> cart = (Map<Integer, Integer>) session.getAttribute("cart");
            if (cart == null || cart.isEmpty()) {
                return "redirect:/cart";
            }
            for (Map.Entry<Integer, Integer> entry : cart.entrySet()) {
                Product product = productDAO.findById(entry.getKey()).orElse(null);
                if (product != null) {
                    totalAmount += product.getPrice() * entry.getValue();
                }
            }
        }

        model.addAttribute("totalAmount", totalAmount);
        return "cart/checkout";
    }

    // ── POST /checkout ────────────────────────────────────────────────────────
    @PostMapping("/checkout")
    public String processCheckout(@RequestParam("address") String address,
                                  @RequestParam("phone") String phone,
                                  HttpSession session) {
        String username = getCurrentUsername();

        try {
            Account account = null;
            if (username != null) {
                account = accountDAO.findById(username).orElse(null);
            }

            // Tạo đơn hàng
            Order order = new Order();
            order.setCreateDate(new Date());
            order.setAddress(address);
            order.setAccount(account);
            Order savedOrder = orderDAO.save(order);

            if (username != null) {
                // ── Đã login → lấy giỏ từ DB ──────────────────────────────
                Cart cart = cartDAO.findByUsername(username);
                if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
                    return "redirect:/cart";
                }

                for (CartItem item : cart.getItems()) {
                    OrderDetail detail = new OrderDetail();
                    detail.setOrder(savedOrder);
                    detail.setProduct(item.getProduct());
                    detail.setPrice(item.getPrice());
                    detail.setQuantity(item.getQuantity());
                    orderDetailDAO.save(detail);
                }

                // Xóa giỏ hàng trong DB sau khi đặt thành công
                cartDAO.delete(cart);

            } else {
                // ── Chưa login → lấy giỏ từ Session ──────────────────────
                @SuppressWarnings("unchecked")
                Map<Integer, Integer> cart = (Map<Integer, Integer>) session.getAttribute("cart");
                if (cart == null || cart.isEmpty()) {
                    return "redirect:/cart";
                }

                for (Map.Entry<Integer, Integer> entry : cart.entrySet()) {
                    Product product = productDAO.findById(entry.getKey()).orElse(null);
                    if (product != null) {
                        OrderDetail detail = new OrderDetail();
                        detail.setOrder(savedOrder);
                        detail.setProduct(product);
                        detail.setPrice(product.getPrice());
                        detail.setQuantity(entry.getValue());
                        orderDetailDAO.save(detail);
                    }
                }

                // Xóa giỏ hàng trong Session sau khi đặt thành công
                session.removeAttribute("cart");
            }

            return "redirect:/order/success";

        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/checkout?error=true";
        }
    }

    // ── GET /order/success ────────────────────────────────────────────────────
    @GetMapping("/order/success")
    public String orderSuccess() {
        return "cart/success";
    }

    // ── GET /order/history ────────────────────────────────────────────────────
    @GetMapping("/order/history")
    public String orderHistory(Model model) {
        String username = getCurrentUsername();
        if (username == null) {
            return "redirect:/login";
        }

        List<Order> orders = orderDAO.findByUsername(username);
        model.addAttribute("orders", orders);
        return "order/history";
    }

    // ── GET /order/detail/{id} ────────────────────────────────────────────────
    @GetMapping("/order/detail/{id}")
    public String orderDetail(@PathVariable Long id, Model model) {
        String username = getCurrentUsername();
        if (username == null) {
            return "redirect:/login";
        }

        Order order = orderDAO.findById(id).orElse(null);
        if (order == null) {
            return "redirect:/order/history?error=notfound";
        }

        if (order.getAccount() != null && !order.getAccount().getUsername().equals(username)) {
            return "redirect:/order/history?error=unauthorized";
        }

        double totalAmount = 0;
        if (order.getOrderDetails() != null) {
            for (OrderDetail detail : order.getOrderDetails()) {
                totalAmount += detail.getPrice() * detail.getQuantity();
            }
        }

        model.addAttribute("order", order);
        model.addAttribute("totalAmount", totalAmount);
        return "order/detail";
    }
}