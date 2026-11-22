package com.testhakni.backend.contact.domain.repository;

import java.util.List;
import java.util.Optional;

import com.testhakni.backend.contact.domain.model.Contact;

/** Port du domaine : liens de contact. */
public interface ContactRepository {

    Optional<Contact> findById(String id);

    Optional<Contact> findByPair(String userA, String userB);

    Contact save(Contact contact);

    void delete(Contact contact);

    /** Tous les liens (en attente, acceptes, refuses) d'un utilisateur, plus recents d'abord. */
    List<Contact> findByUser(String userId);

    long countIncomingPending(String userId);
}
