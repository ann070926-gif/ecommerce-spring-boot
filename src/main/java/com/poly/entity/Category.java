package com.poly.entity;

import java.io.Serializable;
import java.util.List;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
@Entity
@Table(name = "Categories")
public class Category implements Serializable {
    @Id
    String id;
    String name;

    @JsonIgnore
    @OneToMany(mappedBy = "category")
    List<Product> products;
}