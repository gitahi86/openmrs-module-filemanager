/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.filemanager.api.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.*;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.LocationService;
import org.openmrs.api.ObsService;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.filemanager.api.FileManagerService;
import org.openmrs.module.filemanager.api.db.FileManagerDAO;
import org.openmrs.obs.ComplexData;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * It is a default implementation of {@link FileManagerService}.
 */
public class FileManagerServiceImpl extends BaseOpenmrsService implements FileManagerService {

	protected final Log log = LogFactory.getLog(this.getClass());

	private FileManagerDAO dao;

	/**
	 * @param dao the dao to set
	 */
	public void setDao(FileManagerDAO dao) {
		this.dao = dao;
	}

	/**
	 * @return the dao
	 */
	public FileManagerDAO getDao() {
		return dao;
	}

	public Encounter saveComplexObs(Patient patient, Visit visit, MultipartFile file, String description, String notes) throws IOException {

		LocationService locationService = Context.getLocationService();
		ConceptService conceptService = Context.getConceptService();
		ObsService obsService = Context.getObsService();
		EncounterService encounterService = Context.getEncounterService();

		Location location = locationService.getDefaultLocation();
		Concept fileDescriptionConcept = conceptService.getConcept(247);
		Concept fileNotesConcept = conceptService.getConcept(248);
		ConceptComplex conceptComplex = conceptService.getConceptComplex(246);

		Date date = visit.getStartDatetime();

		Encounter encounter = new Encounter();
		encounter.setPatient(patient);
		encounter.setVisit(visit);
		encounter.setEncounterDatetime(date);
		encounter.setLocation(location);
		encounter.setEncounterType(encounterService.getEncounterType(8));
		encounter.setProvider(Context.getAuthenticatedUser());
		encounterService.saveEncounter(encounter);

		Set<Obs> obsSet = new HashSet<Obs>();

		Obs descriptionObs = new Obs(patient, fileDescriptionConcept, date, location);
		descriptionObs.setValueText(description);
		descriptionObs.setEncounter(encounter);
		obsService.saveObs(descriptionObs, null);

		Obs notesObs = new Obs(patient, fileNotesConcept, date, location);
		notesObs.setValueText(notes);
		notesObs.setEncounter(encounter);
		obsService.saveObs(notesObs, null);

		Obs fileObs = new Obs(patient, conceptComplex, date, location);
		fileObs.setEncounter(encounter);
		File tmpFile = multipartToFile(file);
		saveAndTransferFileComplexObs(fileObs, tmpFile);

		obsSet.add(descriptionObs);
		obsSet.add(notesObs);
		obsSet.add(fileObs);
		obsSet.add(fileObs);


		encounter.setObs(obsSet);

		return encounter;
	}

	public File multipartToFile(MultipartFile multipart) throws IllegalStateException, IOException {
		File tmpFile = new File(System.getProperty("java.io.tmpdir") + System.getProperty("file.separator") +
				multipart.getOriginalFilename());
		multipart.transferTo(tmpFile);
		return tmpFile;
	}

	public void saveAndTransferFileComplexObs(Obs obs, File tempFile) {

		try {
			String mergedUrl = tempFile.getCanonicalPath();
			InputStream out1 = new FileInputStream(new File(mergedUrl));

			ComplexData complexData = new ComplexData(obs.getPerson().getId() + "-" + Math.random() + "-" + tempFile.getName(), out1);
			obs.setComplexData(complexData);

			Context.getObsService().saveObs(obs, null);
			tempFile.delete();

		} catch (Exception e) {
			log.error(e);
		}
	}

}