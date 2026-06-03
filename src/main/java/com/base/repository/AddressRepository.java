package com.base.repository;

import com.base.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findByUser_UserId(Long userId);
    Optional<Address> findByUser_UserIdAndIsDefault(Long userId, boolean isDefault);

    @Modifying
    @Query("UPDATE Address a SET a.isDefault = false WHERE a.user.userId = :userId")
    void clearDefaultByUserId(Long userId);

    List<Address> findByUserUserIdOrderByIsDefaultDescAddressIdAsc(Long userId);

    Optional<Address> findByUserUserIdAndIsDefaultTrue(Long userId);

    Optional<Address> findFirstByUserUserIdOrderByAddressIdAsc(Long userId);

    long countByUserUserId(Long userId);
}
