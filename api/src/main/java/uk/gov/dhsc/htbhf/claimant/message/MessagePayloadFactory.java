package uk.gov.dhsc.htbhf.claimant.message;

import uk.gov.dhsc.htbhf.claimant.entitlement.PaymentCycleVoucherEntitlement;
import uk.gov.dhsc.htbhf.claimant.entity.Claim;
import uk.gov.dhsc.htbhf.claimant.entity.Claimant;
import uk.gov.dhsc.htbhf.claimant.entity.PaymentCycle;
import uk.gov.dhsc.htbhf.claimant.message.payload.EmailMessagePayload;
import uk.gov.dhsc.htbhf.claimant.message.payload.EmailType;
import uk.gov.dhsc.htbhf.claimant.message.payload.MakePaymentMessagePayload;
import uk.gov.dhsc.htbhf.claimant.message.payload.NewCardRequestMessagePayload;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.dhsc.htbhf.claimant.message.MoneyUtils.convertPenceToPounds;

public class MessagePayloadFactory {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy");

    public static NewCardRequestMessagePayload buildNewCardMessagePayload(Claim claim,
                                                                          PaymentCycleVoucherEntitlement voucherEntitlement,
                                                                          List<LocalDate> datesOfBirthOfChildren) {
        return NewCardRequestMessagePayload.builder()
                .claimId(claim.getId())
                .voucherEntitlement(voucherEntitlement)
                .datesOfBirthOfChildren(datesOfBirthOfChildren)
                .build();
    }

    public static MakePaymentMessagePayload buildMakePaymentMessagePayload(PaymentCycle paymentCycle) {
        return MakePaymentMessagePayload.builder()
                .claimId(paymentCycle.getClaim().getId())
                .paymentCycleId(paymentCycle.getId())
                .cardAccountId(paymentCycle.getClaim().getCardAccountId())
                .build();
    }

    /**
     * Builds the message payload required to send a new card email message. The email template has paremeterised values
     * which are contained in the emailPersonalisation Map. All monetary amounts are formatted into pounds and the breakdown
     * of voucher payments has been detailed in a bullet point list - any vouchers which are missing are replaced with a
     * single blank line so that we don't have any empty bullet point in the email.
     *
     * @param paymentCycle The payment cycle with payment and voucher details.
     * @return The constructed payload.
     */
    public static EmailMessagePayload buildSendNewCardSuccessEmailPayload(PaymentCycle paymentCycle) {
        Claimant claimant = paymentCycle.getClaim().getClaimant();
        PaymentCycleVoucherEntitlement voucherEntitlement = paymentCycle.getVoucherEntitlement();
        Map<String, Object> emailPersonalisation = new HashMap<>();
        emailPersonalisation.put(EmailTemplateKey.FIRST_NAME.getTemplateKeyName(), claimant.getFirstName());
        emailPersonalisation.put(EmailTemplateKey.LAST_NAME.getTemplateKeyName(), claimant.getLastName());
        emailPersonalisation.put(EmailTemplateKey.FIRST_PAYMENT_AMOUNT.getTemplateKeyName(),
                convertPenceToPounds(paymentCycle.getTotalEntitlementAmountInPence()));
        emailPersonalisation.put(EmailTemplateKey.PREGNANCY_PAYMENT.getTemplateKeyName(), buildPregnancyPaymentAmountSummary(voucherEntitlement));
        emailPersonalisation.put(EmailTemplateKey.CHILDREN_UNDER_1_PAYMENT.getTemplateKeyName(), buildUnder1PaymentSummary(voucherEntitlement));
        emailPersonalisation.put(EmailTemplateKey.CHILDREN_UNDER_4_PAYMENT.getTemplateKeyName(), buildUnder4PaymentSummary(voucherEntitlement));
        String formattedCycleEndDate = paymentCycle.getCycleEndDate().format(DATE_FORMATTER);
        emailPersonalisation.put(EmailTemplateKey.NEXT_PAYMENT_DATE.getTemplateKeyName(), formattedCycleEndDate);
        return EmailMessagePayload.builder()
                .claimId(paymentCycle.getClaim().getId())
                .emailType(EmailType.NEW_CARD)
                .emailPersonalisation(emailPersonalisation)
                .build();
    }

    private static String buildPregnancyPaymentAmountSummary(PaymentCycleVoucherEntitlement voucherEntitlement) {
        return formatPaymentAmountSummary(
                "\n* %s for a pregnancy",
                voucherEntitlement.getVouchersForPregnancy(),
                voucherEntitlement.getSingleVoucherValueInPence());
    }

    private static String buildUnder1PaymentSummary(PaymentCycleVoucherEntitlement voucherEntitlement) {
        return formatPaymentAmountSummary(
                "\n* %s for children under 1",
                voucherEntitlement.getVouchersForChildrenUnderOne(),
                voucherEntitlement.getSingleVoucherValueInPence());
    }

    private static String buildUnder4PaymentSummary(PaymentCycleVoucherEntitlement voucherEntitlement) {
        return formatPaymentAmountSummary(
                "\n* %s for children between 1 and 4",
                voucherEntitlement.getVouchersForChildrenBetweenOneAndFour(),
                voucherEntitlement.getSingleVoucherValueInPence());
    }

    private static String formatPaymentAmountSummary(String summaryTemplate, int numberOfVouchers, int voucherAmountInPence) {
        if (numberOfVouchers == 0) {
            return "";
        }
        int totalAmount = numberOfVouchers * voucherAmountInPence;
        return String.format(summaryTemplate, convertPenceToPounds(totalAmount));
    }

}