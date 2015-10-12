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
package org.apache.cloudstack.api.command.test;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Matchers;
import org.mockito.Mockito;

import org.apache.cloudstack.api.command.user.account.AddAccountToProjectCmd;

import com.cloud.exception.InvalidParameterValueException;
import com.cloud.projects.Project;
import com.cloud.projects.ProjectService;
import com.cloud.user.Account;

public class AddAccountToProjectCmdTest extends TestCase {

    private AddAccountToProjectCmd addAccountToProjectCmd;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Override
    @Before
    public void setUp() {
        addAccountToProjectCmd = new AddAccountToProjectCmd() {

            @Override
            public Long getProjectId() {
                return 2L;
            }

            @Override
            public String getAccountName() {

                // to run the test testGetEventDescriptionForAccount set the
                // accountName
                // return "accountName";
                // to run the test the testGetEventDescriptionForNullAccount
                // return accountname as null
                return null;
            }

            @Override
            public String getEmail() {
                // return "customer@abc.com";
                return null;
            }

        };
    }

    /****
     * Condition not handled in the code
     *
     *****/

    /*
     * @Test public void testGetEntityOwnerIdForNullProject() {
     *
     * ProjectService projectService = Mockito.mock(ProjectService.class);
     * Mockito
     * .when(projectService.getProject(Mockito.anyLong())).thenReturn(null);
     * addAccountToProjectCmd._projectService = projectService;
     *
     * try { addAccountToProjectCmd.getEntityOwnerId(); }
     * catch(InvalidParameterValueException exception) {
     * Assert.assertEquals("Unable to find project by id 2",
     * exception.getLocalizedMessage()); }
     *
     * }
     */

    @Test
    public void testGetEntityOwnerIdForProject() {

        Project project = Mockito.mock(Project.class);
        Mockito.when(project.getId()).thenReturn(2L);

        ProjectService projectService = Mockito.mock(ProjectService.class);
        Account account = Mockito.mock(Account.class);

        Mockito.when(account.getId()).thenReturn(2L);
        Mockito.when(projectService.getProject(Matchers.anyLong())).thenReturn(project);

        Mockito.when(projectService.getProjectOwner(Matchers.anyLong())).thenReturn(account);
        addAccountToProjectCmd._projectService = projectService;

        Assert.assertEquals((Long)2L, addAccountToProjectCmd.getEntityOwnerId());

    }

    /**
     * To run the test uncomment the return statement for getAccountName() in
     * setup() and return null
     *
     * **/

    /*
     * @Test public void testGetEventDescriptionForNullAccount() {
     *
     * String result = addAccountToProjectCmd.getEventDescription(); String
     * expected = "Sending invitation to email null to join project: 2";
     * Assert.assertEquals(expected, result);
     *
     * }
     */

    /***
     *
     *
     *
     * ***/

    /*
     * @Test public void testGetEventDescriptionForAccount() {
     *
     * String result = addAccountToProjectCmd.getEventDescription(); String
     * expected = "Adding account accountName to project: 2";
     * Assert.assertEquals(expected, result);
     *
     * }
     */

    @Test
    public void testExecuteForNullAccountNameEmail() {

        try {
            addAccountToProjectCmd.execute();
        } catch (InvalidParameterValueException exception) {
            Assert.assertEquals("Either accountName or email is required", exception.getLocalizedMessage());
        }

    }

    /*
     * @Test public void testExecuteForAccountNameEmail() {
     *
     * try {
     *
     * ComponentLocator c = Mockito.mock(ComponentLocator.class); UserContext
     * userContext = Mockito.mock(UserContext.class);
     *
     * // Mockito.when(userContext.current()).thenReturn(userContext);
     *
     *
     * addAccountToProjectCmd.execute(); } catch(InvalidParameterValueException
     * exception) {
     * Assert.assertEquals("Either accountName or email is required",
     * exception.getLocalizedMessage()); }
     *
     * }
     */

}
