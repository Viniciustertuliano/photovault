package com.PhotoVault.repository;

import com.PhotoVault.entities.File;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface FileRepository extends JpaRepository<File, Long> {

    @Query("SELECT f FROM File f JOIN FETCH f.folder WHERE f.folder.id = :folderId")
    Page<File> findByFolderId(Long folderId, Pageable pageable);

    List<File> findByFolderId(Long folderId);
}
