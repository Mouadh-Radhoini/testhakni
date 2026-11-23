package com.testhakni.backend.moderation.domain.repository;

import java.util.List;
import java.util.Optional;

import com.testhakni.backend.moderation.domain.model.ContentType;
import com.testhakni.backend.moderation.domain.model.Report;
import com.testhakni.backend.moderation.domain.model.ReportStatus;

/** Port du domaine : signalements de contenus. */
public interface ReportRepository {

    Report insert(Report report);

    Optional<Report> findById(String id);

    List<Report> findAll();

    long countByStatus(ReportStatus status);

    /** Nombre de signalements dans un etat donne pour un meme contenu. */
    long countByTarget(ContentType type, String targetId, ReportStatus status);

    /** Decision atomique : false si le signalement n'est plus OPEN. */
    boolean resolveIfOpen(String id, ReportStatus status, String moderatorId, String decision);
}
