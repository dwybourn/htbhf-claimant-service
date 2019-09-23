package uk.gov.dhsc.htbhf.claimant.message.context;

import lombok.Builder;
import lombok.Data;
import uk.gov.dhsc.htbhf.claimant.entity.Claim;
import uk.gov.dhsc.htbhf.claimant.entity.PaymentCycle;

@Data
@Builder
public class MakePaymentMessageContext {
    private Claim claim;
    private PaymentCycle paymentCycle;
    private String cardAccountId;
}