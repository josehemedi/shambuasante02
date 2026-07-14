package hospicloud.dtos;

public class LiveKitTokenResponse {
    private String token;
    private String roomName;
    private String participantIdentity;
    private String serverUrl;
    private String displayName;

    public LiveKitTokenResponse() {}

    public LiveKitTokenResponse(String token, String roomName, String participantIdentity,
                                 String serverUrl, String displayName) {
        this.token = token;
        this.roomName = roomName;
        this.participantIdentity = participantIdentity;
        this.serverUrl = serverUrl;
        this.displayName = displayName;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }
    public String getParticipantIdentity() { return participantIdentity; }
    public void setParticipantIdentity(String participantIdentity) { this.participantIdentity = participantIdentity; }
    public String getServerUrl() { return serverUrl; }
    public void setServerUrl(String serverUrl) { this.serverUrl = serverUrl; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
}