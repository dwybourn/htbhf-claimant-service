package uk.gov.dhsc.htbhf.claimant.testsupport;

import uk.gov.dhsc.htbhf.claimant.entity.Claim;
import uk.gov.dhsc.htbhf.claimant.message.context.ReportClaimMessageContext;
import uk.gov.dhsc.htbhf.claimant.model.UpdatableClaimantField;

import java.time.LocalDateTime;
import java.util.List;

import static uk.gov.dhsc.htbhf.claimant.model.UpdatableClaimantField.FIRST_NAME;
import static uk.gov.dhsc.htbhf.claimant.model.UpdatableClaimantField.LAST_NAME;
import static uk.gov.dhsc.htbhf.claimant.reporting.ClaimAction.NEW;
import static uk.gov.dhsc.htbhf.claimant.testsupport.ClaimTestDataFactory.aValidClaim;
import static uk.gov.dhsc.htbhf.claimant.testsupport.TestConstants.SINGLE_THREE_YEAR_OLD;

public class ReportClaimMessageContextTestDataFactory {

    public static ReportClaimMessageContext aReportClaimMessageContextWithClaimAndUpdatedFields(Claim claim, List<UpdatableClaimantField> updatedFields) {
        return aReportClaimMessageContextBuilder()
                .updatedClaimFields(updatedFields)
                .claim(claim)
                .build();
    }

    // Without <?, ?> at the end of ReportClaimMessageContextBuilder, an instance of ReportEventMessageContextBuilder is returned instead
    // of a ReportClaimMessageContextBuilder, this is due to how lombok implements it's SuperBuilder.
    private static ReportClaimMessageContext.ReportClaimMessageContextBuilder<?, ?> aReportClaimMessageContextBuilder() {
        return ReportClaimMessageContext.builder()
                .claim(aValidClaim())
                .updatedClaimFields(List.of(FIRST_NAME, LAST_NAME))
                .timestamp(LocalDateTime.now())
                .datesOfBirthOfChildren(SINGLE_THREE_YEAR_OLD)
                .claimAction(NEW);
    }
}