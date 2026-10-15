package in.kumarelectrical.contractor.auth;

import in.kumarelectrical.contractor.state.AppStateService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    public record LoginRequest(@NotBlank String username, @NotBlank String password) {}
    private final SessionTokenService tokens; private final AppStateService state;
    @Value("${app.auth.admin-username:ADMIN}") private String adminUsername;
    @Value("${app.auth.admin-password:admin123}") private String adminPassword;
    @Value("${app.auth.employee-default-password:emp123}") private String employeePassword;
    public AuthController(SessionTokenService tokens, AppStateService state){this.tokens=tokens;this.state=state;}
    @PostMapping("/login") public Map<String,Object> login(@Valid @RequestBody LoginRequest request){
        String id=request.username().trim().toUpperCase();
        if(id.equals(adminUsername.toUpperCase()) && request.password().equals(adminPassword)){
            var user=new SessionTokenService.UserPrincipal("ADMIN","Kumar (Admin)","admin");return Map.of("token",tokens.issue(user),"user",Map.of("id",user.id(),"name",user.name(),"role",user.role()));
        }
        boolean exists=false;String name=id;
        for(var e:state.read().path("employees")){if(id.equalsIgnoreCase(e.path("id").asText())){exists=true;name=e.path("name").asText(id);break;}}
        if(exists && request.password().equals(employeePassword)){
            var user=new SessionTokenService.UserPrincipal(id,name,"employee");return Map.of("token",tokens.issue(user),"user",Map.of("id",user.id(),"name",user.name(),"role",user.role()));
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Invalid username or password");
    }
    @PostMapping("/logout") public Map<String,String> logout(@RequestHeader(value="Authorization",required=false) String header){if(header!=null&&header.startsWith("Bearer "))tokens.revoke(header.substring(7));return Map.of("message","Signed out");}
    @GetMapping("/me") public Map<String,Object> me(Authentication auth){var u=(SessionTokenService.UserPrincipal)auth.getPrincipal();return Map.of("id",u.id(),"name",u.name(),"role",u.role());}
}
