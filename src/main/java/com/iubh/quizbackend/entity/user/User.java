package com.iubh.quizbackend.entity.user;

import com.iubh.quizbackend.entity.module.Module;
import com.iubh.quizbackend.entity.quiz.Quiz;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

@Data
@Builder // Add builder
@NoArgsConstructor
@AllArgsConstructor // Add all-args constructor
@Entity
@Table(name = "users")
public class User implements UserDetails { // Implement UserDetails directly

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Embedded
    private Profile profile;


    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_followed_modules", // Name of the intermediate table
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "module_id")
    )
    @Builder.Default
    private Set<Module> followedModules = new HashSet<>();


    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }


    /**
     * Helper method to follow a module and synchronize the bidirectional relationship.
     * @param module The module to follow.
     */
    public void followModule(Module module) {
        this.followedModules.add(module);
        module.getFollowers().add(this);
    }

    /**
     * Helper method to unfollow a module and synchronize the bidirectional relationship.
     * @param module The module to unfollow.
     */
    public void unfollowModule(Module module) {
        this.followedModules.remove(module);
        module.getFollowers().remove(this);
    }


    /**
     * A list of all quiz attempts made by this user.
     */
    @OneToMany(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<Quiz> quizzes = new ArrayList<>();


}