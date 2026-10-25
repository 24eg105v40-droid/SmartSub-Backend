package com.smartsub.repository;

import com.smartsub.model.SubscriptionEvent;
import com.smartsub.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SubscriptionEventRepository extends JpaRepository<SubscriptionEvent, Long> {
    List<SubscriptionEvent> findByUserOrderByEventDateDesc(User user);
}
