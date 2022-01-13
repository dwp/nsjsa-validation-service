package uk.gov.dwp.jsa.validation.service.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class HouseNumberFactory {

    private static final Pattern HOUSE_NUMBER_PATTERN = Pattern.compile("\\d+");

    public Optional<String> create(final String addressLine1) {

        if (StringUtils.isBlank(addressLine1)) {
            return Optional.empty();
        }

        final Matcher houseNumberMatcher = HOUSE_NUMBER_PATTERN.matcher(addressLine1);
        if (houseNumberMatcher.find()) {
            return Optional.of(houseNumberMatcher.group());
        }
        return Optional.empty();
    }
}

