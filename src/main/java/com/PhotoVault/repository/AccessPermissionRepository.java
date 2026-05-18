package com.PhotoVault.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.PhotoVault.entities.AccessPermission;
import com.PhotoVault.entities.Folder;
import com.PhotoVault.entities.PermissionType;
import com.PhotoVault.entities.User;

import java.util.Optional;

@Repository
public interface AccessPermissionRepository extends JpaRepository<AccessPermission, Long> {

    boolean existsByUserAndFolderAndPermissionType(User user, Folder folder, PermissionType permissionType);

    Optional<AccessPermission> findByUserAndFolderAndPermissionType(User user, Folder folder, PermissionType permissionType);
}
