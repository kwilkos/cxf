/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.cxf.aegis.type.mtom;

import javax.activation.DataHandler;

import org.apache.cxf.aegis.Context;
import org.apache.cxf.attachment.AttachmentImpl;
import org.apache.cxf.message.Attachment;

public class DataHandlerType extends AbstractXOPType {
    @Override
    protected Object readAttachment(Attachment att, Context context) {
        return att.getDataHandler();
    }

    @Override
    protected Attachment createAttachment(Object object, String id) {
        DataHandler handler = (DataHandler)object;

        AttachmentImpl att = new AttachmentImpl(id, handler);
        att.setXOP(true);

        return att;
    }

    @Override
    protected String getContentType(Object object, Context context) {
        return ((DataHandler)object).getContentType();
    }
}
