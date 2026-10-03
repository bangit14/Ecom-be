package com.vn.auth.web.store.repository;

import com.vn.auth.web.store.domain.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface StoreRepository extends JpaRepository<Store, UUID> {

    Optional<Store> findByTaxNumber(String taxNumber);

    boolean existsByTaxNumber(String taxNumber);
}
