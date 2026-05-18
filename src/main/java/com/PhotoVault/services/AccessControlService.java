package com.PhotoVault.services;

import java.text.MessageFormat;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.PhotoVault.entities.Folder;
import com.PhotoVault.entities.PermissionType;
import com.PhotoVault.entities.Photographer;
import com.PhotoVault.entities.User;
import com.PhotoVault.exception.ForbiddenException;
import com.PhotoVault.repository.AccessPermissionRepository;


@Service
@Transactional(readOnly = true)
public class AccessControlService {

    private final AccessPermissionRepository accessPermissionRepository;

    public AccessControlService(AccessPermissionRepository accessPermissionRepository) {
        this.accessPermissionRepository = accessPermissionRepository;
    }

    public boolean hasPermission(User user, Folder folder, PermissionType requiredPermission) {

        if (user instanceof Photographer photographer) {
            if (folder.isOwner(photographer)) {
                return true;
            }
        }

        if (requiredPermission == PermissionType.VIEW) {
            boolean hasView = accessPermissionRepository.existsByUserAndFolderAndPermissionType(user, folder, PermissionType.VIEW);
            boolean hasDownload = accessPermissionRepository.existsByUserAndFolderAndPermissionType(user, folder, PermissionType.DOWNLOAD);

            return hasView || hasDownload;
        }

        return accessPermissionRepository.existsByUserAndFolderAndPermissionType(user, folder, requiredPermission);
    }

    public void validatePermission(User user, Folder folder, PermissionType requiredPermission) {
        if (!hasPermission(user, folder, requiredPermission)) {
            throw new ForbiddenException(MessageFormat.format("Access denied. You do not have {0} privileges for this gallery.", requiredPermission));
        }
    }

}
