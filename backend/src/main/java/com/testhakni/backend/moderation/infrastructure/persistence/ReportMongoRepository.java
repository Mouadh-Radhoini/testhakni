package com.testhakni.backend.moderation.infrastructure.persistence;

import com.testhakni.backend.moderation.domain.model.Report;
import com.testhakni.backend.moderation.domain.model.ReportStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

/** Spring Data : detail technique, utilise uniquement par l'adaptateur. */
interface ReportMongoRepository extends MongoRepository<Report, String> {

    long countByStatus(ReportStatus status);

    long countByTargetTypeAndTargetIdAndStatus(com.testhakni.backend.moderation.domain.model.ContentType type, String targetId, ReportStatus status);
}
