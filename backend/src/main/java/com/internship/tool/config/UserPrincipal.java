package com.internship.tool.config;

import com.internship.tool.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Custom UserDetails that also carries the database user ID,
 * so JwtUtil.getUserId(UserDetails) can return it without an extra DB query.
 */
public class UserPrincipal implements UserDetails {

    private final Long   id;
    private final String username;
    private final String password;
    private final boolean active;
    private final Collection<? extends GrantedAuthority> authorities;

    private UserPrincipal(Long id, String username, String password,
                          boolean active,
                          Collection<? extends GrantedAuthority> authorities) {
        this.id          = id;
        this.username    = username;
        this.password    = password;
        this.active      = active;
        this.authorities = authorities;
    }

    public static UserPrincipal from(User user) {
        return new UserPrincipal(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                Boolean.TRUE.equals(user.getActive()),
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole()))
        );
    }

    public Long getId() { return id; }

    @Override public String getUsername()                                    { return username; }
    @Override public String getPassword()                                    { return password; }
    @Override public Collection<? extends GrantedAuthority> getAuthorities(){ return authorities; }
    @Override public boolean isAccountNonExpired()  { return true; }
    @Override public boolean isAccountNonLocked()   { return true; }
    @Override public boolean isCredentialsNonExpired(){ return true; }
    @Override public boolean isEnabled()            { return active; }
}
