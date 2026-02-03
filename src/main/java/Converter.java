import java.io.OutputStream;               // writing response body bytes in main
import java.net.InetSocketAddress;         // binding HTTP server to port in main
import java.net.URI;                       // request URI in main, and URI.create(apiUrl) in fetchCurrencies
import java.net.http.HttpClient;           // HTTP client in fetchCurrencies
import java.net.http.HttpRequest;          // building GET request in fetchCurrencies
import java.net.http.HttpResponse;         // reading API response in fetchCurrencies
import java.nio.charset.StandardCharsets;  // UTF-8 for request/response body in fetchCurrencies and main
import java.nio.file.Files;                // reading .env file in loadEnv
import java.nio.file.Path;                 // path to .env in loadEnv
import java.time.Instant;                  // timestamp for MongoDB documents
import java.util.HashMap;                  // env map in loadEnv
import java.util.Map;                      // env map type
import java.util.regex.Matcher;            // matching currency rate in getUserCurrency
import java.util.regex.Pattern;            // compiling regex in getUserCurrency
import com.sun.net.httpserver.HttpServer;  // HTTP server and /api/convert handler in main
import com.mongodb.ConnectionString;       // MongoDB connection string for settings
import com.mongodb.MongoClientSettings;    // MongoDB client configuration
import com.mongodb.ServerApi;              // Server API version for Atlas
import com.mongodb.ServerApiVersion;       // Server API version constant
import com.mongodb.client.MongoClient;     // MongoDB client connection
import com.mongodb.client.MongoClients;    // MongoDB client factory
import com.mongodb.client.MongoCollection; // MongoDB collection interface
import com.mongodb.client.MongoDatabase;   // MongoDB database interface
import com.mongodb.client.model.Sorts;     // sort for history query
import org.bson.Document;                  // MongoDB document type


/* Class to fetch rates from exchangerate-api and expose conversion via HTTP GET request */
// Example: /api/convert?from=USD&to=MMK&amount=100
public class Converter {

  private static final long CACHE_MS = 300_000; // 5 minutes
  private static String apiUrl;
  private static String cachedData;
  private static long cacheTime;
  private static Map<String, String> env = new HashMap<>();
  private static MongoClient mongoClient;
  private static MongoDatabase database;
  private static MongoCollection<Document> conversionsCollection;


  /* Function to load environment variables */
  private static void loadEnv() throws java.io.IOException {

    // step 1: check system environment variables (for production)
    String systemApiUrl = System.getenv("CURRENCY_API_URL");
    if (systemApiUrl != null && !systemApiUrl.isEmpty()) {
      apiUrl = systemApiUrl;
      return; // use system env var, skip .env file
    }

    // step 2: fallback to .env file (for local development)
    Path path = Path.of(System.getProperty("user.dir"), ".env");
    if (Files.isRegularFile(path)) {
      // read the .env file
      for (String line : Files.readAllLines(path, StandardCharsets.UTF_8)) {
        // step 2.1: remove leading/trailing whitespace from the line
        line = line.trim();
        // step 2.2: skip empty lines and comment lines (starting with #)
        if (line.isEmpty() || line.startsWith("#")) continue;
        // step 2.3: find the position of '=' character that separates key from value
        int eq = line.indexOf('=');
        // step 2.4: skip if no '=' found (eq == -1) or '=' is at start (eq == 0, invalid format)
        if (eq <= 0) continue;
        // step 2.5: extract key (everything before '=') and remove whitespace
        String key = line.substring(0, eq).trim();
        // step 2.6: extract value (everything after '=') and remove whitespace
        String value = line.substring(eq + 1).trim();
        // step 2.7: if value is wrapped in quotes (e.g. "value_here"), remove the quotes
        if (value.startsWith("\"") && value.endsWith("\"")) value = value.substring(1, value.length() - 1);
        // step 2.8: store key-value pair in env map (e.g. env.put("KEY", "value_here"))
        env.put(key, value);
      }

      // get the CURRENCY_API_URL from .env file
      apiUrl = env.get("CURRENCY_API_URL");
    }

    // step 3: if not found in either place, throw error
    if (apiUrl == null || apiUrl.isEmpty()) {
      throw new RuntimeException("CURRENCY_API_URL not found in system environment variables or .env file");
    }
  }


  /* Function to connect to MongoDB Atlas */
  private static void connectMongoDB() {
    try {
      // get MongoDB connection string from environment variables
      String mongoUri = System.getenv("MONGODB_URI");
      if (mongoUri == null || mongoUri.isEmpty()) {
        mongoUri = env.get("MONGODB_URI");
      }
      
      // if MongoDB URI is not set, skip MongoDB connection (optional)
      if (mongoUri == null || mongoUri.isEmpty()) {
        System.out.println("MongoDB URI not found - conversion history will not be saved");
        return;
      }

      // configure MongoDB client settings
      ServerApi serverApi = ServerApi.builder()
          .version(ServerApiVersion.V1)
          .build();
      MongoClientSettings settings = MongoClientSettings.builder()
          .applyConnectionString(new ConnectionString(mongoUri))
          .serverApi(serverApi)
          .build();
      mongoClient = MongoClients.create(settings);
      database = mongoClient.getDatabase("currency_converter");
      conversionsCollection = database.getCollection("conversions");
      System.out.println("Connected to MongoDB Atlas");
    } catch (Exception e) {
      System.err.println("Failed to connect to MongoDB: " + e.getMessage());
      // MongoDB connection failed - continue conversion API
    }
  }


  /* Function to save conversion history to MongoDB */
  private static void saveConversion(final String from, final String to, final double amount, final double result) {
    if (conversionsCollection == null) return; // skip if MongoDB not connected

    try {
      Document doc = new Document()
          .append("from", from)
          .append("to", to)
          .append("amount", amount)
          .append("result", result)
          .append("timestamp", Instant.now().toString());
      conversionsCollection.insertOne(doc);
    } catch (Exception e) {
      System.err.println("Failed to save conversion to MongoDB: " + e.getMessage());
      // MongoDB saving failed - continue conversion API
    }
  }


  /* Function to return last 5 conversions as JSON array string */
  private static String getConversionHistory() {
    if (conversionsCollection == null) return "[]";
    StringBuilder sb = new StringBuilder("[");
    try {
      var iterable = conversionsCollection.find()
          .sort(Sorts.descending("timestamp"))
          .limit(5);
      boolean first = true;
      for (Document doc : iterable) {
        if (!first) sb.append(",");
        first = false;
        String from = doc.getString("from");
        String to = doc.getString("to");
        double amount = doc.getDouble("amount");
        double result = doc.getDouble("result");
        String timestamp = doc.getString("timestamp");
        if (from == null) from = "";
        if (to == null) to = "";
        if (timestamp == null) timestamp = "";
        sb.append("{\"from\":\"").append(escapeJson(from)).append("\",\"to\":\"").append(escapeJson(to))
            .append("\",\"amount\":").append(amount).append(",\"result\":").append(String.format("%.4f", result))
            .append(",\"timestamp\":\"").append(escapeJson(timestamp)).append("\"}");
      }
    } catch (Exception e) {
      System.err.println("Failed to read conversion history: " + e.getMessage());
    }
    sb.append("]");
    return sb.toString();
  }


  /* Function to escape JSON string */
  private static String escapeJson(String s) {
    if (s == null) return "";
    return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
  }


  /* Function to fetch and cache the latest rates from the API */
  private static String fetchCurrencies() {

    // check if the response is cached
    if (cachedData != null && System.currentTimeMillis() - cacheTime < CACHE_MS) {
      return cachedData;
    }

    // fetch the latest rates from the API
    try {
      HttpClient client = HttpClient.newHttpClient();
      HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create(apiUrl))
          .GET()
          .build();
      HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
      if (response.statusCode() == 200) {
        cachedData = response.body();
        cacheTime = System.currentTimeMillis();
        return cachedData;
      }
    } catch (Exception e) {
      System.err.println("Failed to fetch rates: " + e.getMessage());
    }
    return null;
  }


  /* Function to parse rate for the user's chosen currency code from JSON */
  private static double getUserCurrency(final String json, final String currencyCode) {
    if (json == null) return 0;
    // regex example: match "USD":1 or "EUR":0.846294
    Pattern p = Pattern.compile("\"" + Pattern.quote(currencyCode) + "\":([0-9.]+)");
    Matcher m = p.matcher(json);
    // if match found, return rate; otherwise return 0
    return m.find() ? Double.parseDouble(m.group(1)) : 0;
  }


  /* Function to convert amount from one currency to another */
  // All API rates are "1 USD = X units" of that currency
  // Formula: amountInFrom * (rateTo / rateFrom) = amountInTo
  // - (rateTo/rateFrom) = how many to-units per one from-unit
  // Example: convert 200 SGD to THB
  // - suppose API gives: 1 USD = 1.35 SGD and 1 USD = 35 THB
  // - step 1: convert 200 SGD to USD = 200 / 1.35 = 148.15 USD
  // - step 2: convert 148.15 USD to THB = 148.15 * 35 = 5185.19 THB
  // - same as formula: 200 * (35 / 1.35) = 200 * 25.93 = 5185.19 THB
  // Returns 0 if amount is negative or conversion fails
  public static double convert(final String from, final String to, final double amount) {
    if (amount < 0) return 0;
    String json = fetchCurrencies();
    double rateFrom = getUserCurrency(json, from);
    double rateTo = getUserCurrency(json, to);
    if (rateFrom <= 0 || rateTo <= 0) return 0;
    return amount * (rateTo / rateFrom);
  }


  /* Function to parse query string parameters from user's request URL */
  // Example: query = "from=USD&to=MMK&amount=100"
  // - key = "from", "to", "amount"
  // - returns "USD", "MMK", "100" or empty string if not found
  private static String param(final String query, final String key) {
    String prefix = key + "=";
    for (String part : query.split("&")) {
      if (part.startsWith(prefix)) {
        return part.substring(prefix.length()).trim();
      }
    }
    return "";
  }


  /* Main function: starts HTTP server */
  // Port priority: args > system env PORT > .env VITE_PORT > default 8080.
  public static void main(final String[] args) throws Exception {

    // load environment variables from .env file
    loadEnv();

    // connect to MongoDB Atlas (optional - continues if connection fails)
    connectMongoDB();

    // determine port to use
    int port;
    // A. check if PORT is passed as an argument
    if (args.length > 0) {
      port = Integer.parseInt(args[0]);
    } else {
      // B. check if PORT is set in the system environment variable
      String portStr = System.getenv("PORT");
      // C. check if VITE_PORT is set in the .env file (shared with frontend)
      if (portStr == null || portStr.isEmpty()) {
        portStr = env.get("VITE_PORT");
      }
      if (portStr != null && !portStr.isEmpty()) {
        port = Integer.parseInt(portStr.trim());
      } else {
        port = 8080; // D. if PORT is not set in any of the above, use default 8080
      }
    }

    // start HTTP server
    HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
    server.createContext("/api/convert", exchange -> {
      if (!"GET".equals(exchange.getRequestMethod())) {
        exchange.sendResponseHeaders(405, 0);
        exchange.close();
        return;
      }
      URI uri = exchange.getRequestURI();
      String query = uri.getQuery() != null ? uri.getQuery() : "";
      String from = param(query, "from");
      String to = param(query, "to");
      String amountStr = param(query, "amount");
      double amount = 0;
      try {
        amount = Double.parseDouble(amountStr);
      } catch (NumberFormatException ignored) {}
      double result = convert(from, to, amount);
      
      // save conversion history to MongoDB (if connected)
      if (result > 0) {
        saveConversion(from, to, amount, result);
      }
      
      String body = "{\"from\":\"" + from + "\",\"to\":\"" + to + "\",\"amount\":" + amount
          + ",\"result\":" + String.format("%.4f", result) + "}";
      byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
      exchange.getResponseHeaders().set("Content-Type", "application/json");
      exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
      exchange.sendResponseHeaders(200, bytes.length);
      try (OutputStream out = exchange.getResponseBody()) {
        out.write(bytes);
      }
    });
    server.createContext("/api/history", exchange -> {
      if (!"GET".equals(exchange.getRequestMethod())) {
        exchange.sendResponseHeaders(405, 0);
        exchange.close();
        return;
      }
      String body = getConversionHistory();
      byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
      exchange.getResponseHeaders().set("Content-Type", "application/json");
      exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
      exchange.sendResponseHeaders(200, bytes.length);
      try (OutputStream out = exchange.getResponseBody()) {
        out.write(bytes);
      }
    });
    server.start();
    System.out.println("Currency API on http://localhost:" + port + "/api/convert?from=USD&to=MMK&amount=100 and /api/history");
  }

}
