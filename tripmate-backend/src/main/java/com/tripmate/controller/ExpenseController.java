package com.tripmate.controller;

import com.tripmate.dto.request.CreateExpenseRequest;
import com.tripmate.dto.request.MarkPaidRequest;
import com.tripmate.dto.request.UpdateExpenseRequest;
import com.tripmate.dto.response.BalanceResponse;
import com.tripmate.dto.response.ExpenseResponse;
import com.tripmate.dto.response.SettlementResponse;
import com.tripmate.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @GetMapping("/api/trips/{tripId}/expenses")
    public ResponseEntity<List<ExpenseResponse>> getExpenses(@PathVariable UUID tripId,
                                                             @AuthenticationPrincipal UserDetails ud) {
        return ResponseEntity.ok(expenseService.getExpenses(tripId, ud.getUsername()));
    }

    @PostMapping("/api/trips/{tripId}/expenses")
    public ResponseEntity<ExpenseResponse> createExpense(@PathVariable UUID tripId,
                                                         @RequestBody @Valid CreateExpenseRequest req,
                                                         @AuthenticationPrincipal UserDetails ud) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(expenseService.createExpense(tripId, req, ud.getUsername()));
    }

    @PutMapping("/api/expenses/{expenseId}")
    public ResponseEntity<ExpenseResponse> updateExpense(@PathVariable UUID expenseId,
                                                         @RequestBody UpdateExpenseRequest req,
                                                         @AuthenticationPrincipal UserDetails ud) {
        return ResponseEntity.ok(expenseService.updateExpense(expenseId, req, ud.getUsername()));
    }

    @DeleteMapping("/api/expenses/{expenseId}")
    public ResponseEntity<Void> deleteExpense(@PathVariable UUID expenseId,
                                              @AuthenticationPrincipal UserDetails ud) {
        expenseService.deleteExpense(expenseId, ud.getUsername());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/trips/{tripId}/balances")
    public ResponseEntity<List<BalanceResponse>> getBalances(@PathVariable UUID tripId,
                                                             @AuthenticationPrincipal UserDetails ud) {
        return ResponseEntity.ok(expenseService.getBalances(tripId, ud.getUsername()));
    }

    @GetMapping("/api/trips/{tripId}/settlements")
    public ResponseEntity<List<SettlementResponse>> getSettlements(@PathVariable UUID tripId,
                                                                   @AuthenticationPrincipal UserDetails ud) {
        return ResponseEntity.ok(expenseService.getSettlements(tripId, ud.getUsername()));
    }

    @PutMapping("/api/settlements/mark-paid")
    public ResponseEntity<Void> markPaid(@RequestBody @Valid MarkPaidRequest req,
                                         @AuthenticationPrincipal UserDetails ud) {
        expenseService.markPaid(req, ud.getUsername());
        return ResponseEntity.ok().build();
    }
}
