package org.tkit.onecx.product.store.slot.operator;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SlotSpec {

    @JsonProperty("appId")
    private String appId;

    @JsonProperty("productName")
    private String productName;

    @JsonProperty("description")
    private String description;

    @JsonProperty("name")
    private String name;

    @JsonProperty(value = "deprecated", defaultValue = "false")
    private boolean deprecated = false;

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDeprecated(boolean deprecated) {
        this.deprecated = deprecated;
    }

    public boolean isDeprecated() {
        return deprecated;
    }

    @Override
    public String toString() {
        return "SlotSpec{" + "appId='" + appId + "', productName='" + productName + "', name='" + name + "'}";
    }
}
