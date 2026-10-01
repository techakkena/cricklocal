package com.cricklocal.entity;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "score_operator_access")
public class ScoreOperatorAccess {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "match_id", nullable = false)
    private Match match;

    @Column(nullable = false, unique = true, length = 100)
    private String accessToken;

    @Column(nullable = false, length = 100)
    private String securityCodeHash;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column
    private Instant expiresAt;

    @Column
    private Instant revokedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Match getMatch() {
        return match;
    }

    public void setMatch(Match match) {
        this.match = match;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getSecurityCodeHash() {
        return securityCodeHash;
    }

    public void setSecurityCodeHash(String securityCodeHash) {
        this.securityCodeHash = securityCodeHash;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Instant getRevokedAt() {
        return revokedAt;
    }

    public void setRevokedAt(Instant revokedAt) {
        this.revokedAt = revokedAt;
    }
}