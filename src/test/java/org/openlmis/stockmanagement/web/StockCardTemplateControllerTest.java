package org.openlmis.stockmanagement.web;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.stockmanagement.domain.template.StockCardOptionalFields;
import org.openlmis.stockmanagement.domain.template.StockCardTemplate;
import org.openlmis.stockmanagement.repository.StockCardTemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.UUID;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureMockMvc
public class StockCardTemplateControllerTest extends BaseWebTest {

  @Autowired
  private MockMvc mvc;

  @MockBean
  private StockCardTemplateRepository repository;

  private UUID programId = UUID.randomUUID();
  private UUID facilityTypeId = UUID.randomUUID();

  @Test
  public void should_search_for_stock_card_templates() throws Exception {

    //given
    when(repository.findByProgramIdAndFacilityTypeId(programId, facilityTypeId))
        .thenReturn(createDummyTemplate());

    //when
    MockHttpServletRequestBuilder builder = get("/api/stockCardTemplate")
        .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE)
        .param("program", programId.toString())
        .param("facilityType", facilityTypeId.toString());

    ResultActions resultActions = mvc.perform(builder);

    //then
    resultActions
        .andExpect(status().isOk())
        .andExpect(content().json("{'stockCardOptionalFields':{'donor':true}}"));
  }

  @Test
  public void should_return_404_when_template_not_found() throws Exception {
    //given
    when(repository.findByProgramIdAndFacilityTypeId(programId, facilityTypeId))
        .thenReturn(createDummyTemplate());

    //when

    ResultActions resultActions = mvc.perform(get("/api/stockCardTemplate")
        .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE)
        .param("program", UUID.randomUUID().toString())
        .param("facilityType", UUID.randomUUID().toString()));

    //then
    resultActions
        .andExpect(status().isNotFound());
  }

  @Test
  public void should_return_201_when_create_template() throws Exception {
    //given
    when(repository.save(any(StockCardTemplate.class))).thenReturn(createDummyTemplate());

    //when
    ObjectMapper mapper = new ObjectMapper();
    String jsonString = mapper.writeValueAsString(new StockCardTemplate());

    ResultActions resultActions = mvc.perform(post("/api/stockCardTemplate")
        .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE)
        .contentType(MediaType.APPLICATION_JSON)
        .content(jsonString));

    //then
    resultActions.andExpect(status().isCreated());
  }

  private StockCardTemplate createDummyTemplate() {
    StockCardOptionalFields stockCardOptionalFields = new StockCardOptionalFields();
    stockCardOptionalFields.setDonor(true);

    StockCardTemplate template = new StockCardTemplate();
    template.setStockCardOptionalFields(stockCardOptionalFields);

    return template;
  }
}