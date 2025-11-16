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

    // หรือถ้าอยากใช้ roomNumber แทน id ในอนาคต
    Optional<Users> findByRoomNumberAndRole(String roomNumber, Users.Role role);
    // 🆕 ดึงผู้พักทั้งหมด
    List<Users> findByRole(Users.Role role);

    // ถ้าอยาก filter แค่ ACTIVE
    List<Users> findByRoleAndStatus(Users.Role role, Users.Status status);
}
