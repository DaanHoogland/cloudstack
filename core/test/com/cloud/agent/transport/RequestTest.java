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

package com.cloud.agent.transport;

import java.nio.ByteBuffer;

import junit.framework.TestCase;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.mockito.Mockito;

import org.apache.cloudstack.storage.command.DownloadCommand;
import org.apache.cloudstack.storage.to.TemplateObjectTO;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.Command;
import com.cloud.agent.api.GetHostStatsCommand;
import com.cloud.agent.api.SecStorageFirewallCfgCommand;
import com.cloud.agent.api.UpdateHostPasswordCommand;
import com.cloud.agent.api.storage.DownloadAnswer;
import com.cloud.agent.api.storage.ListTemplateCommand;
import com.cloud.agent.api.to.NfsTO;
import com.cloud.exception.UnsupportedVersionException;
import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.serializer.GsonHelper;
import com.cloud.storage.DataStoreRole;
import com.cloud.storage.Storage.ImageFormat;
import com.cloud.storage.Storage.TemplateType;
import com.cloud.storage.VMTemplateStorageResourceAssoc.Status;
import com.cloud.template.VirtualMachineTemplate;
import com.google.gson.Gson;

/**
 *
 *
 *
 *
 */

public class RequestTest extends TestCase {
    private static final Logger s_logger = Logger.getLogger(RequestTest.class);

    public void testSerDeser() {
        s_logger.info("Testing serializing and deserializing works as expected");

        s_logger.info("UpdateHostPasswordCommand should have two parameters that don't show in logging");
        UpdateHostPasswordCommand cmd1 = new UpdateHostPasswordCommand("abc", "def");
        s_logger.info("SecStorageFirewallCfgCommand has a context map that shouldn't show up in debug level");
        SecStorageFirewallCfgCommand cmd2 = new SecStorageFirewallCfgCommand();
        s_logger.info("GetHostStatsCommand should not show up at all in debug level");
        GetHostStatsCommand cmd3 = new GetHostStatsCommand("hostguid", "hostname", 101L);
        cmd2.addPortConfig("abc", "24", true, "eth0");
        cmd2.addPortConfig("127.0.0.1", "44", false, "eth1");
        Request sreq = new Request(2, 3, new Command[] {cmd1, cmd2, cmd3}, true, true);
        sreq.setSequence(892403717);

        Logger logger = Logger.getLogger(GsonHelper.class);
        Level level = logger.getLevel();

        logger.setLevel(Level.DEBUG);
        String log = sreq.log("Debug", true, Level.DEBUG);
        assertTrue (log.contains(UpdateHostPasswordCommand.class.getSimpleName()));
        assertTrue (log.contains(SecStorageFirewallCfgCommand.class.getSimpleName()));
        assertFalse (log.contains(GetHostStatsCommand.class.getSimpleName()));
        assertFalse (log.contains("username"));
        assertFalse (log.contains("password"));

        logger.setLevel(Level.TRACE);
        log = sreq.log("Trace", true, Level.TRACE);
        assertTrue (log.contains(UpdateHostPasswordCommand.class.getSimpleName()));
        assertTrue (log.contains(SecStorageFirewallCfgCommand.class.getSimpleName()));
        assertTrue (log.contains(GetHostStatsCommand.class.getSimpleName()));
        assertFalse (log.contains("username"));
        assertFalse (log.contains("password"));

        logger.setLevel(Level.INFO);
        log = sreq.log("Info", true, Level.INFO);
        assertNull (log);

        logger.setLevel(level);

        byte[] bytes = sreq.getBytes();

        assertEquals(Request.getSequence(bytes), 892403717);
        assertEquals(Request.getManagementServerId(bytes), 3);
        assertEquals(Request.getAgentId(bytes), 2);
        assertEquals(Request.getViaAgentId(bytes), 2);
        Request creq = null;
        try {
            creq = Request.parse(bytes);
        } catch (ClassNotFoundException e) {
            s_logger.error("Unable to parse bytes: ", e);
        } catch (UnsupportedVersionException e) {
            s_logger.error("Unable to parse bytes: ", e);
        }

        assertNotNull("Couldn't get the request back", creq);

        compareRequest(creq, sreq);

        Answer ans = new Answer(cmd1, true, "No Problem");
        Response cresp = new Response(creq, ans);

        bytes = cresp.getBytes();

        Response sresp = null;
        try {
            sresp = Response.parse(bytes);
        } catch (ClassNotFoundException e) {
            s_logger.error("Unable to parse bytes: ", e);
        } catch (UnsupportedVersionException e) {
            s_logger.error("Unable to parse bytes: ", e);
        }

        assertNotNull("Couldn't get the response back", sresp);

        compareRequest(cresp, sresp);
    }

    public void testSerDeserTO() {
        s_logger.info("Testing serializing and deserializing interface TO works as expected");

        NfsTO nfs = new NfsTO("nfs://192.168.56.10/opt/storage/secondary", DataStoreRole.Image);
        // SecStorageSetupCommand cmd = new SecStorageSetupCommand(nfs, "nfs://192.168.56.10/opt/storage/secondary", null);
        ListTemplateCommand cmd = new ListTemplateCommand(nfs);
        Request sreq = new Request(2, 3, cmd, true);
        sreq.setSequence(892403718);

        byte[] bytes = sreq.getBytes();

        assertEquals(Request.getSequence(bytes), 892403718);
        assertEquals(Request.getManagementServerId(bytes), 3);
        assertEquals(Request.getAgentId(bytes), 2);
        assertEquals(Request.getViaAgentId(bytes), 2);
        Request creq = null;
        try {
            creq = Request.parse(bytes);
        } catch (ClassNotFoundException e) {
            s_logger.error("Unable to parse bytes: ", e);
        } catch (UnsupportedVersionException e) {
            s_logger.error("Unable to parse bytes: ", e);
        }

        assertNotNull("Couldn't get the request back", creq);

        compareRequest(creq, sreq);
        assertEquals("nfs://192.168.56.10/opt/storage/secondary", ((NfsTO)((ListTemplateCommand)creq.getCommand()).getDataStore()).getUrl());
    }

    public void testDownload() {
        s_logger.info("Testing Download answer");
        VirtualMachineTemplate template = Mockito.mock(VirtualMachineTemplate.class);
        Mockito.when(template.getId()).thenReturn(1L);
        Mockito.when(template.getFormat()).thenReturn(ImageFormat.QCOW2);
        Mockito.when(template.getName()).thenReturn("templatename");
        Mockito.when(template.getTemplateType()).thenReturn(TemplateType.USER);
        Mockito.when(template.getDisplayText()).thenReturn("displayText");
        Mockito.when(template.getHypervisorType()).thenReturn(HypervisorType.KVM);
        Mockito.when(template.getUrl()).thenReturn("url");

        NfsTO nfs = new NfsTO("secUrl", DataStoreRole.Image);
        TemplateObjectTO to = new TemplateObjectTO(template);
        to.setImageDataStore(nfs);
        DownloadCommand cmd = new DownloadCommand(to, 30000000l);
        Request req = new Request(1, 1, cmd, true);

        req.logD("Debug for Download");

        DownloadAnswer answer = new DownloadAnswer("jobId", 50, "errorString", Status.ABANDONED, "filesystempath", "installpath", 10000000L, 20000000L, "chksum");
        Response resp = new Response(req, answer);
        resp.logD("Debug for Download");

    }

    public void testCompress() {
        s_logger.info("testCompress");
        int len = 800000;
        ByteBuffer inputBuffer = ByteBuffer.allocate(len);
        for (int i = 0; i < len; i++) {
            inputBuffer.array()[i] = 1;
        }
        inputBuffer.limit(len);
        ByteBuffer compressedBuffer = ByteBuffer.allocate(len);
        compressedBuffer = Request.doCompress(inputBuffer, len);
        s_logger.info("compressed length: " + compressedBuffer.limit());
        ByteBuffer decompressedBuffer = ByteBuffer.allocate(len);
        decompressedBuffer = Request.doDecompress(compressedBuffer, len);
        for (int i = 0; i < len; i++) {
            if (inputBuffer.array()[i] != decompressedBuffer.array()[i]) {
                Assert.fail("Fail at " + i);
            }
        }
    }

    public void testLogging() {
        s_logger.info("Testing Logging");
        GetHostStatsCommand cmd3 = new GetHostStatsCommand("hostguid", "hostname", 101L);
        Request sreq = new Request(2, 3, new Command[] {cmd3}, true, true);
        sreq.setSequence(1);
        Logger logger = Logger.getLogger(GsonHelper.class);
        Level level = logger.getLevel();

        logger.setLevel(Level.DEBUG);
        String log = sreq.log("Debug", true, Level.DEBUG);
        assertNull(log);

        log = sreq.log("Debug", false, Level.DEBUG);
        assertNotNull (log);

        logger.setLevel(Level.TRACE);
        log = sreq.log("Trace", true, Level.TRACE);
        assertNotNull (log);
        assertTrue (log.contains(GetHostStatsCommand.class.getSimpleName()));
        s_logger.debug(log);

        logger.setLevel(level);
    }

    public void testGson() {
        final Gson gson = GsonHelper.getGson();
        s_logger.info("Testing gson");
        Command cmd_in;
        Command cmd_out;
        String json;
        String hostguid = "hostguid";
        String hostname = "hostname";
        Long hostId = 101l;

        cmd_in = new GetHostStatsCommand(hostguid, hostname, hostId);
        json = gson.toJson(cmd_in, GetHostStatsCommand.class);
        cmd_out = gson.fromJson(json, GetHostStatsCommand.class);

        assertEquals(((GetHostStatsCommand)cmd_out).getHostGuid(),hostguid);
        assertEquals(((GetHostStatsCommand)cmd_out).getHostName(),hostname);
        assertEquals(((GetHostStatsCommand)cmd_out).getHostId(),hostId);

        String username = "abc";
        String password = "def";
        cmd_in = new UpdateHostPasswordCommand(username, password);
        json = gson.toJson(cmd_in, UpdateHostPasswordCommand.class);
        cmd_out = gson.fromJson(json, UpdateHostPasswordCommand.class);

        assertEquals(((UpdateHostPasswordCommand)cmd_out).getUsername(), username);
        assertEquals(((UpdateHostPasswordCommand)cmd_out).getNewPassword(), password);
    }
    protected void compareRequest(Request req1, Request req2) {
        assertEquals(req1.getSequence(), req2.getSequence());
        assertEquals(req1.getAgentId(), req2.getAgentId());
        assertEquals(req1.getManagementServerId(), req2.getManagementServerId());
        assertEquals(req1.isControl(), req2.isControl());
        assertEquals(req1.isFromServer(), req2.isFromServer());
        assertEquals(req1.executeInSequence(), req2.executeInSequence());
        assertEquals(req1.stopOnError(), req2.stopOnError());
        assertEquals(req1.getVersion(),req2.getVersion());
        assertEquals(req1.getViaAgentId(), req2.getViaAgentId());
        Command[] cmd1 = req1.getCommands();
        Command[] cmd2 = req2.getCommands();
        for (int i = 0; i < cmd1.length; i++) {
            assert cmd1[i].getClass().equals(cmd2[i].getClass());
        }
    }

}
