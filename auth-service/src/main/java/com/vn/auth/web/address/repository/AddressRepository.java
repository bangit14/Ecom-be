package com.vn.auth.web.address.repository;

import com.vn.auth.web.address.domain.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AddressRepository extends JpaRepository<Address, UUID> {

    List<Address> findByUserId(UUID userId);

    Optional<Address> findByUserIdAndIsDefaultTrue(UUID userId);

    long countByUserId(UUID userId);
}
