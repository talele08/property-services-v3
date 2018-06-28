package org.egov.pt.service;

import org.dozer.DozerBeanMapper;
import org.egov.common.contract.request.RequestInfo;
import org.egov.pt.config.PropertyConfiguration;
import org.egov.pt.repository.IdGenRepository;
import org.egov.pt.util.PropertyUtil;
import org.egov.pt.web.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class EnrichmentService {


    @Autowired
    PropertyUtil propertyutil;

    @Autowired
    IdGenRepository idGenRepository;

    @Autowired
    private PropertyConfiguration config;



    /**
     * Assigns UUIDs to all id fields and also assigns acknowledgementnumber and assessmentnumber generated from idgen
     * @param request  PropertyRequest received for property creation
     * @param onlyPropertyDetail if true only the fields related to propertyDetail are enriched(assigned)
     */
    public void enrichCreateRequest(PropertyRequest request,Boolean onlyPropertyDetail) {
        RequestInfo requestInfo = request.getRequestInfo();
        AuditDetails auditDetails = propertyutil.getAuditDetails(requestInfo.getUserInfo().getId().toString(), !onlyPropertyDetail);

        for (Property property : request.getProperties()) {
            if(!onlyPropertyDetail)
             property.getAddress().setId(UUID.randomUUID().toString());
         //  property.setAccountId(request.getRequestInfo().getUserInfo().getUuid());
            property.setAuditDetails(auditDetails);
            property.setStatus(PropertyInfo.StatusEnum.ACTIVE);
            property.getPropertyDetails().forEach(propertyDetail -> {
                propertyDetail.setAssessmentNumber(UUID.randomUUID().toString());
                propertyDetail.getUnits().forEach(unit -> unit.setId(UUID.randomUUID().toString()));
                propertyDetail.getDocuments().forEach(document -> document.setId(UUID.randomUUID().toString()));
                propertyDetail.setAssessmentDate(new Date().getTime());
            });
        }
        if(!onlyPropertyDetail)
         setIdgenIds(request);
    }

    /**
     * Assigns UUID for new fields that are added and sets propertyDetail and address ids from propertyId
     * @param request  PropertyRequest received for property update
     * @param propertiesFromResponse Properties returned by calling search based on ids in PropertyRequest
     */
    public void enrichUpdateRequest(PropertyRequest request,List<Property> propertiesFromResponse) {
        RequestInfo requestInfo = request.getRequestInfo();
        AuditDetails auditDetails = propertyutil.getAuditDetails(requestInfo.getUserInfo().getId().toString(), false);

        /*Map of propertyId to property is created from the responseproperty list
        * Not required if address id is sent in request was used before when one to one mapping was
        * present between property and propertyDetail
        * */
        Map<String,Property> idToProperty = new HashMap<>();
        propertiesFromResponse.forEach(propertyFromResponse -> {
            idToProperty.put(propertyFromResponse.getPropertyId(),propertyFromResponse);
        });

        /*For every Property if id of any subfield is null new uuid is assigned
         *
          * */
        for (Property property : request.getProperties()){
            property.setAuditDetails(auditDetails);

            //Not Required **
            String id = property.getPropertyId();
            Property responseProperty = idToProperty.get(id);
            property.getAddress().setId(responseProperty.getAddress().getId());
            /**/

            property.getPropertyDetails().forEach(propertyDetail -> {
                if (propertyDetail.getAssessmentNumber() == null)
                    propertyDetail.setAssessmentNumber(UUID.randomUUID().toString());
                Set<Document> documents = propertyDetail.getDocuments();
                Set<Unit> units=propertyDetail.getUnits();

                if(documents!=null && !documents.isEmpty()){
                    documents.forEach(document ->{
                        if(document.getId()==null) document.setId(UUID.randomUUID().toString());
                    });
                }
                if(units!=null && !units.isEmpty()){
                    units.forEach(unit ->{
                        if(unit.getId()==null) unit.setId(UUID.randomUUID().toString());
                    });
                }
            });
        }
    }


    /**
     * Returns a list of numbers generated from idgen
     * @param requestInfo RequestInfo from the request
     * @param tenantId tenantId of the city
     * @param idKey code of the field defined in application properties for which ids are generated for
     * @param idformat format in which ids are to be generated
     * @param count Number of ids to be generated
     * @return List of ids generated using idGen service
     */
    private List<String> getIdList(RequestInfo requestInfo, String tenantId, String idKey,
                                   String idformat,int count) {
        return idGenRepository.getId(requestInfo, tenantId, idKey, idformat,count).getIdResponses().stream()
                .map(IdResponse::getId).collect(Collectors.toList());
    }

    /**
     * Sets the acknowledgement and assessment Numbers for given PropertyRequest
     * @param request PropertyRequest which is to be created
     */
    private void setIdgenIds(PropertyRequest request) {
        RequestInfo requestInfo = request.getRequestInfo();
        String tenantId = request.getProperties().get(0).getTenantId();
        List<Property> properties = request.getProperties();

        List<String> acknowledgementNumbers = getIdList(requestInfo,tenantId,config.getAcknowldgementIdGenName(),config.getAcknowldgementIdGenFormat(),request.getProperties().size());
        for(int i=0;i<acknowledgementNumbers.size();i++)
        { properties.get(i).setAcknowldgementNumber(acknowledgementNumbers.get(i)); }

        List<String> propertyIds = getIdList(requestInfo,tenantId,config.getAcknowldgementIdGenName(),config.getAcknowldgementIdGenFormat(),request.getProperties().size());
        for(int i=0;i<propertyIds.size();i++)
        { properties.get(i).setPropertyId(propertyIds.get(i)); }
    }


    /**
     * Populates the owner fields inside of property objects from the response got from calling user api
     * @param userDetailResponse response from user api which contains list of user which are used to populate owners in properties
     * @param properties List of property whose owner's are to be populated from userDetailResponse
     */
    public void enrichOwner(UserDetailResponse userDetailResponse, List<Property> properties){
        List<OwnerInfo> users = userDetailResponse.getUser();
        Map<String,OwnerInfo> userIdToOwnerMap = new HashMap<>();
        users.forEach(user -> userIdToOwnerMap.put(user.getUuid(),user));
        DozerBeanMapper dozerBeanMapper = new DozerBeanMapper();
        properties.forEach(property -> {
            property.getPropertyDetails().forEach(propertyDetail -> propertyDetail.getOwners().forEach(owner -> addOwnerDetail(owner,userIdToOwnerMap,dozerBeanMapper)));
        });
    }

   /* private void addOwnerDetail(OwnerInfo owner,Map<Long,OwnerInfo> userIdToOwnerMap){
        OwnerInfo user = userIdToOwnerMap.get(owner.getUuid());
        owner.setLastModifiedDate(user.getLastModifiedDate());
        owner.setLastModifiedDate(user.getLastModifiedDate());
        owner.setCreatedBy(user.getCreatedBy());
        owner.setCreatedDate(user.getCreatedDate());
        owner.setUserName(user.getUserName());
        owner.setPassword(user.getPassword());
        owner.setSalutation(user.getSalutation());
        owner.setName(user.getName());
        owner.setGender(user.getGender());
        owner.setMobileNumber(user.getMobileNumber());
        owner.setEmailId(user.getEmailId());
        owner.setAltContactNumber(user.getAltContactNumber());
        owner.setPan(user.getPan());
        owner.setAadhaarNumber(user.getAadhaarNumber());
        owner.setPermanentAddress(user.getPermanentAddress());
        owner.setPermanentCity(user.getPermanentCity());
        owner.setPermanentPincode(user.getPermanentPincode());
        owner.setCorrespondenceAddress(user.getCorrespondenceAddress());
        owner.setCorrespondenceCity(user.getCorrespondenceCity());
        owner.setCorrespondencePincode(user.getCorrespondencePincode());
        owner.setActive(user.getActive());
        owner.setDob(user.getDob());
        owner.setPwdExpiryDate(user.getPwdExpiryDate());
        owner.setLocale(user.getLocale());
        owner.setType(user.getType());
        owner.setAccountLocked(user.getAccountLocked());
        owner.setRoles(user.getRoles());
        owner.setFatherOrHusbandName(user.getFatherOrHusbandName());
        owner.setBloodGroup(user.getBloodGroup());
        owner.setIdentificationMark(user.getIdentificationMark());
        owner.setPhoto(user.getPhoto());
    }*/


    /**
     * Copies all fields to owner from the corresponding ownerInfo in userIdToOwnerMap
     * @param owner Owner whose fields are to be populated
     * @param userIdToOwnerMap Map of userId to OwnerInfo
     */
    private void addOwnerDetail(OwnerInfo owner,Map<String,OwnerInfo> userIdToOwnerMap,DozerBeanMapper dozerBeanMapper ){
        OwnerInfo user = userIdToOwnerMap.get(owner.getUuid());
        // Is the condition correct or we should throw error?
        if(user!=null)
         dozerBeanMapper.map(user,owner);
    }


    /**
     * Populates ownerids in PropertyCriteria with the uuid's of users in userDetailResponse
     * @param criteria PropertyCriteria whose ownerids are to be populated
     * @param userDetailResponse The user response that contains list of users whose uuid's are to added
     */
    public void enrichPropertyCriteriaWithOwnerids(PropertyCriteria criteria, UserDetailResponse userDetailResponse){
        if(CollectionUtils.isEmpty(criteria.getOwnerids())){
            Set<String> ownerids = new HashSet<>();
            userDetailResponse.getUser().forEach(owner -> ownerids.add(owner.getUuid()));
            criteria.setOwnerids(ownerids);
        }
    }

    /**
     * Overloaded function which populates ownerids in criteria from list of property
     * @param criteria PropertyCriteria whose ownerids are to be populated
     * @param properties List of property whose owner's uuids are to added in propertyCriteria
     */
    public void enrichPropertyCriteriaWithOwnerids(PropertyCriteria criteria, List<Property> properties){
        Set<String> ownerids = new HashSet<>();
        properties.forEach(property -> {
            property.getPropertyDetails().forEach(propertyDetail -> propertyDetail.getOwners().forEach(owner -> ownerids.add(owner.getUuid())));
        });
        criteria.setOwnerids(ownerids);
    }

    /**
     * Returns PropertyCriteria with ids populated using propertyids from properties
     * @param properties properties whose propertyids are to added to propertyCriteria for search
     * @return propertyCriteria to search on basis of propertyids
     */
    public PropertyCriteria getPropertyCriteriaFromPropertyIds(List<Property> properties){
        PropertyCriteria criteria = new PropertyCriteria();
        Set<String> propertyids = new HashSet<>();
        properties.forEach(property -> propertyids.add(property.getPropertyId()));
        criteria.setIds(propertyids);
        criteria.setTenantId(properties.get(0).getTenantId());
        return criteria;
    }


}
