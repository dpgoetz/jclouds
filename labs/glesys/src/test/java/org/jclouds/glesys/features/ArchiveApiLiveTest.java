/**
 * Licensed to jclouds, Inc. (jclouds) under one or more
 * contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  jclouds licenses this file
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
package org.jclouds.glesys.features;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.concurrent.TimeUnit;

import org.jclouds.glesys.domain.Archive;
import org.jclouds.glesys.domain.ArchiveAllowedArguments;
import org.jclouds.glesys.internal.BaseGleSYSApiLiveTest;
import org.jclouds.predicates.RetryablePredicate;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.Test;

import com.google.common.base.Predicate;

/**
 * Tests behavior of {@code ArchiveApi}
 *
 * @author Adam Lowe
 */
@Test(groups = "live", testName = "ArchiveApiLiveTest", singleThreaded = true)
public class ArchiveApiLiveTest extends BaseGleSYSApiLiveTest {

   @BeforeGroups(groups = {"live"})
   public void setupContext() {
      super.setupContext();
      
      api = gleContext.getApi().getArchiveApi();
      archiveUser = gleContext.getIdentity().toLowerCase() + "_test9";
      archiveCounter = new RetryablePredicate<Integer>(
            new Predicate<Integer>() {
               public boolean apply(Integer value){
                  return api.listArchives().size() == value;
               }
            }, 30, 1, TimeUnit.SECONDS);
   }
   
   @AfterClass(groups = { "integration", "live" })
   protected void tearDownContext() {
      int before = api.listArchives().size();
      api.deleteArchive(archiveUser);
      assertTrue(archiveCounter.apply(before - 1));

      super.tearDownContext();
   }

   private ArchiveApi api;
   private String archiveUser;
   private RetryablePredicate<Integer> archiveCounter;

   @Test
   public void testAllowedArguments() throws Exception {
      ArchiveAllowedArguments args = api.getArchiveAllowedArguments();
      assertNotNull(args);
      assertNotNull(args.getArchiveSizes());
      assertTrue(args.getArchiveSizes().size() > 0);
      
      for (int size : args.getArchiveSizes()) {
         assertTrue(size > 0);
      }
   }
   
   @Test
   public void testCreateArchive() throws Exception {
      try {
         api.deleteArchive(archiveUser);
      } catch(Exception ex) {
      }
      
      int before = api.listArchives().size();
      
      api.createArchive(archiveUser, "password", 10);

      assertTrue(archiveCounter.apply(before + 1));
   }

   @Test(dependsOnMethods = "testCreateArchive")
   public void testArchiveDetails() throws Exception {
      Archive details = api.getArchive(archiveUser);
      assertEquals(details.getUsername(), archiveUser);
   }

   @Test(dependsOnMethods = "testCreateArchive")
   public void testChangePassword() throws Exception {
      api.changeArchivePassword(archiveUser, "newpassword");      
      // TODO assert something useful!
   }

   @Test(dependsOnMethods = "testCreateArchive")
   public void testResizeArchive() throws Exception {
      api.resizeArchive(archiveUser, 20);

      assertTrue(new RetryablePredicate<String>(
            new Predicate<String>() {
               public boolean apply(String value){
                  return api.getArchive(archiveUser) != null && value.equals(api.getArchive(archiveUser).getTotalSize());
               }
            }, 30, 1, TimeUnit.SECONDS).apply("20 GB"));
   }

}
