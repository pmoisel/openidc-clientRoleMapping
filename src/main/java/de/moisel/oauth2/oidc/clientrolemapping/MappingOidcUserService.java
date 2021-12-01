package de.moisel.oauth2.oidc.clientrolemapping;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

public class MappingOidcUserService extends OidcUserService {

    private final OidcUserService delegate = new OidcUserService();

    private final String defaultRolePrefix = "ROLE_";

    private final String resourceAccess;
    private final String client;
    private final String roleAttribute;

    public MappingOidcUserService(
            String resourceAccessAttribute,
            String clientAttribute,
            String roleAttribute
    ) {
        Assert.notNull(clientAttribute, "client cannot be null");
        this.resourceAccess = StringUtils.hasText(resourceAccessAttribute) ? resourceAccessAttribute : "resource_access";
        this.roleAttribute = StringUtils.hasText(roleAttribute) ? roleAttribute : "roles";
        this.client = clientAttribute;
    }

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = delegate.loadUser(userRequest);

        final Set<GrantedAuthority> mappedAuthorities = new HashSet<>();
        mappedAuthorities.addAll(oidcUser.getAuthorities());

        try {
            final LinkedHashMap<String, LinkedHashMap<String, List<String>>> resourceAccessMap = oidcUser.getAttribute(resourceAccess);
            final LinkedHashMap<String, List<String>> clientMap = resourceAccessMap.get(client);
            final List<String> roles = clientMap.get(roleAttribute);
            for (final String role : roles) {
                mappedAuthorities.add(new SimpleGrantedAuthority(defaultRolePrefix + role));
            }

            final ClientRegistration.ProviderDetails providerDetails = userRequest.getClientRegistration().getProviderDetails();
            final String userNameAttributeName = providerDetails.getUserInfoEndpoint().getUserNameAttributeName();
            if (StringUtils.hasText(userNameAttributeName)) {
                oidcUser = new DefaultOidcUser(mappedAuthorities, userRequest.getIdToken(), oidcUser.getUserInfo(), userNameAttributeName);
            }
            oidcUser = new DefaultOidcUser(mappedAuthorities, oidcUser.getIdToken(), oidcUser.getUserInfo());
            return oidcUser;
        } catch (NullPointerException npe) {
            throw new RuntimeException(String.format("Could not access %s.%s.%s in attributes of oidcUser", resourceAccess, client, roleAttribute), npe);
        }
    }
}
