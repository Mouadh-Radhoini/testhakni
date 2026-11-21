package com.testhakni.backend.repository;

import com.testhakni.backend.model.Report;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ReportRepository extends MongoRepository<Report, String> {
}
