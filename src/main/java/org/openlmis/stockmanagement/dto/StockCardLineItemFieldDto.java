package org.openlmis.stockmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.openlmis.stockmanagement.domain.template.AvailableStockCardLineItemFields;
import org.openlmis.stockmanagement.domain.template.StockCardLineItemFields;
import org.openlmis.stockmanagement.domain.template.StockCardTemplate;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.utils.Message;

import java.util.List;
import java.util.Optional;

import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_STOCK_CARD_FIELD_INVALID;

@Data
@AllArgsConstructor
public class StockCardLineItemFieldDto {
  private String name;
  private boolean isDisplayed;
  private Integer displayOrder;

  /**
   * Create stock card line item field dto object from DB model object.
   *
   * @param model the original object to convert from.
   * @return dto object.
   */
  static StockCardLineItemFieldDto from(StockCardLineItemFields model) {
    return new StockCardLineItemFieldDto(
            model.getAvailableStockCardLineItemFields().getName(),
            model.getIsDisplayed(),
            model.getDisplayOrder());
  }

  /**
   * Convert to DB model object.
   *
   * @param template       the template that this filed belongs to.
   * @param lineItemFields all available fields.
   * @return DB model object.
   */
  StockCardLineItemFields toModel(StockCardTemplate template,
                                  List<AvailableStockCardLineItemFields> lineItemFields) {
    StockCardLineItemFields stockCardLineItemFields = new StockCardLineItemFields();
    stockCardLineItemFields.setStockCardTemplate(template);
    stockCardLineItemFields.setIsDisplayed(isDisplayed);
    stockCardLineItemFields.setDisplayOrder(displayOrder);
    stockCardLineItemFields.setAvailableStockCardLineItemFields(matchByName(lineItemFields));
    return stockCardLineItemFields;
  }

  private AvailableStockCardLineItemFields matchByName(
          List<AvailableStockCardLineItemFields> lineItemFields) {
    Optional<AvailableStockCardLineItemFields> first = lineItemFields.stream()
            .filter(field -> field.getName().equals(name))
            .findFirst();
    if (first.isPresent()) {
      return first.get();
    } else {
      throw new ValidationMessageException(
              new Message(ERROR_STOCK_CARD_FIELD_INVALID, name));
    }
  }
}
