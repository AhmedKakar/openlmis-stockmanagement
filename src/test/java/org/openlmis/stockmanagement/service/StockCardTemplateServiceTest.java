package org.openlmis.stockmanagement.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.stockmanagement.domain.template.StockCardFields;
import org.openlmis.stockmanagement.domain.template.StockCardTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.openlmis.stockmanagement.testutils.StockCardTemplateBuilder.createTemplate;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class StockCardTemplateServiceTest {

  @Autowired
  private StockCardTemplateService stockCardTemplateService;

  @Test
  public void should_update_existing_template() throws Exception {
    //given: there is an existing template
    StockCardTemplate savedTemplate = stockCardTemplateService.saveOrUpdate(createTemplate());
    UUID facilityTypeId = savedTemplate.getFacilityTypeId();
    UUID programId = savedTemplate.getProgramId();

    //when: try to save a template with the same program and facility type
    StockCardTemplate newTemplate = createTemplate();
    newTemplate.setFacilityTypeId(facilityTypeId);
    newTemplate.setProgramId(programId);
    newTemplate.getStockCardFields().get(0).setDisplayOrder(321);

    stockCardTemplateService.saveOrUpdate(newTemplate);
    StockCardTemplate updatedTemplate = stockCardTemplateService
            .findByProgramIdAndFacilityTypeId(programId, facilityTypeId);

    //then
    assertThat(updatedTemplate.getStockCardFields().size(), is(1));

    StockCardFields firstFields = updatedTemplate.getStockCardFields().get(0);
    assertThat(firstFields.getDisplayOrder(), is(321));
    assertThat(firstFields.getAvailableStockCardFields().getName(), is("packSize"));
  }

}