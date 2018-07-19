package org.egov.pt.util;

import org.egov.pt.web.models.OwnerInfo;
import org.egov.pt.web.models.PropertyDetail;
import org.junit.Test;

public class hashtest {


    @Test
    public void name() {
        OwnerInfo owner1 = new OwnerInfo();
        owner1.setName(null);
        owner1.setMobileNumber(null);
        owner1.setUuid("1d37c640-3e12-4290-91e4-6d53e5a5d154");
    //    owner1.setFatherOrHusbandName("arjun");

        OwnerInfo owner2 = new OwnerInfo();
        owner2.setName(null);
        owner2.setMobileNumber(null);
        owner2.setUuid("1d37c640-3e12-4290-91e4-6d53e5a5d154");
 //       owner2.setFatherOrHusbandName("gaurav");

        PropertyDetail detail = new PropertyDetail();
        detail.addOwnersItem(owner1);
        detail.addOwnersItem(owner2);

        System.out.println(detail.getOwners().size());
    }
}
