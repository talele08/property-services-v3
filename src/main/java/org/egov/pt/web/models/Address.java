package org.egov.pt.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Address
 */
@Validated

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Address   {

    @JsonProperty("id")
    private String id;

    @JsonProperty("tenantId")
    private String tenantId;

    @JsonProperty("latitude")
    private Double latitude;

    @JsonProperty("longitude")
    private Double longitude;

    @JsonProperty("addressId")
    private String addressId;

    @JsonProperty("addressNumber")
    private String addressNumber;

    @JsonProperty("type")
    private String type;

    @NotNull
    @Size(max = 256)
    @JsonProperty("addressLine1")
    private String addressLine1;

    @Size(max = 256)
    @JsonProperty("addressLine2")
    private String addressLine2;

    @JsonProperty("landmark")
    private String landmark;

    @NotNull
    @JsonProperty("city")
    private String city;

    @JsonProperty("pincode")
    private String pincode;

    @JsonProperty("detail")
    private String detail;

    @Size(min = 2, max = 64)
    @JsonProperty("buildingName")
    private String buildingName;

    @Size(min = 2, max = 64)
    @JsonProperty("street")
    private String street;

    @Valid
    @JsonProperty("locality")
    private Boundary locality;


}

