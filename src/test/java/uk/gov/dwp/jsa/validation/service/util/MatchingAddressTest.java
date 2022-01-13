package uk.gov.dwp.jsa.validation.service.util;

import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class MatchingAddressTest {
    private static final String POST_CODE = "POST_CODE";
    private static final String HOUSE_NUMBER = "HOUSE_NUMBER";
    private static final String OTHER_POST_CODE = "OTHER_POST_CODE";

    private MatchingAddress matchingAddress;
    private boolean matches;

    @Test
    public void constructorSetsFields() {
        givenAMatchingAddress(Optional.of(HOUSE_NUMBER), POST_CODE);
        thenTheFieldsAreSet();
    }

    @Test
    public void addressDoesNotMatchIfHouseNumberDifferent() {
        givenAMatchingAddress(Optional.of("21"), POST_CODE);
        whenICompareWith(new MatchingAddress(Optional.of("25"), POST_CODE));
        thenTheAddressDoesNotMatch();
    }

    @Test
    public void addressDoesNotMatchIfOnlyCompareAddressHasHouseNumber() {
        givenAMatchingAddress(Optional.empty(), POST_CODE);
        whenICompareWith(new MatchingAddress(Optional.of("21"), POST_CODE));
        thenTheAddressDoesNotMatch();
    }

    @Test
    public void addressDoesNotMatchIfOnlyOriginalAddressHasHouseNumber() {
        givenAMatchingAddress(Optional.of("21"), POST_CODE);
        whenICompareWith(new MatchingAddress(Optional.empty(), POST_CODE));
        thenTheAddressDoesNotMatch();
    }

    @Test
    public void addressMatchesIfNeitherAddressHasHouseNumber() {
        givenAMatchingAddress(Optional.empty(), POST_CODE);
        whenICompareWith(new MatchingAddress(Optional.empty(), POST_CODE));
        thenTheAddressMatches();
    }

    @Test
    public void addressMatchesIfHouseNumberAndPostCodeSame() {
        givenAMatchingAddress(Optional.of("21"), POST_CODE);
        whenICompareWith(new MatchingAddress(Optional.of("21"), POST_CODE));
        thenTheAddressMatches();
    }

    @Test
    public void addressDoesNotMatchIfHouseNumbersAreSameButPostCodeIsDifferent() {
        givenAMatchingAddress(Optional.of("21"), POST_CODE);
        whenICompareWith(new MatchingAddress(Optional.of("21"), OTHER_POST_CODE));
        thenTheAddressDoesNotMatch();
    }

    private void givenAMatchingAddress(final Optional<String> houseNumber, final String postcode) {
        matchingAddress = new MatchingAddress(houseNumber, postcode);
    }

    private void whenICompareWith(final MatchingAddress otherAddressLine1) {
        matches = matchingAddress.matches(otherAddressLine1);
    }

    private void thenTheAddressMatches() {
        assertThat(matches, is(true));
    }

    private void thenTheAddressDoesNotMatch() {
        assertThat(matches, is(false));
    }

    private void thenTheFieldsAreSet() {
        final Optional<String> houseNumber = (Optional<String>) ReflectionTestUtils.getField(matchingAddress, "houseNumber");
        final String postcode = (String) ReflectionTestUtils.getField(matchingAddress, "postcode");
        assertThat(houseNumber.get(), is(HOUSE_NUMBER));
        assertThat(postcode, is(POST_CODE));
    }

}
