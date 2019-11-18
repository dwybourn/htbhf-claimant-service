package uk.gov.dhsc.htbhf.claimant.eligibility;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.dhsc.htbhf.claimant.communications.DetermineEntitlementNotificationHandler;
import uk.gov.dhsc.htbhf.claimant.entitlement.PregnancyEntitlementCalculator;
import uk.gov.dhsc.htbhf.claimant.entity.Claim;
import uk.gov.dhsc.htbhf.claimant.entity.PaymentCycle;
import uk.gov.dhsc.htbhf.claimant.message.processor.ChildDateOfBirthCalculator;
import uk.gov.dhsc.htbhf.claimant.model.ClaimStatus;
import uk.gov.dhsc.htbhf.claimant.model.eligibility.EligibilityAndEntitlementDecision;
import uk.gov.dhsc.htbhf.claimant.repository.ClaimRepository;
import uk.gov.dhsc.htbhf.claimant.service.ClaimMessageSender;
import uk.gov.dhsc.htbhf.claimant.service.audit.EventAuditor;

import java.time.LocalDate;
import java.util.List;

import static uk.gov.dhsc.htbhf.claimant.entity.CardStatus.PENDING_CANCELLATION;
import static uk.gov.dhsc.htbhf.claimant.reporting.ClaimAction.UPDATED_FROM_ACTIVE_TO_EXPIRED;
import static uk.gov.dhsc.htbhf.claimant.reporting.ClaimAction.UPDATED_FROM_ACTIVE_TO_PENDING_EXPIRY;

@Component
@AllArgsConstructor
public class EligibilityDecisionHandler {

    private ClaimRepository claimRepository;
    private DetermineEntitlementNotificationHandler determineEntitlementNotificationHandler;
    private PregnancyEntitlementCalculator pregnancyEntitlementCalculator;
    private ChildDateOfBirthCalculator childDateOfBirthCalculator;
    private EventAuditor eventAuditor;
    private ClaimMessageSender claimMessageSender;

    /**
     * Handles the processing of ineligible {@link EligibilityAndEntitlementDecision}.
     * This includes updating claim/card statuses and sending notifications if necessary.
     *
     * @param claim the claim the decision relates to
     * @param previousPaymentCycle the claim's previous payment cycle
     * @param currentPaymentCycle the claim's current payment cycle
     * @param decision the ineligible entitlement decision
     */
    public void handleIneligibleDecision(Claim claim,
                                         PaymentCycle previousPaymentCycle,
                                         PaymentCycle currentPaymentCycle,
                                         EligibilityAndEntitlementDecision decision) {

        if (shouldExpireClaim(decision, previousPaymentCycle, currentPaymentCycle)) {
            expireActiveClaim(claim, decision.getDateOfBirthOfChildren());
        } else if (decision.getIdentityAndEligibilityResponse().isNotEligible()) {
            handleLossOfQualifyingBenefitStatus(claim, decision.getDateOfBirthOfChildren());
        } else {
            handleNoLongerEligibleForSchemeAsNoChildrenAndNotPregnant(claim, decision.getDateOfBirthOfChildren());
        }

        setCardStatusToPendingCancellation(claim);
    }

    private boolean shouldExpireClaim(EligibilityAndEntitlementDecision decision, PaymentCycle previousPaymentCycle, PaymentCycle currentPaymentCycle) {
        if (decision.childrenPresent() || claimantIsPregnantInCycle(currentPaymentCycle)) {
            return false;
        }
        if (childrenExistedInPreviousCycleAndNowOver4(previousPaymentCycle, currentPaymentCycle)) {
            return true;
        }
        return claimantIsPregnantInCycle(previousPaymentCycle);
    }

    private boolean childrenExistedInPreviousCycleAndNowOver4(PaymentCycle previousPaymentCycle, PaymentCycle currentPaymentCycle) {
        return childDateOfBirthCalculator.hadChildrenUnder4AtStartOfPaymentCycle(previousPaymentCycle)
                && !childDateOfBirthCalculator.hadChildrenUnderFourAtGivenDate(previousPaymentCycle.getChildrenDob(), currentPaymentCycle.getCycleStartDate());
    }

    //Use the PregnancyEntitlementCalculator to check that the claimant is either not pregnant or their pregnancy date is
    //considered too far in the past.
    private boolean claimantIsPregnantInCycle(PaymentCycle paymentCycle) {
        return pregnancyEntitlementCalculator.isEntitledToVoucher(paymentCycle.getExpectedDeliveryDate(), paymentCycle.getCycleStartDate());
    }

    private void handleLossOfQualifyingBenefitStatus(Claim claim, List<LocalDate> dateOfBirthOfChildren) {
        updateClaimStatus(claim, ClaimStatus.PENDING_EXPIRY);
        determineEntitlementNotificationHandler.sendClaimNoLongerEligibleEmail(claim);
        claimMessageSender.sendReportClaimMessage(claim, dateOfBirthOfChildren, UPDATED_FROM_ACTIVE_TO_PENDING_EXPIRY);
    }

    private void handleNoLongerEligibleForSchemeAsNoChildrenAndNotPregnant(Claim claim, List<LocalDate> dateOfBirthOfChildren) {
        expireActiveClaim(claim, dateOfBirthOfChildren);
        determineEntitlementNotificationHandler.sendNoChildrenOnFeedClaimNoLongerEligibleEmail(claim);
    }

    private void expireActiveClaim(Claim claim, List<LocalDate> dateOfBirthOfChildren) {
        updateClaimStatus(claim, ClaimStatus.EXPIRED);
        eventAuditor.auditExpiredClaim(claim);
        claimMessageSender.sendReportClaimMessage(claim, dateOfBirthOfChildren, UPDATED_FROM_ACTIVE_TO_EXPIRED);
    }

    private void setCardStatusToPendingCancellation(Claim claim) {
        claim.updateCardStatus(PENDING_CANCELLATION);
        claimRepository.save(claim);
    }

    private void updateClaimStatus(Claim claim, ClaimStatus claimStatus) {
        claim.updateClaimStatus(claimStatus);
        claimRepository.save(claim);
    }
}
