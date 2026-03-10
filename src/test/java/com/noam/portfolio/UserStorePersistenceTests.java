package com.noam.portfolio;

import com.example.portfolio.Portfolio;
import com.example.portfolio.PortfolioApplication;
import com.example.portfolio.User;
import com.example.portfolio.UserStore;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = PortfolioApplication.class)
class UserStorePersistenceTests {

    @Autowired
    private UserStore userStore;

    @Test
    void seededUsersAuthenticateAgainstDatabase() {
        User demo = userStore.authenticate("demo@demo.com", "demo123");
        User dbCheck = userStore.authenticate("dbcheck@demo.com", "dbcheck123");

        assertNotNull(demo);
        assertNotNull(dbCheck);
        assertEquals("dbcheck@demo.com", userStore.getEmail(dbCheck.getId()));

        Portfolio portfolio = userStore.getPortfolio(dbCheck.getId());
        assertNotNull(portfolio);
        assertFalse(portfolio.getAccounts().isEmpty());
    }

    @Test
    void registerPersistsAndRejectsDuplicateEmail() {
        String email = "user-" + UUID.randomUUID() + "@example.com";

        User created = userStore.register("Persistence Test", email.toUpperCase(), "secret123");

        assertNotNull(created);
        assertTrue(userStore.emailExists(email));
        assertNotNull(userStore.authenticate(email, "secret123"));
        assertNull(userStore.register("Duplicate", email, "secret123"));
    }
}

