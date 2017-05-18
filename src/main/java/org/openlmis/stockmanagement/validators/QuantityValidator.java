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

import static java.util.stream.Collectors.groupingBy;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_DEBIT_QUANTITY_EXCEED_SOH;

import org.openlmis.stockmanagement.domain.card.StockCard;
import org.openlmis.stockmanagement.domain.card.StockCardLineItem;
import org.openlmis.stockmanagement.domain.event.StockEventLineItem;
import org.openlmis.stockmanagement.domain.identity.OrderableLotIdentity;
import org.openlmis.stockmanagement.domain.reason.StockCardLineItemReason;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.repository.StockCardLineItemReasonRepository;
import org.openlmis.stockmanagement.repository.StockCardRepository;
import org.openlmis.stockmanagement.utils.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * This validator makes sure stock on hand does NOT go below zero for any stock card.
 *
 * It does so by re-calculating soh of each orderable/lot combo that either has a destination or has
 * a debit reason.
 *
 * The re-calculation only happens for issue and negative adjustment. It does not apply to physical
 * inventory and receive.
 *
 * This has a negative impact on performance. The impact grows larger as stock card line items
 * accumulates over time. Because re-calculation requires reading stock card line items from DB.
 */
@Component(value = "QuantityValidator")
public class QuantityValidator implements StockEventValidator {

  @Autowired
  private StockCardLineItemReasonRepository reasonRepository;

  @Autowired
  private StockCardRepository stockCardRepository;

  @Override
  public void validate(StockEventDto stockEventDto)
      throws IllegalAccessException, InstantiationException {
    LOGGER.debug("Validate quantity");
    if (!stockEventDto.hasLineItems()) {
      return;
    }

    Map<OrderableLotIdentity, List<StockEventLineItem>> sameOrderableGroups = stockEventDto
        .getLineItems().stream()
        .collect(groupingBy(OrderableLotIdentity::identityOf));

    for (List<StockEventLineItem> group : sameOrderableGroups.values()) {
      boolean anyDebitInGroup = group.stream().anyMatch(this::hasDebitReason);
      if (stockEventDto.hasDestination() || anyDebitInGroup) {
        validateQuantity(stockEventDto, group);
      }
    }
  }

  private void validateQuantity(StockEventDto stockEventDto, List<StockEventLineItem> group)
      throws InstantiationException, IllegalAccessException {
    StockCard foundCard =
        tryFindCard(stockEventDto.getProgramId(), stockEventDto.getFacilityId(), group.get(0));

    //create line item from event line item and add it to stock card for recalculation
    calculateStockOnHand(stockEventDto, group, foundCard);
    foundCard.getLineItems().forEach(item -> {
      if (item.getStockOnHand() < 0) {
        throwQuantityError(group);
      }
    });
  }

  private StockCard tryFindCard(UUID programId, UUID facilityId, StockEventLineItem lineItem) {
    StockCard foundCard = stockCardRepository
        .findByProgramIdAndFacilityIdAndOrderableIdAndLotId(programId, facilityId,
            lineItem.getOrderableId(), lineItem.getLotId());
    if (foundCard == null) {
      StockCard emptyCard = new StockCard();
      emptyCard.setLineItems(new ArrayList<>());
      return emptyCard;
    } else {
      //use a shallow copy of stock card to do recalculation, because some domain model will be
      //modified during recalculation, this will avoid persistence of those modified models
      try {
        return foundCard.shallowCopy();
      } catch (InvocationTargetException | NoSuchMethodException
          | InstantiationException | IllegalAccessException ex) {
        //if this exception is ever seen in front end, that means our code has a bug. we only put
        //this here to satisfy checkstyle/pmd and to make sure potential bug is not hidden.
        throw new ValidationMessageException(new Message("Error during shallow copy", ex));
      }
    }
  }

  private void throwQuantityError(List<StockEventLineItem> group) {
    throw new ValidationMessageException(
        new Message(ERROR_EVENT_DEBIT_QUANTITY_EXCEED_SOH, group));
  }

  private void calculateStockOnHand(StockEventDto eventDto, List<StockEventLineItem> group,
                                    StockCard foundCard)
      throws InstantiationException, IllegalAccessException {
    for (StockEventLineItem lineItem : group) {
      StockCardLineItem stockCardLineItem = StockCardLineItem
          .createLineItemFrom(eventDto, lineItem, foundCard, null, null);
      stockCardLineItem.setReason(findReason(lineItem.getReasonId()));
    }

    foundCard.calculateStockOnHand();
  }

  private StockCardLineItemReason findReason(UUID reasonId) {
    if (reasonId != null) {
      return reasonRepository.findOne(reasonId);
    }
    return null;
  }

  private boolean hasDebitReason(StockEventLineItem lineItem) {
    StockCardLineItemReason reason = findReason(lineItem.getReasonId());
    return reason != null && reason.isDebitReasonType();
  }
}
