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

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.empty;
import static org.openlmis.stockmanagement.domain.identity.OrderableLotIdentity.identityOf;

import org.openlmis.stockmanagement.domain.card.StockCard;
import org.openlmis.stockmanagement.domain.identity.IdentifiableByOrderableLot;
import org.openlmis.stockmanagement.domain.identity.OrderableLotIdentity;
import org.openlmis.stockmanagement.dto.StockCardDto;
import org.openlmis.stockmanagement.dto.referencedata.LotDto;
import org.openlmis.stockmanagement.dto.referencedata.OrderableDto;
import org.openlmis.stockmanagement.repository.StockCardRepository;
import org.openlmis.stockmanagement.service.referencedata.ApprovedProductReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.LotReferenceDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import lombok.Getter;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * This class is in charge of retrieving stock card summaries(stock cards with soh but not line
 * items).
 * Its result may include existing stock cards only, or it may include dummy stock cards for
 * approved products and their lots. See SearchOptions for details.
 */
@Service
public class StockCardSummariesService extends StockCardBaseService {
  private static final Logger LOGGER = LoggerFactory.getLogger(StockCardSummariesService.class);

  @Autowired
  private ApprovedProductReferenceDataService approvedProductService;

  @Autowired
  private LotReferenceDataService lotReferenceDataService;

  @Autowired
  private StockCardRepository cardRepository;

  /**
   * Find all stock cards by program id and facility id. No paging, all in one.
   * Used for generating pdf file of all stock cards.
   *
   * @param programId  program id.
   * @param facilityId facility id.
   * @return found stock cards.
   */
  public List<StockCardDto> findStockCards(UUID programId, UUID facilityId) {
    List<StockCard> cards = cardRepository.findByProgramIdAndFacilityId(programId, facilityId);

    return cardsToDtos(programId, facilityId, cards);
  }

  /**
   * Get a page of stock cards.
   *
   * @param programId  program id.
   * @param facilityId facility id.
   * @param pageable   page object.
   * @return page of stock cards.
   */
  public Page<StockCardDto> findStockCards(UUID programId, UUID facilityId, Pageable pageable) {
    Page<StockCard> pageOfCards = cardRepository
        .findByProgramIdAndFacilityId(programId, facilityId, pageable);

    List<StockCardDto> cardDtos = cardsToDtos(programId, facilityId, pageOfCards.getContent());
    return new PageImpl<>(cardDtos, pageable, pageOfCards.getTotalElements());
  }

  /**
   * Create dummy cards for approved products and lots that don't have cards yet.
   *
   * @param programId  programId
   * @param facilityId facilityId
   * @return dummy cards.
   */
  public List<StockCardDto> createDummyStockCards(UUID programId, UUID facilityId) {
    //this will not read the whole table, only the orderable id and lot id
    List<OrderableLotIdentity> existingCardIdentities =
        cardRepository.getIdentitiesBy(programId, facilityId);

    LOGGER.info("Calling ref data to get all approved orderables");
    Map<OrderableLotIdentity, OrderableLot> orderableLotsMap = createOrderableLots(
        approvedProductService.getAllApprovedProducts(programId, facilityId));

    //create dummy(fake/not persisted) cards for approved orderables that don't have cards yet
    List<StockCard> dummyCards = createDummyCards(programId, facilityId, orderableLotsMap.values(),
        existingCardIdentities).collect(toList());
    return assignOrderableLotRemoveLineItems(createDtos(dummyCards), orderableLotsMap);
  }

  private List<StockCardDto> cardsToDtos(UUID programId, UUID facilityId, List<StockCard> cards) {
    LOGGER.info("Calling ref data to get all approved orderables");
    Map<OrderableLotIdentity, OrderableLot> orderableLotsMap = createOrderableLots(
        approvedProductService.getAllApprovedProducts(programId, facilityId));

    return assignOrderableLotRemoveLineItems(createDtos(cards), orderableLotsMap);
  }

  private List<StockCardDto> assignOrderableLotRemoveLineItems(
      List<StockCardDto> stockCardDtos,
      Map<OrderableLotIdentity, OrderableLot> orderableLotsMap) {
    stockCardDtos.forEach(stockCardDto -> {
      OrderableLot orderableLot = orderableLotsMap.get(identityOf(stockCardDto));
      stockCardDto.setOrderable(orderableLot.getOrderable());
      stockCardDto.setLot(orderableLot.getLot());
      stockCardDto.setLineItems(null);//line items are not needed in summary
    });
    return stockCardDtos;
  }

  private Stream<StockCard> createDummyCards(UUID programId, UUID facilityId,
                                             Collection<OrderableLot> orderableLots,
                                             List<OrderableLotIdentity> cardIdentities) {
    return filterOrderableLotsWithoutCards(orderableLots, cardIdentities)
        .stream()
        .map(orderableLot -> StockCard.builder()
            .programId(programId)
            .facilityId(facilityId)
            .orderableId(orderableLot.getOrderable().getId())
            .lotId(orderableLot.getLotId())
            .lineItems(emptyList())//dummy cards don't have line items
            .build());
  }

  private List<OrderableLot> filterOrderableLotsWithoutCards(
      Collection<OrderableLot> orderableLots, List<OrderableLotIdentity> cardIdentities) {
    return orderableLots.stream()
        .filter(orderableLot -> cardIdentities.stream()
            .noneMatch(cardIdentity -> cardIdentity.equals(identityOf(orderableLot))))
        .collect(toList());
  }

  private Map<OrderableLotIdentity, OrderableLot> createOrderableLots(
      List<OrderableDto> orderableDtos) {
    Stream<OrderableLot> orderableLots = orderableDtos.stream().flatMap(this::lotsOfOrderable);

    Stream<OrderableLot> orderablesOnly = orderableDtos.stream()
        .map(orderableDto -> new OrderableLot(orderableDto, null));

    return concat(orderableLots, orderablesOnly)
        .collect(toMap(OrderableLotIdentity::identityOf, orderableLot -> orderableLot));
  }

  private Stream<OrderableLot> lotsOfOrderable(OrderableDto orderableDto) {
    String tradeItemId = orderableDto.getIdentifiers().get("tradeItem");
    if (tradeItemId != null) {
      return lotReferenceDataService.getAllLotsOf(UUID.fromString(tradeItemId)).stream()
          .map(lot -> new OrderableLot(orderableDto, lot));
    } else {
      return empty();
    }
  }

  @Getter
  private static class OrderableLot implements IdentifiableByOrderableLot {
    private OrderableDto orderable;
    private LotDto lot;

    OrderableLot(OrderableDto orderable, LotDto lot) {
      this.orderable = orderable;
      this.lot = lot;
    }

    public UUID getLotId() {
      return lot == null ? null : lot.getId();
    }

    public UUID getOrderableId() {
      return orderable.getId();
    }
  }

}
