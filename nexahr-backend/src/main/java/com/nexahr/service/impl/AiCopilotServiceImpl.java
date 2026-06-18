package com.nexahr.service.impl;

import com.nexahr.dto.request.CopilotChatRequest;
import com.nexahr.dto.response.AiInsightResponse;
import com.nexahr.dto.response.AnalyticsOverviewResponse;
import com.nexahr.dto.response.AnalyticsResponse;
import com.nexahr.dto.response.CopilotChatResponse;
import com.nexahr.entity.enums.InsightSeverity;
import com.nexahr.service.AiCopilotService;
import com.nexahr.service.AnalyticsService;
import com.nexahr.service.LlmService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AiCopilotServiceImpl implements AiCopilotService {

    private final AnalyticsService analyticsService;
    private final LlmService llmService;

    @Override
    public List<AiInsightResponse> getInsights(Long companyId) {
        AnalyticsResponse analytics = analyticsService.getAnalytics(companyId);
        AnalyticsOverviewResponse o = analytics.getOverview();
        List<AiInsightResponse> insights = new ArrayList<>();

        if (o.getTurnoverRate() > 15) {
            insights.add(AiInsightResponse.builder()
                    .id("turnover-high")
                    .title("Tỷ lệ turnover cao")
                    .description(String.format("Turnover hiện tại %.1f%% — nên xem xét chương trình giữ chân nhân tài và phỏng vấn exit.", o.getTurnoverRate()))
                    .severity(InsightSeverity.WARNING)
                    .category("RETENTION")
                    .actionLabel("Xem phân tích")
                    .actionPath("/analytics")
                    .build());
        } else if (o.getTurnoverRate() < 5) {
            insights.add(AiInsightResponse.builder()
                    .id("turnover-good")
                    .title("Tỷ lệ giữ chân nhân sự tốt")
                    .description(String.format("Turnover chỉ %.1f%% — đội ngũ ổn định.", o.getTurnoverRate()))
                    .severity(InsightSeverity.SUCCESS)
                    .category("RETENTION")
                    .build());
        }

        if (o.getPendingLeaves() > 5) {
            insights.add(AiInsightResponse.builder()
                    .id("leave-backlog")
                    .title("Nhiều đơn nghỉ phép chờ duyệt")
                    .description(String.format("Có %d đơn nghỉ phép đang chờ — nên xử lý sớm để cải thiện trải nghiệm nhân viên.", o.getPendingLeaves()))
                    .severity(InsightSeverity.WARNING)
                    .category("LEAVE")
                    .actionLabel("Duyệt nghỉ phép")
                    .actionPath("/leaves")
                    .build());
        }

        if (o.getNewHiresThisMonth() > 0) {
            insights.add(AiInsightResponse.builder()
                    .id("new-hires")
                    .title("Nhân sự mới trong tháng")
                    .description(String.format("%d nhân viên mới — đảm bảo onboarding và phân công mentor.", o.getNewHiresThisMonth()))
                    .severity(InsightSeverity.INFO)
                    .category("ONBOARDING")
                    .actionLabel("Danh sách nhân viên")
                    .actionPath("/employees")
                    .build());
        }

        if (o.getOpenPositions() > 0 && o.getTotalCandidates() < o.getOpenPositions() * 3) {
            insights.add(AiInsightResponse.builder()
                    .id("recruitment-pipeline")
                    .title("Pipeline tuyển dụng cần bổ sung")
                    .description(String.format("%d vị trí mở nhưng chỉ %d ứng viên — nên đẩy mạnh careers portal.", o.getOpenPositions(), o.getTotalCandidates()))
                    .severity(InsightSeverity.WARNING)
                    .category("RECRUITMENT")
                    .actionLabel("Tuyển dụng")
                    .actionPath("/recruitment")
                    .build());
        }

        BigDecimal payroll = o.getPayrollCostThisMonth();
        if (payroll != null && payroll.compareTo(BigDecimal.valueOf(500_000_000)) > 0) {
            insights.add(AiInsightResponse.builder()
                    .id("payroll-cost")
                    .title("Chi phí lương tháng này")
                    .description(String.format("Tổng chi phí lương tháng: %,.0fđ — theo dõi biến động qua báo cáo analytics.", payroll))
                    .severity(InsightSeverity.INFO)
                    .category("PAYROLL")
                    .actionLabel("Bảng lương")
                    .actionPath("/payroll")
                    .build());
        }

        if (insights.isEmpty()) {
            insights.add(AiInsightResponse.builder()
                    .id("all-good")
                    .title("Mọi chỉ số ổn định")
                    .description("Không phát hiện vấn đề nghiêm trọng. Tiếp tục theo dõi dashboard hàng tuần.")
                    .severity(InsightSeverity.SUCCESS)
                    .category("GENERAL")
                    .build());
        }

        return insights;
    }

    @Override
    public CopilotChatResponse chat(Long companyId, CopilotChatRequest request) {
        AnalyticsResponse analytics = analyticsService.getAnalytics(companyId);
        AnalyticsOverviewResponse o = analytics.getOverview();

        String systemPrompt = buildSystemPrompt(o);
        var llmReply = llmService.chat(systemPrompt, request.getMessage());
        if (llmReply.isPresent()) {
            return CopilotChatResponse.builder()
                    .reply(llmReply.get())
                    .suggestions(defaultSuggestions())
                    .llmPowered(true)
                    .build();
        }

        String msg = request.getMessage().toLowerCase(Locale.ROOT).trim();
        String reply = ruleBasedReply(msg, o);

        return CopilotChatResponse.builder()
                .reply(reply.replace("**", ""))
                .suggestions(defaultSuggestions())
                .llmPowered(false)
                .build();
    }

    private String buildSystemPrompt(AnalyticsOverviewResponse o) {
        BigDecimal payroll = o.getPayrollCostThisMonth() != null ? o.getPayrollCostThisMonth() : BigDecimal.ZERO;
        return String.format("""
                Bạn là NexaHR Copilot — trợ lý HR cho doanh nghiệp Việt Nam.
                Trả lời ngắn gọn, thân thiện, bằng tiếng Việt (trừ khi user hỏi tiếng Anh).
                Chỉ dùng số liệu sau, không bịa thêm:
                - Nhân viên đang làm: %d
                - Tuyển mới tháng này: %d
                - Turnover: %.1f%%
                - Nghỉ việc năm nay: %d
                - Đơn nghỉ phép chờ duyệt: %d
                - Đơn nghỉ đã duyệt tháng này: %d
                - Chi phí lương tháng: %,.0f VND
                - Vị trí tuyển dụng mở: %d
                - Ứng viên trong pipeline: %d
                """,
                o.getTotalEmployees(), o.getNewHiresThisMonth(), o.getTurnoverRate(),
                o.getResignedThisYear(), o.getPendingLeaves(), o.getApprovedLeavesThisMonth(),
                payroll, o.getOpenPositions(), o.getTotalCandidates());
    }

    private List<String> defaultSuggestions() {
        return List.of(
                "Có bao nhiêu nhân viên?",
                "Tỷ lệ turnover là bao nhiêu?",
                "Có bao nhiêu đơn nghỉ phép chờ duyệt?",
                "Chi phí lương tháng này?"
        );
    }

    private String ruleBasedReply(String msg, AnalyticsOverviewResponse o) {
        String reply;
        if (containsAny(msg, "nhân viên", "employee", "headcount", "bao nhiêu người")) {
            reply = String.format("Hiện có **%d nhân viên** đang làm việc. Tuyển mới tháng này: %d người.",
                    o.getTotalEmployees(), o.getNewHiresThisMonth());
        } else if (containsAny(msg, "turnover", "nghỉ việc", "rotasyon")) {
            reply = String.format("Tỷ lệ turnover hiện tại là **%.1f%%**. %d nhân viên đã nghỉ việc.",
                    o.getTurnoverRate(), o.getResignedThisYear());
        } else if (containsAny(msg, "nghỉ phép", "leave", "phép")) {
            reply = String.format("Có **%d đơn nghỉ phép** đang chờ duyệt. Đã duyệt trong tháng: %d đơn.",
                    o.getPendingLeaves(), o.getApprovedLeavesThisMonth());
        } else if (containsAny(msg, "lương", "payroll", "salary", "chi phí")) {
            BigDecimal p = o.getPayrollCostThisMonth() != null ? o.getPayrollCostThisMonth() : BigDecimal.ZERO;
            reply = String.format("Chi phí lương tháng này: **%,.0f VND**.", p);
        } else if (containsAny(msg, "tuyển", "recruit", "ứng viên", "candidate")) {
            reply = String.format("Đang có **%d vị trí** mở và **%d ứng viên** trong pipeline.",
                    o.getOpenPositions(), o.getTotalCandidates());
        } else if (containsAny(msg, "xin chào", "hello", "hi", "chào")) {
            reply = "Xin chào! Tôi là **NexaHR Copilot**. Hỏi tôi về nhân sự, nghỉ phép, lương hoặc tuyển dụng.";
        } else {
            reply = "Tôi có thể trả lời về: số nhân viên, turnover, nghỉ phép, chi phí lương, tuyển dụng. Hãy thử hỏi cụ thể hơn!";
        }
        return reply;
    }

    private boolean containsAny(String text, String... keywords) {
        for (String k : keywords) {
            if (text.contains(k)) return true;
        }
        return false;
    }
}
