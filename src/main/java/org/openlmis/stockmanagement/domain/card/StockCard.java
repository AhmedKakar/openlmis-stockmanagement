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

package org.openlmis.stockmanagement.domain.card;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.LazyCollection;
import org.openlmis.stockmanagement.domain.BaseEntity;
import org.openlmis.stockmanagement.domain.event.StockEvent;
import org.openlmis.stockmanagement.dto.StockEventDto;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static javax.persistence.CascadeType.ALL;
import static org.hibernate.annotations.LazyCollectionOption.FALSE;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "stock_cards", schema = "stockmanagement")
public class StockCard extends BaseEntity {

  @ManyToOne()
  @JoinColumn(nullable = false)
  private StockEvent originEvent;

  @Column(nullable = false)
  private UUID facilityId;
  @Column(nullable = false)
  private UUID programId;
  @Column(nullable = false)
  private UUID orderableId;

  @LazyCollection(FALSE)
  @OneToMany(cascade = ALL, mappedBy = "stockCard")
  private List<StockCardLineItem> lineItems;

  @Transient
  private Integer stockOnHand;

  /**
   * Create stock card from stock event dto.
   *
   * @param stockEventDto the origin event dto.
   * @param savedEventId  the saved event id.
   * @return the created stock card.
   * @throws InstantiationException InstantiationException.
   * @throws IllegalAccessException IllegalAccessException.
   */
  public static StockCard createStockCardFrom(StockEventDto stockEventDto, UUID savedEventId)
      throws InstantiationException, IllegalAccessException {
    return new StockCard(fromId(savedEventId, StockEvent.class),
        stockEventDto.getFacilityId(), stockEventDto.getProgramId(),
        stockEventDto.getOrderableId(), new ArrayList<>(), 0);
  }

  /**
   * Calculate stock on hand for each line item and the card itself.
   */
  public void calculateStockOnHand() {
    reorderLineItemsByDates();

    int previousSoh = 0;
    for (StockCardLineItem lineItem : getLineItems()) {
      lineItem.calculateStockOnHand(previousSoh);
      previousSoh = lineItem.getStockOnHand();
    }
    setStockOnHand(previousSoh);
  }

  private void reorderLineItemsByDates() {
    Comparator<StockCardLineItem> byOccurred =
        comparing(StockCardLineItem::getOccurredDate);
    Comparator<StockCardLineItem> byNoticed =
        comparing(StockCardLineItem::getNoticedDate);

    setLineItems(lineItems.stream().sorted(byOccurred.thenComparing(byNoticed)).collect(toList()));
  }
}
