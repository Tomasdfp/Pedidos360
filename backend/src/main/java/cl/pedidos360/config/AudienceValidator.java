package cl.pedidos360.config;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

public class AudienceValidator implements OAuth2TokenValidator<Jwt> {

    private final String expectedAudience;
    private final String clientId;

    public AudienceValidator(String expectedAudience, String clientId) {
        this.expectedAudience = expectedAudience;
        this.clientId = clientId;
    }

    public AudienceValidator(String expectedAudience) {
        this(expectedAudience, null);
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {
        List<String> audience = jwt.getAudience();
        if (audience != null && (audience.contains(expectedAudience) || (clientId != null && audience.contains(clientId)))) {
            return OAuth2TokenValidatorResult.success();
        }
        OAuth2Error error = new OAuth2Error("invalid_token", "The required audience is missing or invalid", null);
        return OAuth2TokenValidatorResult.failure(error);
    }
}
