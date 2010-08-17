/**
 *
 * Copyright (C) 2010 Cloud Conscious, LLC. <info@cloudconscious.com>
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ====================================================================
 */

package org.jclouds.rackspace.cloudservers.compute.strategy;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.strategy.GetNodeMetadataStrategy;
import org.jclouds.rackspace.cloudservers.CloudServersClient;
import org.jclouds.rackspace.cloudservers.domain.Server;

import com.google.common.base.Function;

/**
 * @author Adrian Cole
 */
@Singleton
public class CloudServersGetNodeMetadataStrategy implements GetNodeMetadataStrategy {

   private final CloudServersClient client;
   private final Function<Server, NodeMetadata> serverToNodeMetadata;

   @Inject
   protected CloudServersGetNodeMetadataStrategy(CloudServersClient client,
            Function<Server, NodeMetadata> serverToNodeMetadata) {
      this.client = client;
      this.serverToNodeMetadata = serverToNodeMetadata;
   }

   @Override
   public NodeMetadata execute(String id) {
      int serverId = Integer.parseInt(id);
      Server server = client.getServer(serverId);
      return server == null ? null : serverToNodeMetadata.apply(server);
   }
}