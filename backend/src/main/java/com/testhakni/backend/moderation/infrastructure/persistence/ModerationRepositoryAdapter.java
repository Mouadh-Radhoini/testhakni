package com.testhakni.backend.moderation.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import com.testhakni.backend.moderation.domain.model.ContentType;
import com.testhakni.backend.moderation.domain.model.ModerationAction;
import com.testhakni.backend.moderation.domain.model.Report;
import com.testhakni.backend.moderation.domain.model.ReportStatus;
import com.testhakni.backend.moderation.domain.repository.ModerationActionRepository;
import com.testhakni.backend.moderation.domain.repository.ReportRepository;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

/** Adaptateur MongoDB des ports de moderation (signalements et journal d'audit). */
@Repository
class ModerationRepositoryAdapter implements ReportRepository, ModerationActionRepository {

    private final ReportMongoRepository reports;
    private final MongoTemplate mongo;

    ModerationRepositoryAdapter(ReportMongoRepository reports, MongoTemplate mongo) {
        this.reports = reports;
        this.mongo = mongo;
    }

    @Override
    public Report insert(Report report) {
        return reports.insert(report);
    }

    @Override
    public Optional<Report> findById(String id) {
        return reports.findById(id);
    }

    @Override
    public List<Report> findAll() {
        return reports.findAll();
    }

    @Override
    public long countByStatus(ReportStatus status) {
        return reports.countByStatus(status);
    }

    @Override
    public long countByTarget(ContentType type, String targetId, ReportStatus status) {
        return reports.countByTargetTypeAndTargetIdAndStatus(type, targetId, status);
    }

    @Override
    public boolean resolveIfOpen(String id, ReportStatus status, String moderatorId, String decision) {
        var result = mongo.updateFirst(Query.query(Criteria.where("_id").is(id).and("status").is(ReportStatus.OPEN)),
                Update.update("status", status).set("moderatorId", moderatorId).set("decision", decision), Report.class);
        return result.getModifiedCount() == 1;
    }

    @Override
    public ModerationAction insert(ModerationAction action) {
        return mongo.insert(action);
    }
}
