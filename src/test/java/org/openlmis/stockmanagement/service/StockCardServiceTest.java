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

import static java.util.UUID.fromString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.openlmis.stockmanagement.testutils.StockEventDtoBuilder.createStockEventDto2;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.stockmanagement.BaseTest;
import org.openlmis.stockmanagement.domain.card.StockCard;
import org.openlmis.stockmanagement.domain.card.StockCardLineItem;
import org.openlmis.stockmanagement.domain.event.StockEvent2;
import org.openlmis.stockmanagement.dto.FacilityDto;
import org.openlmis.stockmanagement.dto.OrderableDto;
import org.openlmis.stockmanagement.dto.ProgramDto;
import org.openlmis.stockmanagement.dto.StockCardDto;
import org.openlmis.stockmanagement.dto.StockCardLineItemDto;
import org.openlmis.stockmanagement.dto.StockEventDto2;
import org.openlmis.stockmanagement.exception.PermissionMessageException;
import org.openlmis.stockmanagement.repository.StockCardRepository;
import org.openlmis.stockmanagement.repository.StockEventsRepository2;
import org.openlmis.stockmanagement.service.referencedata.FacilityReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.OrderableReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.ProgramReferenceDataService;
import org.openlmis.stockmanagement.testutils.StockEventDtoBuilder;
import org.openlmis.stockmanagement.utils.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.UUID;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class StockCardServiceTest extends BaseTest {

  @Autowired
  private StockCardService2 stockCardService;

  @Autowired
  private StockEventsRepository2 stockEventsRepository;

  @Autowired
  private StockCardRepository stockCardRepository;

  @MockBean
  private FacilityReferenceDataService facilityReferenceDataService;

  @MockBean
  private ProgramReferenceDataService programReferenceDataService;

  @MockBean
  private OrderableReferenceDataService orderableReferenceDataService;

  @MockBean
  private PermissionService permissionService;

  @After
  public void tearDown() throws Exception {
    stockCardRepository.deleteAll();
    stockEventsRepository.deleteAll();
  }

  @Test
  public void should_save_stock_card_line_items_and_create_stock_card_for_first_movement()
      throws Exception {
    //given
    UUID userId = UUID.randomUUID();
    StockEventDto2 stockEventDto = createStockEventDto2();
    StockEvent2 savedEvent = save(stockEventDto, userId);

    //when
    stockCardService.saveFromEvent(stockEventDto, savedEvent.getId(), userId);

    //then
    StockCard savedCard = stockCardRepository.findByOriginEvent2(savedEvent);
    StockCardLineItem firstLineItem = savedCard.getLineItems().get(0);

    assertThat(firstLineItem.getUserId(), is(userId));
    assertThat(firstLineItem.getSource().isRefDataFacility(), is(true));
    assertThat(firstLineItem.getDestination().isRefDataFacility(), is(false));

    assertThat(firstLineItem.getStockCard().getOriginEvent2().getId(), is(savedEvent.getId()));
    assertThat(firstLineItem.getStockCard().getFacilityId(), is(savedEvent.getFacilityId()));
    assertThat(firstLineItem.getStockCard().getProgramId(), is(savedEvent.getProgramId()));
    UUID orderableId = savedEvent.getLineItems().get(0).getOrderableId();
    assertThat(firstLineItem.getStockCard().getOrderableId(), is(orderableId));
  }

  @Test
  public void should_save_line_items_with_program_facility_orderable_for_non_first_movement()
      throws Exception {
    //given
    //1. there is an existing event that caused a stock card to exist
    StockEventDto2 existingEventDto = createStockEventDto2();
    final StockEvent2 existingEvent = save(existingEventDto, UUID.randomUUID());
    UUID orderableId = existingEventDto.getLineItems().get(0).getOrderableId();

    //2. and there is a new event coming
    StockEventDto2 newEventDto = createStockEventDto2();
    newEventDto.setProgramId(existingEventDto.getProgramId());
    newEventDto.setFacilityId(existingEventDto.getFacilityId());
    newEventDto.getLineItems().get(0).setOrderableId(orderableId);

    //when
    long cardAmountBeforeSave = stockCardRepository.count();
    UUID userId = UUID.randomUUID();
    StockEvent2 savedNewEvent = save(newEventDto, userId);
    long cardAmountAfterSave = stockCardRepository.count();

    //then
    StockCard savedCard = stockCardRepository.findByOriginEvent2(existingEvent);
    List<StockCardLineItem> lineItems = savedCard.getLineItems();
    StockCardLineItem latestLineItem = lineItems.get(lineItems.size() - 1);

    assertThat(cardAmountAfterSave, is(cardAmountBeforeSave));
    assertThat(latestLineItem.getOriginEvent2().getId(), is(savedNewEvent.getId()));
    assertThat(latestLineItem.getStockCard().getId(), is(savedCard.getId()));
    assertThat(latestLineItem.getUserId(), is(userId));
  }

  @Test
  public void should_get_refdata_and_convert_organizations_when_find_stock_card()
      throws Exception {
    //given
    UUID userId = UUID.randomUUID();
    StockEventDto2 stockEventDto = createStockEventDto2();

    //1. mock ref data service
    FacilityDto cardFacility = new FacilityDto();
    FacilityDto sourceFacility = new FacilityDto();
    ProgramDto programDto = new ProgramDto();
    OrderableDto orderableDto = new OrderableDto();

    when(facilityReferenceDataService.findOne(stockEventDto.getFacilityId()))
        .thenReturn(cardFacility);
    when(facilityReferenceDataService.findOne(fromString("e6799d64-d10d-4011-b8c2-0e4d4a3f65ce")))
        .thenReturn(sourceFacility);

    when(programReferenceDataService.findOne(stockEventDto.getProgramId()))
        .thenReturn(programDto);
    when(orderableReferenceDataService
        .findOne(stockEventDto.getLineItems().get(0).getOrderableId()))
        .thenReturn(orderableDto);

    //2. there is an existing stock card with line items
    StockEvent2 savedEvent = save(stockEventDto, userId);

    //when
    StockCard savedCard = stockCardRepository.findByOriginEvent2(savedEvent);
    StockCardDto foundCardDto = stockCardService.findStockCardById(savedCard.getId());

    //then
    assertThat(foundCardDto.getFacility(), is(cardFacility));
    assertThat(foundCardDto.getProgram(), is(programDto));
    assertThat(foundCardDto.getOrderable(), is(orderableDto));

    StockCardLineItemDto lineItemDto = foundCardDto.getLineItems().get(0);
    assertThat(lineItemDto.getSource(), is(sourceFacility));
    assertThat(lineItemDto.getDestination().getName(), is("NGO"));
  }

  @Test
  public void should_get_stock_card_with_calculated_soh_when_find_stock_card() throws Exception {
    //given
    StockEventDto2 stockEventDto = StockEventDtoBuilder.createStockEventDto2();
    stockEventDto.setSourceId(null);
    stockEventDto.setDestinationId(null);
    StockEvent2 savedEvent = save(stockEventDto, UUID.randomUUID());

    //when
    UUID cardId = stockCardRepository.findByOriginEvent2(savedEvent).getId();
    StockCardDto card = stockCardService.findStockCardById(cardId);

    //then
    assertThat(card.getStockOnHand(), is(stockEventDto.getLineItems().get(0).getQuantity()));
  }

  @Test
  public void should_return_null_when_can_not_find_stock_card_by_id() throws Exception {
    //when
    UUID nonExistingCardId = UUID.randomUUID();
    StockCardDto cardDto = stockCardService.findStockCardById(nonExistingCardId);

    //then
    assertNull(cardDto);
  }

  @Test(expected = PermissionMessageException.class)
  public void should_throw_permission_exception_if_user_has_no_permission_to_view_card()
      throws Exception {
    //given
    StockEvent2 savedEvent = save(createStockEventDto2(), UUID.randomUUID());
    doThrow(new PermissionMessageException(new Message("some error")))
        .when(permissionService)
        .canViewStockCard(savedEvent.getProgramId(), savedEvent.getFacilityId());

    //when
    UUID savedCardId = stockCardRepository.findByOriginEvent2(savedEvent).getId();
    stockCardService.findStockCardById(savedCardId);
  }

  private StockEvent2 save(StockEventDto2 eventDto, UUID userId)
      throws InstantiationException, IllegalAccessException {
    StockEvent2 savedEvent = stockEventsRepository
        .save(eventDto.toEvent(UUID.randomUUID()));
    stockCardService.saveFromEvent(eventDto, savedEvent.getId(), userId);
    return savedEvent;
  }

  private UUID getEventIdOfNthLineItem(StockCardDto card, int nth) {
    return card.getLineItems().get(nth - 1).getLineItem().getOriginEvent().getId();
  }
}