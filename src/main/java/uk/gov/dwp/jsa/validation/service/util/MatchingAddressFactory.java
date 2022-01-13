package uk.gov.dwp.jsa.validation.service.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MatchingAddressFactory {

    private final HouseNumberFactory houseNumberFactory;

    @Autowired
    public MatchingAddressFactory(final HouseNumberFactory houseNumberFactory) {
        this.houseNumberFactory = houseNumberFactory;
    }

    public MatchingAddress create(final String line1, final String postcode) {
        return new MatchingAddress(houseNumberFactory.create(line1), postcode);
    }

}

