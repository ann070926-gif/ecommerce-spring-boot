package com.poly.entity;

import java.io.Serializable;
import java.util.List;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
@Entity
@Table(name = "Roles")
public class Role implements Serializable {
    @Id
    String id;
    String name;

    @JsonIgnore
    @OneToMany(mappedBy = "role")
    List<Authority> authorities;
}