package com.hospital.hms.repo;

import com.hospital.hms.domain.User;
import com.hospital.hms.domain.UserRoleAssignment;
import com.hospital.hms.domain.UserRoleAssignmentId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRoleAssignmentRepository extends JpaRepository<UserRoleAssignment, UserRoleAssignmentId> {

    @Query("select ur from UserRoleAssignment ur join fetch ur.role where ur.user.id = :userId and ur.deletedAt is null")
    List<UserRoleAssignment> findActiveWithRolesByUserId(@Param("userId") Long userId);

    List<UserRoleAssignment> findByUser_IdAndDeletedAtIsNull(Long userId);

    List<UserRoleAssignment> findByUser_Id(Long userId);

    java.util.Optional<UserRoleAssignment> findById_UserIdAndId_RoleId(Long userId, Long roleId);

    @Query("select ur.user from UserRoleAssignment ur join ur.role r where r.name = :roleName and ur.deletedAt is null order by ur.user.fullName")
    List<User> findActiveUsersByRoleName(@Param("roleName") String roleName);
}
