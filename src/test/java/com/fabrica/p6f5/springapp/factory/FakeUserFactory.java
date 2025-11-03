package com.fabrica.p6f5.springapp.factory;

import com.fabrica.p6f5.springapp.user.model.User;
import net.datafaker.Faker;

public class FakeUserFactory {

    private final Faker faker = new Faker();

    public User createFakeUser() {
        return User.builder()
                .id(faker.number().randomNumber())
                .username(faker.name().username())
                .email(faker.internet().emailAddress())
                .fullName(faker.name().fullName())
                .password(faker.internet().password(8, 16))
                .isActive(true)
                .build();
    }

}
