package org.egov.pt.util;

import com.jayway.jsonpath.JsonPath;
import org.egov.pt.web.models.OwnerInfo;
import org.egov.pt.web.models.PropertyDetail;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.cglib.core.CollectionUtils;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

public class hashtest {

    @Test
    public void name() {

     /*   String message = "Dear Citizen, Your property has been created in the system with Property Tax Assessment ID <insert ID>. Property Address: <House No.>, <Colony Name>, <Street No.>, <Mohalla>, <City>. <Pincode>";
        message = message.replace("<insert ID>","ads")
                .replace("<House No.>","asd")
                .replace("<Colony Name>","vcg");
        System.out.println(message);*/

        /*LinkedHashMap responseMap = new LinkedHashMap();
        String jsonString = new JSONObject(responseMap).toString();
        String message = JsonPath.parse(jsonString).read("$.messages[0].message");*/


        String tenantId = "pb.amritsar";
        tenantId = tenantId.split("\\.")[0];
        System.out.println(tenantId);

    }
}
