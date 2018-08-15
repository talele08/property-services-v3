package org.egov.pt.util;

import com.jayway.jsonpath.JsonPath;
import org.egov.pt.config.PropertyConfiguration;
import org.egov.pt.producer.Producer;
import org.egov.pt.service.PropertyService;
import org.egov.pt.web.models.OwnerInfo;
import org.egov.pt.web.models.PropertyDetail;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.cglib.core.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class hashtest {

    @Autowired
    private KafkaTemplate kafkaTemplate;

    @Autowired
    private PropertyConfiguration config;

    @Test
    public void name() {

      String request = "{\n" +
              "    \"ResponseInfo\": {\n" +
              "        \"apiId\": \"Rainmaker\",\n" +
              "        \"ver\": \".01\",\n" +
              "        \"ts\": null,\n" +
              "        \"resMsgId\": \"uief87324\",\n" +
              "        \"msgId\": \"20170310130900|en_IN\",\n" +
              "        \"status\": \"successful\"\n" +
              "    },\n" +
              "    \"Properties\": [\n" +
              "        {\n" +
              "            \"auditDetails\": {\n" +
              "                \"createdBy\": \"530968f3-76b3-4fd1-b09d-9e22eb1f85df\",\n" +
              "                \"lastModifiedBy\": \"530968f3-76b3-4fd1-b09d-9e22eb1f85df\",\n" +
              "                \"createdTime\": 1534231779792,\n" +
              "                \"lastModifiedTime\": 1534231779792\n" +
              "            },\n" +
              "            \"creationReason\": null,\n" +
              "            \"occupancyDate\": null,\n" +
              "            \"propertyDetails\": [\n" +
              "                {\n" +
              "                    \"institution\": null,\n" +
              "                    \"tenantId\": \"pb.jalandhar\",\n" +
              "                    \"citizenInfo\": {\n" +
              "                        \"isPrimaryOwner\": null,\n" +
              "                        \"ownerShipPercentage\": null,\n" +
              "                        \"ownerType\": null,\n" +
              "                        \"institutionId\": null,\n" +
              "                        \"documents\": null,\n" +
              "                        \"relationship\": null,\n" +
              "                        \"id\": 23349,\n" +
              "                        \"uuid\": \"530968f3-76b3-4fd1-b09d-9e22eb1f85df\",\n" +
              "                        \"userName\": \"9404052047\",\n" +
              "                        \"password\": null,\n" +
              "                        \"salutation\": null,\n" +
              "                        \"name\": \"Aniket Talele\",\n" +
              "                        \"gender\": null,\n" +
              "                        \"mobileNumber\": \"9404052047\",\n" +
              "                        \"emailId\": \"\",\n" +
              "                        \"altContactNumber\": null,\n" +
              "                        \"pan\": null,\n" +
              "                        \"aadhaarNumber\": null,\n" +
              "                        \"permanentAddress\": null,\n" +
              "                        \"permanentCity\": null,\n" +
              "                        \"permanentPinCode\": null,\n" +
              "                        \"correspondenceCity\": null,\n" +
              "                        \"correspondencePinCode\": null,\n" +
              "                        \"correspondenceAddress\": null,\n" +
              "                        \"active\": null,\n" +
              "                        \"dob\": null,\n" +
              "                        \"pwdExpiryDate\": null,\n" +
              "                        \"locale\": null,\n" +
              "                        \"type\": \"CITIZEN\",\n" +
              "                        \"signature\": null,\n" +
              "                        \"accountLocked\": null,\n" +
              "                        \"roles\": [\n" +
              "                            {\n" +
              "                                \"id\": 281,\n" +
              "                                \"name\": \"Citizen\",\n" +
              "                                \"code\": \"CITIZEN\",\n" +
              "                                \"description\": null,\n" +
              "                                \"createdBy\": null,\n" +
              "                                \"createdDate\": null,\n" +
              "                                \"lastModifiedBy\": null,\n" +
              "                                \"lastModifiedDate\": null,\n" +
              "                                \"tenantId\": null\n" +
              "                            }\n" +
              "                        ],\n" +
              "                        \"fatherOrHusbandName\": null,\n" +
              "                        \"bloodGroup\": null,\n" +
              "                        \"identificationMark\": null,\n" +
              "                        \"photo\": null,\n" +
              "                        \"createdBy\": null,\n" +
              "                        \"createdDate\": null,\n" +
              "                        \"lastModifiedBy\": null,\n" +
              "                        \"lastModifiedDate\": null,\n" +
              "                        \"otpReference\": null,\n" +
              "                        \"tenantId\": \"pb\"\n" +
              "                    },\n" +
              "                    \"source\": null,\n" +
              "                    \"usage\": null,\n" +
              "                    \"noOfFloors\": 1,\n" +
              "                    \"landArea\": 200,\n" +
              "                    \"buildUpArea\": null,\n" +
              "                    \"units\": null,\n" +
              "                    \"documents\": null,\n" +
              "                    \"additionalDetails\": null,\n" +
              "                    \"financialYear\": \"2018-19\",\n" +
              "                    \"propertyType\": \"VACANT\",\n" +
              "                    \"propertySubType\": null,\n" +
              "                    \"assessmentNumber\": \"PB-AS-2018-08-14-000641\",\n" +
              "                    \"assessmentDate\": 1534231779844,\n" +
              "                    \"usageCategoryMajor\": null,\n" +
              "                    \"ownershipCategory\": \"INDIVIDUAL\",\n" +
              "                    \"subOwnershipCategory\": \"SINGLEOWNER\",\n" +
              "                    \"adhocExemption\": null,\n" +
              "                    \"adhocPenalty\": null,\n" +
              "                    \"adhocExemptionReason\": null,\n" +
              "                    \"adhocPenaltyReason\": null,\n" +
              "                    \"owners\": [\n" +
              "                        {\n" +
              "                            \"isPrimaryOwner\": null,\n" +
              "                            \"ownerShipPercentage\": null,\n" +
              "                            \"ownerType\": \"NONE\",\n" +
              "                            \"institutionId\": null,\n" +
              "                            \"documents\": [\n" +
              "                                {\n" +
              "                                    \"id\": \"3cc03282-cf9c-4531-b0f1-3ae9cbdfae24\",\n" +
              "                                    \"documentType\": \"Pancard\",\n" +
              "                                    \"fileStore\": null,\n" +
              "                                    \"documentUid\": \"548850008000\"\n" +
              "                                }\n" +
              "                            ],\n" +
              "                            \"relationship\": null,\n" +
              "                            \"id\": 23592,\n" +
              "                            \"uuid\": \"f67e14d1-6100-41f5-af64-c1d25eb9b5fa\",\n" +
              "                            \"userName\": \"9004376134\",\n" +
              "                            \"password\": null,\n" +
              "                            \"salutation\": null,\n" +
              "                            \"name\": \"Aniket\",\n" +
              "                            \"gender\": \"Male\",\n" +
              "                            \"mobileNumber\": \"9004376134\",\n" +
              "                            \"emailId\": null,\n" +
              "                            \"altContactNumber\": null,\n" +
              "                            \"pan\": null,\n" +
              "                            \"aadhaarNumber\": null,\n" +
              "                            \"permanentAddress\": null,\n" +
              "                            \"permanentCity\": null,\n" +
              "                            \"permanentPinCode\": null,\n" +
              "                            \"correspondenceCity\": null,\n" +
              "                            \"correspondencePinCode\": null,\n" +
              "                            \"correspondenceAddress\": null,\n" +
              "                            \"active\": true,\n" +
              "                            \"dob\": null,\n" +
              "                            \"pwdExpiryDate\": null,\n" +
              "                            \"locale\": null,\n" +
              "                            \"type\": \"CITIZEN\",\n" +
              "                            \"signature\": null,\n" +
              "                            \"accountLocked\": null,\n" +
              "                            \"roles\": [\n" +
              "                                {\n" +
              "                                    \"id\": null,\n" +
              "                                    \"name\": \"Citizen\",\n" +
              "                                    \"code\": \"CITIZEN\",\n" +
              "                                    \"description\": null,\n" +
              "                                    \"createdBy\": null,\n" +
              "                                    \"createdDate\": null,\n" +
              "                                    \"lastModifiedBy\": null,\n" +
              "                                    \"lastModifiedDate\": null,\n" +
              "                                    \"tenantId\": null\n" +
              "                                }\n" +
              "                            ],\n" +
              "                            \"fatherOrHusbandName\": \"BB Prasad\",\n" +
              "                            \"bloodGroup\": null,\n" +
              "                            \"identificationMark\": null,\n" +
              "                            \"photo\": null,\n" +
              "                            \"createdBy\": \"530968f3-76b3-4fd1-b09d-9e22eb1f85df\",\n" +
              "                            \"createdDate\": 1534231780048,\n" +
              "                            \"lastModifiedBy\": \"530968f3-76b3-4fd1-b09d-9e22eb1f85df\",\n" +
              "                            \"lastModifiedDate\": 1534231780048,\n" +
              "                            \"otpReference\": null,\n" +
              "                            \"tenantId\": \"pb.jalandhar\"\n" +
              "                        }\n" +
              "                    ],\n" +
              "                    \"auditDetails\": {\n" +
              "                        \"createdBy\": \"530968f3-76b3-4fd1-b09d-9e22eb1f85df\",\n" +
              "                        \"lastModifiedBy\": \"530968f3-76b3-4fd1-b09d-9e22eb1f85df\",\n" +
              "                        \"createdTime\": 1534231779792,\n" +
              "                        \"lastModifiedTime\": 1534231779792\n" +
              "                    },\n" +
              "                    \"calculation\": {\n" +
              "                        \"serviceNumber\": \"PB-AS-2018-08-14-000641\",\n" +
              "                        \"totalAmount\": 0,\n" +
              "                        \"taxAmount\": 0,\n" +
              "                        \"penalty\": 0,\n" +
              "                        \"exemption\": 0,\n" +
              "                        \"rebate\": 0,\n" +
              "                        \"fromDate\": 1522540800000,\n" +
              "                        \"toDate\": 1554076799000,\n" +
              "                        \"tenantId\": \"pb.jalandhar\",\n" +
              "                        \"taxHeadEstimates\": [\n" +
              "                            {\n" +
              "                                \"taxHeadCode\": \"PT_TAX\",\n" +
              "                                \"estimateAmount\": 0,\n" +
              "                                \"category\": \"TAX\"\n" +
              "                            },\n" +
              "                            {\n" +
              "                                \"taxHeadCode\": \"PT_UNIT_USAGE_EXEMPTION\",\n" +
              "                                \"estimateAmount\": 0,\n" +
              "                                \"category\": \"EXEMPTION\"\n" +
              "                            },\n" +
              "                            {\n" +
              "                                \"taxHeadCode\": \"PT_OWNER_EXEMPTION\",\n" +
              "                                \"estimateAmount\": 0,\n" +
              "                                \"category\": \"EXEMPTION\"\n" +
              "                            },\n" +
              "                            {\n" +
              "                                \"taxHeadCode\": \"PT_FIRE_CESS\",\n" +
              "                                \"estimateAmount\": 0,\n" +
              "                                \"category\": \"TAX\"\n" +
              "                            }\n" +
              "                        ]\n" +
              "                    },\n" +
              "                    \"channel\": null\n" +
              "                }\n" +
              "            ],\n" +
              "            \"propertyId\": \"PB-PT-2018-08-14-000623\",\n" +
              "            \"tenantId\": \"pb.jalandhar\",\n" +
              "            \"acknowldgementNumber\": \"PB-AC-2018-08-14-000634\",\n" +
              "            \"oldPropertyId\": null,\n" +
              "            \"status\": \"ACTIVE\",\n" +
              "            \"address\": {\n" +
              "                \"id\": \"5a5f9633-1800-45e6-8db5-0a938016e97a\",\n" +
              "                \"tenantId\": \"pb.jalandhar\",\n" +
              "                \"latitude\": null,\n" +
              "                \"longitude\": null,\n" +
              "                \"addressId\": null,\n" +
              "                \"addressNumber\": null,\n" +
              "                \"type\": null,\n" +
              "                \"addressLine1\": null,\n" +
              "                \"addressLine2\": null,\n" +
              "                \"landmark\": null,\n" +
              "                \"doorNo\": null,\n" +
              "                \"city\": \"Jalandhar\",\n" +
              "                \"pincode\": null,\n" +
              "                \"detail\": null,\n" +
              "                \"buildingName\": \"New Colony\",\n" +
              "                \"street\": \"New Street\",\n" +
              "                \"locality\": {\n" +
              "                    \"code\": \"JLC474\",\n" +
              "                    \"name\": \"Boota Pind\",\n" +
              "                    \"label\": \"Locality\",\n" +
              "                    \"latitude\": null,\n" +
              "                    \"longitude\": null,\n" +
              "                    \"area\": \"Area1\",\n" +
              "                    \"children\": [],\n" +
              "                    \"materializedPath\": null\n" +
              "                }\n" +
              "            }\n" +
              "        }\n" +
              "    ]\n" +
              "}";




        Object req = request;
        kafkaTemplate.send("save-pt-property",req);

    }
}
