package uk.gov.dwp.jsa.validation.service.util;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Optional;
import java.util.stream.Stream;

public class MatchingAddress {

    private final Optional<String> houseNumber;
    private final String postcode;

    public MatchingAddress(final Optional<String> houseNumber, final String postcode) {
        this.houseNumber = houseNumber;
        this.postcode = postcode;
    }

    public boolean matches(final MatchingAddress otherMatchingAddress) {
        final Optional<String> otherHouseNumber = otherMatchingAddress.houseNumber;
        if (houseNumber.isPresent() && otherHouseNumber.isPresent()) {
            return houseNumber.get().equals(otherHouseNumber.get()) && postcodeMatches(otherMatchingAddress);
        } else if (Stream.of(houseNumber, otherHouseNumber).anyMatch(Optional::isPresent)) {
            return false;
        }
        return postcodeMatches(otherMatchingAddress);
    }

    private boolean postcodeMatches(final MatchingAddress otherMatchingAddress) {
        return postcode.equals(otherMatchingAddress.postcode);
    }

    @Override
    public boolean equals(final Object o) {
        return EqualsBuilder.reflectionEquals(this, o);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }
}

