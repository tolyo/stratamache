package db;

public interface Model {
  String getId();

  String getBaseQuery();

  default java.util.Optional<? extends Model> findById(String id) {
    return Db.queryVal(this.getClass(), getBaseQuery().concat(" WHERE id = ?"), id);
  }
}
