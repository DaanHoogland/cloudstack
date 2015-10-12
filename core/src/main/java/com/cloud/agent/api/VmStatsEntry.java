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

package com.cloud.agent.api;

import com.cloud.vm.VmStats;

public class VmStatsEntry implements VmStats {

    double cpuUtilization;
    double networkReadKBs;
    double networkWriteKBs;
    double diskReadIOs;
    double diskWriteIOs;
    double diskReadKBs;
    double diskWriteKBs;
    double memoryKBs;
    double intfreememoryKBs;
    double targetmemoryKBs;
    int numCPUs;
    String entityType;

    public VmStatsEntry() {
    }

    public VmStatsEntry(double memoryKBs,double intfreememoryKBs,double targetmemoryKBs, double cpuUtilization, double networkReadKBs, double networkWriteKBs, int numCPUs, String entityType) {
        this.memoryKBs = memoryKBs;
        this.intfreememoryKBs = intfreememoryKBs;
        this.targetmemoryKBs = targetmemoryKBs;
        this.cpuUtilization = cpuUtilization;
        this.networkReadKBs = networkReadKBs;
        this.networkWriteKBs = networkWriteKBs;
        this.numCPUs = numCPUs;
        this.entityType = entityType;
    }

    public VmStatsEntry(double cpuUtilization, double networkReadKBs, double networkWriteKBs, double diskReadKBs, double diskWriteKBs, int numCPUs, String entityType) {
        this.cpuUtilization = cpuUtilization;
        this.networkReadKBs = networkReadKBs;
        this.networkWriteKBs = networkWriteKBs;
        this.diskReadKBs = diskReadKBs;
        this.diskWriteKBs = diskWriteKBs;
        this.numCPUs = numCPUs;
        this.entityType = entityType;
    }

    @Override
    public Double getCPUUtilization() {
        return cpuUtilization;
    }

    public void setCPUUtilization(double cpuUtilization) {
        this.cpuUtilization = cpuUtilization;
    }

    @Override
    public Double getNetworkReadKBs() {
        return networkReadKBs;
    }

    public void setNetworkReadKBs(double networkReadKBs) {
        this.networkReadKBs = networkReadKBs;
    }

    @Override
    public Double getNetworkWriteKBs() {
        return networkWriteKBs;
    }

    public void setNetworkWriteKBs(double networkWriteKBs) {
        this.networkWriteKBs = networkWriteKBs;
    }

    @Override
    public Double getDiskReadIOs() {
        return diskReadIOs;
    }

    public void setDiskReadIOs(double diskReadIOs) {
        this.diskReadIOs = diskReadIOs;
    }

    @Override
    public Double getDiskWriteIOs() {
        return diskWriteIOs;
    }

    public void setDiskWriteIOs(double diskWriteIOs) {
        this.diskWriteIOs = diskWriteIOs;
    }

    @Override
    public Double getDiskReadKBs() {
        return diskReadKBs;
    }

    public void setDiskReadKBs(double diskReadKBs) {
        this.diskReadKBs = diskReadKBs;
    }

    @Override
    public Double getDiskWriteKBs() {
        return diskWriteKBs;
    }

    public void setDiskWriteKBs(double diskWriteKBs) {
        this.diskWriteKBs = diskWriteKBs;
    }

    @Override
    public Double getMemoryKBs() {
        return memoryKBs;
    }

    public void setMemoryKBs(double memoryKBs) {
        this.memoryKBs = memoryKBs;
    }

    @Override
    public Double getIntFreeMemoryKBs() {
        return intfreememoryKBs;
    }

    public void setIntFreeMemoryKBs(double intfreememoryKBs) {
        this.intfreememoryKBs = intfreememoryKBs;
    }

    @Override
    public Double getTargetMemoryKBs() {
        return targetmemoryKBs;
    }

    public void setTargetMemoryKBs(double targetmemoryKBs) {
        this.targetmemoryKBs = targetmemoryKBs;
    }

    public Integer getNumCPUs() {
        return numCPUs;
    }

    public void setNumCPUs(int numCPUs) {
        this.numCPUs = numCPUs;
    }

    public String getEntityType() {
        return this.entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

}
