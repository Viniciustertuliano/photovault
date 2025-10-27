package com.PhotoVault.repository;

import com.PhotoVault.entities.Folder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FolderRepository extends JpaRepository<Folder, Long> {
}
