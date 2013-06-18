/**
 * Copyright (C) 2011 Cubeia Ltd <info@cubeia.com>
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
package com.cubeia.firebase.api.action.mtt;

import java.io.Serializable;
import java.nio.ByteBuffer;

import com.cubeia.firebase.api.action.visitor.MttActionVisitor;

public class MttRoundReportAction extends AbstractMttAction {

	/** Version ID */
	private static final long serialVersionUID = 1L;
	
    private byte[] data;
    private Object attachment;
    private int tableId;
	
	public MttRoundReportAction(int mttId, int tableId) {
		super(mttId);
		this.tableId = tableId;
	}

    /**
     * Rewinds and returns a new bytebuffer
     * @return
     */
    public ByteBuffer getData() {
        return ByteBuffer.wrap(data);
    }

    /**
     * Sets and rewinds the data bytebuffer.
     * @param data
     */
    public void setData(ByteBuffer src) {
        data = new byte[src.remaining()];
        src.get(data);
    }

    public Object getAttachment() {
        return attachment;
    }
    
    /**
     * Set an (optional) attachment to the round report. The attachment must be Serializable.
     * @param attachment the attachment to set
     */
    public void setAttachment(Object attachment) {
        if (!(attachment instanceof Serializable)) {
            throw new RuntimeException("the attachment must be serializable");
        }
        this.attachment = attachment;
    }
    
    public int getTableId() {
        return tableId;
    }
    
	public void accept(MttActionVisitor visitor) {
		visitor.visit(this);
	}
	
    public String toString() {
        return "MTT Round Report Action : mttId["+getMttId()+"] tableId["+tableId+"] Data(bytes)["+
            (data == null ? "null" : "" + data.length)+"] Attachment[" + attachment + "]";
    }
}
