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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.openlmis.stockmanagement.testutils.StockCardLineItemReasonBuilder.createReason;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.stockmanagement.domain.adjustment.StockCardLineItemReason;
import org.openlmis.stockmanagement.exception.PermissionMessageException;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.repository.StockCardLineItemReasonRepository;
import org.openlmis.stockmanagement.utils.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.UUID;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class StockCardLineItemReasonServiceTest {
  @Autowired
  private StockCardLineItemReasonService reasonService;

  @Autowired
  private StockCardLineItemReasonRepository reasonRepository;

  @MockBean
  private PermissionService permissionService;

  @After
  public void tearDown() throws Exception {
    reasonRepository.deleteAll();
  }

  @Test(expected = ValidationMessageException.class)
  public void should_throw_validation_exception_with_unavailable_reason_dto() throws Exception {
    //given
    StockCardLineItemReason reason = new StockCardLineItemReason();

    //when
    reasonService.saveOrUpdate(reason);
  }

  @Test(expected = ValidationMessageException.class)
  public void should_throw_validation_exception_when_reason_id_not_found_in_db() throws Exception {
    //given
    reasonService.checkUpdateReasonIdExists(UUID.randomUUID());
  }

  @Test(expected = PermissionMessageException.class)
  public void should_throw_exception_when_user_has_no_permission_to_create_or_update_reason()
      throws Exception {
    doThrow(new PermissionMessageException(new Message("key")))
        .when(permissionService).canManageReasons();
    reasonService.saveOrUpdate(createReason());
  }

  @Test(expected = PermissionMessageException.class)
  public void should_throw_exception_when_user_has_no_permission_to_get_reasons() throws Exception {
    doThrow(new PermissionMessageException(new Message("key")))
        .when(permissionService).canManageReasons();
    reasonService.findReasons();
  }

  @Test
  public void should_get_all_reasons_when_pass_validation() throws Exception {
    //given
    reasonService.saveOrUpdate(createReason("test reason 1"));
    reasonService.saveOrUpdate(createReason("test reason 2"));
    reasonService.saveOrUpdate(createReason("test reason 3"));

    //when
    List<StockCardLineItemReason> reasons = reasonService.findReasons();

    //then
    assertThat(reasons.size(), is(3));
    assertThat(reasons.get(0).getName(), is("test reason 1"));
    assertThat(reasons.get(1).getName(), is("test reason 2"));
    assertThat(reasons.get(2).getName(), is("test reason 3"));
  }

  @Test
  public void should_save_reason_when_pass_null_value_validation() throws Exception {
    assertThat(reasonRepository.count(), is(0L));

    //when
    reasonService.saveOrUpdate(createReason());

    //then
    assertThat(reasonRepository.count(), is(1L));
  }

  @Test
  public void should_update_existing_reason() throws Exception {
    //given: there is an existing reason
    reasonService.saveOrUpdate(createReason());
    assertThat(reasonRepository.count(), is(1L));
    UUID savedReasonId = reasonRepository.findAll().iterator().next().getId();

    //when:
    StockCardLineItemReason newReason = createReason();
    newReason.setId(savedReasonId);
    newReason.setIsFreeTextAllowed(false);
    StockCardLineItemReason updatedReason = reasonService.saveOrUpdate(newReason);

    //then
    assertThat(updatedReason.getIsFreeTextAllowed(), is(newReason.getIsFreeTextAllowed()));
    assertThat(reasonRepository.count(), is(1L));
  }

}