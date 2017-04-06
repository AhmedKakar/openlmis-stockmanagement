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

package org.openlmis.stockmanagement.service;

import static java.util.stream.Collectors.toList;
import static org.openlmis.stockmanagement.domain.BaseEntity.fromId;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_PHYSICAL_INVENTORY_LINE_ITEMS_MISSING;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_PHYSICAL_INVENTORY_ORDERABLE_MISSING;

import org.openlmis.stockmanagement.domain.event.StockEvent;
import org.openlmis.stockmanagement.domain.physicalinventory.PhysicalInventory;
import org.openlmis.stockmanagement.dto.PhysicalInventoryDto;
import org.openlmis.stockmanagement.dto.PhysicalInventoryLineItemDto;
import org.openlmis.stockmanagement.dto.StockCardDto;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.repository.PhysicalInventoriesRepository;
import org.openlmis.stockmanagement.utils.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.UUID;

@Service
public class PhysicalInventoryService {

  private static final Logger LOGGER = LoggerFactory.getLogger(PhysicalInventoryService.class);

  @Autowired
  private PhysicalInventoriesRepository physicalInventoriesRepository;

  @Autowired
  private StockCardSummariesService stockCardSummariesService;

  /**
   * Persist physical inventory, with an event id.
   *
   * @param inventoryDto inventoryDto.
   * @param eventId      eventId.
   * @throws IllegalAccessException IllegalAccessException.
   * @throws InstantiationException InstantiationException.
   */
  public void submitPhysicalInventory(PhysicalInventoryDto inventoryDto, UUID eventId)
      throws IllegalAccessException, InstantiationException {
    LOGGER.info("submit physical inventory");

    deleteExistingDraft(inventoryDto);

    PhysicalInventory inventory = inventoryDto.toPhysicalInventoryForSubmit();
    inventory.setStockEvent(fromId(eventId, StockEvent.class));

    physicalInventoriesRepository.save(inventory);
  }

  /**
   * Find draft by program and facility.
   *
   * @param programId  programId.
   * @param facilityId facilityId.
   * @return found draft, or if not found, returns empty draft.
   */
  public PhysicalInventoryDto findDraft(UUID programId, UUID facilityId) {
    PhysicalInventory foundInventory = physicalInventoriesRepository
        .findByProgramIdAndFacilityIdAndIsDraft(programId, facilityId, true);
    if (foundInventory == null) {
      return createEmptyInventory(programId, facilityId);
    } else {
      return assignOrderablesAndSoh(PhysicalInventoryDto.from(foundInventory));
    }
  }

  /**
   * Save or update draft.
   *
   * @param dto physical inventory dto.
   * @return the saved inventory.
   */
  public PhysicalInventoryDto saveDraft(PhysicalInventoryDto dto) {
    validateLineItems(dto);
    deleteExistingDraft(dto);

    physicalInventoriesRepository.save(dto.toPhysicalInventoryForDraft());
    return dto;
  }

  private void deleteExistingDraft(PhysicalInventoryDto dto) {
    PhysicalInventory foundInventory = physicalInventoriesRepository
        .findByProgramIdAndFacilityIdAndIsDraft(dto.getProgramId(), dto.getFacilityId(), true);
    if (foundInventory != null) {
      physicalInventoriesRepository.delete(foundInventory);
    }
  }

  private PhysicalInventoryDto assignOrderablesAndSoh(PhysicalInventoryDto inventoryDto) {
    List<StockCardDto> stockCards = stockCardSummariesService
        .findStockCards(inventoryDto.getProgramId(), inventoryDto.getFacilityId());
    inventoryDto.mergeWith(stockCards);
    return inventoryDto;
  }

  private PhysicalInventoryDto createEmptyInventory(UUID programId, UUID facilityId) {
    return PhysicalInventoryDto.builder()
        .programId(programId)
        .facilityId(facilityId)
        .isStarter(true)
        .lineItems(stockCardSummariesService.findStockCards(programId, facilityId)
            .stream().map(stockCardDto -> PhysicalInventoryLineItemDto.builder()
                .orderable(stockCardDto.getOrderable())
                .stockOnHand(stockCardDto.getStockOnHand())
                .build())
            .collect(toList()))
        .build();
  }

  private void validateLineItems(PhysicalInventoryDto dto) {
    List<PhysicalInventoryLineItemDto> lineItems = dto.getLineItems();
    if (CollectionUtils.isEmpty(lineItems)) {
      throw new ValidationMessageException(
          new Message(ERROR_PHYSICAL_INVENTORY_LINE_ITEMS_MISSING));
    }
    boolean orderableMissing = lineItems.stream()
        .anyMatch(lineItem -> lineItem.getOrderable() == null);
    if (orderableMissing) {
      throw new ValidationMessageException(
          new Message(ERROR_PHYSICAL_INVENTORY_ORDERABLE_MISSING));
    }
  }
}
