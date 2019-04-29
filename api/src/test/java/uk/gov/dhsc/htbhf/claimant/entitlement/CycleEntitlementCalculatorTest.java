package uk.gov.dhsc.htbhf.claimant.entitlement;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.dhsc.htbhf.claimant.entity.Claimant;

import java.time.LocalDate;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static uk.gov.dhsc.htbhf.claimant.testsupport.ClaimantTestDataFactory.aValidClaimant;

@ExtendWith(MockitoExtension.class)
class CycleEntitlementCalculatorTest {

    private static final int PAYMENT_CYCLE_DURATION_IN_DAYS = 3;
    private static final int NUMBER_OF_CALCULATION_PERIODS = 3;

    private EntitlementCalculator entitlementCalculator = mock(EntitlementCalculator.class);

    private CycleEntitlementCalculator cycleEntitlementCalculator
            = new CycleEntitlementCalculator(PAYMENT_CYCLE_DURATION_IN_DAYS, NUMBER_OF_CALCULATION_PERIODS, entitlementCalculator);

    @Test
    void shouldThrowExceptionWhenDurationIsNotDivisibleByNumberOfCalculationPeriods() {
        // 10 is not divisible by 3, so should throw an exception
        IllegalArgumentException thrown = catchThrowableOfType(
                () -> new CycleEntitlementCalculator(10, 3, entitlementCalculator), IllegalArgumentException.class);

        assertThat(thrown.getMessage()).isEqualTo("Payment cycle duration of 10 days is not divisible by number of calculation periods 3");
    }

    @Test
    void shouldThrowExceptionWhenDurationIsZero() {
        // 10 is not divisible by 3, so should throw an exception
        IllegalArgumentException thrown = catchThrowableOfType(
                () -> new CycleEntitlementCalculator(0, 1, entitlementCalculator), IllegalArgumentException.class);

        assertThat(thrown.getMessage()).isEqualTo("Payment cycle duration can not be zero");
    }

    @Test
    void shouldThrowExceptionWhenNumberOrCalculationPeriodsIsZero() {
        // 10 is not divisible by 3, so should throw an exception
        IllegalArgumentException thrown = catchThrowableOfType(
                () -> new CycleEntitlementCalculator(1, 0, entitlementCalculator), IllegalArgumentException.class);

        assertThat(thrown.getMessage()).isEqualTo("Number of calculation periods can not be zero");
    }

    @Test
    void shouldCallEntitlementCalculatorForEachEntitlementDate() {
        VoucherEntitlement voucherEntitlement = VoucherEntitlement.builder().voucherValueInPence(100).vouchersForChildrenUnderOne(1).build();
        given(entitlementCalculator.calculateVoucherEntitlement(any(), any(), any())).willReturn(voucherEntitlement);
        List<LocalDate> dateOfBirthsOfChildren = singletonList(LocalDate.now().minusMonths(6));
        Claimant claimant = aValidClaimant();

        Integer totalEntitlement = cycleEntitlementCalculator.calculateEntitlementInPence(claimant, dateOfBirthsOfChildren);

        // each voucher entitlement is worth 100, the cycle should contain three vouchers. 3 * 100 = 300
        assertThat(totalEntitlement).isEqualTo(300);
        verify(entitlementCalculator).calculateVoucherEntitlement(claimant, dateOfBirthsOfChildren, LocalDate.now());
        verify(entitlementCalculator).calculateVoucherEntitlement(claimant, dateOfBirthsOfChildren, LocalDate.now().plusDays(1));
        verify(entitlementCalculator).calculateVoucherEntitlement(claimant, dateOfBirthsOfChildren, LocalDate.now().plusDays(2));
    }
}
