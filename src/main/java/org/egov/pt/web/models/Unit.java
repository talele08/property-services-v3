package org.egov.pt.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * Unit
 */
@Validated
@javax.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2018-05-11T14:12:44.497+05:30")

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode(exclude={"usage"})
public class Unit   {
        @JsonProperty("id")
        private String id;

        @Size(min = 2, max = 256)
        @JsonProperty("tenantId")
        private String tenantId;

        @Size(min = 1, max = 64)
        @JsonProperty("floorNo")
        private String floorNo;

        @Size(min = 1, max = 64)
        @JsonProperty("unitType")
        private String unitType;

        @JsonProperty("unitArea")
        private Float unitArea;

        @Size(min = 1, max = 64)
        @JsonProperty("usageCategoryMajor")
        private String usageCategoryMajor;

        @Size(min = 1, max = 64)
        @JsonProperty("usageCategoryMinor")
        private String usageCategoryMinor;

        @Size(min = 1, max = 64)
        @JsonProperty("usageCategorySubMinor")
        private String usageCategorySubMinor;

        @Size(min = 1, max = 64)
        @JsonProperty("usageCategoryDetail")
        private String usageCategoryDetail;

        @Size(min = 1, max = 64)
        @JsonProperty("occupancyType")
        private String occupancyType;

        @JsonProperty("occupancyDate")
        private Long occupancyDate;

        @Size(min = 1, max = 64)
        @JsonProperty("constructionType")
        private String constructionType;

        @Size(min = 1, max = 64)
        @JsonProperty("constructionSubType")
        private String constructionSubType;

        @JsonProperty("arv")
        private BigDecimal arv;





}

