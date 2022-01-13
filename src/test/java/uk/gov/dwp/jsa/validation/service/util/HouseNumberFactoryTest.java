package uk.gov.dwp.jsa.validation.service.util;

import org.junit.Test;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class HouseNumberFactoryTest {

    private HouseNumberFactory factory;
    private Optional<String> houseNumber;

    @Test
    public void createsHouseNumber() {
        givenAFactory();
        whenICreateWith("25 High Street");
        thenTheHouseNumberIs("25");
    }

    @Test
    public void createsEmptyHouseNumberIfNonSpecified() {
        givenAFactory();
        whenICreateWith("High Street");
        thenTheHouseNumberIsEmpty();
    }

    @Test
    public void createsEmptyHouseNumberFortEmptyString() {
        givenAFactory();
        whenICreateWith("");
        thenTheHouseNumberIsEmpty();
    }

    @Test
    public void createsEmptyHouseNumberFortNullString() {
        givenAFactory();
        whenICreateWith(null);
        thenTheHouseNumberIsEmpty();
    }

    @Test
    public void createsHouseNumberFromFlatNumberNotHouseNumber() {
        givenAFactory();
        whenICreateWith("Flat 1, 25 High Street");
        thenTheHouseNumberIs("1");
    }

    @Test
    public void createsHouseNumberFromFlatNumber() {
        givenAFactory();
        whenICreateWith("Flat 1, High Street");
        thenTheHouseNumberIs("1");
    }

    private void givenAFactory() {
        factory = new HouseNumberFactory();
    }

    private void whenICreateWith(final String addressLine1) {
        houseNumber = factory.create(addressLine1);
    }

    private void thenTheHouseNumberIsEmpty() {
        assertThat(houseNumber, is(Optional.empty()));
    }

    private void thenTheHouseNumberIs(final String expectedHouseNumber) {
        assertThat(houseNumber.get(), is(expectedHouseNumber));
    }


}
