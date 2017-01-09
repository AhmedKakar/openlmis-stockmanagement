package org.openlmis.stockmanagement.repository;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.stockmanagement.domain.template.StockCardTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
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
    StockCardTemplate template = new StockCardTemplate();
    template.setFacilityTypeId(UUID.randomUUID());
    template.setProgramId(UUID.randomUUID());
    template.getStockCardOptionalFields().setDonor(true);
    return template;
  }
}