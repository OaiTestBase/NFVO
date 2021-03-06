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

package org.openbaton.nfvo.api.catalogue;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.swagger.annotations.ApiOperation;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.validation.Valid;
import org.openbaton.catalogue.mano.descriptor.VirtualNetworkFunctionDescriptor;
import org.openbaton.catalogue.nfvo.Script;
import org.openbaton.catalogue.nfvo.VNFPackage;
import org.openbaton.exceptions.*;
import org.openbaton.nfvo.core.interfaces.VNFPackageManagement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/vnf-packages")
@ConfigurationProperties(prefix = "nfvo.marketplace.privateip")
public class RestVNFPackage {
  private String ip;

  public String getIp() {
    return this.ip;
  }

  public void setIp(String ip) {
    this.ip = ip;
  }

  private Logger log = LoggerFactory.getLogger(this.getClass());

  @Autowired private VNFPackageManagement vnfPackageManagement;

  /** Adds a new VNFPackage to the VNFPackages repository */
  @ApiOperation(
    value = "Adding a VNFPackage",
    notes =
        "The request parameter 'file' specifies an archive which is needed to instantiate a VNFPackage. "
            + "On how to create such an archive refer to: http://openbaton.github.io/documentation/vnfpackage/"
  )
  @RequestMapping(method = RequestMethod.POST)
  @ResponseBody
  public VirtualNetworkFunctionDescriptor onboard(
      @RequestParam("file") MultipartFile file,
      @RequestHeader(value = "project-id") String projectId)
      throws IOException, VimException, NotFoundException, SQLException, PluginException,
          IncompatibleVNFPackage, AlreadyExistingException, NetworkServiceIntegrityException,
          BadRequestException, BadFormatException, InterruptedException,
          EntityUnreachableException {

    log.debug("Onboarding");
    if (file == null || file.isEmpty()) throw new BadRequestException("File is null or empty!");
    byte[] bytes = file.getBytes();
    VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor = null;
    try {
      virtualNetworkFunctionDescriptor = vnfPackageManagement.add(bytes, false, projectId, false);
    } catch (ExecutionException
        | ExistingVNFPackage
        | DescriptorWrongFormat
        | VNFPackageFormatException e) {
      if (log.isDebugEnabled()) log.error(e.getMessage(), e);
      else log.error(e.getMessage());
      throw new BadRequestException(e.getMessage());
    }
    return virtualNetworkFunctionDescriptor;
  }

  @ApiOperation(
    value = "Adding a VNFPackage from the Open Baton marketplace",
    notes =
        "The JSON object in the request body contains a field named link, which holds the URL to the package on the Open Baton Marketplace"
  )
  @RequestMapping(
    value = "/marketdownload",
    method = RequestMethod.POST,
    consumes = MediaType.APPLICATION_JSON_VALUE
  )
  public String marketDownload(
      @RequestBody JsonObject link, @RequestHeader(value = "project-id") String projectId)
      throws IOException, PluginException, VimException, NotFoundException, IncompatibleVNFPackage,
          AlreadyExistingException, NetworkServiceIntegrityException, BadRequestException,
          InterruptedException, EntityUnreachableException, BadFormatException {
    Gson gson = new Gson();
    JsonObject jsonObject = gson.fromJson(link, JsonObject.class);
    if (!jsonObject.has("link"))
      throw new BadRequestException("The sent Json has to contain a field named: link");

    String downloadlink;
    try {
      downloadlink = jsonObject.getAsJsonPrimitive("link").getAsString();
    } catch (Exception e) {
      e.printStackTrace();
      throw new BadRequestException("The provided link has to be a string.");
    }
    VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor =
        vnfPackageManagement.onboardFromMarket(downloadlink, projectId);
    return "{ \"id\": \"" + virtualNetworkFunctionDescriptor.getVnfPackageLocation() + "\"}";
  }

  @ApiOperation(
    value = "Adding a VNFPackage from the Package Repository",
    notes =
        "The JSON object in the request body contains a field named link, which holds the URL to the package on the Open Baton Marketplace"
  )
  @RequestMapping(
    value = "/package-repository-download",
    method = RequestMethod.POST,
    consumes = MediaType.APPLICATION_JSON_VALUE
  )
  public String packageRepositoryDownload(
      @RequestBody JsonObject link, @RequestHeader(value = "project-id") String projectId)
      throws IOException, PluginException, VimException, NotFoundException, IncompatibleVNFPackage,
          AlreadyExistingException, NetworkServiceIntegrityException, BadRequestException,
          EntityUnreachableException, InterruptedException {
    Gson gson = new Gson();
    JsonObject jsonObject = gson.fromJson(link, JsonObject.class);
    if (!jsonObject.has("link"))
      throw new BadRequestException("The sent Json has to contain a field named: link");

    String downloadlink;
    try {
      downloadlink = jsonObject.getAsJsonPrimitive("link").getAsString();
    } catch (Exception e) {
      e.printStackTrace();
      throw new BadRequestException("The provided link has to be a string.");
    }
    VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor =
        vnfPackageManagement.onboardFromPackageRepository(downloadlink, projectId);
    return "{ \"id\": \"" + virtualNetworkFunctionDescriptor.getVnfPackageLocation() + "\"}";
  }

  /**
   * Removes the VNFPackage from the VNFPackages repository
   *
   * @param id: id of the package to delete
   */
  @ApiOperation(
    value = "Remove a VNFPackage",
    notes = "The id of the package that has to be removed in in the URL"
  )
  @RequestMapping(value = "{id}", method = RequestMethod.DELETE)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(
      @PathVariable("id") String id, @RequestHeader(value = "project-id") String projectId)
      throws WrongAction {
    vnfPackageManagement.delete(id, projectId);
  }
  /**
   * Removes multiple VNFPackage from the VNFPackages repository
   *
   * @param ids: The List of the VNFPackage Id to be deleted
   * @throws NotFoundException, WrongAction
   */
  @ApiOperation(
    value = "Removing multiple VNFPackages",
    notes = "A list of VNF Package ids has to be provided in the Request Body"
  )
  @RequestMapping(
    value = "/multipledelete",
    method = RequestMethod.POST,
    consumes = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void multipleDelete(
      @RequestBody @Valid List<String> ids, @RequestHeader(value = "project-id") String projectId)
      throws NotFoundException, WrongAction {
    for (String id : ids) vnfPackageManagement.delete(id, projectId);
  }

  /**
   * Returns the list of the VNFPackages available
   *
   * @return List<VNFPackage>: The list of VNFPackages available
   */
  @ApiOperation(
    value = "Retrieve all VNFPackages",
    notes = "Returns all VNF Packages onboarded on the specified project"
  )
  @RequestMapping(method = RequestMethod.GET)
  public List<VNFPackage> findAll(@RequestHeader(value = "project-id") String projectId) {
    return (List<VNFPackage>) vnfPackageManagement.queryByProjectId(projectId);
  }

  @ApiOperation(
    value = "Retrieve a script from a VNF Package",
    notes = "The ids of the package and the script are provided in the URL"
  )
  @RequestMapping(
    value = "{id}/scripts/{scriptId}",
    method = RequestMethod.GET,
    produces = MediaType.TEXT_PLAIN_VALUE
  )
  public String getScript(
      @PathVariable("id") String id,
      @PathVariable("scriptId") String scriptId,
      @RequestHeader(value = "project-id") String projectId)
      throws NotFoundException {
    VNFPackage vnfPackage = vnfPackageManagement.query(id, projectId);
    if (vnfPackage == null) throw new NotFoundException("No VNFPackage found with ID " + id);
    for (Script script : vnfPackage.getScripts()) {
      if (script.getId().equals(scriptId)) {
        return new String(script.getPayload());
      }
    }
    throw new NotFoundException(
        "Script with id " + scriptId + " was not found into package with id " + id);
  }

  @ApiOperation(
    value = "Update a script of a VNF Package",
    notes = "The updated script has to be passed in the Request Body"
  )
  @RequestMapping(
    value = "{id}/scripts/{scriptId}",
    method = RequestMethod.PUT,
    produces = MediaType.TEXT_PLAIN_VALUE,
    consumes = MediaType.TEXT_PLAIN_VALUE
  )
  public String updateScript(
      @PathVariable("id") String vnfPackageId,
      @PathVariable("scriptId") String scriptId,
      @RequestBody String scriptNew,
      @RequestHeader(value = "project-id") String projectId)
      throws NotFoundException, BadFormatException, ExecutionException, InterruptedException {
    VNFPackage vnfPackage = vnfPackageManagement.query(vnfPackageId, projectId);
    if (vnfPackage == null)
      throw new NotFoundException("No VNFPackage found with ID " + vnfPackageId);
    for (Script script : vnfPackage.getScripts()) {
      if (script.getId().equals(scriptId)) {
        script.setPayload(scriptNew.getBytes());
        script = vnfPackageManagement.updateScript(script, vnfPackageId);
        return new String(script.getPayload());
      }
    }
    throw new NotFoundException(
        "Script with id " + scriptId + " was not found into package with id " + vnfPackageId);
  }

  /**
   * Returns the VNFPackage selected by id
   *
   * @param id : The id of the VNFPackage
   * @return VNFPackage: The VNFPackage selected
   */
  @ApiOperation(
    value = "Retrieve a VNFPackage",
    notes = "Returns the VNF Package corresponding to the id specified in the URL"
  )
  @RequestMapping(value = "{id}", method = RequestMethod.GET)
  public VNFPackage findById(
      @PathVariable("id") String id, @RequestHeader(value = "project-id") String projectId)
      throws NotFoundException {
    VNFPackage vnfPackage = vnfPackageManagement.query(id, projectId);
    if (vnfPackage == null) throw new NotFoundException("No VNFPackage found with ID " + id);
    return vnfPackage;
  }

  /**
   * Updates the VNFPackage
   *
   * @param vnfPackage_new : The VNFPackage to be updated
   * @param id : The id of the VNFPackage
   * @return VNFPackage The VNFPackage updated
   */
  @ApiOperation(
    value = "Update a VNFPackage",
    notes = "The updated VNF Package is passed in the request body"
  )
  @RequestMapping(
    value = "{id}",
    method = RequestMethod.PUT,
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseStatus(HttpStatus.ACCEPTED)
  public VNFPackage update(
      @RequestBody @Valid VNFPackage vnfPackage_new,
      @PathVariable("id") String id,
      @RequestHeader(value = "project-id") String projectId)
      throws NotFoundException {
    return vnfPackageManagement.update(id, vnfPackage_new, projectId);
  }
}
