//
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
//

package com.cloud.agent.api.storage;

import com.cloud.agent.api.Command;
import com.cloud.agent.api.to.StorageFilerTO;

public class ResizeVolumeCommand extends Command {
    private String _path;
    private StorageFilerTO _pool;
    private Long _currentSize;
    private Long _newSize;
    private boolean _shrinkOk;
    private String _vmInstance;

    /* For managed storage */
    private boolean _managed;
    private String _iScsiName;

    protected ResizeVolumeCommand() {
    }

    public ResizeVolumeCommand(String path, StorageFilerTO pool, Long currentSize, Long newSize, boolean shrinkOk, String vmInstance) {
        _path = path;
        _pool = pool;
        _currentSize = currentSize;
        _newSize = newSize;
        _shrinkOk = shrinkOk;
        _vmInstance = vmInstance;
        _managed = false;
    }

    public ResizeVolumeCommand(String path, StorageFilerTO pool, Long currentSize, Long newSize, boolean shrinkOk, String vmInstance,
                               boolean isManaged, String iScsiName) {
        this(path, pool, currentSize, newSize, shrinkOk, vmInstance);

        _iScsiName = iScsiName;
        _managed = isManaged;
    }

    public String getPath() {
        return _path;
    }

    public String getPoolUuid() {
        return _pool.getUuid();
    }

    public StorageFilerTO getPool() {
        return _pool;
    }

    public long getCurrentSize() { return _currentSize; }

    public long getNewSize() { return _newSize; }

    public boolean getShrinkOk() { return _shrinkOk; }

    public String getInstanceName() {
        return _vmInstance;
    }

    public boolean isManaged() { return _managed; }

    public String get_iScsiName() {return _iScsiName; }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean executeInSequence() {
        return false;
    }
}
