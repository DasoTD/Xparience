package com.xparience.profile;

import jakarta.persistence.*;

@Entity
@Table(name = "profile_images")
public class ProfileImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    private Profile profile;

    @Column(nullable = false)
    private String imageUrl;

    private Integer position;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Profile getProfile() { return profile; }
    public void setProfile(Profile profile) { this.profile = profile; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public Integer getPosition() { return position; }
    public void setPosition(Integer position) { this.position = position; }
}
