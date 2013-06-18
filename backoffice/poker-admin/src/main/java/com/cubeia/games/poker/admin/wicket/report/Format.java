/**
 * Copyright (C) 2010 Cubeia Ltd <info@cubeia.com>
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

package com.cubeia.games.poker.admin.wicket.report;

public enum Format {

	XLS("application/vnd.ms-excel", false, true),
	CSV("text/csv", false, true),
	XML("text/xml", false, false),
	HTML("text/html", false, false),
	PDF("application/pdf", true, true);
	
	private final String contentType;
	private final boolean paged;
	private final boolean attachment;
	
	private Format(String type, boolean paged, boolean attachment) {
		this.contentType = type;
		this.paged = paged;
		this.attachment = attachment;
	}
	
	public boolean isAttachment() {
		return attachment;
	}
	
	public boolean isPaged() {
		return paged;
	}
	
	public String getContentType() {
		return contentType;
	}
}
