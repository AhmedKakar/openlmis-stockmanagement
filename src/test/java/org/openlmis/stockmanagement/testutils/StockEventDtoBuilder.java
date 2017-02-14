package org.openlmis.stockmanagement.testutils;

import org.openlmis.stockmanagement.dto.StockEventDto;

import java.time.ZonedDateTime;
import java.util.UUID;

public class StockEventDtoBuilder {

  /**
   * Create stock event dto object for testing.
   *
   * @return created dto object.
   */
  public static StockEventDto createStockEventDto() {
    StockEventDto stockEventDto = new StockEventDto();

    stockEventDto.setSourceFreeText("a");
    stockEventDto.setDestinationFreeText("b");
    stockEventDto.setDocumentNumber("c");
    stockEventDto.setReasonFreeText("d");
    stockEventDto.setSignature("e");

    stockEventDto.setQuantity(1);
    stockEventDto.setReasonId(UUID.fromString("d3fc3cf3-da18-44b0-a220-77c985202e06"));

    stockEventDto.setSourceId(UUID.fromString("0bd28568-43f1-4836-934d-ec5fb11398e8"));
    stockEventDto.setDestinationId(UUID.fromString("087e81f6-a74d-4bba-9d01-16e0d64e9609"));

    stockEventDto.setProgramId(UUID.randomUUID());
    stockEventDto.setFacilityId(UUID.randomUUID());
    stockEventDto.setOrderableId(UUID.randomUUID());

    stockEventDto.setNoticedDate(ZonedDateTime.now());
    stockEventDto.setOccurredDate(ZonedDateTime.now());
    return stockEventDto;
  }

}
