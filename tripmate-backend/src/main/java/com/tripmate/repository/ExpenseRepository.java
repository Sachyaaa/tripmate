package com.tripmate.repository;

import com.tripmate.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, UUID> {
    List<Expense> findByTripIdOrderByDateDescCreatedAtDesc(UUID tripId);

    @Query("SELECT e.paidBy.id, SUM(e.amount) FROM Expense e WHERE e.trip.id = :tripId GROUP BY e.paidBy.id")
    List<Object[]> sumPaidByUserForTrip(@Param("tripId") UUID tripId);
}
