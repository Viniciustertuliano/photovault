package com.PhotoVault.entities;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tb_clients")
public class Client extends User{

    @Column(name = "phone_number")
    private String phoneNumber;

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL)
    private List<ShareLink> accessedLinks = new ArrayList<>();

    public Client() {
    }

    public Client(Long id, String name, String email, String password, UserRole role, String phoneNumber) {
        super(id, name, email, password, role);
        this.phoneNumber = phoneNumber;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
