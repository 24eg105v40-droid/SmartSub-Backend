package com.smartsub.repository;

import com.smartsub.model.PriceHistory;
import com.smartsub.model.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PriceHistoryRepository extends JpaRepository<PriceHistory, Long> {
    List<PriceHistory> findBySubscriptionOrderByChangeDateDesc(Subscription subscription);
}
