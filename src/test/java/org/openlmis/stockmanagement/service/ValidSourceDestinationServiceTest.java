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
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.stockmanagement.domain.movement.Node;
import org.openlmis.stockmanagement.domain.movement.Organization;
import org.openlmis.stockmanagement.domain.movement.ValidDestinationAssignment;
import org.openlmis.stockmanagement.dto.FacilityTypeDto;
import org.openlmis.stockmanagement.dto.ProgramDto;
import org.openlmis.stockmanagement.dto.ValidDestinationAssignmentDto;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.repository.OrganizationRepository;
import org.openlmis.stockmanagement.repository.ValidDestinationAssignmentRepository;
import org.openlmis.stockmanagement.service.referencedata.FacilityTypeReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.ProgramReferenceDataService;

import java.util.List;
import java.util.UUID;

@RunWith(MockitoJUnitRunner.class)
public class ValidSourceDestinationServiceTest {

  @InjectMocks
  private ValidSourceDestinationService validSourceDestinationService;

  @Mock
  private ProgramReferenceDataService programRefDataService;

  @Mock
  private FacilityTypeReferenceDataService facilityTypeRefDataService;

  @Mock
  private ValidDestinationAssignmentRepository validDestinationAssignmentRepository;

  @Mock
  private OrganizationRepository organizationRepository;

  @Test(expected = ValidationMessageException.class)
  public void should_throw_validation_exception_when_program_and_facilityType_not_found()
      throws Exception {
    //given
    UUID programId = UUID.randomUUID();
    UUID facilityTypeId = UUID.randomUUID();
    when(programRefDataService.findOne(programId)).thenReturn(null);
    when(facilityTypeRefDataService.findOne(facilityTypeId)).thenReturn(null);

    //when
    validSourceDestinationService.findValidDestinations(programId, facilityTypeId);
  }

  @Test
  public void should_return_empty_list_when_valid_destination_assignment_not_found()
      throws Exception {
    //given
    UUID programId = UUID.randomUUID();
    UUID facilityTypeId = UUID.randomUUID();
    when(programRefDataService.findOne(programId)).thenReturn(new ProgramDto());
    when(facilityTypeRefDataService.findOne(facilityTypeId)).thenReturn(new FacilityTypeDto());

    //when
    List<ValidDestinationAssignmentDto> validDestinations =
        validSourceDestinationService.findValidDestinations(programId, facilityTypeId);

    //then
    assertThat(validDestinations.isEmpty(), is(true));
  }

  @Test
  public void should_return_list_of_destination_dtos_when_find_valid_destination_assignment()
      throws Exception {
    //given
    UUID programId = UUID.fromString("dce17f2e-af3e-40ad-8e00-3496adef44c3");
    UUID facilityTypeId = UUID.fromString("ac1d268b-ce10-455f-bf87-9c667da8f060");
    when(programRefDataService.findOne(programId)).thenReturn(new ProgramDto());
    when(facilityTypeRefDataService.findOne(facilityTypeId)).thenReturn(new FacilityTypeDto());

    mockValidDestinationAssignment(programId, facilityTypeId);

    //when
    List<ValidDestinationAssignmentDto> validDestinations =
        validSourceDestinationService.findValidDestinations(programId, facilityTypeId);

    //then
    assertThat(validDestinations.size(), is(1));
    assertThat(validDestinations.get(0).getName(), is("CHW"));
  }

  private void mockValidDestinationAssignment(UUID programId, UUID facilityTypeId) {
    Organization organization = new Organization();
    organization.setName("CHW");
    organization.setId(UUID.randomUUID());

    Node node = new Node();
    node.setRefDataFacility(false);
    node.setId(UUID.randomUUID());
    node.setReferenceId(organization.getId());

    ValidDestinationAssignment destination = new ValidDestinationAssignment();
    destination.setNode(node);

    when(organizationRepository.findOne(node.getReferenceId())).thenReturn(organization);
    when(validDestinationAssignmentRepository
        .findByProgramIdAndFacilityTypeId(programId, facilityTypeId))
        .thenReturn(singletonList(destination));
  }
}