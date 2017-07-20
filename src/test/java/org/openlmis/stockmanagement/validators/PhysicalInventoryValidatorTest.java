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

import static org.junit.rules.ExpectedException.none;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_ADJUSTMENT_QUANITITY_INVALID;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_PHYSICAL_INVENTORY_STOCK_ADJUSTMENTS_NOT_PROVIDED;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_PHYSICAL_INVENTORY_STOCK_ON_HAND_CURRENT_STOCK_DIFFER;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.stockmanagement.domain.physicalinventory.StockAdjustment;
import org.openlmis.stockmanagement.domain.reason.StockCardLineItemReason;
import org.openlmis.stockmanagement.dto.PhysicalInventoryDto;
import org.openlmis.stockmanagement.dto.PhysicalInventoryLineItemDto;
import org.openlmis.stockmanagement.dto.referencedata.OrderableDto;
import org.openlmis.stockmanagement.exception.ValidationMessageException;

import java.util.ArrayList;
import java.util.Collections;

@RunWith(MockitoJUnitRunner.class)
public class PhysicalInventoryValidatorTest {

  @Rule
  public ExpectedException expectedException = none();

  @Mock
  private VvmValidator vvmValidator;

  @InjectMocks
  private PhysicalInventoryValidator validator;

  @Test(expected = ValidationMessageException.class)
  public void shouldRejectWhenNoLineItemsPresent()
      throws InstantiationException, IllegalAccessException {
    // given
    PhysicalInventoryDto inventory = new PhysicalInventoryDto();
    inventory.setLineItems(Collections.emptyList());

    doNothing()
        .when(vvmValidator).validate(eq(inventory.getLineItems()), anyString());

    // when
    validator.validate(inventory);

    // then
    verify(vvmValidator, atLeastOnce()).validate(eq(inventory.getLineItems()), anyString());
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldRejectWhenLineItemHasNoOrderable()
      throws InstantiationException, IllegalAccessException {
    // given
    PhysicalInventoryDto inventory = new PhysicalInventoryDto();
    inventory.setLineItems(Collections.singletonList(new PhysicalInventoryLineItemDto()));

    doNothing()
        .when(vvmValidator).validate(eq(inventory.getLineItems()), anyString());

    // when
    validator.validate(inventory);

    // then
    verify(vvmValidator, atLeastOnce()).validate(eq(inventory.getLineItems()), anyString());
  }

  @Test
  public void shouldCallVvmValidator() throws InstantiationException, IllegalAccessException {
    // given
    PhysicalInventoryDto inventory = new PhysicalInventoryDto();
    inventory.setLineItems(Collections.singletonList(generateLineItem(5, 5)));

    doNothing()
        .when(vvmValidator).validate(any(), anyString());

    // when
    validator.validate(inventory);

    // then
    verify(vvmValidator, atLeastOnce()).validate(eq(inventory.getLineItems()), anyString());
  }

  @Test
  public void shouldRejectWhenAnyAdjustmentHasNegativeQuantity() throws Exception {
    //expect
    expectedException.expect(ValidationMessageException.class);
    expectedException.expectMessage(ERROR_EVENT_ADJUSTMENT_QUANITITY_INVALID);

    //given
    StockAdjustment adjustment = StockAdjustment
        .builder()
        .reason(StockCardLineItemReason.physicalCredit())
        .quantity(-10)
        .build();

    PhysicalInventoryLineItemDto invalidItem = generateLineItem(10, 5);
    invalidItem.setStockAdjustments(Collections.singletonList(adjustment));

    PhysicalInventoryDto inventory = new PhysicalInventoryDto();
    inventory.setLineItems(Collections.singletonList(invalidItem));

    //when
    validator.validate(inventory);
  }

  @Test
  public void shouldRejectWhenStockOnHandDoesNotMatchQuantityAndNoAdjustmentsProvided()
      throws Exception {
    //expect
    expectedException.expect(ValidationMessageException.class);
    expectedException.expectMessage(ERROR_PHYSICAL_INVENTORY_STOCK_ADJUSTMENTS_NOT_PROVIDED);

    //given
    PhysicalInventoryLineItemDto invalidItem = generateLineItem(10, 5);

    PhysicalInventoryDto inventory = new PhysicalInventoryDto();
    inventory.setLineItems(Collections.singletonList(invalidItem));

    //when
    validator.validate(inventory);
  }

  @Test
  public void shouldRejectWhenStockOnHandWithAdjustmentsDoesNotMatchQuantity() throws Exception {
    //expect
    expectedException.expect(ValidationMessageException.class);
    expectedException.expectMessage(ERROR_PHYSICAL_INVENTORY_STOCK_ON_HAND_CURRENT_STOCK_DIFFER);

    //given
    StockAdjustment adjustment = StockAdjustment
        .builder()
        .reason(StockCardLineItemReason.physicalCredit())
        .quantity(30)
        .build();

    PhysicalInventoryLineItemDto invalidItem = generateLineItem(10, 5);
    invalidItem.setStockAdjustments(Collections.singletonList(adjustment));

    PhysicalInventoryDto inventory = new PhysicalInventoryDto();
    inventory.setLineItems(Collections.singletonList(invalidItem));

    //when
    validator.validate(inventory);
  }

  private PhysicalInventoryLineItemDto generateLineItem(int soh, int quantity) {
    return PhysicalInventoryLineItemDto
        .builder()
        .orderable(new OrderableDto())
        .stockOnHand(soh)
        .quantity(quantity)
        .stockAdjustments(new ArrayList<>())
        .build();
  }
}
