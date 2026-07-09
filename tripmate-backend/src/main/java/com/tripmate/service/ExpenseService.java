package com.tripmate.service;

import com.tripmate.dto.request.CreateExpenseRequest;
import com.tripmate.dto.request.ExpenseSplitRequest;
import com.tripmate.dto.request.MarkPaidRequest;
import com.tripmate.dto.request.UpdateExpenseRequest;
import com.tripmate.dto.response.BalanceResponse;
import com.tripmate.dto.response.ExpenseResponse;
import com.tripmate.dto.response.ExpenseSplitResponse;
import com.tripmate.dto.response.SettlementResponse;
import com.tripmate.entity.*;
import com.tripmate.entity.enums.SplitType;
import com.tripmate.event.ExpenseCreatedEvent;
import com.tripmate.event.ExpenseEventProducer;
import com.tripmate.exception.BadRequestException;
import com.tripmate.exception.ResourceNotFoundException;
import com.tripmate.exception.TripAccessDeniedException;
import com.tripmate.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseSplitRepository splitRepository;
    private final TripRepository tripRepository;
    private final TripMemberRepository memberRepository;
    private final UserRepository userRepository;
    private final ExpenseEventProducer expenseEventProducer;;

    public List<ExpenseResponse> getExpenses(UUID tripId, String email) {
        verifyMembership(tripId, email);
        return expenseRepository.findByTripIdOrderByDateDescCreatedAtDesc(tripId).stream()
                .map(e -> toExpenseResponse(e, splitRepository.findByExpenseId(e.getId())))
                .toList();
    }

    @Transactional
    public ExpenseResponse createExpense(UUID tripId, CreateExpenseRequest req, String email) {
        verifyMembership(tripId, email);
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found"));
        User paidBy = userRepository.findById(req.getPaidByUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Paid-by user not found"));

        Expense expense = Expense.builder()
                .trip(trip).title(req.getTitle()).amount(req.getAmount())
                .currency(req.getCurrency() != null ? req.getCurrency() : "INR")
                .paidBy(paidBy).category(req.getCategory())
                .splitType(req.getSplitType()).date(req.getDate())
                .build();
        expense = expenseRepository.save(expense);

        List<ExpenseSplit> splits = createSplits(expense, req.getSplitType(), req.getAmount(), req.getSplits(), tripId);
        ExpenseCreatedEvent event = ExpenseCreatedEvent.builder()
                .eventId(UUID.randomUUID())
                .occurredAt(Instant.now())
                .paidByName(paidBy.getDisplayName())
                .tripId(tripId)
                .expenseId(expense.getId())
                .title(expense.getTitle())
                .amount(expense.getAmount())
                .currency(expense.getCurrency())
                .paidByUserId(req.getPaidByUserId())
                .build();

        expenseEventProducer.publishExpenseCreated(event);

        return toExpenseResponse(expense, splits);
    }

    @Transactional
    public ExpenseResponse updateExpense(UUID expenseId, UpdateExpenseRequest req, String email) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found"));
        verifyMembership(expense.getTrip().getId(), email);

        if (req.getTitle() != null) expense.setTitle(req.getTitle());
        if (req.getAmount() != null) expense.setAmount(req.getAmount());
        if (req.getCurrency() != null) expense.setCurrency(req.getCurrency());
        if (req.getCategory() != null) expense.setCategory(req.getCategory());
        if (req.getDate() != null) expense.setDate(req.getDate());
        if (req.getPaidByUserId() != null) {
            User paidBy = userRepository.findById(req.getPaidByUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            expense.setPaidBy(paidBy);
        }
        if (req.getSplitType() != null) {
            expense.setSplitType(req.getSplitType());
            splitRepository.deleteByExpenseId(expenseId);
            Expense saved = expenseRepository.save(expense);
            BigDecimal amount = req.getAmount() != null ? req.getAmount() : expense.getAmount();
            List<ExpenseSplit> splits = createSplits(saved, req.getSplitType(), amount, req.getSplits(), expense.getTrip().getId());
            return toExpenseResponse(saved, splits);
        }
        Expense saved = expenseRepository.save(expense);
        return toExpenseResponse(saved, splitRepository.findByExpenseId(expenseId));
    }

    @Transactional
    public void deleteExpense(UUID expenseId, String email) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found"));
        verifyMembership(expense.getTrip().getId(), email);
        splitRepository.deleteByExpenseId(expenseId);
        expenseRepository.delete(expense);
    }

    public List<BalanceResponse> getBalances(UUID tripId, String email) {
        verifyMembership(tripId, email);
        Map<UUID, BigDecimal> balances = computeNetBalances(tripId);
        Map<UUID, String> colors = buildColorCache(tripId);

        return balances.entrySet().stream().map(e -> {
            User user = userRepository.findById(e.getKey()).orElseThrow();
            return BalanceResponse.builder()
                    .userId(user.getId()).displayName(user.getDisplayName())
                    .avatarUrl(user.getAvatarUrl())
                    .color(colors.getOrDefault(user.getId(), "#888888"))
                    .netBalance(e.getValue().setScale(2, RoundingMode.HALF_UP))
                    .currency("INR")
                    .build();
        }).toList();
    }

    public List<SettlementResponse> getSettlements(UUID tripId, String email) {
        verifyMembership(tripId, email);
        Map<UUID, BigDecimal> netBalances = computeNetBalances(tripId);
        Map<UUID, String> colors = buildColorCache(tripId);

        PriorityQueue<long[]> creditors = new PriorityQueue<>((a, b) -> Long.compare(b[1], a[1]));
        PriorityQueue<long[]> debtors = new PriorityQueue<>(Comparator.comparingLong(a -> a[1]));
        Map<Long, UUID> idxToUuid = new HashMap<>();
        long idx = 0;

        // Convert BigDecimal balances to paisa (cents) to avoid floating point issues
        for (Map.Entry<UUID, BigDecimal> entry : netBalances.entrySet()) {
            long paisa = entry.getValue().multiply(BigDecimal.valueOf(100)).longValue();
            idxToUuid.put(idx, entry.getKey());
            if (paisa > 0) creditors.add(new long[]{idx, paisa});
            else if (paisa < 0) debtors.add(new long[]{idx, paisa});
            idx++;
        }

        List<SettlementResponse> results = new ArrayList<>();
        Map<UUID, User> userCache = new HashMap<>();

        while (!creditors.isEmpty() && !debtors.isEmpty()) {
            long[] creditor = creditors.poll();
            long[] debtor = debtors.poll();
            long creditAmt = creditor[1];
            long debtAmt = -debtor[1];
            long settle = Math.min(creditAmt, debtAmt);

            UUID fromId = idxToUuid.get(debtor[0]);
            UUID toId = idxToUuid.get(creditor[0]);
            User fromUser = userCache.computeIfAbsent(fromId, id -> userRepository.findById(id).orElseThrow());
            User toUser = userCache.computeIfAbsent(toId, id -> userRepository.findById(id).orElseThrow());

            results.add(SettlementResponse.builder()
                    .fromUserId(fromId).fromUserName(fromUser.getDisplayName())
                    .fromUserColor(colors.getOrDefault(fromId, "#888888"))
                    .toUserId(toId).toUserName(toUser.getDisplayName())
                    .toUserColor(colors.getOrDefault(toId, "#888888"))
                    .amount(BigDecimal.valueOf(settle).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP))
                    .currency("INR")
                    .build());

            long remCredit = creditAmt - settle;
            long remDebt = debtAmt - settle;
            if (remCredit > 0) creditors.add(new long[]{creditor[0], remCredit});
            if (remDebt > 0) debtors.add(new long[]{debtor[0], -remDebt});
        }
        return results;
    }

    @Transactional
    public void markPaid(MarkPaidRequest req, String email) {
        verifyMembership(req.getTripId(), email);
        splitRepository.markSplitsAsPaid(req.getFromUserId(), req.getToUserId(), req.getTripId());
    }

    Map<UUID, BigDecimal> computeNetBalances(UUID tripId) {
        List<TripMember> members = memberRepository.findByTripId(tripId);
        Map<UUID, BigDecimal> balances = new HashMap<>();
        for (TripMember m : members) balances.put(m.getUser().getId(), BigDecimal.ZERO);

        for (Object[] row : expenseRepository.sumPaidByUserForTrip(tripId)) {
            UUID uid = (UUID) row[0];
            BigDecimal paid = (BigDecimal) row[1];
            balances.merge(uid, paid, BigDecimal::add);
        }
        for (Object[] row : splitRepository.sumUnpaidByUserForTrip(tripId)) {
            UUID uid = (UUID) row[0];
            BigDecimal owed = (BigDecimal) row[1];
            balances.merge(uid, owed.negate(), BigDecimal::add);
        }
        return balances;
    }

    private Map<UUID, String> buildColorCache(UUID tripId) {
        Map<UUID, String> colors = new HashMap<>();
        memberRepository.findByTripId(tripId).forEach(m -> colors.put(m.getUser().getId(), m.getColor()));
        return colors;
    }

    private List<ExpenseSplit> createSplits(Expense expense, SplitType splitType,
                                            BigDecimal total, List<ExpenseSplitRequest> customSplits, UUID tripId) {
        List<TripMember> members = memberRepository.findByTripId(tripId);
        if (splitType == SplitType.EQUAL) {
            BigDecimal each = total.divide(BigDecimal.valueOf(members.size()), 2, RoundingMode.HALF_UP);
            return members.stream().map(m -> splitRepository.save(ExpenseSplit.builder()
                    .expense(expense).user(m.getUser()).amount(each).paid(false).build())).toList();
        } else {
            if (customSplits == null || customSplits.isEmpty()) {
                throw new BadRequestException("Custom splits must be provided");
            }
            BigDecimal splitTotal = customSplits.stream()
                    .map(ExpenseSplitRequest::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            if (splitTotal.compareTo(total) != 0) {
                throw new BadRequestException("Custom splits must sum to total amount");
            }
            return customSplits.stream().map(s -> {
                User user = userRepository.findById(s.getUserId())
                        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
                return splitRepository.save(ExpenseSplit.builder()
                        .expense(expense).user(user).amount(s.getAmount()).paid(false).build());
            }).toList();
        }
    }

    private void verifyMembership(UUID tripId, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (!memberRepository.existsByTripIdAndUserId(tripId, user.getId())) {
            throw new TripAccessDeniedException("You are not a member of this trip");
        }
    }

    private ExpenseResponse toExpenseResponse(Expense e, List<ExpenseSplit> splits) {
        return ExpenseResponse.builder()
                .id(e.getId()).title(e.getTitle()).amount(e.getAmount())
                .currency(e.getCurrency()).paidBy(AuthService.toUserResponse(e.getPaidBy()))
                .category(e.getCategory()).splitType(e.getSplitType())
                .date(e.getDate()).createdAt(e.getCreatedAt())
                .splits(splits.stream().map(s -> ExpenseSplitResponse.builder()
                        .userId(s.getUser().getId()).displayName(s.getUser().getDisplayName())
                        .amount(s.getAmount()).paid(s.isPaid()).build()).toList())
                .build();
    }
}
