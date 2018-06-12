package org.egov.pt.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import lombok.*;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

/**
 * Contract class to receive request. Array of Property items  are used in case of create . Where as single Property item is used for update
 */
@ApiModel(description = "Contract class to receive request. Array of Property items  are used in case of create . Where as single Property item is used for update")
@Validated
@javax.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2018-05-11T14:12:44.497+05:30")
@ToString
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PropertyRequest   {
        @JsonProperty("RequestInfo")
        private RequestInfo requestInfo;

        @JsonProperty("properties")
        @Size(min = 1, max = 100)
        @Valid
        private List<Property> properties;


        public PropertyRequest addPropertiesItem(Property propertiesItem) {
            if (this.properties == null) {
            this.properties = new ArrayList<>();
            }
        this.properties.add(propertiesItem);
        return this;
        }

}

