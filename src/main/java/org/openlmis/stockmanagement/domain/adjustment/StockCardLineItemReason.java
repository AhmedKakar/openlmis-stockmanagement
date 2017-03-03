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

package org.openlmis.stockmanagement.domain.adjustment;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import org.openlmis.stockmanagement.domain.BaseEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "stock_card_line_item_reasons", schema = "stockmanagement")
@JsonInclude(NON_NULL)
public class StockCardLineItemReason extends BaseEntity {

  @Column(nullable = false, columnDefinition = TEXT_COLUMN_DEFINITION)
  private String name;

  @Column(columnDefinition = TEXT_COLUMN_DEFINITION)
  private String description;

  @Column(nullable = false, columnDefinition = TEXT_COLUMN_DEFINITION)
  @Enumerated(value = EnumType.STRING)
  private ReasonType reasonType;

  @Column(nullable = false, columnDefinition = TEXT_COLUMN_DEFINITION)
  @Enumerated(value = EnumType.STRING)
  private ReasonCategory reasonCategory;

  @Column(nullable = false)
  private Boolean isFreeTextAllowed = false;

  /**
   * Create physical credit reason.
   *
   * @return physical credit reason.
   */
  public static StockCardLineItemReason physicalCredit() {
    return builder()
        .reasonType(ReasonType.CREDIT)
        .reasonCategory(ReasonCategory.PHYSICAL_INVENTORY)
        .name("Overstock")
        .description("Inventory correction in case of overstock")
        .build();
  }


  /**
   * Create physical debit reason.
   *
   * @return physical debit reason.
   */
  public static StockCardLineItemReason physicalDebit() {
    return builder()
        .reasonType(ReasonType.DEBIT)
        .reasonCategory(ReasonCategory.PHYSICAL_INVENTORY)
        .name("Understock")
        .description("Inventory correction in case of understock")
        .build();
  }

  /**
   * Create physical balance reason.
   *
   * @return physical balance reason.
   */
  public static StockCardLineItemReason physicalBalance() {
    return builder()
        .reasonType(ReasonType.BALANCE_ADJUSTMENT)
        .reasonCategory(ReasonCategory.PHYSICAL_INVENTORY)
        .name("Balance adjustment")
        .description("Balance adjustment")
        .build();
  }

  @Override
  public boolean equals(Object other) {
    if (other == null) {
      return false;
    }
    if (other == this) {
      return true;
    }
    if (!(other instanceof StockCardLineItemReason)) {
      return false;
    }

    StockCardLineItemReason otherMyClass = (StockCardLineItemReason) other;
    return this.getName().equals(otherMyClass.getName())
        && this.getDescription().equals(otherMyClass.getDescription())
        && this.getReasonType() == otherMyClass.getReasonType()
        && this.getReasonCategory() == otherMyClass.getReasonCategory()
        && this.isFreeTextAllowed == otherMyClass.isFreeTextAllowed;
  }

  @Override
  public int hashCode() {
    if (id != null) {
      return id.hashCode();
    } else {
      return super.hashCode();
    }
  }

  @JsonIgnore
  public boolean isCreditReasonType() {
    return getReasonType() == ReasonType.CREDIT;
  }

  @JsonIgnore
  public boolean isDebitReasonType() {
    return getReasonType() == ReasonType.DEBIT;
  }

  @JsonIgnore
  public boolean isAdjustmentReasonCategory() {
    return getReasonCategory() == ReasonCategory.ADJUSTMENT;
  }

  @JsonIgnore
  public boolean hasNoName() {
    return name == null;
  }

  @JsonIgnore
  public boolean hasNoType() {
    return reasonType == null;
  }

  @JsonIgnore
  public boolean hasNoCategory() {
    return reasonCategory == null;
  }

  @JsonIgnore
  public boolean hasNoIsFreeTextAllowed() {
    return isFreeTextAllowed == null;
  }
}
