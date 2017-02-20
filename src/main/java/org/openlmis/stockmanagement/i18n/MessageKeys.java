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

  private MessageKeys() {
    throw new UnsupportedOperationException();
  }
}
