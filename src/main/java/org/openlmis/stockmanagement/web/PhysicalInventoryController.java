/*
 * This program is part of the OpenLMIS logistics management information system platform software.
 * Copyright © 2017 VillageReach
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of the GNU Affero General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *  
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU Affero General Public License for more details. You should have received a copy of
 * the GNU Affero General Public License along with this program. If not, see
 * http://www.gnu.org/licenses.  For additional information contact info@OpenLMIS.org. 
 */

package org.openlmis.stockmanagement.web;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import org.openlmis.stockmanagement.dto.PhysicalInventoryDto;
import org.openlmis.stockmanagement.service.PermissionService;
import org.openlmis.stockmanagement.service.PhysicalInventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@Controller
@RequestMapping("/api")
public class PhysicalInventoryController {

  @Autowired
  private PermissionService permissionService;

  @Autowired
  private PhysicalInventoryService physicalInventoryService;

  /**
   * Get a draft physical inventory.
   *
   * @param program  program ID.
   * @param facility facility ID.
   * @return returns found draft, if not found, returns empty draft.
   */
  @RequestMapping(value = "physicalInventories/draft", method = GET)
  public ResponseEntity<PhysicalInventoryDto> findDraft(
      @RequestParam UUID program,
      @RequestParam UUID facility) {
    permissionService.canEditPhysicalInventory(program, facility);
    return new ResponseEntity<>(physicalInventoryService.findDraft(program, facility), OK);
  }

  /**
   * Save a draft physical inventory.
   *
   * @param dto physical inventory dto.
   * @return created physical inventory dto.
   */
  @Transactional
  @RequestMapping(value = "physicalInventories/draft", method = POST)
  public ResponseEntity<PhysicalInventoryDto> saveDraft(@RequestBody PhysicalInventoryDto dto) {
    permissionService.canEditPhysicalInventory(dto.getProgramId(), dto.getFacilityId());
    return new ResponseEntity<>(physicalInventoryService.saveDraft(dto), CREATED);
  }

  /**
   * Delete a draft physical inventory.
   *
   * @param dto physical inventory dto.
   * @return No content status.
   */
  @RequestMapping(value = "physicalInventories/draft", method = DELETE)
  public ResponseEntity deleteDraft(@RequestBody PhysicalInventoryDto dto) {
    permissionService.canEditPhysicalInventory(dto.getProgramId(), dto.getFacilityId());
    physicalInventoryService.deleteExistingDraft(dto);
    return new ResponseEntity<>(null, NO_CONTENT);
  }

}
