package org.openlmis.stockmanagement.service;

import org.openlmis.stockmanagement.domain.event.StockEvent;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.repository.StockEventsRepository;
import org.openlmis.stockmanagement.util.AuthenticationHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * A service that is in charge of saving stock events and generating stock cards
 * and line items from stock events.
 */
@Service
public class StockEventProcessor {

  @Autowired
  private AuthenticationHelper authenticationHelper;

  @Autowired
  private StockEventValidationsService stockEventValidationsService;

  @Autowired
  private StockEventsRepository stockEventsRepository;

  /**
   * Validate and persist event and its line items.
   *
   * @param stockEventDto stock event dto.
   * @return the persisted event's id.
   */
  public UUID process(StockEventDto stockEventDto)
          throws IllegalAccessException, InstantiationException {
    stockEventValidationsService.validate(stockEventDto);

    StockEvent stockEvent = stockEventDto.toEvent(authenticationHelper.getCurrentUser().getId());
    return stockEventsRepository.save(stockEvent).getId();
  }
}
