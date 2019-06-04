package uk.gov.dhsc.htbhf.claimant.entity;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.dhsc.htbhf.assertions.AbstractValidationTest;

import java.util.Set;
import java.util.UUID;
import javax.validation.ConstraintViolation;

import static uk.gov.dhsc.htbhf.assertions.ConstraintViolationAssert.assertThat;
import static uk.gov.dhsc.htbhf.claimant.testsupport.ClaimTestDataFactory.*;
import static uk.gov.dhsc.htbhf.claimant.testsupport.ClaimantTestDataFactory.aClaimantWithLastName;

class ClaimTest extends AbstractValidationTest {

    @Test
    void shouldValidateClaimSuccessfully() {
        //Given
        Claim claim = aValidClaim();
        //When
        Set<ConstraintViolation<Claim>> violations = validator.validate(claim);
        //Then
        assertThat(violations).hasNoViolations();
    }

    @Test
    void shouldFailToValidateClaimWithoutClaimStatus() {
        //Given
        Claim claim = aClaimWithClaimStatus(null);
        //When
        Set<ConstraintViolation<Claim>> violations = validator.validate(claim);
        //Then
        assertThat(violations).hasSingleConstraintViolation("must not be null", "claimStatus");
    }

    @Test
    void shouldFailToValidateClaimWithoutEligibilityStatus() {
        //Given
        Claim claim = aClaimWithEligibilityStatus(null);
        //When
        Set<ConstraintViolation<Claim>> violations = validator.validate(claim);
        //Then
        assertThat(violations).hasSingleConstraintViolation("must not be null", "eligibilityStatus");
    }

    @Test
    void shouldFailToValidateClaimWithoutClaimant() {
        //Given
        Claim claim = aClaimWithClaimant(null);
        //When
        Set<ConstraintViolation<Claim>> violations = validator.validate(claim);
        //Then
        assertThat(violations).hasSingleConstraintViolation("must not be null", "claimant");
    }

    @Test
    void shouldFailToValidateClaimWithInvalidClaimant() {
        //Given
        Claimant invalidClaimant = aClaimantWithLastName(null);
        Claim claim = aClaimWithClaimant(invalidClaimant);
        //When
        Set<ConstraintViolation<Claim>> violations = validator.validate(claim);
        //Then
        assertThat(violations).hasSingleConstraintViolation("must not be null", "claimant.lastName");
    }

    @Test
    void shouldAlwaysReturnAnIdFromGetId() {
        //Given
        Claim claim = Claim.builder().build();
        //When
        UUID id = claim.getId();
        //Then
        Assertions.assertThat(id).isNotNull();
    }

    @Test
    void shouldReturnTheSameIdIfOneIsSet() {
        //Given
        UUID id = UUID.randomUUID();
        //When
        Claim claim = Claim.builder().build();
        ReflectionTestUtils.setField(claim, "id", id);
        //Then
        Assertions.assertThat(id).isEqualTo(claim.getId());
    }
}