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
package org.apache.cloudstack.api.command.admin.solidfire;

import com.cloud.user.Account;

import javax.inject.Inject;

import org.apache.log4j.Logger;

import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.BaseCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.response.solidfire.ApiSolidFireVolumeAccessGroupIdResponse;
import org.apache.cloudstack.context.CallContext;
import org.apache.cloudstack.solidfire.SolidFireIntegrationTestManager;
import org.apache.cloudstack.util.solidfire.SolidFireIntegrationTestUtil;

@APICommand(name = "getSolidFireVolumeAccessGroupId", responseObject = ApiSolidFireVolumeAccessGroupIdResponse.class, description = "Get the SF Volume Access Group ID",
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class GetSolidFireVolumeAccessGroupIdCmd extends BaseCmd {
    private static final Logger s_logger = Logger.getLogger(GetSolidFireVolumeAccessGroupIdCmd.class.getName());
    private static final String s_name = "getsolidfirevolumeaccessgroupidresponse";

    @Parameter(name = ApiConstants.CLUSTER_ID, type = CommandType.STRING, description = "Cluster UUID", required = true)
    private String _clusterUuid;
    @Parameter(name = ApiConstants.STORAGE_ID, type = CommandType.STRING, description = "Storage Pool UUID", required = true)
    private String _storagePoolUuid;

    @Inject private SolidFireIntegrationTestManager _manager;
    @Inject private SolidFireIntegrationTestUtil _util;

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        Account account = CallContext.current().getCallingAccount();

        if (account != null) {
            return account.getId();
        }

        return Account.ACCOUNT_ID_SYSTEM; // no account info given, parent this command to SYSTEM so ERROR events are tracked
    }

    @Override
    public void execute() {
        s_logger.info("'GetSolidFireVolumeAccessGroupIdCmd.execute' method invoked");

        long sfVagId = _manager.getSolidFireVolumeAccessGroupId(_clusterUuid, _storagePoolUuid);

        ApiSolidFireVolumeAccessGroupIdResponse response = new ApiSolidFireVolumeAccessGroupIdResponse(sfVagId);

        response.setResponseName(getCommandName());
        response.setObjectName("apisolidfirevolumeaccessgroupid");

        this.setResponseObject(response);
    }
}