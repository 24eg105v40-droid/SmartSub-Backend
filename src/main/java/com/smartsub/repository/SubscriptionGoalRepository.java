package com.smartsub.repository;

import com.smartsub.model.Subscription;
import com.smartsub.model.SubscriptionGoal;
import com.smartsub.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionGoalRepository extends JpaRepository<SubscriptionGoal, Long> {
    List<SubscriptionGoal> findByUser(User user);
    List<SubscriptionGoal> findBySubscription(Subscription subscription);
    Optional<SubscriptionGoal> findByIdAndUser(Long id, User user);
}
