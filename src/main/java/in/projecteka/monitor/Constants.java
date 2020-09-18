package in.projecteka.monitor;

public class Constants {
    public static final String API_VERSION = "v0.5";
    //APIs
    public static final String CURRENT_VERSION = "/" + API_VERSION;

    public static final String PATH_HEARTBEAT = CURRENT_VERSION + "/heartbeat";
    public static final String PATH_READINESS = CURRENT_VERSION + "/readiness";

    private Constants() {}
}
