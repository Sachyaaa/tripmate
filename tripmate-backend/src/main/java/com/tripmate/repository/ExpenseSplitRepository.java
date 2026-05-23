package com.tripmate.repository;

import com.tripmate.entity.ExpenseSplit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ExpenseSplitRepository extends JpaRepository<ExpenseSplit, UUID> {
    List<ExpenseSplit> findByExpenseId(UUID expenseId);

    void deleteByExpenseId(UUID expenseId);

    @Query("SELECT es.user.id, SUM(es.amount) FROM ExpenseSplit es " +
           "WHERE es.expense.trip.id = :tripId AND es.paid = false GROUP BY es.user.id")
    List<Object[]> sumUnpaidByUserForTrip(@Param("tripId") UUID tripId);

    @Modifying
    @Query("UPDATE ExpenseSplit es SET es.paid = true " +
           "WHERE es.user.id = :fromUserId AND es.expense.paidBy.id = :toUserId " +
           "AND es.expense.trip.id = :tripId AND es.paid = false")
    void markSplitsAsPaid(@Param("fromUserId") UUID fromUserId,
                          @Param("toUserId") UUID toUserId,
                          @Param("tripId") UUID tripId);
}
