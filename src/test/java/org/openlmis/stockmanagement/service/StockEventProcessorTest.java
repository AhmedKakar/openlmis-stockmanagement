package org.openlmis.stockmanagement.service;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.openlmis.stockmanagement.BaseTest;
import org.openlmis.stockmanagement.domain.card.StockCard;
import org.openlmis.stockmanagement.domain.event.StockEvent;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.dto.UserDto;
import org.openlmis.stockmanagement.repository.StockCardLineItemsRepository;
import org.openlmis.stockmanagement.repository.StockCardRepository;
import org.openlmis.stockmanagement.repository.StockEventsRepository;
import org.openlmis.stockmanagement.util.AuthenticationHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsIterableWithSize.iterableWithSize;
import static org.mockito.Mockito.when;
import static org.openlmis.stockmanagement.domain.BaseEntity.fromId;
import static org.openlmis.stockmanagement.testutils.StockEventDtoBuilder.createStockEventDto;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class StockEventProcessorTest extends BaseTest {

  @MockBean
  private AuthenticationHelper authenticationHelper;

  @MockBean
  private StockEventValidationsService stockEventValidationsService;

  @Autowired
  private StockEventProcessor stockEventProcessor;

  @Autowired
  private StockEventsRepository stockEventsRepository;

  @Autowired
  private StockCardRepository stockCardRepository;

  @Autowired
  private StockCardLineItemsRepository stockCardLineItemsRepository;

  @Before
  public void setUp() throws Exception {
    UUID userId = UUID.randomUUID();
    UserDto userDto = new UserDto();
    userDto.setId(userId);
    when(authenticationHelper.getCurrentUser()).thenReturn(userDto);
  }

  @After
  public void tearDown() throws Exception {
    stockCardLineItemsRepository.deleteAll();
    stockCardRepository.deleteAll();
    stockEventsRepository.deleteAll();
  }

  @Test
  public void should_not_save_events_if_anything_goes_wrong_in_validations_service()
          throws Exception {
    //given
    StockEventDto stockEventDto = createStockEventDto();

    Mockito.doThrow(new RuntimeException("something wrong from validations service"))
            .when(stockEventValidationsService)
            .validate(stockEventDto);

    //when
    try {
      stockEventProcessor.process(stockEventDto);
    } catch (RuntimeException ex) {
      //then
      assertEventAndCardAndLineItemTableSize(0);
      return;
    }

    Assert.fail();
  }

  @Test
  public void should_save_event_and_line_items_when_validation_service_passes() throws Exception {
    //when
    assertEventAndCardAndLineItemTableSize(0);

    StockEventDto stockEventDto = createStockEventDto();
    stockEventProcessor.process(stockEventDto);

    //then
    assertEventAndCardAndLineItemTableSize(1);
  }

  @Test
  public void should_assign_alternative_indentifier_for_dto_if_they_are_missing()
          throws Exception {
    //given
    //1. there is an existing event
    StockEventDto stockEventDto1 = createStockEventDto();
    UUID saveEventId = stockEventProcessor.process(stockEventDto1);
    StockCard card = stockCardRepository.findByOriginEvent(fromId(saveEventId, StockEvent.class));

    //2. later, another even with out program, facility and orderable
    StockEventDto stockEventDto = createStockEventDto();
    stockEventDto.setProgramId(null);
    stockEventDto.setFacilityId(null);
    stockEventDto.setOrderableId(null);
    stockEventDto.setStockCardId(card.getId());

    //when
    stockEventProcessor.process(stockEventDto);

    //then
    assertThat(stockEventDto.getProgramId(), is(card.getProgramId()));
    assertThat(stockEventDto.getFacilityId(), is(card.getFacilityId()));
    assertThat(stockEventDto.getOrderableId(), is(card.getOrderableId()));
  }

  private void assertEventAndCardAndLineItemTableSize(int size) {
    assertThat(stockEventsRepository.findAll(), iterableWithSize(size));
    assertThat(stockCardLineItemsRepository.findAll(), iterableWithSize(size));
    assertThat(stockCardRepository.findAll(), iterableWithSize(size));
  }
}
