package org.openlmis.stockmanagement.repository;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.stockmanagement.Application;
import org.openlmis.stockmanagement.domain.template.StockCardLineItemOptionalFields;
import org.openlmis.stockmanagement.domain.template.StockCardOptionalFields;
import org.openlmis.stockmanagement.domain.template.StockCardTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(Application.class)
public class StockCardTemplateRepositoryTest {

  @Autowired
  private StockCardTemplateRepository stockCardTemplateRepository;

  @Test
  public void should_search_for_stock_card_template_by_facility_type_and_program()
          throws Exception {
    //given
    StockCardTemplate template = createTemplate();

    stockCardTemplateRepository.save(template);

    //when
    StockCardTemplate found = stockCardTemplateRepository.findByProgramIdAndFacilityTypeId(
            template.getProgramId(), template.getFacilityTypeId());

    //then
    assertThat(found.getStockCardOptionalFields().getDonor(), is(true));
  }

  private StockCardTemplate createTemplate() {
    StockCardOptionalFields stockCardOptionalFields = new StockCardOptionalFields();
    stockCardOptionalFields.setDonor(true);

    StockCardTemplate template = new StockCardTemplate();

    template.setFacilityTypeId(UUID.randomUUID());
    template.setProgramId(UUID.randomUUID());
    template.setStockCardOptionalFields(stockCardOptionalFields);
    template.setStockCardLineItemOptionalFields(new StockCardLineItemOptionalFields());

    return template;
  }
}