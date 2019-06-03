package uk.gov.dhsc.htbhf.claimant.service.audit;

import lombok.Builder;
import uk.gov.dhsc.htbhf.logging.event.Event;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static uk.gov.dhsc.htbhf.claimant.service.audit.ClaimEventMetadataKey.*;


public class MakePaymentEvent extends Event {

    @Builder
    public MakePaymentEvent(UUID claimId, UUID paymentId, String reference, Integer paymentAmountInPence, Integer entitlementAmountInPence) {
        super(ClaimEventType.MAKE_PAYMENT,
                LocalDateTime.now(),
                constructMetaData(claimId, paymentId, reference, paymentAmountInPence, entitlementAmountInPence));
    }

    private static Map<String, Object> constructMetaData(UUID claimId,
                                                         UUID paymentId,
                                                         String reference,
                                                         Integer paymentAmountInPence,
                                                         Integer entitlementAmountInPence) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put(CLAIM_ID.getKey(), claimId);
        metadata.put(ENTITLEMENT_AMOUNT_IN_PENCE.getKey(), entitlementAmountInPence);
        metadata.put(PAYMENT_AMOUNT.getKey(), paymentAmountInPence);
        metadata.put(PAYMENT_ID.getKey(), paymentId);
        metadata.put(PAYMENT_REFERENCE.getKey(), reference);
        return metadata;
    }
}
