package org.egov.pt.web.controllers;

import javax.validation.Valid;

import org.egov.common.contract.request.RequestInfo;
import org.egov.pt.service.DraftsService;
import org.egov.pt.web.models.DraftRequest;
import org.egov.pt.web.models.DraftResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/drafts")
public class DraftsController {
	
	@Autowired
	private DraftsService draftsService;
	
	
	@RequestMapping(value = "/_create", method = RequestMethod.POST)
	public ResponseEntity<DraftResponse> create(@Valid @RequestBody DraftRequest draftRequest) {
		DraftResponse response = draftsService.createDraft(draftRequest);
		return new ResponseEntity<>(response, HttpStatus.CREATED);
	}
	
	@RequestMapping(value = "/_update", method = RequestMethod.POST)
	public ResponseEntity<DraftResponse> update(@Valid @RequestBody DraftRequest draftRequest) {
		DraftResponse response = draftsService.updateDraft(draftRequest);
		return new ResponseEntity<>(response, HttpStatus.CREATED);
	}
	
	@RequestMapping(value = "/_search", method = RequestMethod.POST)
	public ResponseEntity<DraftResponse> update(@Valid @RequestBody RequestInfo requestInfo, 
			@RequestParam(value = "userId") String userId, @RequestParam(value = "tenantId") String tenantId) {
		DraftResponse response = draftsService.searchDrafts(requestInfo, userId, tenantId);
		return new ResponseEntity<>(response, HttpStatus.CREATED);
	}
}
