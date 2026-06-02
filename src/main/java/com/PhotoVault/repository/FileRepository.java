package com.PhotoVault.repository;

import com.PhotoVault.entities.File;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface FileRepository extends JpaRepository<File, Long> {

    @Query("SELECT f FROM File f JOIN FETCH f.folder WHERE f.folder.id = :folderId")
    Page<File> findByFolderId(Long folderId, Pageable pageable);

    List<File> findByFolderId(Long folderId);

    @Query(value = "SELECT * FROM tb_files WHERE deleted_at IS NOT NULL AND deleted_at < :threshold", nativeQuery = true)    List<File> findExpiredFiles(@Param("threshold") LocalDateTime threshold);

    @Modifying
    @Query(value = "DELETE FROM tb_files WHERE id = :id", nativeQuery = true)
    void hardDeleteById(@Param("id") Long id);
}
