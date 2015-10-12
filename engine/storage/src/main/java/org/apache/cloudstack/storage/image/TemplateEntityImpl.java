/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.cloudstack.storage.image;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.cloudstack.engine.cloud.entity.api.TemplateEntity;
import org.apache.cloudstack.engine.subsystem.api.storage.TemplateInfo;
import org.apache.cloudstack.storage.image.datastore.ImageStoreInfo;

import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.storage.Storage.ImageFormat;
import com.cloud.storage.Storage.TemplateType;
import com.cloud.template.VirtualMachineTemplate;

public class TemplateEntityImpl implements TemplateEntity {
    protected TemplateInfo templateInfo;

    @Override
    public State getState() {
        return templateInfo.getState();
    }

    public TemplateEntityImpl(TemplateInfo templateInfo) {
        this.templateInfo = templateInfo;
    }

    public ImageStoreInfo getImageDataStore() {
        return (ImageStoreInfo)templateInfo.getDataStore();
    }

    public long getImageDataStoreId() {
        return getImageDataStore().getImageStoreId();
    }

    public TemplateInfo getTemplateInfo() {
        return templateInfo;
    }

    @Override
    public String getUuid() {
        return templateInfo.getUuid();
    }

    @Override
    public Long getId() {
        return templateInfo.getId();
    }

    public String getExternalId() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getCurrentState() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getDesiredState() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Date getCreatedTime() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Date getLastUpdatedTime() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getOwner() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<String, String> getDetails() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Boolean isDynamicallyScalable() {
        return false;
    }

    @Override
    public void addDetail(String name, String value) {
        // TODO Auto-generated method stub

    }

    @Override
    public void delDetail(String name, String value) {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateDetail(String name, String value) {
        // TODO Auto-generated method stub

    }

    @Override
    public List<Method> getApplicableActions() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Boolean isFeatured() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Boolean isPublicTemplate() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Boolean isExtractable() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ImageFormat getFormat() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Boolean isRequiresHvm() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public String getDisplayText() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Boolean isEnablePassword() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Boolean isEnableSshKey() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Boolean isCrossZones() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Date getCreated() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long getGuestOSId() {
        // TODO Auto-generated method stub
        return 0l;
    }

    @Override
    public Boolean isBootable() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public TemplateType getTemplateType() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public HypervisorType getHypervisorType() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Integer getBits() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public String getUniqueName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getUrl() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getChecksum() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long getSourceTemplateId() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getTemplateTag() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long getAccountId() {
        // TODO Auto-generated method stub
        return 0l;
    }

    @Override
    public Long getDomainId() {
        // TODO Auto-generated method stub
        return 0l;
    }

    @Override
    public Long getPhysicalSize() {
        // TODO Auto-generated method stub
        return 0l;
    }

    @Override
    public Long getVirtualSize() {
        // TODO Auto-generated method stub
        return 0l;
    }

    @Override
    public Class<?> getEntityType() {
        return VirtualMachineTemplate.class;
    }

    @Override
    public Long getUpdatedCount() {
        // TODO Auto-generated method stub
        return 0l;
    }

    @Override
    public void incrUpdatedCount() {
        // TODO Auto-generated method stub
    }

    @Override
    public Date getUpdated() {
        return null;
    }

    @Override
    public Long getParentTemplateId() {
        return null;
    }
}
