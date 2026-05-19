package com.PhotoVault.repository;

import com.PhotoVault.entities.Folder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface FolderRepository extends JpaRepository<Folder, Long> {

    Optional<Folder> findByName(String name);

    //Custom query with JOIN FETCH to avoid the N+1 problem.
    @Query("SELECT f FROM Folder f JOIN f.owners o WHERE o.id = :ownerId")
    Page<Folder> findByOwnerId(@Param("ownerId") Long ownerId, Pageable pageable);

    @Query("SELECT f FROM Folder f WHERE f.id = :id AND f.deletedAt IS NOT NULL")
    Optional<Folder> findByIdIncluidingDeleted(@Param("id") Long id);

    @Query("SELECT f FROM Folder f JOIN AccessPermission ap ON ap.folder = f WHERE ap.user.id = :userId")
    Page<Folder> findByFoldersSharedWithUser(@Param("userId") Long userId, Pageable pageable);

    @Query(value = "SELECT * FROM tb_folders WHERE deleted_at IS NOT NULL AND deleted_at < :threshold", nativeQuery = true)
    List<Folder> findExpiredFolders(@Param("threshold") LocalDateTime threshold);

    @Modifying
    @Query(value = "DELETE FROM tb_folders WHERE id = :id", nativeQuery = true)
    void hardDeleteById(@Param("id") Long id);
}
