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

package org.openlmis.stockmanagement.service;

import static java.io.File.createTempFile;
import static java.util.Collections.singletonList;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_GENERATE_REPORT_FAILED;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperReport;
import org.openlmis.stockmanagement.exception.JasperReportViewException;
import org.openlmis.stockmanagement.utils.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.jasperreports.JasperReportsPdfView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class JasperReportService {
  @Autowired
  private ApplicationContext appContext;

  @Autowired
  private StockCardService stockCardService;

  private static final String CARD_REPORT_URL = "/jasperTemplates/stockCard.jrxml";
  private static final String CARD_SUMMARY_REPORT_URL = "/jasperTemplates/stockCardSummary.jrxml";

  /**
   * Generate stock card report in PDF format.
   *
   * @param stockCardId stock card id
   * @return generated stock card report.
   */
  public ModelAndView getStockCardReportView(UUID stockCardId) {
    Map<String, Object> params = new HashMap<>();
    params.put("datasource", singletonList(stockCardService.findStockCardById(stockCardId)));

    JasperReportsPdfView view = new JasperReportsPdfView();
    compileReport(view);

    view.setApplicationContext(appContext);
    return new ModelAndView(view, params);
  }

  public ModelAndView getStockCardSummariesReportView(UUID program, UUID facility) {
    return null;
  }

  private void compileReport(JasperReportsPdfView view) {
    try (InputStream inputStream = getClass().getResourceAsStream(CARD_REPORT_URL)) {
      File reportTempFile = createTempFile("stockCardReport_temp", ".jasper");
      JasperReport report = JasperCompileManager.compileReport(inputStream);

      try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
           ObjectOutputStream out = new ObjectOutputStream(bos)) {

        out.writeObject(report);

        Path path = Paths.get(reportTempFile.getAbsolutePath());
        Files.write(path, bos.toByteArray());

        view.setUrl(reportTempFile.toURI().toURL().toString());
      }
    } catch (IOException | JRException ex) {
      throw new JasperReportViewException(new Message(ERROR_GENERATE_REPORT_FAILED), ex);
    }
  }
}
