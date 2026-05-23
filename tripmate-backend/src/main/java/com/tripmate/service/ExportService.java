package com.tripmate.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.tripmate.dto.response.BalanceResponse;
import com.tripmate.dto.response.SettlementResponse;
import com.tripmate.entity.*;
import com.tripmate.exception.ResourceNotFoundException;
import com.tripmate.exception.TripAccessDeniedException;
import com.tripmate.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ExportService {

    private final TripRepository tripRepository;
    private final TripMemberRepository memberRepository;
    private final ItineraryDayRepository dayRepository;
    private final ItineraryItemRepository itemRepository;
    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;
    private final ExpenseService expenseService;

    public byte[] generateTripPdf(UUID tripId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (!memberRepository.existsByTripIdAndUserId(tripId, user.getId())) {
            throw new TripAccessDeniedException("Not a member of this trip");
        }
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found"));
        List<TripMember> members = memberRepository.findByTripId(tripId);
        List<ItineraryDay> days = dayRepository.findByTripIdOrderByDayNumber(tripId);
        List<Expense> expenses = expenseRepository.findByTripIdOrderByDateDescCreatedAtDesc(tripId);
        List<BalanceResponse> balances = expenseService.getBalances(tripId, userEmail);
        List<SettlementResponse> settlements = expenseService.getSettlements(tripId, userEmail);

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document doc = new Document(PageSize.A4, 50, 50, 60, 60);
            PdfWriter.getInstance(doc, baos);
            doc.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, new BaseColor(63, 63, 240));
            Font h2Font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, new BaseColor(30, 41, 59));
            Font h3Font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.DARK_GRAY);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.DARK_GRAY);
            Font smallFont = FontFactory.getFont(FontFactory.HELVETICA, 9, BaseColor.GRAY);

            // Title
            Paragraph title = new Paragraph(trip.getCoverEmoji() + "  " + trip.getName(), titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(4);
            doc.add(title);

            if (trip.getStartDate() != null && trip.getEndDate() != null) {
                Paragraph dates = new Paragraph(trip.getStartDate() + "  →  " + trip.getEndDate(), smallFont);
                dates.setAlignment(Element.ALIGN_CENTER);
                doc.add(dates);
            }
            doc.add(new Paragraph(" "));

            // Members
            doc.add(new Paragraph("Members", h2Font));
            doc.add(new Paragraph(" "));
            for (TripMember m : members) {
                doc.add(new Paragraph("  •  " + m.getUser().getDisplayName()
                        + "  (" + m.getRole().name().toLowerCase() + ")", normalFont));
            }
            doc.add(new Paragraph(" "));

            // Itinerary
            doc.add(new Paragraph("Itinerary", h2Font));
            doc.add(new Paragraph(" "));
            for (ItineraryDay day : days) {
                String dayTitle = "Day " + day.getDayNumber()
                        + (day.getTitle() != null && !day.getTitle().isBlank() ? ": " + day.getTitle() : "")
                        + (day.getDayDate() != null ? "  (" + day.getDayDate() + ")" : "");
                doc.add(new Paragraph(dayTitle, h3Font));
                List<ItineraryItem> items = itemRepository.findByDayIdOrderByPosition(day.getId());
                if (items.isEmpty()) {
                    doc.add(new Paragraph("    No activities", smallFont));
                }
                for (ItineraryItem item : items) {
                    String timeStr = item.getTime() != null ? "[" + item.getTime() + "]  " : "";
                    doc.add(new Paragraph("    " + timeStr + item.getTitle(), normalFont));
                    if (item.getNotes() != null && !item.getNotes().isBlank()) {
                        doc.add(new Paragraph("        " + item.getNotes(), smallFont));
                    }
                }
                doc.add(new Paragraph(" "));
            }

            // Expenses
            doc.add(new Paragraph("Expenses", h2Font));
            doc.add(new Paragraph(" "));
            if (expenses.isEmpty()) {
                doc.add(new Paragraph("No expenses recorded.", normalFont));
            } else {
                PdfPTable table = new PdfPTable(4);
                table.setWidthPercentage(100);
                table.setWidths(new float[]{3, 1.5f, 2, 1.5f});
                addTableHeader(table, h3Font, "Title", "Amount", "Paid By", "Category");
                BigDecimal total = BigDecimal.ZERO;
                for (Expense e : expenses) {
                    table.addCell(styledCell(e.getTitle(), normalFont));
                    table.addCell(styledCell(e.getCurrency() + " " + e.getAmount(), normalFont));
                    table.addCell(styledCell(e.getPaidBy().getDisplayName(), normalFont));
                    table.addCell(styledCell(e.getCategory() != null ? e.getCategory() : "-", normalFont));
                    total = total.add(e.getAmount());
                }
                doc.add(table);
                Paragraph totalPara = new Paragraph("Total Spend: INR " + total.setScale(2), h3Font);
                totalPara.setSpacingBefore(6);
                doc.add(totalPara);
            }
            doc.add(new Paragraph(" "));

            // Balances
            doc.add(new Paragraph("Balances", h2Font));
            doc.add(new Paragraph(" "));
            for (BalanceResponse b : balances) {
                String sign = b.getNetBalance().compareTo(BigDecimal.ZERO) >= 0 ? "+ " : "";
                doc.add(new Paragraph("  " + b.getDisplayName() + ":  " + sign + b.getCurrency()
                        + " " + b.getNetBalance().abs(), normalFont));
            }
            doc.add(new Paragraph(" "));

            // Settlements
            doc.add(new Paragraph("Settlement Summary", h2Font));
            doc.add(new Paragraph(" "));
            if (settlements.isEmpty()) {
                doc.add(new Paragraph("All settled! ✓", normalFont));
            } else {
                for (SettlementResponse s : settlements) {
                    doc.add(new Paragraph("  " + s.getFromUserName() + "  →  " + s.getToUserName()
                            + ":  " + s.getCurrency() + " " + s.getAmount(), normalFont));
                }
            }

            doc.close();
            return baos.toByteArray();
        } catch (DocumentException e) {
            throw new RuntimeException("Failed to generate PDF", e);
        }
    }

    private void addTableHeader(PdfPTable table, Font font, String... headers) {
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, font));
            cell.setBackgroundColor(new BaseColor(238, 242, 255));
            cell.setPadding(6);
            table.addCell(cell);
        }
    }

    private PdfPCell styledCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(5);
        return cell;
    }
}
