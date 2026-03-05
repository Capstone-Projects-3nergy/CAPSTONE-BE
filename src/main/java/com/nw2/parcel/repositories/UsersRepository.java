package com.nw2.parcel.repositories;

import com.nw2.parcel.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsersRepository extends JpaRepository<Users, Integer> {
    Optional<Users> findByFirebaseUid(String firebaseUid);
    Optional<Users> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<Users> findByUserIdAndRole(Integer userId, Users.Role role);
    Optional<Users> findByRoomNumberAndRole(String roomNumber, Users.Role role);
    List<Users> findByRole(Users.Role role);
    List<Users> findByRoleAndStatus(Users.Role role, Users.Status status);
    List<Users> findByStatusNot(Users.Status status);
    List<Users> findByRoleInAndStatusNot(List<Users.Role> roles, Users.Status status);
    Optional<Users> findByLineUserId(String lineUserId);
}