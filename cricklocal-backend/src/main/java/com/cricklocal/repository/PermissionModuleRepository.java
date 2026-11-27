package com.cricklocal.repository;

import com.cricklocal.entity.PermissionModule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PermissionModuleRepository
        extends JpaRepository<PermissionModule, Long> {

    Optional<PermissionModule> findByModuleKey(String moduleKey);

    List<PermissionModule> findByActiveTrueOrderByIdAsc();
}