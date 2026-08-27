package com.example.mini_project_ss14.mcp.validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SafeSqlValidatorTest {

    private SafeSqlValidator validator;

    @BeforeEach
    void setUp() {
        validator = new SafeSqlValidator();
    }

    @Test
    void shouldAllowSelectFromDeliveries() {
        assertDoesNotThrow(() ->
                validator.validate(
                        "SELECT * FROM deliveries"
                )
        );
    }

    @Test
    void shouldAllowSelectFromIncidents() {
        assertDoesNotThrow(() ->
                validator.validate(
                        "SELECT * FROM incidents"
                )
        );
    }

    @Test
    void shouldAllowJoinBetweenAllowedTables() {
        assertDoesNotThrow(() ->
                validator.validate("""
                        SELECT d.id, d.status, i.incident_type
                        FROM deliveries d
                        JOIN incidents i
                            ON i.delivery_id = d.id
                        """)
        );
    }

    @Test
    void shouldRejectInsert() {
        assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate(
                        "INSERT INTO deliveries VALUES (1)"
                )
        );
    }

    @Test
    void shouldRejectUpdate() {
        assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate(
                        "UPDATE deliveries SET status = 'DELIVERED'"
                )
        );
    }

    @Test
    void shouldRejectDelete() {
        assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate(
                        "DELETE FROM deliveries"
                )
        );
    }

    @Test
    void shouldRejectDrop() {
        assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate(
                        "DROP TABLE deliveries"
                )
        );
    }

    @Test
    void shouldRejectUnauthorizedTable() {
        assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate(
                        "SELECT * FROM users"
                )
        );
    }

    @Test
    void shouldRejectMultipleStatements() {
        assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate(
                        "SELECT * FROM deliveries; DROP TABLE deliveries"
                )
        );
    }

    @Test
    void shouldRejectEmptySql() {
        assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate("")
        );
    }

    @Test
    void shouldAllowDangerousWordsInsideStringValues() {
        assertDoesNotThrow(() ->
                validator.validate(
                        "SELECT * FROM deliveries " +
                                "WHERE status = 'DELETE'"
                )
        );
    }
}
