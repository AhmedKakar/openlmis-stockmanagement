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

import static java.util.Collections.singletonList;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.stockmanagement.dto.ApprovedProductDto;
import org.openlmis.stockmanagement.dto.ProgramOrderableDto;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.testutils.StockEventDtoBuilder;
import org.openlmis.stockmanagement.util.StockEventProcessContext;

import java.util.UUID;

@RunWith(MockitoJUnitRunner.class)
public class ApprovedOrderableValidatorTest {

  @InjectMocks
  private ApprovedOrderableValidator approvedOrderableValidator;

  @Test(expected = ValidationMessageException.class)
  public void stock_event_with_orderable_id_not_in_approved_list_should_not_pass_validation()
      throws Exception {
    //given:
    StockEventDto stockEventDto = StockEventDtoBuilder.createStockEventDto();

    ProgramOrderableDto programOrderableDto = new ProgramOrderableDto();
    programOrderableDto.setOrderableId(UUID.randomUUID());

    ApprovedProductDto approvedProductDto = new ApprovedProductDto();
    approvedProductDto.setProgramOrderable(programOrderableDto);

    stockEventDto.setContext(StockEventProcessContext.builder()
        .allApprovedProducts(singletonList(approvedProductDto)).build());

    //when:
    approvedOrderableValidator.validate(stockEventDto);
  }

  @Test
  public void stock_event_with_orderable_id_in_approved_list_should_pass()
      throws Exception {
    //given:
    String orderableIdString = "d8290082-f9fa-4a37-aefb-a3d76ff805a8";
    UUID orderableId = UUID.fromString(orderableIdString);

    StockEventDto stockEventDto = StockEventDtoBuilder.createStockEventDto();
    stockEventDto.setOrderableId(orderableId);

    ProgramOrderableDto programOrderableDto = new ProgramOrderableDto();
    programOrderableDto.setOrderableId(UUID.fromString(orderableIdString));

    ApprovedProductDto approvedProductDto = new ApprovedProductDto();
    approvedProductDto.setProgramOrderable(programOrderableDto);

    stockEventDto.setContext(StockEventProcessContext.builder()
        .allApprovedProducts(singletonList(approvedProductDto)).build());

    //when:
    approvedOrderableValidator.validate(stockEventDto);
  }

  @Test
  public void should_not_throw_validation_exception_if_event_has_no_program_and_facility_id()
      throws Exception {
    //given
    StockEventDto stockEventDto = StockEventDtoBuilder.createStockEventDto();
    stockEventDto.setProgramId(null);

    //when
    approvedOrderableValidator.validate(stockEventDto);

    //given
    stockEventDto = StockEventDtoBuilder.createStockEventDto();
    stockEventDto.setFacilityId(null);

    //when
    approvedOrderableValidator.validate(stockEventDto);
  }

}