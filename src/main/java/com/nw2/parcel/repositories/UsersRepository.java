package com.nw2.parcel.repositories;

import com.nw2.parcel.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UsersRepository extends JpaRepository<Users, Long> {
    Optional<Users> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByFirebaseUid(String firebaseUid);
}

