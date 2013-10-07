package org.openmrs.module.filemanager.page.controller;

import org.openmrs.ui.framework.UiUtils;
import org.openmrs.util.OpenmrsUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created with IntelliJ IDEA.
 * User: gitahi
 * Date: 10/7/13
 * Time: 4:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class DownloadPageController {

	public void controller(HttpServletRequest request,
						   UiUtils ui,
						   HttpServletResponse response) {
		String url = OpenmrsUtil.getApplicationDataDirectory() + "complex_obs/" + request.getParameter("fileName");
		OutputStream outputStream = null;
		InputStream in = null;
		try {
			in = new FileInputStream(url);
			byte[] buffer = new byte[1024];
			int bytesRead = 0;
			response.setHeader("Content-Disposition",
					"attachment;filename=\"" + url + "\"");
			outputStream = response.getOutputStream();
			while (0 < (bytesRead = in.read(buffer))) {
				outputStream.write(buffer, 0, bytesRead);
			}
		} catch (Exception ex) {

		} finally {
			try {
				if (null != in) {
					in.close();
				}
			} catch (Exception ex) {}
		}
	}
}
