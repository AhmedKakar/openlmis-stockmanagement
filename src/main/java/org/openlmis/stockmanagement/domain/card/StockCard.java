package org.openlmis.stockmanagement.domain.card;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.openlmis.stockmanagement.domain.BaseEntity;
import org.openlmis.stockmanagement.domain.event.StockEvent;
import org.openlmis.stockmanagement.dto.StockEventDto;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.UUID;

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

  public static StockCard createStockCardFrom(StockEventDto stockEventDto, UUID savedEventId)
          throws InstantiationException, IllegalAccessException {
    return new StockCard(fromId(savedEventId, StockEvent.class), stockEventDto.getFacilityId(),
            stockEventDto.getProgramId(), stockEventDto.getOrderableId());
  }
}
