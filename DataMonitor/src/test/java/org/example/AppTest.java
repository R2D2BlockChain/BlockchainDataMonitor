package org.example;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AppTest {

    @Test
    void shouldLoadApplicationClass() {
        assertTrue(App.class.getName().contains("App"));
    }
}
