package com.smartsub.repository;

import com.smartsub.model.SharedMember;
import com.smartsub.model.Subscription;
import com.smartsub.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SharedMemberRepository extends JpaRepository<SharedMember, Long> {
    List<SharedMember> findBySubscription(Subscription subscription);
    Optional<SharedMember> findByIdAndSubscription_User(Long id, User user);
}
