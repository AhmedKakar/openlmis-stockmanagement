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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.stockmanagement.dto.FacilityDto;
import org.openlmis.stockmanagement.dto.FacilityTypeDto;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.repository.ValidReasonAssignmentRepository;
import org.openlmis.stockmanagement.service.referencedata.FacilityReferenceDataService;
import org.openlmis.stockmanagement.testutils.StockEventDtoBuilder;

import java.util.ArrayList;
import java.util.UUID;

import static org.junit.rules.ExpectedException.none;
import static org.mockito.Mockito.when;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_REASON_NOT_IN_VALID_LIST;

@RunWith(MockitoJUnitRunner.class)
public class ReasonAssignmentValidatorTest {
  @Rule
  public ExpectedException expectedEx = none();

  @Mock
  private ValidReasonAssignmentRepository validReasonAssignmentRepository;

  @Mock
  private FacilityReferenceDataService facilityReferenceDataService;

  @InjectMocks
  private ReasonAssignmentValidator reasonAssignmentValidator;

  @Test
  public void should_not_throw_error_if_event_has_no_reason_id() throws Exception {
    //given
    StockEventDto stockEventDto = StockEventDtoBuilder.createStockEventDto();
    stockEventDto.setReasonId(null);

    //when
    reasonAssignmentValidator.validate(stockEventDto);

    //then: no exception
  }

  @Test
  public void should_throw_error_if_event_reason_id_not_found_in_assignment_list()
          throws Exception {
    //expect
    expectedEx.expect(ValidationMessageException.class);
    expectedEx.expectMessage(ERROR_EVENT_REASON_NOT_IN_VALID_LIST);

    //given
    StockEventDto stockEventDto = StockEventDtoBuilder.createStockEventDto();
    stockEventDto.setReasonId(UUID.randomUUID());

    FacilityTypeDto facilityTypeDto = new FacilityTypeDto();
    facilityTypeDto.setId(UUID.randomUUID());

    FacilityDto facilityDto = new FacilityDto();
    facilityDto.setType(facilityTypeDto);

    UUID programId = stockEventDto.getProgramId();
    UUID facilityTypeId = facilityTypeDto.getId();

    when(facilityReferenceDataService.findOne(stockEventDto.getFacilityId()))
            .thenReturn(facilityDto);
    when(validReasonAssignmentRepository
            .findByProgramIdAndFacilityTypeId(programId, facilityTypeId))
            .thenReturn(new ArrayList<>());

    //when
    reasonAssignmentValidator.validate(stockEventDto);
  }

  @Test
  public void should_not_throw_error_if_event_has_facility_id_not_in_ref_data()
          throws Exception {
    //given
    StockEventDto stockEventDto = StockEventDtoBuilder.createStockEventDto();
    when(facilityReferenceDataService.findOne(stockEventDto.getFacilityId())).thenReturn(null);

    //when
    reasonAssignmentValidator.validate(stockEventDto);

    //then: no error
  }

  @Test
  public void should_not_throw_error_if_event_has_no_program_id()
          throws Exception {
    //given
    StockEventDto stockEventDto = StockEventDtoBuilder.createStockEventDto();
    stockEventDto.setProgramId(null);

    //when
    reasonAssignmentValidator.validate(stockEventDto);

    //then: no error
  }
}