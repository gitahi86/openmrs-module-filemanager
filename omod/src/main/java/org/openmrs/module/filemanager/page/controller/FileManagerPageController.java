package org.openmrs.module.filemanager.page.controller;

import org.openmrs.Person;
import org.openmrs.Patient;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.emrapi.patient.PatientDomainWrapper;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.page.PageModel;
import org.openmrs.ui.framework.annotation.InjectBeans;
import org.openmrs.ui.framework.page.PageModel;
import org.openmrs.web.WebConstants;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class FileManagerPageController {

    private AdministrationService administrationService = Context.getAdministrationService();

	public void controller(@RequestParam("patientId") Patient patient,
                           PageModel model,
                           @InjectBeans PatientDomainWrapper patientDomainWrapper) {

		List<Person> personList = new ArrayList<Person>();
		for (int i = 1; i <= 10; i++) {
			Person p = new Person();
			p.setPersonId(i);
			p.setBirthdate(new Date());
			p.setGender("Type " + i);
			p.setPersonVoidReason("Link " + i);
			personList.add(p);
		}

        patientDomainWrapper.setPatient(patient);

        model.addAttribute("patient", patientDomainWrapper);
//		model.addAttribute("user", sessionContext.getCurrentUser());
		model.addAttribute("files", personList);
        model.addAttribute("defaultDescriptions", 
            administrationService.getGlobalProperty("filemanager.defaultDescriptions"));
	}

    public String post(@RequestParam(value = "patientId", required = false) Patient patient,
                       @RequestParam(value = "returnUrl", required = false) String returnUrl,
                       UiUtils ui,
                       HttpServletRequest request, PageModel model) {
        if (request instanceof MultipartHttpServletRequest) {
            MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
            MultipartFile file = multipartRequest.getFile("file");
            if (!file.isEmpty()) {
                if (file.getSize() <= 5242880) {
                    try {
                        FileOutputStream fos = new FileOutputStream("/home/gitahi/aaaaa2.jpg");
                        fos.write(file.getBytes());
                        fos.close();
                    } catch (Exception ex) {

                    }
                } else {
                    request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR,
                            "feedback.notification.feedback.error");
                    return null;
                }
            }
        }
        return null;
    }
}