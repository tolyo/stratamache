package model;

import db.Db;
import db.Model;
import java.util.UUID;

public class User implements Model {
  String id;
  String username;
  String email;
  Integer rating;

  public User() {}

  public User(String id, String username, String email, Integer rating) {
    this.id = id;
    this.username = username;
    this.email = email;
    this.rating = rating;
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public String getBaseQuery() {
    return """
        SELECT id, username, email, rating FROM users
      """;
  }

  public static UUID create(String username, String email, String password) {
    return Db.executeFunction(
        "SELECT register_user(?, ?, ?)", UUID.class, username, email, password);
  }
}
