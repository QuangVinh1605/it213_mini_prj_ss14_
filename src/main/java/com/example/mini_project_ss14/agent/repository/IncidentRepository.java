package com.example.mini_project_ss14.agent.repository;

import com.example.mini_project_ss14.agent.entity.Incident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IncidentRepository extends JpaRepository<Incident, Long> {
    List<Incident> findByTrackingCode(String trackingCode);
}
