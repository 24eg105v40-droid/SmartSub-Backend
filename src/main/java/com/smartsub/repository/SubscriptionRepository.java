package com.smartsub.repository;

import com.smartsub.model.Subscription;
import com.smartsub.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    List<Subscription> findByUser(User user);
    List<Subscription> findByUserAndStatus(User user, String status);
    Optional<Subscription> findByIdAndUser(Long id, User user);
    List<Subscription> findByUserAndCategory(User user, String category);
    List<Subscription> findByUserAndRenewalDateBetween(User user, LocalDate startDate, LocalDate endDate);
}
