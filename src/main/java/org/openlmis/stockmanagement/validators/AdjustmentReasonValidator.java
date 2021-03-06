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

package org.openlmis.stockmanagement.validators;

import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_ADJUSTMENT_REASON_CATEGORY_INVALID;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_ADJUSTMENT_REASON_TYPE_INVALID;

import org.openlmis.stockmanagement.domain.event.StockEventLineItem;
import org.openlmis.stockmanagement.domain.reason.StockCardLineItemReason;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.util.Message;
import org.springframework.stereotype.Component;

/**
 * An adjustment should have a reason that is either DEBIT or CREDIT.
 * And it should have a reason category that is ADJUSTMENT
 */
@Component(value = "AdjustmentReasonValidator")
public class AdjustmentReasonValidator implements StockEventValidator {

  @Override
  public void validate(StockEventDto stockEventDto) {
    LOGGER.debug("Validate adjustment reason");
    boolean hasSourceOrDestination = stockEventDto.hasSource() || stockEventDto.hasDestination();
    if (hasSourceOrDestination || !stockEventDto.hasLineItems()) {
      return;
    }

    for (StockEventLineItem lineItem : stockEventDto.getLineItems()) {
      if (lineItem.hasReasonId()) {
        validateReason(stockEventDto, lineItem);
      }
    }
  }

  private void validateReason(StockEventDto event, StockEventLineItem lineItem) {
    StockCardLineItemReason foundReason = event
        .getContext()
        .findEventReason(lineItem.getReasonId());

    //this validator does not care if reason id not found in db
    //that is handled by other validators
    if (foundReason != null) {
      validReasonType(foundReason);
      validReasonCategory(foundReason);
    }
  }

  private void validReasonType(StockCardLineItemReason reason) {
    if (!reason.isCreditReasonType() && !reason.isDebitReasonType()) {
      throw new ValidationMessageException(
          new Message(ERROR_EVENT_ADJUSTMENT_REASON_TYPE_INVALID, reason.getReasonType()));
    }
  }

  private void validReasonCategory(StockCardLineItemReason reason) {
    if (!reason.isAdjustmentReasonCategory()) {
      throw new ValidationMessageException(
          new Message(ERROR_EVENT_ADJUSTMENT_REASON_CATEGORY_INVALID,
              reason.getReasonCategory()));
    }
  }
}
