package org.openmrs.module.filemanager.page.controller;

import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.Visit;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.context.Context;
import org.openmrs.module.emrapi.patient.PatientDomainWrapper;
import org.openmrs.module.filemanager.UploadedFile;
import org.openmrs.module.filemanager.api.FileManagerService;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.InjectBeans;
import org.openmrs.ui.framework.page.PageModel;
import org.openmrs.util.OpenmrsUtil;
import org.openmrs.web.WebConstants;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class FileManagerPageController {

	private AdministrationService administrationService = Context.getAdministrationService();

	public void controller(@RequestParam("patientId") Patient patient,
						   @RequestParam("visitId") Visit visit,
						   PageModel model,
						   @InjectBeans PatientDomainWrapper patientDomainWrapper) {

		EncounterService encounterService = Context.getEncounterService();
		ConceptService conceptService = Context.getConceptService();

		EncounterType encounterType = encounterService.getEncounterType(8);
		Set<Encounter> encounters = visit.getEncounters();

		List<UploadedFile> fileList = new ArrayList<UploadedFile>();

		for (Encounter e : encounters) {
			if (e.getEncounterType().getUuid().equals(encounterType.getUuid())) {
				UploadedFile uf = new UploadedFile();
				for (Obs o : e.getObs()) {
					if (o.getConcept().equals(conceptService.getConcept(247))) {
						uf.description = o.getValueText();
					} else if (o.getConcept().equals(conceptService.getConcept(248))) {
						uf.notes = o.getValueText();
					} else if (o.getConcept().equals(conceptService.getConcept(246))) {
						String fileName = o.getValueComplex().split("\\|")[1];
						uf.name = fileName;
						uf.url = OpenmrsUtil.getApplicationDataDirectory() + "complex_obs/" + fileName;
					}
				}
				fileList.add(uf);
			}
		}

		patientDomainWrapper.setPatient(patient);

		model.addAttribute("patient", patientDomainWrapper);
		model.addAttribute("files", fileList);
		model.addAttribute("defaultDescriptions",
				administrationService.getGlobalProperty("filemanager.defaultDescriptions"));
	}

	public String post(@RequestParam("patientId") Patient patient,
					   @RequestParam("visitId") Visit visit,
					   @RequestParam(value = "returnUrl", required = false) String returnUrl,
					   HttpServletRequest request,
					   UiUtils ui) {
		if (request instanceof MultipartHttpServletRequest) {
			MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
			MultipartFile file = multipartRequest.getFile("file");
			if (!file.isEmpty()) {
				if (file.getSize() <= 5242880) {
					try {
						Encounter encounter = Context.getService(FileManagerService.class).saveComplexObs(patient, visit, file,
								request.getParameter("description"), request.getParameter("notes"));
					} catch (Exception ex) {
						System.out.println(ex.getMessage());
					}
				} else {
					request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR,
							"feedback.notification.feedback.error");
				}
			}
		}
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("patientId", patient.getId());
		params.put("visitId", visit.getId());
		params.put("returnUrl", returnUrl);
		return "redirect:" + ui.pageLink("filemanager", "fileManager", params);
	}
}