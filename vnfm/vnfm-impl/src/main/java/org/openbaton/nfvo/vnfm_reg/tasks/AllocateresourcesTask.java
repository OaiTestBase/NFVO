/*
 * Copyright (c) 2016 Open Baton (http://www.openbaton.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.openbaton.nfvo.vnfm_reg.tasks;

import java.util.Date;
import java.util.Map;
import java.util.Set;
import org.openbaton.catalogue.mano.common.Event;
import org.openbaton.catalogue.mano.descriptor.VNFComponent;
import org.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.openbaton.catalogue.nfvo.Action;
import org.openbaton.catalogue.nfvo.messages.Interfaces.NFVMessage;
import org.openbaton.catalogue.nfvo.messages.OrVnfmGenericMessage;
import org.openbaton.catalogue.nfvo.viminstances.BaseVimInstance;
import org.openbaton.catalogue.security.Key;
import org.openbaton.nfvo.core.interfaces.ResourceManagement;
import org.openbaton.nfvo.repositories.VimRepository;
import org.openbaton.nfvo.vnfm_reg.tasks.abstracts.AbstractTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/** Created by lto on 06/08/15. */
@Service
@Scope("prototype")
public class AllocateresourcesTask extends AbstractTask {
  @Autowired private ResourceManagement resourceManagement;
  private Map<String, BaseVimInstance> vims;
  private String userData;
  @Autowired private VimRepository vimRepository;

  @Override
  protected void setEvent() {
    event = Event.ALLOCATE.name();
  }

  @Override
  protected void setDescription() {
    description =
        "All the resources that are contained in this VNFR were instantiated in the chosen VIM(s)";
  }

  @Override
  protected NFVMessage doWork() throws Exception {
    if (virtualNetworkFunctionRecord.getName().contains("client")) log.info("client");
    log.info(
        "Executing task: AllocateResources for VNFR: " + virtualNetworkFunctionRecord.getName());
    log.trace(
        "VNFR ("
            + virtualNetworkFunctionRecord.getId()
            + ") received hibernate version is = "
            + virtualNetworkFunctionRecord.getHbVersion());

    printOldAndNewHibernateVersion();

    for (VirtualDeploymentUnit vdu : virtualNetworkFunctionRecord.getVdu()) {
      BaseVimInstance vimInstance = vims.get(vdu.getId());
      if (vimInstance == null) {
        throw new NullPointerException(
            "Our algorithms are too complex, even for us, this is what abnormal IQ means :(");
      }
      vimInstance = vimRepository.findFirstById(vimInstance.getId());
      log.debug(
          "Allocating VDU: "
              + vdu.getName()
              + " to vim instance: "
              + vimInstance.getName()
              + " - id: "
              + vimInstance.getId());

      for (VNFComponent vnfc : vdu.getVnfc()) {
        resourceManagement
            .allocate(vdu, virtualNetworkFunctionRecord, vnfc, vimInstance, userData)
            .get();
      }
    }

    setHistoryLifecycleEvent(new Date());
    saveVirtualNetworkFunctionRecord();

    OrVnfmGenericMessage orVnfmGenericMessage =
        new OrVnfmGenericMessage(virtualNetworkFunctionRecord, Action.ALLOCATE_RESOURCES);
    log.trace(
        "Answering the AllocateResources call via RPC with the following message: "
            + orVnfmGenericMessage);
    log.info(
        "Finished task: AllocateResources for VNFR: " + virtualNetworkFunctionRecord.getName());
    return orVnfmGenericMessage;
  }

  @Override
  public boolean isAsync() {
    return true;
  }

  public void setVims(Map<String, BaseVimInstance> vimChosen) {
    this.vims = vimChosen;
  }

  public void setUserData(String userData) {
    this.userData = userData;
  }

  public void setKeys(Set<Key> keys) {}
}
