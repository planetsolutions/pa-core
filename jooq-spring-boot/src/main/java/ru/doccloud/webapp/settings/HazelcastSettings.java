package ru.doccloud.webapp.settings;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "pa.hazelcast")
public class HazelcastSettings {

    private String address;
    private String сonnectionAttemptLimit;
    private String сonnectionAttemptPeriod;
    private String сonnectionTimeout;

    public HazelcastSettings() {
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getСonnectionAttemptLimit() {
        return сonnectionAttemptLimit;
    }

    public void setСonnectionAttemptLimit(String сonnectionAttemptLimit) {
        this.сonnectionAttemptLimit = сonnectionAttemptLimit;
    }

    public String getСonnectionAttemptPeriod() {
        return сonnectionAttemptPeriod;
    }

    public void setСonnectionAttemptPeriod(String сonnectionAttemptPeriod) {
        this.сonnectionAttemptPeriod = сonnectionAttemptPeriod;
    }

    public String getСonnectionTimeout() {
        return сonnectionTimeout;
    }

    public void setСonnectionTimeout(String сonnectionTimeout) {
        this.сonnectionTimeout = сonnectionTimeout;
    }

    @Override
    public String toString() {
        return "HazelcastSettings{" +
                "address='" + address + '\'' +
                ", сonnectionAttemptLimit='" + сonnectionAttemptLimit + '\'' +
                ", сonnectionAttemptPeriod='" + сonnectionAttemptPeriod + '\'' +
                ", сonnectionTimeout='" + сonnectionTimeout + '\'' +
                '}';
    }
}
