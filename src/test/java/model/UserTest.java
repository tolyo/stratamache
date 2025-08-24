package model;

import static org.junit.jupiter.api.Assertions.*;

import db.Db;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class UserTest {

  @BeforeAll
  static void setUp() {
    Db.init();
    Db.execute("DELETE FROM users"); // clear users table on start
  }

  @AfterAll
  static void tearDown() {
    Db.close();
  }

  @Test
  void testQueryValNoResult() {
    Optional<?> result =
        Db.queryVal(String.class, "SELECT username FROM users WHERE username = 'test'");

    assertTrue(result.isEmpty());
  }

  @Test
  void testInsert() {
    UUID userId = User.create("alice", "alice@example.com", "super_secret");
    assertNotNull(userId);
    assertNotNull(userId.toString());
  }
}
