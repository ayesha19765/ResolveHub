package com.ayesha.resolvehub.repository;

import com.ayesha.resolvehub.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {
}
