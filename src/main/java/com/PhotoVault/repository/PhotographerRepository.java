package com.PhotoVault.repository;

import com.PhotoVault.entities.Photographer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PhotographerRepository extends JpaRepository<Photographer, Long> {

    public Optional<Photographer> findByEmail(String email);

    @Modifying
    @Query(value = "UPDATE tb_photographers SET storage_used_bytes = storage_used_bytes + :size WHERE id = :id AND (storage_used_bytes + :size) <= storage_quota_bytes", nativeQuery = true)
    int allocateStorageAtoms(@Param("id") Long id, @Param("size") Long size);

    @Modifying
    @Query(value = "UPDATE tb_photographers SET storage_used_bytes = GREATEST(0, storage_used_bytes - :size) WHERE id = :id", nativeQuery = true)
    void freeUpStorage(@Param("id") Long id, @Param("size") Long size);

}
