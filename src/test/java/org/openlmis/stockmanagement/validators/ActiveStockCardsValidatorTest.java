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

import static org.mockito.Mockito.when;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_PHYSICAL_INVENTORY_NOT_INCLUDE_ACTIVE_STOCK_CARD;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.repository.StockCardRepository;
import org.openlmis.stockmanagement.testutils.StockEventDtoBuilder;

import java.util.Arrays;
import java.util.UUID;

@RunWith(MockitoJUnitRunner.class)
public class ActiveStockCardsValidatorTest {

  @Rule
  public ExpectedException expectedEx = ExpectedException.none();

  @Mock
  private StockCardRepository stockCardRepository;

  @InjectMocks
  private ActiveStockCardsValidator activeStockCardsValidator;

  @Test
  public void should_throw_exception_if_existing_card_not_covered() throws Exception {
    expectedEx.expectMessage(ERROR_PHYSICAL_INVENTORY_NOT_INCLUDE_ACTIVE_STOCK_CARD);

    //given
    StockEventDto stockEventDto = StockEventDtoBuilder.createStockEventDto();
    stockEventDto.getLineItems().get(0).setReasonId(null);
    stockEventDto.setSourceId(null);
    stockEventDto.setDestinationId(null);

    when(stockCardRepository
        .getStockCardOrderableIdsBy(stockEventDto.getProgramId(), stockEventDto.getFacilityId()))
        .thenReturn(Arrays.asList(UUID.randomUUID()));

    //when
    activeStockCardsValidator.validate(stockEventDto);
  }
}