package larrytllama.pvcmappermod;

import java.net.http.HttpClient;
import java.time.Duration;

public class NetworkUtils {
    /**
     * Singleton HTTP Client instance.
     * Reuses connections, prevents socket exhaustion, and shifts logic off the main thread.
     */
    public static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    // API Endpoints
    public static final String BASE_URL = "https://pvc.coolwebsite.uk";
    public static final String API_V1 = BASE_URL + "/api/v1";
    public static final String API_V2 = BASE_URL + "/api/v2";
}
