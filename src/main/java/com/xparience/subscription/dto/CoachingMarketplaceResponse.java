package com.xparience.subscription.dto;

import java.util.List;

public class CoachingMarketplaceResponse {
    private boolean accessGranted;
    private String message;
    private List<CoachingPackageDto> packages;

    public boolean isAccessGranted() { return accessGranted; }
    public void setAccessGranted(boolean accessGranted) { this.accessGranted = accessGranted; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public List<CoachingPackageDto> getPackages() { return packages; }
    public void setPackages(List<CoachingPackageDto> packages) { this.packages = packages; }
}
