package org.egov.pt.repository.builder;

import org.egov.pt.web.models.PropertyCriteria;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

@Component
public class PropertyQueryBuilder {

	public static final String INNER_JOIN_STRING = "INNER JOIN";
	public static final String LEFT_OUTER_JOIN = "LEFT OUTER JOIN";
	
	static final String QUERY = "SELECT pt.*,ptdl.*,address.*,owner.*,doc.*,unit.*,"
			+ " pt.propertyid as propertyid,ptdl.assessmentnumber as propertydetailid,doc.id as documentid,unit.id as unitid,"
			+ "address.id as addresskeyid"
			+ " FROM eg_pt_property_v2 pt "
			+ INNER_JOIN_STRING
			+ " eg_pt_propertydetail_v2 ptdl ON pt.propertyid =ptdl.property "
			+ INNER_JOIN_STRING
			+ " eg_pt_owner_v2 owner ON ptdl.assessmentnumber=owner.propertydetail "
			+ INNER_JOIN_STRING
			+ " eg_pt_unit_v2 unit ON ptdl.assessmentnumber=unit.propertydetail "
			+ INNER_JOIN_STRING
			+" eg_pt_address_v2 address on address.property=pt.propertyid "
			+ LEFT_OUTER_JOIN
			+ " eg_pt_document_v2 doc ON ptdl.assessmentnumber=doc.propertydetail "
			+ " WHERE ";
	
	public String getPropertySearchQuery(PropertyCriteria criteria, List<Object> preparedStmtList) {
		
		StringBuilder builder = new StringBuilder(QUERY);
        builder.append("pt.tenantid=?");
		preparedStmtList.add(criteria.getTenantId());
		
		Set<String> ids = criteria.getIds();
		if(!CollectionUtils.isEmpty(ids)) {
			
			builder.append(" and pt.propertyid IN ("+convertSetToString(ids)+")");

		}

		Set<String> propertyDetailids = criteria.getPropertyDetailids();
		if(!CollectionUtils.isEmpty(propertyDetailids)) {

			builder.append("and ptdl.assessmentnumber IN ("+convertSetToString(propertyDetailids)+")");

		}

		Set<String> addressids = criteria.getAddressids();
		if(!CollectionUtils.isEmpty(addressids)) {

			builder.append("and address.id IN (").append(convertSetToString(addressids)).append(")");

		}

		List<Long> ownerids = criteria.getOwnerids();
		if(!CollectionUtils.isEmpty(ownerids)) {

			builder.append("and owner.userid IN (").append(convertSetToLong(ownerids)).append(")");

		}

		Set<String> unitids = criteria.getUnitids();
		if(!CollectionUtils.isEmpty(unitids)) {

			builder.append("and unit.id IN ("+convertSetToString(unitids)+")");

		}


		Set<String> documentids = criteria.getDocumentids();
		if(!CollectionUtils.isEmpty(documentids)) {

			builder.append("and doc.id IN ("+convertSetToString(documentids)+")");

		}

		return builder.toString();
	}
	
	private String convertSetToString(Set<String> ids) {
		
		final String quotes = "'";
		final String comma = ",";
		StringBuilder builder = new StringBuilder();
		Iterator<String> iterator = ids.iterator();
		while(iterator.hasNext()) {
			builder.append(quotes+iterator.next()+quotes);
			if(iterator.hasNext()) builder.append(comma);
		}
		return builder.toString();
	}

	private String convertSetToLong(List<Long> ids) {

		final String quotes = "'";
		final String comma = ",";
		StringBuilder builder = new StringBuilder();
		Iterator<Long> iterator = ids.iterator();
		while(iterator.hasNext()) {
			builder.append(quotes+iterator.next()+quotes);
			if(iterator.hasNext()) builder.append(comma);
		}
		return builder.toString();
	}
}
