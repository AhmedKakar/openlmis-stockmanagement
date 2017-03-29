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

import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_NO_FOLLOWING_PERMISSION;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import org.openlmis.stockmanagement.dto.StockEventDto2;
import org.openlmis.stockmanagement.exception.PermissionMessageException;
import org.openlmis.stockmanagement.service.PermissionService;
import org.openlmis.stockmanagement.service.StockEventProcessor2;
import org.openlmis.stockmanagement.utils.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.UUID;

/**
 * Controller used to create stock event.
 */
@Controller
@RequestMapping("/api")
public class StockEventsController2 {

  private static final Logger LOGGER = LoggerFactory.getLogger(StockEventsController2.class);

  @Autowired
  private PermissionService permissionService;

  @Autowired
  private StockEventProcessor2 stockEventProcessor;

  /**
   * Create stock event.
   *
   * @param eventDto a stock event bound to request body.
   * @return created stock event's ID.
   * @throws InstantiationException InstantiationException.
   * @throws IllegalAccessException IllegalAccessException.
   */
  @RequestMapping(value = "stockEventsRevised", method = POST)
  @Transactional(rollbackFor = {InstantiationException.class, IllegalAccessException.class})
  public ResponseEntity<UUID> createStockEvent(@RequestBody StockEventDto2 eventDto)
      throws InstantiationException, IllegalAccessException {
    LOGGER.debug("Try to create a stock event");
    rejectIfIssueOrReceive(eventDto);
    permissionService.canMakeAdjustment(eventDto.getProgramId(), eventDto.getFacilityId());
    UUID createdEventId = stockEventProcessor.process(eventDto).get(0);
    return new ResponseEntity<>(createdEventId, CREATED);
  }

  //this method blocks user from doing issue/receive.
  //this block will be removed when we support issue/receive.
  private void rejectIfIssueOrReceive(StockEventDto2 eventDto) {
    if (eventDto.hasSource() || eventDto.hasDestination()) {
      throw new PermissionMessageException(
          new Message(ERROR_NO_FOLLOWING_PERMISSION, "Issue/Receive blocked for now"));
    }
  }

}
