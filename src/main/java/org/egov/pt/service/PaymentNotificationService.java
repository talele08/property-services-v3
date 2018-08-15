package org.egov.pt.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.pt.config.PropertyConfiguration;
import org.egov.pt.producer.Producer;
import org.egov.pt.repository.ServiceRequestRepository;
import org.egov.pt.util.PTConstants;
import org.egov.pt.web.models.Property;
import org.egov.pt.web.models.PropertyCriteria;
import org.egov.pt.web.models.SMSRequest;
import org.egov.tracer.model.CustomException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;

@Slf4j
@Service
public class PaymentNotificationService {

    @Autowired
    private Producer producer;

    @Autowired
    private ServiceRequestRepository serviceRequestRepository;

    @Autowired
    private PropertyConfiguration propertyConfiguration;

    @Autowired
    private PropertyService propertyService;

    @Value("${egov.localization.host}")
    private String localizationHost;

    @Value("${egov.localization.context.path}")
    private String localizationContextPath;

    @Value("${egov.localization.search.endpoint}")
    private String localizationSearchEndpoint;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Generates message from the received object and sends SMSRequest to kafka queue
     * @param record The Object received from kafka topic
     * @param topic The topic name from which Object is received
     */
    public void process(HashMap<String, Object> record,String topic){
        Map<String,String> valMap;
        List<String> mobileNumbers;
        RequestInfo requestInfo;

        try{
            String jsonString = new JSONObject(record).toString();
            DocumentContext documentContext = JsonPath.parse(jsonString);
            requestInfo = objectMapper.convertValue(record.get("RequestInfo"),RequestInfo.class);
            if(requestInfo==null)
                requestInfo = new RequestInfo();
            if(topic.equalsIgnoreCase(propertyConfiguration.getReceiptTopic()))
                valMap = getValuesFromReceipt(documentContext);
            else
                valMap = getValuesFromTransaction(documentContext);

            Map<String,List<String>> propertyAttributes = getPropertyAttributes(valMap,requestInfo);
            mobileNumbers = propertyAttributes.get("mobileNumbers");
            addUserNumber(topic,requestInfo,valMap,mobileNumbers);
            valMap.put("financialYear",propertyAttributes.get("financialYear").get(0));
            valMap.put("oldPropertyId",propertyAttributes.get("oldPropertyId").get(0));
        }
        catch (Exception e)
        {   e.printStackTrace();
            throw new CustomException("PARSING ERROR","Error while parsing the json received from kafka");
        }

        try{
            StringBuilder uri = getUri(valMap.get("tenantId"));
            LinkedHashMap responseMap = (LinkedHashMap)serviceRequestRepository.fetchResult(uri, requestInfo);
            String messagejson = new JSONObject(responseMap).toString();
            List<SMSRequest> smsRequests = new ArrayList<>();

            if(topic.equalsIgnoreCase(propertyConfiguration.getReceiptTopic()) ||
                    (topic.equalsIgnoreCase(propertyConfiguration.getPgTopic()) && "FAILURE".equalsIgnoreCase(valMap.get("txnStatus")))){
                String path = getJsonPath(topic,valMap);
                Object messageObj = JsonPath.parse(messagejson).read(path);
                String message = ((ArrayList<String>)messageObj).get(0);
                String customMessage = getCustomizedMessage(valMap,message,path);
                smsRequests = getSMSRequests(mobileNumbers,customMessage);
            }
            if(valMap.get("oldPropertyId")==null && topic.equalsIgnoreCase(propertyConfiguration.getReceiptTopic()))
                smsRequests.addAll(addOldpropertyIdAbsentSMS(messagejson,valMap,mobileNumbers));
            sendSMS(smsRequests);
        }
        catch(Exception e)
        {throw new CustomException("LOCALIZATION ERROR","Unable to get message from localization");}
    }

    /**
     * Generate and returns SMSRequest if oldPropertyId is not present
     * @param messagejson The list of messages received from localization
     * @param valMap The map containing all the values as key,value pairs
     * @param mobileNumbers The list of mobileNumbers to which sms are to be sent
     * @return List of SMS request to be sent
     */
    private List<SMSRequest> addOldpropertyIdAbsentSMS(String messagejson,Map<String,String> valMap,List<String> mobileNumbers){
        String path = "$..messages[?(@.code==\"{}\")].message";
        path = path.replace("{}",PTConstants.NOTIFICATION_OLDPROPERTYID_ABSENT);
        Object messageObj = JsonPath.parse(messagejson).read(path);
        String message = ((ArrayList<String>)messageObj).get(0);
        String customMessage = getCustomizedOldPropertyIdAbsentMessage(message,valMap);
        return getSMSRequests(mobileNumbers,customMessage);
    }


    /**
     * Returns the map of the values required from the record
     * @param documentContext The DocumentContext of the record Object
     * @return The required values as key,value pair
     */
    private Map<String,String> getValuesFromReceipt(DocumentContext documentContext){
        BigDecimal totalAmount,amountPaid;
        String consumerCode,transactionId,paymentMode,tenantId;
        Map<String,String> valMap = new HashMap<>();



        try{
            totalAmount = new BigDecimal((Integer)documentContext.read("$.Receipt[0].Bill[0].billDetails[0].totalAmount"));
            valMap.put("totalAmount",totalAmount.toString());

            amountPaid = new BigDecimal((Integer)documentContext.read("$.Receipt[0].instrument.amount"));
            valMap.put("amountPaid",amountPaid.toString());
            valMap.put("amountDue",totalAmount.subtract(amountPaid).toString());

            consumerCode = documentContext.read("$.Receipt[0].Bill[0].billDetails[0].consumerCode");
            valMap.put("consumerCode",consumerCode);
            valMap.put("propertyId",consumerCode.split(":")[0]);
            valMap.put("assessmentNumber",consumerCode.split(":")[1]);

            transactionId = documentContext.read("$.Receipt[0].instrument.transactionNumber");
            valMap.put("transactionId",transactionId);

            paymentMode = documentContext.read("$.Receipt[0].instrument.instrumentType.name");
            valMap.put("paymentMode",paymentMode);

            tenantId = documentContext.read("$.Receipt[0].tenantId");
            valMap.put("tenantId",tenantId);
        }
        catch (Exception e)
        {
            throw new CustomException("PARSING ERROR","Failed to fetch values from the Receipt Object");
        }

        return valMap;
    }

    /**
     * Returns the map of the values required from the record
     * @param documentContext The DocumentContext of the record Object
     * @return The required values as key,value pair
     */
    private Map<String,String> getValuesFromTransaction(DocumentContext documentContext){
        String txnStatus,txnAmount,moduleId,tenantId,mobileNumber;
        HashMap<String,String> valMap = new HashMap<>();

        try{
            txnStatus = documentContext.read("$.Transaction[0].txnStatus");
            valMap.put("txnStatus",txnStatus);

            txnAmount = documentContext.read("$.Transaction[0].txnAmount");
            valMap.put("txnAmount",txnAmount.toString());

            tenantId = documentContext.read("$.Transaction[0].tenantId");
            valMap.put("tenantId",tenantId);

            moduleId = documentContext.read("$.Transaction[0].moduleId");
            valMap.put("moduleId",moduleId);
            valMap.put("propertyId",moduleId.split(":")[0]);
            valMap.put("assessmentNumber",moduleId.split(":")[1]);

            mobileNumber = documentContext.read("$.Transaction[0].user.mobileNumber");
            valMap.put("mobileNumber",mobileNumber);
        }
        catch (Exception e)
        {  e.printStackTrace();
        //    throw new CustomException("PARSING ERROR","Failed to fetch values from the Transaction Object");
        }

        return valMap;
    }

    /**
     * Searches the property and extracts the needed values in map
     * @param valMap The map of the required values
     * @param requestInfo The requestInfo of the propertyRequest
     * @return Map of required values fetched from the property
     */
    private Map<String,List<String>> getPropertyAttributes(Map<String,String> valMap, RequestInfo requestInfo){
        PropertyCriteria propertyCriteria = new PropertyCriteria();
        propertyCriteria.setPropertyDetailids(Collections.singleton(valMap.get("assessmentNumber")));
        propertyCriteria.setTenantId(valMap.get("tenantId"));
        List<Property> properties = propertyService.searchProperty(propertyCriteria,requestInfo);

        if(CollectionUtils.isEmpty(properties))
            throw new CustomException("ASSESSMENT NOT FOUND","The assessment for the given consumer code is not available");

        // Extracting all the mobileNumbers to which notification be sent
        List<String> mobileNumbers = new LinkedList<>();
        properties.forEach(property -> {
            property.getPropertyDetails().forEach(propertyDetail -> {
                propertyDetail.getOwners().forEach(owner -> {
                    mobileNumbers.add(owner.getMobileNumber());
                });
            });
        });

        String fianancialYear = properties.get(0).getPropertyDetails().get(0).getFinancialYear();
        String oldPropertyId = properties.get(0).getOldPropertyId();

        Map<String,List<String>> propertyAttributes = new HashMap<>();
        propertyAttributes.put("mobileNumbers",mobileNumbers);
        propertyAttributes.put("financialYear",Collections.singletonList(fianancialYear));
        propertyAttributes.put("oldPropertyId",Collections.singletonList(oldPropertyId));

        return propertyAttributes;
    }

     private void addUserNumber(String topic,RequestInfo requestInfo,Map<String,String> valMap,List<String> mobileNumbers)
     {
         // If the requestInfo is of citizen add citizen's MobileNumber
         if(topic.equalsIgnoreCase(propertyConfiguration.getReceiptTopic()) && requestInfo.getUserInfo().getType().equalsIgnoreCase("CITIZEN"))
             mobileNumbers.add(requestInfo.getUserInfo().getMobileNumber());
         else if(topic.equalsIgnoreCase(propertyConfiguration.getPgTopic()))
             mobileNumbers.add(valMap.get("mobileNumber"));
     }

    /**
     * Returns the uri for the localization call
     * @param tenantId TenantId of the propertyRequest
     * @return The uri for localization search call
     */
    private StringBuilder getUri(String tenantId){
        if(propertyConfiguration.getIsStateLevel())
            tenantId = tenantId.split("\\.")[0];
        StringBuilder uri = new StringBuilder();
        uri.append(localizationHost).append(localizationContextPath).append(localizationSearchEndpoint);
        uri.append("?").append("locale=").append(PTConstants.NOTIFICATION_LOCALE)
                .append("&tenantId=").append(tenantId)
                .append("&module=").append(PTConstants.MODULE);
        return uri;
    }


    /**
     *  Returns the jsonPath
     * @param topic The topic name from which object is received
     * @param valMap The map of the required values
     * @return  The jsonPath
     */
    private String getJsonPath(String topic,Map<String,String> valMap){
        String path = "$..messages[?(@.code==\"{}\")].message";
        String paymentMode = valMap.get("paymentMode");
        if(topic.equalsIgnoreCase(propertyConfiguration.getReceiptTopic()) && paymentMode.equalsIgnoreCase("online"))
            path = path.replace("{}",PTConstants.NOTIFICATION_PAYMENT_ONLINE);

        if(topic.equalsIgnoreCase(propertyConfiguration.getReceiptTopic()) && !paymentMode.equalsIgnoreCase("online"))
            path = path.replace("{}",PTConstants.NOTIFICATION_PAYMENT_OFFLINE);

        if(topic.equalsIgnoreCase(propertyConfiguration.getPgTopic()))
            path = path.replace("{}",PTConstants.NOTIFICATION_PAYMENT_FAIL);

        return path;
    }

    /**
     * Returns customized message for
     * @param valMap The map of the required values
     * @param message The message template from localization
     * @param path The json path used to fetch message
     * @return Customized message depending on values in valMap
     */
   private String getCustomizedMessage(Map<String,String> valMap,String message,String path){
        String customMessage = null;
        if(path.contains(PTConstants.NOTIFICATION_PAYMENT_ONLINE))
            customMessage = getCustomizedOnlinePaymentMessage(message,valMap);
        if(path.contains(PTConstants.NOTIFICATION_PAYMENT_OFFLINE))
            customMessage = getCustomizedOfflinePaymentMessage(message,valMap);
        if(path.contains(PTConstants.NOTIFICATION_PAYMENT_FAIL))
            customMessage = getCustomizedPaymentFailMessage(message,valMap);
        if(path.contains(PTConstants.NOTIFICATION_OLDPROPERTYID_ABSENT))
            customMessage = getCustomizedOldPropertyIdAbsentMessage(message,valMap);
        return customMessage;
    }

    /**
     * @param message The message template from localization
     * @param valMap The map of the required values
     * @return Customized message depending on values in valMap
     */
   private String getCustomizedOnlinePaymentMessage(String message,Map<String,String> valMap){
        message = message.replace("< insert amount paid>",valMap.get("amountPaid"));
        message = message.replace("< insert payment transaction id from PG>",valMap.get("transactionId"));
        message = message.replace("<insert Property Tax Assessment ID>",valMap.get("propertyId"));
        message = message.replace("<pt due>.",valMap.get("amountDue"));
        message = message.replace("<FY>",valMap.get("financialYear"));
        return message;
   }


    /**
     * @param message The message template from localization
     * @param valMap The map of the required values
     * @return Customized message depending on values in valMap
     */
   private String getCustomizedOfflinePaymentMessage(String message,Map<String,String> valMap){
        message = message.replace("<amount>",valMap.get("amountPaid"));
        message = message.replace("<insert mode of payment>",valMap.get("paymentMode"));
        message = message.replace("<Enter pending amount>",valMap.get("amountDue"));
        message = message.replace("<insert inactive citizen application web URL>.",PTConstants.NOTIFICATION_CITIZEN_URL);
        message = message.replace("<Insert FY>",valMap.get("financialYear"));
        return message;
    }


    /**
     * @param message The message template from localization
     * @param valMap The map of the required values
     * @return Customized message depending on values in valMap
     */
   private String getCustomizedPaymentFailMessage(String message,Map<String,String> valMap){
        message = message.replace("<insert amount to pay>",valMap.get("txnAmount"));
        message = message.replace("<insert ID>",valMap.get("propertyId"));
        message = message.replace("<FY>",valMap.get("financialYear"));
        return message;
   }


    /**
     * @param message The message template from localization
     * @param valMap The map of the required values
     * @return Customized message depending on values in valMap
     */
   private String getCustomizedOldPropertyIdAbsentMessage(String message,Map<String,String> valMap){
        message = message.replace("<insert Property Tax Assessment ID>",valMap.get("propertyId"));
        message = message.replace("<FY>",valMap.get("financialYear"));
        return  message;
   }


    /**
     * Creates SMSRequest for the given mobileNumber with the given message
     * @param mobileNumbers The set of mobileNumber for which SMSRequest has to be created
     * @param customizedMessage The message to sent
     * @return List of SMSRequest
     */
    private List<SMSRequest> getSMSRequests(List<String> mobileNumbers, String customizedMessage){
        List<SMSRequest> smsRequests = new ArrayList<>();
        mobileNumbers.forEach(mobileNumber-> {
            if(mobileNumber!=null)
            {
                SMSRequest smsRequest = new SMSRequest(mobileNumber,customizedMessage);
                smsRequests.add(smsRequest);
            }
        });
        return smsRequests;
    }

    /**
     * Send the SMSRequest on the SMSNotification kafka topic
     * @param smsRequestList The list of SMSRequest to be sent
     */
    private void sendSMS(List<SMSRequest> smsRequestList){
        if (propertyConfiguration.getIsSMSNotificationEnabled()) {
            if (CollectionUtils.isEmpty(smsRequestList))
                log.error("Messages from localization couldn't be fetched!");
            for(SMSRequest smsRequest: smsRequestList) {
                producer.push(propertyConfiguration.getSmsNotifTopic(), smsRequest);
            }
        }
    }




}
