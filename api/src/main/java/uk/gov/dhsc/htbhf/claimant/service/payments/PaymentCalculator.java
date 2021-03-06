package uk.gov.dhsc.htbhf.claimant.service.payments;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.dhsc.htbhf.claimant.entitlement.PaymentCycleVoucherEntitlement;

import java.time.LocalDateTime;

import static uk.gov.dhsc.htbhf.claimant.entity.PaymentCycleStatus.FULL_PAYMENT_MADE;
import static uk.gov.dhsc.htbhf.claimant.entity.PaymentCycleStatus.PARTIAL_PAYMENT_MADE;
import static uk.gov.dhsc.htbhf.claimant.service.payments.PaymentCalculation.aBalanceTooHighPaymentCalculation;

@Component
public class PaymentCalculator {

    private final int maximumBalancePeriod;

    public PaymentCalculator(@Value("${payment-cycle.maximum-balance-period}") int maximumBalancePeriod) {
        this.maximumBalancePeriod = maximumBalancePeriod;
    }

    /**
     * Calculates the amount to apply to a new payment request.
     * Returns the full entitlement amount when the full entitlement amount plus the card balance is less than or equal to the maximum card balance.
     * Returns a partial amount when the full entitlement amount plus the card balance is greater than or equal to the maximum card balance.
     * Returns zero when the card balance is greater than or equal to the maximum card balance.
     *
     * @param entitlement        a payment cycle voucher entitlement
     * @param cardBalanceInPence the card balance in pence
     * @return the payment calculation which comprises the amount in pence to apply to the next payment request and the status of the PaymentCycle
     */
    public PaymentCalculation calculatePaymentCycleAmountInPence(PaymentCycleVoucherEntitlement entitlement, int cardBalanceInPence) {
        int firstWeekEntitlementInPence = entitlement.getFirstVoucherEntitlementForCycle().getTotalVoucherValueInPence();
        int maximumAllowedCardBalanceInPence = firstWeekEntitlementInPence * maximumBalancePeriod;

        if (isCardBalanceTooHigh(cardBalanceInPence, maximumAllowedCardBalanceInPence)) {
            return aBalanceTooHighPaymentCalculation(cardBalanceInPence);
        }
        int paymentCycleTotalEntitlementInPence = entitlement.getTotalVoucherValueInPence();
        if (fullPaymentKeepsBalanceWithinThreshold(cardBalanceInPence, maximumAllowedCardBalanceInPence, paymentCycleTotalEntitlementInPence)) {
            return PaymentCalculation.builder()
                    .paymentAmount(paymentCycleTotalEntitlementInPence)
                    .paymentCycleStatus(FULL_PAYMENT_MADE)
                    .availableBalanceInPence(cardBalanceInPence)
                    .balanceTimestamp(LocalDateTime.now())
                    .build();
        }
        return PaymentCalculation.builder()
                .paymentAmount(maximumAllowedCardBalanceInPence - cardBalanceInPence)
                .paymentCycleStatus(PARTIAL_PAYMENT_MADE)
                .availableBalanceInPence(cardBalanceInPence)
                .balanceTimestamp(LocalDateTime.now())
                .build();
    }

    private boolean isCardBalanceTooHigh(int cardBalanceInPence, int maximumAllowedCardBalanceInPence) {
        return cardBalanceInPence >= maximumAllowedCardBalanceInPence;
    }

    private boolean fullPaymentKeepsBalanceWithinThreshold(int cardBalanceInPence,
                                                           int maxAllowedCardBalanceInPence,
                                                           int paymentCycleTotalEntitlementInPence) {
        return cardBalanceInPence + paymentCycleTotalEntitlementInPence <= maxAllowedCardBalanceInPence;
    }
}
