package db;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import util.EnvLoader;

public class Db {
  private static Connection conn;

  public static void init() {
    EnvLoader.loadFromFile("config/dev.env");

    String host = EnvLoader.get("POSTGRES_HOST");
    String port = EnvLoader.get("POSTGRES_PORT");
    String db = EnvLoader.get("POSTGRES_DB");
    String user = EnvLoader.get("POSTGRES_USER");
    String pass = EnvLoader.get("POSTGRES_PASSWORD");

    String url = "jdbc:postgresql://" + host + ":" + port + "/" + db;

    Properties props = new Properties();
    props.setProperty("user", user);
    props.setProperty("password", pass);

    try {
      conn = DriverManager.getConnection(url, props);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public static void close() {
    try {
      conn.close();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Executes a SQL query and maps the result to an instance of the specified type.
   *
   * <p>The method uses the provided SQL query, along with any optional arguments, to execute a
   * query on the database. It then maps the result set to an object of the specified type.
   *
   * <p>If the query result contains more entries than the fields in the specified type, additional
   * entries are ignored, allowing usage of '*' wildcards in SQL queries;
   *
   * @param type The class representing the type to which the query result should be mapped.
   * @param query The SQL query to be executed.
   * @param args Optional arguments to be used in the SQL query.
   * @param <T> The type to which the query result should be mapped.
   * @return An Optional containing the mapped object if the query result is not empty, or an empty
   *     Optional otherwise.
   * @throws RuntimeException If a database access error occurs.
   */
  public static <T> Optional<T> queryVal(Class<T> type, String query, Object... args) {
    try (PreparedStatement statement = conn.prepareStatement(query)) {
      setParameters(statement, args);
      try (ResultSet resultSet = statement.executeQuery()) {
        return resultSet.next()
            ? Optional.of(mapResultSetToType(resultSet, type))
            : Optional.empty();
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Executes a SQL query to select all records from the table corresponding to the given type and
   * returns the result as a list of objects of that type.
   *
   * @param <T> the type of the objects in the returned list
   * @param type the class type of the objects in the returned list
   * @return a list of objects of the specified type containing all records from the table
   */
  public static <T> List<T> queryList(Class<T> type) {
    return queryList(type, "SELECT * FROM " + getTableName(type));
  }

  /**
   * Executes a SQL query with the specified parameters and returns the result as a list of objects
   * of the given type.
   *
   * @param <T> the type of the objects in the returned list
   * @param type the class type of the objects in the returned list
   * @param query the SQL query to be executed
   * @param args the arguments to be set in the SQL query
   * @return a list of objects of the specified type containing the query results
   */
  public static <T> List<T> queryList(Class<T> type, String query, Object... args) {
    List<T> result = new ArrayList<>();
    try (PreparedStatement statement = conn.prepareStatement(query)) {
      setParameters(statement, args);
      try (ResultSet resultSet = statement.executeQuery()) {
        while (resultSet.next()) {
          result.add(mapResultSetToType(resultSet, type));
        }
      }
    } catch (SQLException e) {
      return result;
    }
    return result;
  }

  /**
   * Executes a SQL query with the specified parameters.
   *
   * @param query the SQL query to be executed
   * @param args the arguments to be set in the SQL query
   * @return true if the result is a ResultSet object; false if it is an update count or there are
   *     no results
   * @throws RuntimeException if a database access error occurs
   */
  public static boolean execute(String query, Object... args) {
    try {
      PreparedStatement statement = conn.prepareStatement(query);
      setParameters(statement, args);
      return statement.execute();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public static <T> T executeFunction(String sql, Class<T> type, Object... args) {
    try {
      PreparedStatement statement = conn.prepareStatement(sql);
      setParameters(statement, args);

      try (ResultSet rs = statement.executeQuery()) {
        if (rs.next()) {
          return rs.getObject(1, type);
        }
        return null;
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  private static <T> T mapResultSetToType(ResultSet resultSet, Class<T> type) {
    try {

      ResultSetMetaData metaData = resultSet.getMetaData();
      int columnCount = metaData.getColumnCount();
      String[] columnNames = new String[columnCount];
      for (int i = 1; i <= columnCount; i++) {
        columnNames[i - 1] = toCamelCase(metaData.getColumnName(i));
      }

      Map<String, Object> res = new HashMap<>();
      for (int i = 1; i <= columnCount; i++) {
        res.put(columnNames[i - 1], resultSet.getObject(i));
      }

      ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.configure(
          com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

      return objectMapper.convertValue(res, type);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Retrieves the count of records in the specified table.
   *
   * @param conn the database connection to use
   * @param tableName the name of the table for which to count the records
   * @return an {@link Optional} containing the count of records, or an empty {@link Optional} if
   *     the count could not be retrieved
   * @throws RuntimeException if a database access error occurs
   */
  public static Optional<Integer> getCount(Connection conn, String tableName) {
    return queryVal(Integer.class, "SELECT COUNT(*) FROM " + tableName);
  }

  /**
   * Deletes all records from the specified table.
   *
   * @param tableName the name of the table from which to delete all records
   * @throws RuntimeException if a database access error occurs
   */
  public static void deleteAll(String tableName) {
    try (PreparedStatement statement = conn.prepareStatement("DELETE FROM " + tableName)) {
      statement.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Creates a new record in the database for the given model object and returns the generated ID.
   *
   * @param model the model object to be inserted into the database
   * @return the generated ID of the newly created record
   * @throws RuntimeException if a database access error or illegal access error occurs
   */
  public static String create(Object model) {
    try {
      String tableName = getTableName(model);
      Field[] fields =
          Arrays.stream(model.getClass().getDeclaredFields())
              .filter(field -> !"id".equals(field.getName()))
              .toArray(Field[]::new);
      String query =
          String.format(
              "INSERT INTO %s (%s) VALUES (%s) RETURNING id",
              tableName,
              Arrays.stream(fields)
                  .map(x -> toSnakeCase(x.getName()))
                  .collect(Collectors.joining(", ")),
              IntStream.range(0, fields.length)
                  .mapToObj(x -> "?")
                  .collect(Collectors.joining(", ")));

      PreparedStatement preparedStatement = conn.prepareStatement(query);
      int index = 1;
      for (Field f : fields) {
        Object value = f.get(model);
        addCastedValue(preparedStatement, index, value);
        index++;
      }

      ResultSet resultSet = preparedStatement.executeQuery();
      if (resultSet.next()) {
        return resultSet.getString(1);
      } else {
        throw new IllegalArgumentException("Unable to create new entry");
      }
    } catch (SQLException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  private static void setParameters(PreparedStatement statement, Object... args)
      throws SQLException {
    for (int i = 0; i < args.length; i++) {
      statement.setObject(i + 1, args[i]);
    }
  }

  public static String getTableName(Object model) {
    return getTableName(model.getClass());
  }

  public static String getTableName(Class<?> clazz) {
    String[] split = clazz.getName().split("\\.");
    String tableName = split[split.length - 1] + "s";
    return tableName.toLowerCase(Locale.getDefault());
  }

  public static void update(String id, Object model) {
    try {
      Field[] fields =
          Arrays.stream(model.getClass().getDeclaredFields())
              .filter(field -> !"id".equals(field.getName()))
              .toArray(Field[]::new);
      String setClause =
          Arrays.stream(fields)
              .map(field -> toSnakeCase(field.getName()) + " = ?")
              .collect(Collectors.joining(", "));

      String query =
          String.format(
              "UPDATE %s SET %s WHERE id = %s", getTableName(model), setClause, toSnakeCase(id));

      PreparedStatement preparedStatement = conn.prepareStatement(query);
      int index = 1;
      for (Field f : fields) {
        Object value = f.get(model);
        addCastedValue(preparedStatement, index, value);
        index++;
      }

      int affectedRows = preparedStatement.executeUpdate();
      if (affectedRows == 0) {
        throw new IllegalArgumentException("No record found with the specified id");
      }
    } catch (SQLException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  public static boolean delete(String tableName, String id) {
    return Db.execute("DELETE FROM " + tableName + " WHERE id = ?", new BigInteger(id));
  }

  private static void addCastedValue(PreparedStatement preparedStatement, int index, Object value)
      throws SQLException {
    if (value instanceof String string) {
      preparedStatement.setString(index, string);
    } else if (value instanceof Integer integer) {
      preparedStatement.setInt(index, integer);
    } else if (value instanceof BigInteger) {
      preparedStatement.setObject(index, value, java.sql.Types.BIGINT);
    } else if (value instanceof Double aDouble) {
      preparedStatement.setDouble(index, aDouble);
    } else if (value instanceof BigDecimal bigDecimal) {
      preparedStatement.setBigDecimal(index, bigDecimal);
    } else {
      throw new RuntimeException("Unknown field type " + value);
    }
  }

  private static String toSnakeCase(String input) {
    StringBuilder result = new StringBuilder();
    for (int i = 0; i < input.length(); i++) {
      char currentChar = input.charAt(i);
      if (Character.isUpperCase(currentChar)) {
        result.append("_").append(Character.toLowerCase(currentChar));
      } else {
        result.append(currentChar);
      }
    }
    return result.toString();
  }

  private static String toCamelCase(String input) {
    StringBuilder result = new StringBuilder();
    boolean capitalizeNext = false;

    for (int i = 0; i < input.length(); i++) {
      char currentChar = input.charAt(i);

      if (currentChar == '_') {
        capitalizeNext = true;
      } else {
        result.append(capitalizeNext ? Character.toUpperCase(currentChar) : currentChar);
        capitalizeNext = false;
      }
    }

    return result.toString();
  }

  public static <T> Optional<T> findById(Class<T> clazz, BigInteger id) {
    return helper(clazz, id);
  }

  public static <T> Optional<T> findById(Class<T> clazz, String id) {
    return helper(clazz, id);
  }

  private static <T> Optional<T> helper(Class<T> clazz, Object id) {
    return Db.queryVal(clazz, "SELECT * FROM " + getTableName(clazz) + " WHERE id = ?", id);
  }
}
