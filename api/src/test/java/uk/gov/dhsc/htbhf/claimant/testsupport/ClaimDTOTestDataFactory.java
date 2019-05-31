package uk.gov.dhsc.htbhf.claimant.testsupport;

import uk.gov.dhsc.htbhf.claimant.model.AddressDTO;
import uk.gov.dhsc.htbhf.claimant.model.ClaimDTO;
import uk.gov.dhsc.htbhf.claimant.model.ClaimantDTO;

import java.time.LocalDate;

import static java.time.LocalDate.now;

public final class ClaimDTOTestDataFactory {

    private static final String VALID_NINO = "EB123456C";
    private static final String VALID_FIRST_NAME = "James";
    private static final String VALID_LAST_NAME = "Smith";
    private static final LocalDate VALID_DOB = LocalDate.parse("1985-12-31");
    public static final String VALID_ADDRESS_LINE_1 = "Flat b";
    public static final String VALID_ADDRESS_LINE_2 = "123 Fake street";
    public static final String VALID_TOWN_OR_CITY = "Springfield";
    public static final String VALID_POSTCODE = "AA1 1AA";

    public static ClaimDTO aValidClaimDTO() {
        return ClaimDTO.builder()
                .claimant(aValidClaimantBuilder().build())
                .build();
    }

    public static ClaimDTO aValidClaimDTOWithExpectedDeliveryDate(LocalDate expectedDeliveryDate) {
        return ClaimDTO.builder()
                .claimant(aValidClaimantBuilder()
                        .expectedDeliveryDate(expectedDeliveryDate)
                        .build())
                .build();
    }

    public static ClaimDTO aValidClaimDTOWithNoNullFields() {
        return ClaimDTO.builder()
                .claimant(aValidClaimantDTOWithNoNullFields())
                .build();
    }

    public static ClaimantDTO aValidClaimantDTOWithNoNullFields() {
        return aValidClaimantBuilder()
                .expectedDeliveryDate(now().plusMonths(4))
                .build();
    }



    private static ClaimantDTO.ClaimantDTOBuilder aValidClaimantBuilder() {
        return ClaimantDTO.builder()
                .firstName(VALID_FIRST_NAME)
                .lastName(VALID_LAST_NAME)
                .nino(VALID_NINO)
                .dateOfBirth(VALID_DOB)
                .address(aValidAddressBuilder().build())
                .expectedDeliveryDate(now().plusMonths(1));
    }

    private static AddressDTO.AddressDTOBuilder aValidAddressBuilder() {
        return AddressDTO.builder()
                .addressLine1(VALID_ADDRESS_LINE_1)
                .addressLine2(VALID_ADDRESS_LINE_2)
                .townOrCity(VALID_TOWN_OR_CITY)
                .postcode(VALID_POSTCODE);
    }

}
