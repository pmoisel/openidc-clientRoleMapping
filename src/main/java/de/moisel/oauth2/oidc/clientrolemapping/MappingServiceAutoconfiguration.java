package de.moisel.oauth2.oidc.clientrolemapping;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("oidc.clientrolemapping")
public class MappingServiceAutoconfiguration {

    private String resourceAccess;
    private String client;
    private String role;

    @Bean
    public MappingOidcUserService mappingOidcUserService() {
        return new MappingOidcUserService(
                resourceAccess,
                client,
                role);
    }

    public String getResourceAccess() {
        return resourceAccess;
    }

    public void setResourceAccess(String resourceAccess) {
        this.resourceAccess = resourceAccess;
    }

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
