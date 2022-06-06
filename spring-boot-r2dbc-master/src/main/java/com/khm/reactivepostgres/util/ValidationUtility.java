package com.khm.reactivepostgres.util;

import com.sun.javafx.binding.StringConstant;
import lombok.experimental.UtilityClass;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collector;

@UtilityClass
public class ValidationUtility {
    private final Pattern pattern = Pattern.compile("[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+");

    public List<String> convertEmails(String email) {
        Matcher matcher = pattern.matcher(email);
        List<String> list = new ArrayList<>();
        while (matcher.find()) {
            String emailMatcher = matcher.group();
            list.add(emailMatcher);
            email = email.replace(emailMatcher, "");
        }
        return list;
    }
}
