<%
ui.decorateWith("appui", "standardEmrPage")
ui.includeJavascript("uicommons", "typeahead.js");
%>

${ ui.includeFragment("coreapps", "patientHeader", [ patient: patient.patient ]) }

<div id="content" class="container">
    <h1>${ui.message("filemanager.upload.title")}</h1>
    <fieldset>
        <legend>${ui.message("filemanager.upload.file")}</legend>
        <form class="simple-form-ui" id="uploadFile" method="post"
        enctype="multipart/form-data">
            <input type="file" name="file" id="file" multiple="true">
            <input type="text" id="typeahead" data-provide="typeahead"
                placeholder="Description">
            <textarea name="notes" placeholder="Notes"></textarea>

            <input type="submit" value="Upload">
        </form>
    </fieldset>
</div>

<table>
    <thead>
        <tr>
            <th>${ui.message("filemanager.upload.date")}</th>
            <th>${ui.message("filemanager.upload.type")}</th>
            <th>${ui.message("filemanager.upload.file")}</th>
        </tr>
    </thead>
    <tbody>
        <% if (files.size() == 0) { %>
        <tr>
            <td colspan="4">${ui.message("coreapps.none")}</td>
        </tr>
        <% } %>
        <% files.each { f ->
            def date = f.birthdate
            def type = f.gender
            def link = f.personVoidReason
        %>
        <tr id="visit-${f.personId}">
            <td>${ui.format(date)}</td>
            <td>${ui.format(type)}</td>
            <td>${ui.format(link)}</td>
        </tr>
        <% } %>
    </tbody>
</table>

<script type="text/javascript">
    var jq = jQuery;
    var breadcrumbs = [
        { icon: "icon-home", link: '/' + OPENMRS_CONTEXT_PATH + '/index.htm' },
        { label: "${ ui.message("filemanager.label")}"}
    ];

    jq('#typeahead').typeahead({
        source: function() {
            var descriptions = "${defaultDescriptions}";
            return descriptions.split(',');
        },
        //prefetch: '${ui.resourceLink("filemanager", "filetypes.json")}', 
        items: 3
    })
</script>