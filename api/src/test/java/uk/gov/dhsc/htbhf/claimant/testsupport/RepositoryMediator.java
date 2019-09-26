package uk.gov.dhsc.htbhf.claimant.testsupport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.dhsc.htbhf.claimant.entity.Claim;
import uk.gov.dhsc.htbhf.claimant.entity.PaymentCycle;
import uk.gov.dhsc.htbhf.claimant.entity.PaymentCycleStatus;
import uk.gov.dhsc.htbhf.claimant.repository.ClaimRepository;
import uk.gov.dhsc.htbhf.claimant.repository.MessageRepository;
import uk.gov.dhsc.htbhf.claimant.repository.PaymentCycleRepository;
import uk.gov.dhsc.htbhf.claimant.repository.PaymentRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import javax.transaction.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.dhsc.htbhf.claimant.testsupport.PaymentCycleVoucherEntitlementTestDataFactory.aPaymentCycleVoucherEntitlement;

/**
 * Mediates between test case and the repositories to create & persist entities, and ensure that loaded entities are fully initialised.
 */
@Component
public class RepositoryMediator {

    @Autowired
    private ClaimRepository claimRepository;
    @Autowired
    private PaymentCycleRepository paymentCycleRepository;
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private MessageRepository messageRepository;

    /**
     * Asserts that there is a current PaymentCycle, and initialises the collections of the returned PaymentCycle.
     * @param claim the claim to get the cycle for.
     * @return the current PaymentCycle.
     */
    @Transactional
    public PaymentCycle getCurrentPaymentCycleForClaim(Claim claim) {
        Optional<PaymentCycle> cycleOptional = paymentCycleRepository.findCurrentCycleForClaim(claim);
        assertThat(cycleOptional.isPresent()).isTrue();
        PaymentCycle paymentCycle = cycleOptional.get();
        // ensure that everything has been loaded
        paymentCycle.getPayments().size();
        paymentCycle.getClaim().toString();
        return paymentCycle;
    }

    /**
     * Invokes deleteAll on all repositories we have access to.
     */
    public void deleteAllEntities() {
        messageRepository.deleteAll();
        paymentRepository.deleteAll();
        paymentCycleRepository.deleteAll();
        claimRepository.deleteAll();
    }

    /**
     * Ensures the given claim is persistent, then creates and persists a PaymentCycle for the given start date.
     * The payment cycle is created in a completed state (the payment having been made) based on the dates of birth,
     * the expected due date of the claimant and an assumption that they are eligible.
     * @param claim the claim to create a paymentCycle for.
     * @param cycleStartDate the start date of the cycle.
     * @param childrensDatesOfBirth dates of birth of children.
     * @return the persistent PaymentCycle.
     */
    public PaymentCycle createAndSavePaymentCycle(Claim claim, LocalDate cycleStartDate, List<LocalDate> childrensDatesOfBirth) {
        claimRepository.save(claim);
        PaymentCycle completedPaymentCycle = PaymentCycleTestDataFactory.aValidPaymentCycleBuilder()
                .claim(claim)
                .childrenDob(childrensDatesOfBirth)
                .cycleStartDate(cycleStartDate)
                .cycleEndDate(cycleStartDate.plusDays(27))
                .paymentCycleStatus(PaymentCycleStatus.FULL_PAYMENT_MADE)
                .voucherEntitlement(aPaymentCycleVoucherEntitlement(cycleStartDate, childrensDatesOfBirth, claim.getClaimant().getExpectedDeliveryDate()))
                .build();
        paymentCycleRepository.save(completedPaymentCycle);
        return completedPaymentCycle;
    }

}
