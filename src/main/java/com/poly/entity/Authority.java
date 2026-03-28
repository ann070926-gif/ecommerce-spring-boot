package com.poly.entity;

import java.io.Serializable;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "Authorities", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"Username", "Roleid"})
})
public class Authority implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @ManyToOne
    @JoinColumn(name = "Username")
    Account account;

    @ManyToOne
    @JoinColumn(name = "Roleid")
    Role role;
}