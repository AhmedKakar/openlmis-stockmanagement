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

package org.openlmis.stockmanagement.web;

import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.openlmis.stockmanagement.domain.adjustment.ValidReasonAssignment;
import org.openlmis.stockmanagement.exception.PermissionMessageException;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.repository.StockCardLineItemReasonRepository;
import org.openlmis.stockmanagement.repository.ValidReasonAssignmentRepository;
import org.openlmis.stockmanagement.service.PermissionService;
import org.openlmis.stockmanagement.service.referencedata.ProgramFacilityTypeExistenceService;
import org.openlmis.stockmanagement.utils.Message;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Arrays;
import java.util.UUID;

public class ValidReasonAssignmentControllerTest extends BaseWebTest {
  private static final String VALID_REASON_API = "/api/validReasons";
  private static final String PROGRAM = "program";
  private static final String FACILITY_TYPE = "facilityType";

  @MockBean
  private ValidReasonAssignmentRepository reasonAssignmentRepository;

  @MockBean
  private ProgramFacilityTypeExistenceService programFacilityTypeExistenceService;

  @MockBean
  private PermissionService permissionService;

  @MockBean
  private StockCardLineItemReasonRepository reasonRepository;

  @Test
  public void get_valid_reason_by_program_and_facility_type() throws Exception {
    //given
    UUID programId = UUID.randomUUID();
    UUID facilityTypeId = UUID.randomUUID();

    when(reasonAssignmentRepository.findByProgramIdAndFacilityTypeId(programId, facilityTypeId))
        .thenReturn(Arrays.asList(new ValidReasonAssignment()));

    //when
    ResultActions resultActions = mvc.perform(
        get(VALID_REASON_API)
            .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE)
            .param(PROGRAM, programId.toString())
            .param(FACILITY_TYPE, facilityTypeId.toString()));

    //then
    resultActions
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)));
  }

  @Test
  public void should_assign_reason_to_program_facility_type() throws Exception {
    //given
    UUID programId = UUID.randomUUID();
    UUID facilityTypeId = UUID.randomUUID();
    UUID reasonId = UUID.randomUUID();
    when(reasonRepository.exists(reasonId)).thenReturn(true);

    //when
    ResultActions resultActions = mvc.perform(post(VALID_REASON_API)
        .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE)
        .param(PROGRAM, programId.toString())
        .param(FACILITY_TYPE, facilityTypeId.toString())
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectToJsonString(reasonId.toString())));

    //then
    ArgumentCaptor<ValidReasonAssignment> assignmentCaptor = forClass(ValidReasonAssignment.class);

    resultActions
        .andDo(print())
        .andExpect(status().isCreated());
    verify(reasonAssignmentRepository, times(1)).save(assignmentCaptor.capture());
    assertThat(assignmentCaptor.getValue().getReason().getId(), is(reasonId));
    verify(programFacilityTypeExistenceService, times(1))
        .checkProgramAndFacilityTypeExist(programId, facilityTypeId);
    verify(permissionService, times(1)).canManageReasons();
  }

  @Test
  public void should_not_assign_same_reason_twice() throws Exception {
    //given
    UUID programId = UUID.randomUUID();
    UUID facilityTypeId = UUID.randomUUID();
    UUID reasonId = UUID.randomUUID();
    when(reasonRepository.exists(reasonId)).thenReturn(true);

    when(reasonAssignmentRepository
        .findByProgramIdAndFacilityTypeIdAndReasonId(programId, facilityTypeId, reasonId))
        .thenReturn(new ValidReasonAssignment());

    //when
    ResultActions resultActions = mvc.perform(post(VALID_REASON_API)
        .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE)
        .param(PROGRAM, programId.toString())
        .param(FACILITY_TYPE, facilityTypeId.toString())
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectToJsonString(reasonId.toString())));

    //then
    resultActions.andExpect(status().isOk());
    verify(reasonAssignmentRepository, never()).save(any(ValidReasonAssignment.class));
  }

  @Test
  public void should_return_400_if_reason_id_is_null() throws Exception {
    //given
    UUID programId = UUID.randomUUID();
    UUID facilityTypeId = UUID.randomUUID();

    //when
    ResultActions resultActions = mvc.perform(post(VALID_REASON_API)
        .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE)
        .param(PROGRAM, programId.toString())
        .param(FACILITY_TYPE, facilityTypeId.toString())
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectToJsonString(null)));

    //then
    resultActions.andExpect(status().isBadRequest());
    verify(reasonAssignmentRepository, never()).save(any(ValidReasonAssignment.class));
  }

  @Test
  public void should_return_400_if_reason_not_exist() throws Exception {
    //given
    UUID programId = UUID.randomUUID();
    UUID facilityTypeId = UUID.randomUUID();
    UUID reasonId = UUID.randomUUID();
    when(reasonRepository.findOne(reasonId)).thenReturn(null);

    //when
    ResultActions resultActions = mvc.perform(post(VALID_REASON_API)
        .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE)
        .param(PROGRAM, programId.toString())
        .param(FACILITY_TYPE, facilityTypeId.toString())
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectToJsonString(reasonId)));

    //then
    resultActions.andExpect(status().isBadRequest());
    verify(reasonAssignmentRepository, never()).save(any(ValidReasonAssignment.class));
  }

  @Test
  public void return_400_when_program_not_found() throws Exception {
    //given
    //not exist in demo data
    UUID programId = randomUUID();
    UUID facilityTypeId = UUID.fromString("ac1d268b-ce10-455f-bf87-9c667da8f060");

    doThrow(new ValidationMessageException("errorKey")).when(programFacilityTypeExistenceService)
        .checkProgramAndFacilityTypeExist(programId, facilityTypeId);

    //when
    ResultActions resultActions = mvc.perform(
        get(VALID_REASON_API)
            .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE)
            .param(PROGRAM, programId.toString())
            .param(FACILITY_TYPE, facilityTypeId.toString()));

    //then
    resultActions.andExpect(status().isBadRequest());
  }

  @Test
  public void return_400_when_facility_type_not_found() throws Exception {
    //given
    //not exist in demo data
    UUID facilityTypeId = randomUUID();
    UUID programId = UUID.fromString("dce17f2e-af3e-40ad-8e00-3496adef44c3");
    doThrow(new ValidationMessageException("errorKey")).when(programFacilityTypeExistenceService)
        .checkProgramAndFacilityTypeExist(programId, facilityTypeId);

    //when
    ResultActions resultActions = mvc.perform(
        get(VALID_REASON_API)
            .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE)
            .param(PROGRAM, programId.toString())
            .param(FACILITY_TYPE, facilityTypeId.toString()));

    //then
    resultActions.andExpect(status().isBadRequest());
  }

  @Test
  public void return_403_when_current_user_has_no_permission() throws Exception {
    //given
    //not exist in demo data
    UUID facilityTypeId = randomUUID();
    UUID programId = UUID.fromString("dce17f2e-af3e-40ad-8e00-3496adef44c3");
    doThrow(new PermissionMessageException(new Message("key"))).when(permissionService)
        .canViewReasons(programId, facilityTypeId);

    //when
    ResultActions resultActions = mvc.perform(
        get(VALID_REASON_API)
            .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE)
            .param(PROGRAM, programId.toString())
            .param(FACILITY_TYPE, facilityTypeId.toString()));

    //then
    resultActions.andExpect(status().isForbidden());
  }
}