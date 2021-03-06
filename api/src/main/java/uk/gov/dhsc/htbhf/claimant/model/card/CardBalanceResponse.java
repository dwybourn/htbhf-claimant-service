package uk.gov.dhsc.htbhf.claimant.model.card;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor(onConstructor_ = {@JsonCreator})
public class CardBalanceResponse {

    @JsonProperty("availableBalanceInPence")
    private Integer availableBalanceInPence;

    @JsonProperty("ledgerBalanceInPence")
    private Integer ledgerBalanceInPence;
}
