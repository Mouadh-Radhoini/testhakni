package com.testhakni.backend.moderation.domain.repository;

import com.testhakni.backend.moderation.domain.model.ModerationAction;

/** Port du domaine : journal d'audit des decisions de moderation. */
public interface ModerationActionRepository {

    ModerationAction insert(ModerationAction action);
}
