package com.poly.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.poly.dao.OrderDAO;
import com.poly.entity.Order;

@Controller
@RequestMapping("/admin/order")
public class AdminOrderController {

    @Autowired
    private OrderDAO orderDAO;

    // 1. Hiển thị danh sách tất cả đơn hàng
    @GetMapping("")
    public String index(Model model) {
        model.addAttribute("orders", orderDAO.findAll());
        return "admin/order";
    }

    // 2. Xem chi tiết đơn hàng
    @GetMapping("/detail/{id}")
    public String detail(@PathVariable("id") Long id, Model model) {
        Order order = orderDAO.findById(id).orElse(null);
        if (order == null) {
            return "redirect:/admin/order";
        }
        model.addAttribute("order", order);
        
        // Tính tổng tiền
        Double totalAmount = 0.0;
        if (order.getOrderDetails() != null && !order.getOrderDetails().isEmpty()) {
            for (com.poly.entity.OrderDetail detail : order.getOrderDetails()) {
                totalAmount += detail.getPrice() * detail.getQuantity();
            }
        }
        model.addAttribute("totalAmount", totalAmount);
        
        return "admin/order-detail";
    }

    // 3. Xóa đơn hàng
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable("id") Long id) {
        orderDAO.deleteById(id);
        return "redirect:/admin/order";
    }
}
