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
package com.cloud.vm;

public class VmWorkResizeVolume extends VmWork {
    private static final long serialVersionUID = 6112366316907642498L;

    private long _volumeId;
    private long _currentSize;
    private long _newSize;
    private Long _newMinIops;
    private Long _newMaxIops;
    private Integer _newHypervisorSnapshotReserve;
    private Long _newServiceOfferingId;
    private boolean _shrinkOk;

    public VmWorkResizeVolume(long userId, long accountId, long vmId, String handlerName, long volumeId, long currentSize, long newSize,
                              Long newMinIops, Long newMaxIops, Integer newHypervisorSnapshotReserve, Long newServiceOfferingId, boolean shrinkOk) {
        super(userId, accountId, vmId, handlerName);

        _volumeId = volumeId;
        _currentSize = currentSize;
        _newSize = newSize;
        _newMinIops = newMinIops;
        _newMaxIops = newMaxIops;
        _newHypervisorSnapshotReserve = newHypervisorSnapshotReserve;
        _newServiceOfferingId = newServiceOfferingId;
        _shrinkOk = shrinkOk;
    }

    public long getVolumeId() {
        return _volumeId;
    }

    public long getCurrentSize() {
        return _currentSize;
    }

    public long getNewSize() {
        return _newSize;
    }

    public Long getNewMinIops() {
        return _newMinIops;
    }

    public Long getNewMaxIops() {
        return _newMaxIops;
    }

    public Long getNewServiceOfferingId() {
        return _newServiceOfferingId;
    }

    public boolean isShrinkOk() {
        return _shrinkOk;
    }

    public Integer getNewHypervisorSnapshotReserve() { return _newHypervisorSnapshotReserve; }
}
