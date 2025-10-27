package com.PhotoVault.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tb_photographers")
public class Photographer extends User{

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Folder> folders = new ArrayList<>();

    public Photographer() {
    }

    public Photographer(Long id, String name, String email, String password, UserRole role) {
        super(id, name, email, password, role);
    }
}
