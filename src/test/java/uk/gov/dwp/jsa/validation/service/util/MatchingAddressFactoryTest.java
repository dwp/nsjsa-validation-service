package uk.gov.dwp.jsa.validation.service.util;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class MatchingAddressFactoryTest {

    private static final String ADDRESS_LINE1 = "ADDRESS_LINE1";
    private static final String HOUSE_NUMBER = "HOUSE_NUMBER";
    private static final String POST_CODE = "POST_CODE";

    @Mock
    private HouseNumberFactory houseNumberFactory;

    private MatchingAddressFactory matchingAddressFactory;
    private MatchingAddress matchingAddress;

    @Before
    public void beforeEachTest() {
        initMocks(this);
    }

    @Test
    public void createsMatchingAddress() {
        givenAFactory();
        whenICallCreateWith(ADDRESS_LINE1, POST_CODE);
        thenTheMatchingAddressIsCreated();

    }

    private void givenAFactory() {
        matchingAddressFactory = new MatchingAddressFactory(houseNumberFactory);
        when(houseNumberFactory.create(ADDRESS_LINE1)).thenReturn(Optional.of(HOUSE_NUMBER));
    }

    private void whenICallCreateWith(final String addressLine1, final String postCode) {
        matchingAddress = matchingAddressFactory.create(addressLine1, postCode);
    }

    private void thenTheMatchingAddressIsCreated() {
        assertThat(matchingAddress, is(new MatchingAddress(Optional.of(HOUSE_NUMBER), POST_CODE)));
    }
}
