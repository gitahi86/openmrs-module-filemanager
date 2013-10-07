package org.openmrs.module.filemanager;

public class UploadedFile {
    public String url;
    public String description;
    public String notes;

    public void UploadedFile() {
    }

    public void UploadedFile(String url, String description, String notes) {
        this.url = url;
        this.description = description;
        this.notes = notes;
    }
}