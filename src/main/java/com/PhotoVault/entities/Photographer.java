package com.PhotoVault.entities;

import jakarta.persistence.Column;
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

    @Column(name = "storage_quota_bytes", nullable = false)
    private Long storageQuotaBytes = 5368709120L; // 5GB

    @Column(name = "storage_used_bytes", nullable = false)
    private Long storageUsedBytes = 0L;

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
    public Long getStorageQuotaBytes() {
        return storageQuotaBytes;
    }

    public void setStorageQuotaBytes(Long storageQuotaBytes) {
        this.storageQuotaBytes = storageQuotaBytes;
    }

    public Long getStorageUsedBytes() {
        return storageUsedBytes;
    }

    public void setStorageUsedBytes(Long storageUsedBytes) {
        this.storageUsedBytes = storageUsedBytes;
    }

}
