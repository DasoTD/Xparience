package com.xparience.subscription.dto;

import java.math.BigDecimal;

public class CoachingPackageDto {
    private String packageId;
    private String title;
    private String duration;
    private BigDecimal price;

    public String getPackageId() { return packageId; }
    public void setPackageId(String packageId) { this.packageId = packageId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
}
