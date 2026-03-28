package com.poly.controller;


import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.poly.dao.AccountDAO;
import com.poly.dao.OrderDAO;
import com.poly.dao.OrderDetailDAO;
import com.poly.dao.ProductDAO;
import com.poly.entity.Account;
import com.poly.entity.Order;
import com.poly.entity.OrderDetail;
import com.poly.entity.Product;

import jakarta.servlet.http.HttpSession;

@Controller
public class OrderController {

    @Autowired
    private OrderDAO orderDAO;
    
    @Autowired
    private OrderDetailDAO orderDetailDAO;
    
    @Autowired
    private ProductDAO productDAO;
    
    @Autowired
    private AccountDAO accountDAO;

    @GetMapping("/checkout")
    public String checkout(HttpSession session, Model model) {
        Map<Integer, Integer> cart = (Map<Integer, Integer>) session.getAttribute("cart");
        
        if (cart == null || cart.isEmpty()) {
            return "redirect:/cart";
        }

        // Tính tổng tiền giỏ hàng truyền sang View
        double totalAmount = 0;
        for (Map.Entry<Integer, Integer> entry : cart.entrySet()) {
            Product product = productDAO.findById(entry.getKey()).orElse(null);
            if (product != null) {
                totalAmount += (product.getPrice() * entry.getValue());
            }
        }
        
        // Gửi tổng tiền sang giao diện
        model.addAttribute("totalAmount", totalAmount);
        
        return "cart/checkout"; 
    }

    // Xử lý khi bấm nút "Xác nhận đặt hàng"
    @PostMapping("/checkout")
    public String processCheckout(@RequestParam("address") String address,
                                  @RequestParam("phone") String phone,
                                  HttpSession session) {
        
        Map<Integer, Integer> cart = (Map<Integer, Integer>) session.getAttribute("cart");
        if (cart == null || cart.isEmpty()) {
            return "redirect:/cart";
        }

        try {
            // Lấy user đang login (nếu có)
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Account account = null;
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
                account = accountDAO.findById(auth.getName()).orElse(null);
            }

            // Bước 2.1: Tạo đơn hàng mới (Bảng Orders)
            Order order = new Order();
            order.setCreateDate(new Date());
            order.setAddress(address);
            order.setAccount(account); // Lưu user đang login
            
            // Lưu Order xuống DB trước để lấy ID
            Order savedOrder = orderDAO.save(order);

            // Bước 2.2: Lưu chi tiết đơn hàng (Bảng OrderDetails)
            for (Map.Entry<Integer, Integer> entry : cart.entrySet()) {
                Integer productId = entry.getKey();
                Integer quantity = entry.getValue();
                
                Product product = productDAO.findById(productId).orElse(null);
                if (product != null) {
                    OrderDetail detail = new OrderDetail();
                    detail.setOrder(savedOrder);
                    detail.setProduct(product);
                    detail.setPrice(product.getPrice()); // Lưu giá tại thời điểm mua
                    detail.setQuantity(quantity);
                    
                    orderDetailDAO.save(detail);
                }
            }

            // Bước 2.3: Xóa giỏ hàng khỏi Session sau khi đặt thành công
            session.removeAttribute("cart");

            // Chuyển hướng sang trang thông báo thành công
            return "redirect:/order/success";
            
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/checkout?error=true";
        }
    }

    // Hiển thị trang Đặt hàng thành công
    @GetMapping("/order/success")
    public String orderSuccess() {
        return "cart/success";
    }

    // Hiển thị danh sách đơn hàng của user đang login
    @GetMapping("/order/history")
    public String orderHistory(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return "redirect:/login";
        }

        String username = auth.getName();
        List<Order> orders = orderDAO.findByUsername(username);
        model.addAttribute("orders", orders);
        
        return "order/history";
    }

    // Hiển thị chi tiết 1 đơn hàng
    @GetMapping("/order/detail/{id}")
    public String orderDetail(@PathVariable Long id, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return "redirect:/login";
        }

        Order order = orderDAO.findById(id).orElse(null);
        if (order == null) {
            return "redirect:/order/history?error=notfound";
        }

        // Kiểm tra xem order này có thuộc về user đang login không
        if (order.getAccount() != null && !order.getAccount().getUsername().equals(auth.getName())) {
            return "redirect:/order/history?error=unauthorized";
        }

        double totalAmount = 0;
        if (order.getOrderDetails() != null) {
            for (OrderDetail detail : order.getOrderDetails()) {
                totalAmount += (detail.getPrice() * detail.getQuantity());
            }
        }

        model.addAttribute("order", order);
        model.addAttribute("totalAmount", totalAmount);
        
        return "order/detail";
    }
}