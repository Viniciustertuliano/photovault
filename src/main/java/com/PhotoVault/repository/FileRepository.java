package com.PhotoVault.repository;

import com.PhotoVault.entities.File;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FileRepository extends JpaRepository<File, Long> {
    Page<File> findByFolderId(Long folderId, Pageable pageable);

    List<File> findByFolderId(Long folderId);
}
