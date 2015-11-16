// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.
package org.apache.cloudstack.solidfire;

import javax.inject.Inject;

import org.apache.cloudstack.storage.datastore.db.PrimaryDataStoreDao;
// import org.apache.log4j.Logger;
import org.apache.cloudstack.storage.datastore.util.SolidFireUtil;
import org.apache.cloudstack.util.solidfire.SolidFireIntegrationTestUtil;
import org.apache.cloudstack.engine.subsystem.api.storage.DataStoreProviderManager;
import org.springframework.stereotype.Component;

import com.cloud.dc.ClusterDetailsDao;
import com.cloud.dc.ClusterDetailsVO;
import com.cloud.storage.VolumeDetailVO;
import com.cloud.storage.VolumeVO;
import com.cloud.storage.dao.VolumeDao;
import com.cloud.storage.dao.VolumeDetailsDao;
import com.cloud.user.AccountDetailsDao;
import com.cloud.user.AccountDetailVO;
import com.cloud.utils.exception.CloudRuntimeException;

@Component
public class SolidFireIntegrationTestManagerImpl implements SolidFireIntegrationTestManager {
    // private static final Logger s_logger = Logger.getLogger(SolidFireIntegrationTestManagerImpl.class);

    @Inject private AccountDetailsDao _accountDetailsDao;
    @Inject private ClusterDetailsDao _clusterDetailsDao;
    @Inject private DataStoreProviderManager _dataStoreProviderMgr;
    @Inject private PrimaryDataStoreDao _storagePoolDao;
    @Inject private SolidFireIntegrationTestUtil _util;
    @Inject private VolumeDao _volumeDao;
    @Inject private VolumeDetailsDao _volumeDetailsDao;

    @Override
    public long getSolidFireAccountId(String csAccountUuid, String storagePoolUuid) {
        long csAccountId = _util.getAccountIdForAccountUuid(csAccountUuid);
        long storagePoolId = _util.getStoragePoolIdForStoragePoolUuid(storagePoolUuid);

        AccountDetailVO accountDetail = _accountDetailsDao.findDetail(csAccountId, SolidFireUtil.getAccountKey(storagePoolId));
        String sfAccountId = accountDetail.getValue();

        return Long.parseLong(sfAccountId);
    }

    @Override
    public long getSolidFireVolumeAccessGroupId(String csClusterUuid, String storagePoolUuid) {
        long csClusterId = _util.getClusterIdForClusterUuid(csClusterUuid);
        long storagePoolId = _util.getStoragePoolIdForStoragePoolUuid(storagePoolUuid);

        ClusterDetailsVO clusterDetails = _clusterDetailsDao.findDetail(csClusterId, SolidFireUtil.getVagKey(storagePoolId));
        String sfVagId = clusterDetails.getValue();

        return Long.parseLong(sfVagId);
    }

    @Override
    public long getSolidFireVolumeSize(String volumeUuid) {
        VolumeVO volume = _volumeDao.findByUuid(volumeUuid);

        VolumeDetailVO volumeDetail = _volumeDetailsDao.findDetail(volume.getId(), SolidFireUtil.VOLUME_SIZE);

        if (volumeDetail != null && volumeDetail.getValue() != null) {
            return Long.parseLong(volumeDetail.getValue());
        }

        throw new CloudRuntimeException("Unable to determine the size of the SolidFire volume");
    }
}
