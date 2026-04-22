package com.PhotoVault.repository;

import com.PhotoVault.entities.Folder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FolderRepository extends JpaRepository<Folder, Long> {

    Optional<Folder> findByName(String name);

    //Custom query with JOIN FETCH to avoid the N+1 problem.
    @Query("SELECT f FROM Folder f JOIN FETCH f.owner WHERE f.owner.id = :ownerId")
    Page<Folder> findByOwnerId(Long ownerId, Pageable pageable);

    @Query("SELECT f FROM Folder f WHERE f.id = :id AND f.deletedAt IS NOT NULL")
    Optional<Folder> findByIdIncluidingDeleted(@Param("id") Long id);
}
