package org.egov.pt.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.pt.repository.ServiceRequestRepository;
import org.egov.pt.web.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@Slf4j
public class UserService {

    @Autowired
    private ServiceRequestRepository serviceRequestRepository;

    @Value("${egov.user.host}")
    private String userHost;

    @Value("${egov.user.context.path}")
    private String userContextPath;

    @Value("${egov.user.create.path}")
    private String userCreateEndpoint;

    @Value("${egov.user.search.path}")
    private String userSearchEndpoint;

    @Value("${egov.user.update.path}")
    private String userUpdateEndpoint;

    public void createUser(PropertyRequest request){
        StringBuilder uri = new StringBuilder(userHost).append(userContextPath).append(userCreateEndpoint);
        List<Property> properties = request.getProperties();
        RequestInfo requestInfo = request.getRequestInfo();
        properties.forEach(property -> {
            property.getPropertyDetails().forEach(propertyDetail -> {
                Set<String> listOfMobileNumbers = getMobileNumbers(propertyDetail);
                propertyDetail.getOwners().forEach(owner -> {
                    if(owner.getUuid()==null){
                        setUserName(owner,listOfMobileNumbers);
                        UserDetailResponse userDetailResponse = userExists(owner,requestInfo);
                        if(CollectionUtils.isEmpty(userDetailResponse.getUser()))
                        {   LinkedHashMap responseMap = (LinkedHashMap)serviceRequestRepository.fetchResult(uri, new CreateUserRequest(requestInfo,owner));
                            parseResponse(responseMap,"dd/MM/yyyy");
                            ObjectMapper mapper = new ObjectMapper();
                            userDetailResponse = mapper.convertValue(responseMap,UserDetailResponse.class);
                        }
                        setOwnerFields(owner,userDetailResponse);
                    }
                });
            });
        });
    }


    private UserDetailResponse userExists(OwnerInfo owner,RequestInfo requestInfo){
        UserSearchRequest userSearchRequest =new UserSearchRequest();
        userSearchRequest.setTenantId(owner.getTenantId());
        userSearchRequest.setUserName(owner.getUserName());
        userSearchRequest.setMobileNumber(owner.getMobileNumber());
        userSearchRequest.setName(owner.getName());
        userSearchRequest.setRequestInfo(requestInfo);
        userSearchRequest.setActive(false);
        if(owner.getUuid()!=null)
         userSearchRequest.setUuid(Arrays.asList(owner.getUuid()));
        return searchUser(userSearchRequest);
    }

    private void setUserName(OwnerInfo owner,Set<String> listOfMobileNumber){
        if(listOfMobileNumber.contains(owner.getMobileNumber())){
            owner.setUserName(owner.getMobileNumber());
            listOfMobileNumber.remove(owner.getMobileNumber());
        }
        else {
            String username = (Long.toString(new Date().getTime())).substring(3);
            owner.setUserName(username);
        }
    }

    private Set<String> getMobileNumbers(PropertyDetail propertyDetail){
        Set<String> listOfMobileNumbers = new HashSet<>();
        propertyDetail.getOwners().forEach(owner -> {listOfMobileNumbers.add(owner.getMobileNumber());});
        return listOfMobileNumbers;
    }

    public UserDetailResponse getUser(PropertyCriteria criteria,RequestInfo requestInfo){
        UserSearchRequest userSearchRequest = getUserSearchRequest(criteria,requestInfo);
        UserDetailResponse userDetailResponse = searchUser(userSearchRequest);
        return userDetailResponse;
    }

    public UserDetailResponse searchUser(UserSearchRequest userSearchRequest) {
        StringBuilder uri = new StringBuilder(userHost).append(userSearchEndpoint);
        LinkedHashMap responseMap = (LinkedHashMap)serviceRequestRepository.fetchResult(uri, userSearchRequest);
        parseResponse(responseMap,"dd-MM-yyyy");
        ObjectMapper mapper = new ObjectMapper();
        UserDetailResponse userDetailResponse = mapper.convertValue(responseMap,UserDetailResponse.class);
        return userDetailResponse;
    }


    private void parseResponse(LinkedHashMap responeMap,String dobFormat){
        List<LinkedHashMap> users = (List<LinkedHashMap>)responeMap.get("user");
        String format1 = "dd-MM-yyyy HH:mm:ss";
        users.forEach( map -> {
            map.put("createdDate",dateTolong((String)map.get("createdDate"),format1));
            map.put("lastModifiedDate",dateTolong((String)map.get("lastModifiedDate"),format1));
            map.put("dob",dateTolong((String)map.get("dob"),dobFormat));
            map.put("pwdExpiryDate",dateTolong((String)map.get("pwdExpiryDate"),format1)); }
        );
    }


    private Long dateTolong(String date,String format){
        SimpleDateFormat f = new SimpleDateFormat(format);
        Date d = null;
        try {
            d = f.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return  d.getTime();
    }


    private void setOwnerFields(OwnerInfo owner, UserDetailResponse userDetailResponse){
        owner.setUuid(userDetailResponse.getUser().get(0).getUuid());
        owner.setCreatedBy(userDetailResponse.getUser().get(0).getCreatedBy());
        owner.setCreatedDate(userDetailResponse.getUser().get(0).getCreatedDate());
        owner.setLastModifiedBy(userDetailResponse.getUser().get(0).getLastModifiedBy());
        owner.setLastModifiedDate(userDetailResponse.getUser().get(0).getLastModifiedDate());
        owner.setActive(userDetailResponse.getUser().get(0).getActive());
    }

    private UserSearchRequest getUserSearchRequest(PropertyCriteria criteria,RequestInfo requestInfo){
        UserSearchRequest userSearchRequest = new UserSearchRequest();
        Set<String> userIds = criteria.getOwnerids();
        if(!CollectionUtils.isEmpty(userIds))
         userSearchRequest.setUuid( new ArrayList(userIds));
        userSearchRequest.setRequestInfo(requestInfo);
        userSearchRequest.setTenantId(criteria.getTenantId());
        userSearchRequest.setMobileNumber(criteria.getMobileNumber());
        userSearchRequest.setUserName(criteria.getUserName());
        userSearchRequest.setName(criteria.getName());
        userSearchRequest.setActive(false);
        return userSearchRequest;
    }


    public void updateUser(PropertyRequest request){
        List<Property> properties = request.getProperties();
        RequestInfo requestInfo = request.getRequestInfo();
        properties.forEach(property -> {
            property.getPropertyDetails().forEach(propertyDetail -> {
                propertyDetail.getOwners().forEach(owner -> {
                    UserDetailResponse userDetailResponse = userExists(owner,requestInfo);
                    StringBuilder uri  = new StringBuilder(userHost);
                    String dobFormat;
                    if(CollectionUtils.isEmpty(userDetailResponse.getUser())) {
                        uri = uri.append(userContextPath).append(userCreateEndpoint);
                        dobFormat="dd/MM/yyyy";
                    }
                    else
                    { owner.setId(userDetailResponse.getUser().get(0).getId());
                      uri=uri.append(userContextPath).append(owner.getId()).append(userUpdateEndpoint);
                      dobFormat = "dd-MM-yyyy";
                    }
                    LinkedHashMap responseMap = (LinkedHashMap)serviceRequestRepository.fetchResult(uri, new CreateUserRequest(requestInfo,owner));
                    parseResponse(responseMap,dobFormat);
                    ObjectMapper mapper = new ObjectMapper();
                    userDetailResponse = mapper.convertValue(responseMap,UserDetailResponse.class);
                    setOwnerFields(owner,userDetailResponse);
                });
            });
        });
    }



}
