package in.kumarelectrical.contractor.state;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import in.kumarelectrical.contractor.auth.SessionTokenService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class StateController {
    private final AppStateService state;
    public StateController(AppStateService state){this.state=state;}
    private SessionTokenService.UserPrincipal user(Authentication a){return (SessionTokenService.UserPrincipal)a.getPrincipal();}
    @GetMapping("/health") public Map<String,Object> health(){return Map.of("status","UP","service","Kumar Electrical Contractor API");}
    @GetMapping("/state") public ObjectNode getState(Authentication auth){var u=user(auth);return u.role().equals("admin")?state.read():state.forEmployee(u.id());}
    @PutMapping("/state") public ObjectNode putState(@RequestBody JsonNode payload,Authentication auth){if(!user(auth).role().equals("admin"))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Only administrators can update company data");return state.write(payload);}
    @GetMapping("/employees") public JsonNode employees(Authentication auth){return getState(auth).path("employees");}
    @GetMapping("/sites") public JsonNode sites(Authentication auth){return getState(auth).path("sites");}
    @GetMapping("/attendance") public JsonNode attendance(Authentication auth){return getState(auth).path("attendance");}
    @GetMapping("/assignments") public JsonNode assignments(Authentication auth){return getState(auth).path("attendance");}
    @GetMapping("/projects") public JsonNode projects(Authentication auth){return getState(auth).path("projects");}
    @GetMapping("/payroll") public JsonNode payroll(Authentication auth){if(!user(auth).role().equals("admin"))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Payroll summary is admin-only");return getState(auth).path("attendance");}
    @PostMapping("/attendance/verify-location") public Map<String,Object> verifyLocation(@RequestBody JsonNode body,Authentication auth){
        var s=state.read().path("sites").deepCopy();String siteId=body.path("siteId").asText("");double lat=body.path("latitude").asDouble(Double.NaN),lng=body.path("longitude").asDouble(Double.NaN);
        for(JsonNode site:s){if(siteId.equals(site.path("id").asText())){double slat=site.path("lat").asDouble(Double.NaN),slng=site.path("lng").asDouble(Double.NaN);if(Double.isNaN(lat)||Double.isNaN(lng)||Double.isNaN(slat)||Double.isNaN(slng))return Map.of("verified",false,"reason","Site or device coordinates are missing","distanceMeters",-1);double distance=distance(lat,lng,slat,slng);double radius=site.path("radius").asDouble(150);return Map.of("verified",distance<=radius,"distanceMeters",Math.round(distance),"radiusMeters",radius,"note","GPS is a location signal and may be spoofed.");}}
        throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Worksite not found");
    }
    private double distance(double lat1,double lon1,double lat2,double lon2){double r=6371000,dLat=Math.toRadians(lat2-lat1),dLon=Math.toRadians(lon2-lon1);double a=Math.sin(dLat/2)*Math.sin(dLat/2)+Math.cos(Math.toRadians(lat1))*Math.cos(Math.toRadians(lat2))*Math.sin(dLon/2)*Math.sin(dLon/2);return r*2*Math.atan2(Math.sqrt(a),Math.sqrt(1-a));}
}
