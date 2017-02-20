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

package org.openlmis.stockmanagement.i18n;

public abstract class MessageKeys {
  private static final String SERVICE_PREFIX = "stockmanagement";
  private static final String ERROR_PREFIX = SERVICE_PREFIX + ".error";

  public static final String ERROR_STOCK_CARD_FIELD_INVALID =
          ERROR_PREFIX + ".field.invalid";

  public static final String ERROR_PROGRAM_NOT_FOUND =
          ERROR_PREFIX + ".program.notFound";
  public static final String ERROR_FACILITY_TYPE_NOT_FOUND =
          ERROR_PREFIX + ".facilityType.notFound";

  public static final String ERROR_STOCK_EVENT_REASON_NOT_MATCH =
          ERROR_PREFIX + ".reason.notMatch";

  public static final String ERROR_ORDERABLE_NOT_FOUND =
          ERROR_PREFIX + ".orderable.notFound";

  public static final String ERROR_SOURCE_DESTINATION_BOTH_PRESENT =
          ERROR_PREFIX + ".sourceAndDestination.bothPresent";

  public static final String ERROR_SOURCE_NOT_VALID =
          ERROR_PREFIX + ".source.invalid";

  public static final String ERROR_DESTINATION_NOT_VALID =
          ERROR_PREFIX + ".destination.invalid";

  public static final String ERROR_SOURCE_FREE_TEXT_NOT_ALLOWED =
          ERROR_PREFIX + ".sourceFreeText.notAllowed";

  public static final String ERROR_NO_FOLLOWING_PERMISSION = ERROR_PREFIX
          + ".authorization.noFollowingPermission";

  public static final String ERROR_EVENT_OCCURRED_DATE_INVALID = ERROR_PREFIX
      + ".event.occurredDate.invalid";

  public static final String ERROR_EVENT_QUANTITY_INVALID = ERROR_PREFIX
      + ".event.quantity.invalid";

  public static final String ERROR_EVENT_FACILITY_INVALID = ERROR_PREFIX
      + ".event.facilityId.invalid";

  public static final String ERROR_EVENT_PROGRAM_INVALID = ERROR_PREFIX
      + ".event.programId.invalid";

  public static final String ERROR_EVENT_ORDERABLE_INVALID = ERROR_PREFIX
      + ".event.ordeableId.invalid";

  private MessageKeys() {
    throw new UnsupportedOperationException();
  }
}
