/**
 * Copyright (C) 2012 Cubeia Ltd <info@cubeia.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.cubeia.games.poker.admin.wicket.pages.tournaments.payouts;

import com.cubeia.games.poker.admin.db.AdminDAO;
import com.cubeia.games.poker.admin.wicket.BasePage;
import com.cubeia.games.poker.tournament.configuration.payouts.PayoutStructure;
import com.cubeia.games.poker.tournament.configuration.payouts.PayoutStructureParser;
import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.file.Files;
import org.apache.wicket.util.lang.Bytes;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class CreateOrEditPayoutStructure extends BasePage {

    private static final Logger log = Logger.getLogger(CreateOrEditPayoutStructure.class);

    private final FeedbackPanel feedback = new FeedbackPanel("feedback");

    @SpringBean(name = "adminDAO")
    private AdminDAO adminDAO;

    public CreateOrEditPayoutStructure(PageParameters p) {
        super(p);
        add(new FileUploadForm("upload"));

        ExternalLink csvLink = new ExternalLink("csvLink", "/payouts/default_payouts.csv");
        csvLink.setContextRelative(true);
        add(csvLink);
    }

    @Override
    public String getPageTitle() {
        return "Create or Edit Payout Structure";
    }

    private class FileUploadForm extends Form<Void> {
        FileUploadField fileUploadField;
        RequiredTextField<String> name = new RequiredTextField<String>("name", new Model<String>());

        public FileUploadForm(String name) {
            super(name);

            // set this form to multipart mode (always needed for uploads!)
            setMultiPart(true);

            // Add one file input field
            add(fileUploadField = new FileUploadField("fileInput"));

            // Set maximum size to 100K for demo purposes
            setMaxSize(Bytes.kilobytes(100));
            add(this.name);
            add(feedback);
        }

        public RequiredTextField<String> getName() {
            return name;
        }

        public void setName(RequiredTextField<String> name) {
            this.name = name;
        }

        /**
         * @see org.apache.wicket.markup.html.form.Form#onSubmit()
         */
        @Override
        protected void onSubmit() {
            log.debug("Name = " + name.getModel().getObject());
            final List<FileUpload> uploads = fileUploadField.getFileUploads();
            if (uploads != null) {
                for (FileUpload upload : uploads) {
                    // Create a new file
                    File newFile = new File(getUploadFolder(), upload.getClientFileName());

                    // Check new file, delete if it already existed
                    checkFileExists(newFile);
                    try {
                        // Save to new file
                        boolean creationOK = newFile.createNewFile();
                        if (creationOK) {
                            upload.writeTo(newFile);

                            log.debug("Verifying payout " + upload.getClientFileName());
                            PayoutStructureParser parser = new PayoutStructureParser();
                            PayoutStructure structure = parser.parsePayouts(newFile);
                            structure.verify();
                            structure.setName(name.getModel().getObject());
                            info("Structure verified: " + upload.getClientFileName());
                            CreateOrEditPayoutStructure.this.adminDAO.persist(structure);
                            setResponsePage(ListPayoutStructures.class);
                        } else {
                            warn("Failed saving file " + upload.getClientFileName());
                        }
                    } catch (NumberFormatException e) {
                        error("Payout structure is invalid: " + e.getMessage());
                    } catch (IllegalStateException e) {
                        error("Payout structure is invalid: " + e.getMessage());
                    } catch (Exception e) {
                        throw new RuntimeException("Failed uploading file: " + e.getMessage(), e);
                    }
                }
            }
        }

        /**
         * Check whether the file already exists, and if so, try to delete it.
         *
         * @param newFile the file to check
         */
        private void checkFileExists(File newFile) {
            if (newFile.exists()) {
                // Try to delete the file
                if (!Files.remove(newFile)) {
                    throw new IllegalStateException("Unable to overwrite " + newFile.getAbsolutePath());
                }
            }
        }

        private File getUploadFolder() {
            // return "/tmp/"; // Seems to fail on Windows
            try {
            	final File temp;
				temp = File.createTempFile("temp", Long.toString(System.nanoTime()));

	            if(!(temp.delete()))
	            {
	                throw new RuntimeException("Could not delete temp file: " + temp.getAbsolutePath());
	            }
	
	            if(!(temp.mkdir()))
	            {
	                throw new RuntimeException("Could not create temp directory: " + temp.getAbsolutePath());
	            }

	            return temp;
            } catch (IOException e) {
            	throw new RuntimeException("Failed to create temporary file.", e);
            }
        }
    }
}
