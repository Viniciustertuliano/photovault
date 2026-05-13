package com.PhotoVault.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tb_photographers")
public class Photographer extends User{

    @ManyToMany(mappedBy = "owners")
    private Set<Folder> folders = new HashSet<>();

    public Photographer() {
    }

    public Photographer(Long id, String name, String email, String password, UserRole role) {
        super(id, name, email, password, role);
    }

    public Set<Folder> getFolders() {
        return folders;
    }

    public void setFolders(Set<Folder> folders) {
        this.folders = folders;
    }

}
