package com.poly.dao;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.poly.entity.Product;

public interface ProductDAO extends JpaRepository<Product, Integer> {
    // Truy vấn sản phẩm theo Category ID (Dùng cho trang liệt kê hàng hóa)
    @Query("SELECT p FROM Product p WHERE p.category.id=?1")
    List<Product> findByCategoryId(String cid);
}