package uk.gov.dwp.jsa.validation.service.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(value = Parameterized.class)
public class MatchingAddressAcceptanceCriteriaTest {

    private final MatchingAddressFactory factory = new MatchingAddressFactory(new HouseNumberFactory());

    private final String addressLine1;
    private final String postcode;
    private final String otherAddressLine1;
    private final String otherPostcode;
    private final boolean matchesHouseNumber;


    public MatchingAddressAcceptanceCriteriaTest(
            final String addressLine1,
            final String postcode,
            final String otherAddressLine1,
            final String otherPostcode,
            final boolean matchesHouseNumber) {
        this.addressLine1 = addressLine1;
        this.postcode = postcode;
        this.otherAddressLine1 = otherAddressLine1;
        this.otherPostcode = otherPostcode;
        this.matchesHouseNumber = matchesHouseNumber;
    }

    @Parameterized.Parameters(name = "{index}: address({0}, {1}) == otherAddress({2}, {3}) = {4}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"Flat 1, High Street", "AA12 1AB", "Flat 2, High Street", "AA12 1AB", false},
                {"1 High Street", "AA12 1AB", "Flat 1 High Street", "AA12 1AB", true},
                {"1 High Street", "AA12 1AB", "The Willows, High Street", "AA12 1AB", false},
                {"The Willows", "AA12 1AB", "1 High Street", "AA12 1AB", false},
                {"The Willows", "AA12 1AB", "The Willows", "AA12 1AB", true},
                {"The Willows", "AA12 1AB", "The Willows", "AA77 1AB", false},
                {"Flat 1, 22 High Street", "AA12 1AB", "Flat 1, 23 High Street", "AA12 1AB", true}
        });
    }

    @Test
    public void matchesHouseNumber() {
        final MatchingAddress address = factory.create(addressLine1, postcode);
        final MatchingAddress otherAddress = factory.create(otherAddressLine1, otherPostcode);
        assertThat(address.matches(otherAddress), is(matchesHouseNumber));
    }

}
