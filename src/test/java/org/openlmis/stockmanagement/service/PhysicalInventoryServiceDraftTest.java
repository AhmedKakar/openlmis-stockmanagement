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

import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.stockmanagement.dto.ApprovedProductDto;
import org.openlmis.stockmanagement.dto.OrderableDto;
import org.openlmis.stockmanagement.dto.PhysicalInventoryDto;
import org.openlmis.stockmanagement.dto.PhysicalInventoryLineItemDto;
import org.openlmis.stockmanagement.dto.ProgramOrderableDto;
import org.openlmis.stockmanagement.repository.PhysicalInventoriesRepository;
import org.openlmis.stockmanagement.service.referencedata.ApprovedProductReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.OrderableReferenceDataService;

import java.util.UUID;

@RunWith(MockitoJUnitRunner.class)
public class PhysicalInventoryServiceDraftTest {
  @InjectMocks
  private PhysicalInventoryService physicalInventoryService;

  @Mock
  private PhysicalInventoriesRepository physicalInventoriesRepository;

  @Mock
  private ApprovedProductReferenceDataService approvedProductReferenceDataService;

  @Mock
  private OrderableReferenceDataService orderableReferenceDataService;

  @Test
  public void should_generate_empty_draft_if_no_saved_draft_is_found() throws Exception {
    //given
    UUID programId = UUID.randomUUID();
    UUID facilityId = UUID.randomUUID();

    when(physicalInventoriesRepository
        .findByProgramIdAndFacilityIdAndIsDraft(programId, facilityId, true))
        .thenReturn(null);

    UUID orderableId = UUID.randomUUID();
    ProgramOrderableDto programOrderable = new ProgramOrderableDto();
    programOrderable.setOrderableId(orderableId);
    ApprovedProductDto approvedProductDto = new ApprovedProductDto();
    approvedProductDto.setProgramOrderable(programOrderable);

    when(approvedProductReferenceDataService.getAllApprovedProducts(programId, facilityId))
        .thenReturn(singletonList(approvedProductDto));

    OrderableDto orderableDto = new OrderableDto();
    when(orderableReferenceDataService.findOne(orderableId)).thenReturn(orderableDto);

    //when
    PhysicalInventoryDto inventory = physicalInventoryService.findDraft(programId, facilityId);

    //then
    assertThat(inventory.getProgramId(), is(programId));
    assertThat(inventory.getFacilityId(), is(facilityId));
    assertThat(inventory.getLineItems().size(), is(1));

    PhysicalInventoryLineItemDto inventoryLineItemDto = inventory.getLineItems().get(0);
    assertThat(inventoryLineItemDto.getOrderable(), is(orderableDto));
    assertThat(inventoryLineItemDto.getQuantity(), nullValue());
  }
}