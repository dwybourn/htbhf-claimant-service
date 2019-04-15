package uk.gov.dhsc.htbhf.claimant.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import uk.gov.dhsc.htbhf.claimant.exception.CardClientException;
import uk.gov.dhsc.htbhf.claimant.model.card.CardRequest;
import uk.gov.dhsc.htbhf.claimant.model.card.CardResponse;

/**
 * Service for interacting with the card services api..
 */
@Service
@Slf4j
public class CardClient {

    private static final String CARDS_ENDPOINT = "/v1/cards";
    private final String cardsUri;
    private final RestTemplate restTemplate;

    /**
     * Uses a non request scope rest template due to being outside the context of a request.
     *
     * @param baseUri      base url of the card services api.
     * @param restTemplate non request scope rest template
     */
    public CardClient(@Value("${card.base-uri}") String baseUri,
                      @Qualifier("schedulerRestTemplate") RestTemplate restTemplate) {
        this.cardsUri = baseUri + CARDS_ENDPOINT;
        this.restTemplate = restTemplate;
    }

    public CardResponse createNewCardRequest(CardRequest cardRequest) {
        try {
            ResponseEntity<CardResponse> response = restTemplate.postForEntity(cardsUri, cardRequest, CardResponse.class);
            if (response.getStatusCode() != HttpStatus.CREATED) {
                throw new CardClientException(response.getStatusCode());
            }

            return response.getBody();
        } catch (RestClientException e) {
            log.error("Exception caught trying to post to {}", cardsUri);
            throw new CardClientException(e, cardsUri);
        }
    }
}
