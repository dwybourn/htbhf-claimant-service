package uk.gov.dhsc.htbhf.claimant.reporting;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.dhsc.htbhf.claimant.entitlement.PaymentCycleVoucherEntitlement;
import uk.gov.dhsc.htbhf.claimant.entity.Claim;
import uk.gov.dhsc.htbhf.claimant.entity.PaymentCycle;
import uk.gov.dhsc.htbhf.claimant.message.MessageQueueClient;
import uk.gov.dhsc.htbhf.claimant.message.payload.ReportPaymentMessagePayload;

import java.time.LocalDateTime;

import static uk.gov.dhsc.htbhf.claimant.message.MessageType.REPORT_PAYMENT;
import static uk.gov.dhsc.htbhf.claimant.reporting.PaymentAction.TOP_UP_PAYMENT;

/**
 * Class responsible for sending report payment messages.
 */
@Component
@AllArgsConstructor
public class ReportPaymentMessageSender {

    private final MessageQueueClient messageQueueClient;

    /**
     * Sends a report payment message with a {@link PaymentAction} of {@link PaymentAction#TOP_UP_PAYMENT}.
     * @param claim the claim the payment is made against
     * @param paymentCycle the current payment cycle
     * @param paymentForPregnancyInPence the payment amount made for pregnancy
     */
    public void sendReportPregnancyTopUpPaymentMessage(Claim claim, PaymentCycle paymentCycle, int paymentForPregnancyInPence) {
        ReportPaymentMessagePayload payload = ReportPaymentMessagePayload.builder()
                .claimId(claim.getId())
                .paymentCycleId(paymentCycle.getId())
                .identityAndEligibilityResponse(paymentCycle.getIdentityAndEligibilityResponse())
                .timestamp(LocalDateTime.now())
                .paymentAction(TOP_UP_PAYMENT)
                .paymentForPregnancy(paymentForPregnancyInPence)
                .build();

        messageQueueClient.sendMessage(payload, REPORT_PAYMENT);
    }

    public void sendReportPaymentMessage(Claim claim, PaymentCycle paymentCycle, PaymentAction paymentAction) {
        ReportPaymentMessagePayload payload = createPayloadWithPaymentAmountsDerivedFromPaymentCycle(claim, paymentCycle, paymentAction);
        messageQueueClient.sendMessage(payload, REPORT_PAYMENT);
    }

    private ReportPaymentMessagePayload createPayloadWithPaymentAmountsDerivedFromPaymentCycle(Claim claim,
                                                                                               PaymentCycle paymentCycle,
                                                                                               PaymentAction paymentAction) {
        PaymentCycleVoucherEntitlement voucherEntitlement = paymentCycle.getVoucherEntitlement();
        int singleVoucherValueInPence = voucherEntitlement.getSingleVoucherValueInPence();
        return ReportPaymentMessagePayload.builder()
                .claimId(claim.getId())
                .paymentCycleId(paymentCycle.getId())
                .identityAndEligibilityResponse(paymentCycle.getIdentityAndEligibilityResponse())
                .timestamp(LocalDateTime.now())
                .paymentAction(paymentAction)
                .paymentForChildrenUnderOne(voucherEntitlement.getVouchersForChildrenUnderOne() * singleVoucherValueInPence)
                .paymentForChildrenBetweenOneAndFour(voucherEntitlement.getVouchersForChildrenBetweenOneAndFour() * singleVoucherValueInPence)
                .paymentForPregnancy(voucherEntitlement.getVouchersForPregnancy() * singleVoucherValueInPence)
                .paymentForBackdatedVouchers(voucherEntitlement.getBackdatedVouchersValueInPence())
                .build();
    }
}
