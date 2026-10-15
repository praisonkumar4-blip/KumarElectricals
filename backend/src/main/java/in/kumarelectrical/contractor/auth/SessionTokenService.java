package in.kumarelectrical.contractor.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SessionTokenService {
    public record UserPrincipal(String id, String name, String role) {}
    private record Session(UserPrincipal user, Instant expiresAt) {}
    private final Map<String, Session> sessions = new ConcurrentHashMap<>();
    @Value("${app.auth.token-hours:8}") private long tokenHours;
    public String issue(UserPrincipal user) {
        String token=UUID.randomUUID()+"."+UUID.randomUUID();
        sessions.put(token,new Session(user,Instant.now().plusSeconds(Math.max(1,tokenHours)*3600)));
        return token;
    }
    public UserPrincipal resolve(String token) {
        if(token==null || token.isBlank()) return null;
        Session s=sessions.get(token);
        if(s==null) return null;
        if(s.expiresAt().isBefore(Instant.now())) { sessions.remove(token); return null; }
        return s.user();
    }
    public void revoke(String token) { if(token!=null) sessions.remove(token); }
}
