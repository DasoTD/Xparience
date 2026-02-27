package com.xparience.date.dto;

public class StreamingIntegrationResponse {
    private Long inviteId;
    private String platform;
    private String contentLink;
    private String providerContentId;
    private String integrationStatus;

    public Long getInviteId() {
        return inviteId;
    }

    public void setInviteId(Long inviteId) {
        this.inviteId = inviteId;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getContentLink() {
        return contentLink;
    }

    public void setContentLink(String contentLink) {
        this.contentLink = contentLink;
    }

    public String getProviderContentId() {
        return providerContentId;
    }

    public void setProviderContentId(String providerContentId) {
        this.providerContentId = providerContentId;
    }

    public String getIntegrationStatus() {
        return integrationStatus;
    }

    public void setIntegrationStatus(String integrationStatus) {
        this.integrationStatus = integrationStatus;
    }
}
