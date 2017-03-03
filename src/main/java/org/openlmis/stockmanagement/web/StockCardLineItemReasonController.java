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
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

import org.openlmis.stockmanagement.domain.adjustment.StockCardLineItemReason;
import org.openlmis.stockmanagement.service.StockCardLineItemReasonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/api")
public class StockCardLineItemReasonController {

  @Autowired
  private StockCardLineItemReasonService reasonService;

  /**
   * Create a new stock card line item reason. If the id is specified, ID will be ignored.
   *
   * @param reason a stock card line item reason bound to request body
   * @return created stock card line item reason
   */
  @RequestMapping(value = "stockCardLineItemReasons", method = POST)
  public ResponseEntity<StockCardLineItemReason> createReason(
      @RequestBody StockCardLineItemReason reason) {
    if (reasonService.reasonExists(reason)) {
      return new ResponseEntity<>(reason, OK);
    }
    reason.setId(null);
    return new ResponseEntity<>(reasonService.saveOrUpdate(reason), CREATED);
  }

  @RequestMapping(value = "stockCardLineItemReasons", method = GET)
  public ResponseEntity<List<StockCardLineItemReason>> getAllReasons() {
    return new ResponseEntity<>(reasonService.findReasons(), OK);
  }

  /**
   * Update a stock card line item reason.
   *
   * @param reason   a stock card line item reason bound to request body
   * @param reasonId ID of the reason would be updated
   * @return updated stock card line item reason
   */
  @RequestMapping(value = "stockCardLineItemReasons/{id}", method = PUT)
  public ResponseEntity<StockCardLineItemReason> updateReason(
      @RequestBody StockCardLineItemReason reason, @PathVariable("id") UUID reasonId) {
    reasonService.checkUpdateReasonIdExists(reasonId);
    reason.setId(reasonId);
    return new ResponseEntity<>(reasonService.saveOrUpdate(reason), OK);
  }
}
