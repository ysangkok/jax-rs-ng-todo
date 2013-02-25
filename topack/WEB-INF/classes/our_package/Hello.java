package our_package;

import javax.ws.rs.core.*;
import javax.ws.rs.*;

import java.util.*;
import java.io.*;

import com.almworks.sqlite4java.*;
import com.google.gson.*;

@Path("/items")
public class Hello {

  static Gson gson = new Gson();

  @Produces(MediaType.APPLICATION_JSON)
  @GET
  public String list() throws SQLiteException {
    List<Map<String,Object>> orders = doQuery("SELECT text, done, rowid FROM todo", new String[] {}, false);
    JsonArray fin = new JsonArray();
    for (Map<String,Object> i : orders) {
      JsonObject row = new JsonObject();
      for (Map.Entry<String,Object> j : i.entrySet()) {
        if (j.getKey().equals("rowid")) {
          JsonObject o = generateId(j.getValue());
          row.add("_id" , o);
        } else if (j.getKey().equals("done")) {
          row.add("done", new JsonPrimitive(((Number) j.getValue()).longValue() == 1L));
        } else {
          row.add(j.getKey(), gson.toJsonTree(j.getValue()));
        }
      }
      fin.add(row);
    }
    return gson.toJson(fin);
  }

  private JsonObject generateId(Object obj) {
          JsonObject o = new JsonObject();
          o.add("$oid", gson.toJsonTree(obj));
          return o;
  }
  
  SQLiteConnection db = new SQLiteConnection(new File("/tmp/database"));
  public Hello() {
  }

  private List<Map<String,Object>> doQuery(String sql, Object[] binds, boolean dontOpenClose) throws SQLiteException {
    if (!dontOpenClose) db.open(true);

    List<Map<String,Object>> orders = new ArrayList<>();
    SQLiteStatement st = db.prepare(sql);
    try {
      {
        int i = 1;
        for (Object b : binds) {
          if (b instanceof Long)
            st.bind(i++, (Long) b);
          else if (b instanceof Integer)
            st.bind(i++, (Integer) b);
          else if (b instanceof Boolean)
            st.bind(i++, ((Boolean) b) ? 1 : 0);
          else if (b instanceof Double)
            st.bind(i++, (Double) b);
          else if (b instanceof String)
            st.bind(i++, (String) b);
          else if (b instanceof byte[])
            st.bind(i++, (byte[]) b);
          else
            throw new RuntimeException("type unsuppored " + b.getClass().getSimpleName());
        }
      }

      while (st.step()) {
        Map<String,Object> m = new HashMap<>();
        for (int i = 0; i < st.columnCount(); i++) {
          String columnName = st.getColumnName(i);
          int type = st.columnType(i);
          switch (type) {
            case SQLiteConstants.SQLITE_INTEGER:
              m.put(columnName, st.columnLong(i));
              break;
            case SQLiteConstants.SQLITE_FLOAT:
              m.put(columnName, st.columnDouble(i));
              break;
            case SQLiteConstants.SQLITE_TEXT:
              m.put(columnName, st.columnString(i));
              break;
            case SQLiteConstants.SQLITE_BLOB:
              m.put(columnName, st.columnBlob(i));
              break;
            case SQLiteConstants.SQLITE_NULL:
              m.put(columnName, null);
              break;
            default:
              throw new RuntimeException("Unknown SQLite type");
          }
        }
        orders.add(m);
      }
    } finally {
      st.dispose();
    }
    if (!dontOpenClose) db.dispose();
    return orders;
  }

  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @POST
  public String addNew(String json) throws SQLiteException {
    JsonElement e = new JsonParser().parse(json);
    String text = e.getAsJsonObject().get("text").getAsJsonPrimitive().getAsString();
    db.open(true);
    doQuery("INSERT INTO todo(text, done) VALUES(:text, 0)", new Object[]{text}, true);
    long id = db.getLastInsertId();
    db.dispose();
    JsonObject obj = new JsonObject();
    obj.add("text", new JsonPrimitive(text));
    obj.add("_id", generateId(id));
    return obj.toString();
  }

  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @Path("/{id}")
  @PUT
  public String changeDone(@PathParam("id") int id, String json) throws SQLiteException {
    JsonObject o = new JsonParser().parse(json).getAsJsonObject();
    boolean done = o.get("done").getAsJsonPrimitive().getAsBoolean();
    String text = o.get("text").getAsJsonPrimitive().getAsString();
    doQuery("UPDATE todo SET done=:done, text=:text WHERE rowid=:rowid", new Object[]{done, text, id}, false);
    o.add("_id", generateId(id));
    return o.toString();
  }

  @Path("/{id}")
  @DELETE
  public void delete(@PathParam("id") int id) throws SQLiteException {
    doQuery("DELETE FROM todo WHERE rowid=:rowid", new Object[]{id}, false);
  }
} 
