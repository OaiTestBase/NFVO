/*
 * Copyright (c) 2016 Open Baton (http://openbaton.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openbaton.nfvo.api.admin;

import io.swagger.annotations.ApiOperation;
import java.util.List;
import org.openbaton.catalogue.nfvo.VnfmManagerEndpoint;
import org.openbaton.nfvo.core.interfaces.VNFManagerManagement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/vnfmanagers")
public class RestVNFManager {

  private Logger log = LoggerFactory.getLogger(this.getClass());

  @Autowired private VNFManagerManagement vnfManagerManagement;

  @ApiOperation(
    value = "Retrieve all registered VNFM endpoints",
    notes = "Returns all Virtual Network Function Managers running and attached to the NFVO"
  )
  @RequestMapping(method = RequestMethod.GET)
  @ResponseStatus(HttpStatus.OK)
  public List<VnfmManagerEndpoint> findAll() {
    return (List<VnfmManagerEndpoint>) vnfManagerManagement.query();
  }

  @ApiOperation(
    value = "Retrieving a registered VNFM’s endpoint",
    notes = "Specify the id of the Virtual Network Function Manager in the URL"
  )
  @RequestMapping(value = "{id}", method = RequestMethod.GET)
  @ResponseStatus(HttpStatus.OK)
  public VnfmManagerEndpoint findById(@PathVariable("id") String id) {
    return vnfManagerManagement.query(id);
  }

  @ApiOperation(
    value = "Remove a registered VNFM from the NFVO",
    notes = "Specify the id of the Virtual Network Function Manager in the URL"
  )
  @RequestMapping(value = "{id}", method = RequestMethod.DELETE)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteVnfm(@PathVariable("id") String id) {
    vnfManagerManagement.delete(id);
  }
}
