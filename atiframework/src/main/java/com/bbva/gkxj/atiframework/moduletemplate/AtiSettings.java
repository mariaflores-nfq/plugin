package com.bbva.gkxj.atiframework.moduletemplate;

public class AtiSettings {
    private String uuaa;

    private String serviceName;

    private String servicePublicName;

    private String serviceDescription;

    private String novaVersion;

    private String novaCliVersion;

    private String atiVersion;

    private String atiRelease;

    private Boolean isNovaTransferIncluded;

    private Boolean isEpsilonSupportIncluded;

    private Boolean isOracleIncluded;

    private Boolean isMongoIncluded;

    private Boolean isPostgreIncluded;

    public Boolean getIsPostgreIncluded() {
        return isPostgreIncluded;
    }
    public void setIsPostgreIncluded(Boolean isPostgreIncluded) {
        this.isPostgreIncluded = isPostgreIncluded;
    }

    public Boolean getIsMongoIncluded() {
        return isMongoIncluded;
    }
    public void setIsMongoIncluded(Boolean isMongoIncluded) {
        this.isMongoIncluded = isMongoIncluded;
    }

    public Boolean getIsOracleIncluded() {
        return isOracleIncluded;
    }
    public void setIsOracleIncluded(Boolean isOracleIncluded) {
        this.isOracleIncluded = isOracleIncluded;
    }

    public Boolean getIsEpsilonSupportIncluded() {
        return isEpsilonSupportIncluded;
    }
    public void setIsEpsilonSupportIncluded(Boolean isEpsilonSupportIncluded) {
        this.isEpsilonSupportIncluded = isEpsilonSupportIncluded;
    }

    public Boolean getIsNovaTransferIncluded() {
        return isNovaTransferIncluded;
    }
    public void setIsNovaTransferIncluded(Boolean isNovaTransferIncluded) {
        this.isNovaTransferIncluded = isNovaTransferIncluded;
    }

    public String getAtiVersion() {
        return atiVersion;
    }
    public void setAtiVersion(String atiVersion) {
        this.atiVersion = atiVersion;
    }

    public String getAtiRelease() {
        return atiRelease;
    }
    public void setAtiRelease(String atiRelease) { this.atiRelease = atiRelease; }

    public String getNovaCliVersion() {
        return novaCliVersion;
    }
    public void setNovaCliVersion(String novaCliVersion) {
        this.novaCliVersion = novaCliVersion;
    }

    public String getNovaVersion() {
        return novaVersion;
    }
    public void setNovaVersion(String novaVersion) {
        this.novaVersion = novaVersion;
    }

    public String getServiceDescription() {
        return serviceDescription;
    }
    public void setServiceDescription(String serviceDescription) {
        this.serviceDescription = serviceDescription;
    }

    public String getServicePublicName() {
        return servicePublicName;
    }
    public void setServicePublicName(String servicePublicName) {
        this.servicePublicName = servicePublicName;
    }

    public String getServiceName() {
        return serviceName;
    }
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getUuaa() {
        return uuaa;
    }
    public void setUuaa(String uuaa) {
        this.uuaa = uuaa;
    }

}
