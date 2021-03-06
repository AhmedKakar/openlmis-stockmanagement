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

package org.openlmis.stockmanagement.testutils;

import org.openlmis.stockmanagement.domain.sourcedestination.Node;
import org.openlmis.stockmanagement.domain.sourcedestination.ValidDestinationAssignment;
import org.openlmis.stockmanagement.domain.sourcedestination.ValidSourceAssignment;

public class ValidSourceDestinationBuilder {

  public static ValidSourceAssignment createFacilitySourceAssignment(Node facilityNode) {
    return getValidSourceAssignment(facilityNode);
  }

  public static ValidSourceAssignment createOrganizationSourceAssignment(Node organizationNode) {
    return getValidSourceAssignment(organizationNode);
  }

  public static ValidDestinationAssignment createFacilityDestination(Node facilityNode) {
    return getValidDestinationAssignment(facilityNode);
  }

  public static ValidDestinationAssignment createOrganizationDestination(Node organizationNode) {
    return getValidDestinationAssignment(organizationNode);
  }

  private static ValidSourceAssignment getValidSourceAssignment(Node organizationNode) {
    ValidSourceAssignment source = new ValidSourceAssignment();
    source.setNode(organizationNode);
    return source;
  }

  private static ValidDestinationAssignment getValidDestinationAssignment(Node facilityNode) {
    ValidDestinationAssignment destination = new ValidDestinationAssignment();
    destination.setNode(facilityNode);
    return destination;
  }
}
