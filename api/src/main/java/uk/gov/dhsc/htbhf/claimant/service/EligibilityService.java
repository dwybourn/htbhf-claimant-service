package uk.gov.dhsc.htbhf.claimant.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.dhsc.htbhf.claimant.entitlement.CycleEntitlementCalculator;
import uk.gov.dhsc.htbhf.claimant.entitlement.PaymentCycleVoucherEntitlement;
import uk.gov.dhsc.htbhf.claimant.entity.Claimant;
import uk.gov.dhsc.htbhf.claimant.entity.PaymentCycle;
import uk.gov.dhsc.htbhf.claimant.model.eligibility.EligibilityAndEntitlementDecision;
import uk.gov.dhsc.htbhf.claimant.model.eligibility.EligibilityResponse;
import uk.gov.dhsc.htbhf.claimant.repository.ClaimRepository;
import uk.gov.dhsc.htbhf.eligibility.model.EligibilityStatus;

import java.time.LocalDate;
import java.util.Optional;

import static uk.gov.dhsc.htbhf.claimant.model.eligibility.EligibilityAndEntitlementDecision.buildWithStatus;

@Service
@AllArgsConstructor
public class EligibilityService {

    private final EligibilityClient client;
    private final EligibilityStatusCalculator eligibilityStatusCalculator;
    private final ClaimRepository claimRepository;
    private final CycleEntitlementCalculator cycleEntitlementCalculator;

    /**
     * Determines the eligibility for the given new claimant. If the claimant's NINO is not found in the database,
     * the external eligibility service is called.
     * Claimants determined to be eligible must still either be pregnant or have children under 4,
     * otherwise they will be ineligible.
     *
     * @param claimant the claimant to check the eligibility for
     * @return the eligibility and entitlement for the claimant
     */
    public EligibilityAndEntitlementDecision determineEligibilityAndEntitlementForNewClaimant(Claimant claimant) {
        if (claimRepository.liveClaimExistsForNino(claimant.getNino())) {
            return buildWithStatus(EligibilityStatus.DUPLICATE);
        }
        EligibilityResponse eligibilityResponse = checkEligibilityForNewClaimant(claimant);
        PaymentCycleVoucherEntitlement entitlement = cycleEntitlementCalculator.calculateEntitlement(
                Optional.ofNullable(claimant.getExpectedDeliveryDate()),
                eligibilityResponse.getDateOfBirthOfChildren(),
                LocalDate.now());
        return buildDecision(eligibilityResponse, entitlement);
    }

    /**
     * Determines the eligibility for the given existing claimant. No check is made on the NINO as they already exist in the
     * database. The eligibility status is checked by calling the external service.
     * Claimants determined to be eligible must still either be pregnant or have children under 4,
     * otherwise they will be ineligible.
     *
     * @param claimant the claimant to check the eligibility for
     * @return the eligibility and entitlement for the claimant
     */
    public EligibilityAndEntitlementDecision determineEligibilityAndEntitlementForExistingClaimant(
            Claimant claimant,
            LocalDate cycleStartDate,
            PaymentCycle previousCycle) {
        EligibilityResponse eligibilityResponse = client.checkEligibility(claimant);
        PaymentCycleVoucherEntitlement entitlement = cycleEntitlementCalculator.calculateEntitlement(
                Optional.ofNullable(claimant.getExpectedDeliveryDate()),
                eligibilityResponse.getDateOfBirthOfChildren(),
                cycleStartDate,
                previousCycle.getVoucherEntitlement());
        return buildDecision(eligibilityResponse, entitlement);
    }

    private EligibilityResponse checkEligibilityForNewClaimant(Claimant claimant) {
        EligibilityResponse eligibilityResponse = client.checkEligibility(claimant);
        EligibilityStatus eligibilityStatus = eligibilityStatusCalculator.determineEligibilityStatusForNewClaim(eligibilityResponse);
        return eligibilityResponse.toBuilder()
                .eligibilityStatus(eligibilityStatus)
                .build();
    }

    private EligibilityAndEntitlementDecision buildDecision(EligibilityResponse eligibilityResponse, PaymentCycleVoucherEntitlement entitlement) {
        EligibilityStatus eligibilityStatus = determineEligibilityStatus(eligibilityResponse, entitlement);
        return EligibilityAndEntitlementDecision.builder()
                .eligibilityStatus(eligibilityStatus)
                .voucherEntitlement(entitlement)
                .dateOfBirthOfChildren(eligibilityResponse.getDateOfBirthOfChildren())
                .dwpHouseholdIdentifier(eligibilityResponse.getDwpHouseholdIdentifier())
                .hmrcHouseholdIdentifier(eligibilityResponse.getHmrcHouseholdIdentifier())
                .build();
    }

    private EligibilityStatus determineEligibilityStatus(EligibilityResponse response, PaymentCycleVoucherEntitlement voucherEntitlement) {
        if (response.getEligibilityStatus() == EligibilityStatus.ELIGIBLE && voucherEntitlement.getTotalVoucherEntitlement() == 0) {
            return EligibilityStatus.INELIGIBLE;
        }
        return response.getEligibilityStatus();
    }

}
