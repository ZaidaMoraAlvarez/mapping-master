package org.mm.test;

import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import org.mm.exceptions.MappingMasterException;
import org.mm.parser.ASTExpression;
import org.mm.parser.MappingMasterParser;
import org.mm.parser.ParseException;
import org.mm.parser.SimpleNode;
import org.mm.parser.node.ExpressionNode;
import org.mm.parser.node.MMExpressionNode;
import org.mm.renderer.owlapi.OWLAPICoreRenderer;
import org.mm.renderer.text.TextRenderer;
import org.mm.rendering.owlapi.OWLAPIRendering;
import org.mm.rendering.text.TextRendering;
import org.mm.ss.SpreadSheetDataSource;
import org.mm.ss.SpreadsheetLocation;
import org.semanticweb.owlapi.model.OWLOntology;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class IntegrationTestBase
{
  /**
   * Create a single sheet workbook. Not clear how to create an in-memory-only {@link Workbook} in JXL so
   * we create a {@link WritableWorkbook} (which is not a subclass of {@link Workbook}) save it to a temporary file and
   * then create a {@link Workbook} from it.
   */
  protected Workbook createWorkbook(String sheetName, Set<Label> cells)
    throws IOException, WriteException, BiffException
  {
    File file = File.createTempFile("temp", "xlsx");
    WritableWorkbook writableWorkbook = Workbook.createWorkbook(file);
    WritableSheet sheet = writableWorkbook.createSheet(sheetName, 0);

    for (Label cell : cells)
      sheet.addCell(cell);

    writableWorkbook.write();
    writableWorkbook.close();

    return Workbook.getWorkbook(file);
  }

  /**
   * @param data Map of sheet name to cells
   * @return A workbook
   * @throws IOException
   * @throws WriteException
   * @throws BiffException
   */
  protected Workbook createWorkbook(Map<String, Set<Label>> data) throws IOException, WriteException, BiffException
  {
    File file = File.createTempFile("temp", "xlsx");
    WritableWorkbook writableWorkbook = Workbook.createWorkbook(file);

    int sheetIndex = 0;
    for (String sheetName : data.keySet()) {
      Set<Label> cells = data.get(sheetName);
      WritableSheet sheet = writableWorkbook.createSheet(sheetName, sheetIndex++);

      for (Label cell : cells)
        sheet.addCell(cell);
    }

    writableWorkbook.write();
    writableWorkbook.close();

    return Workbook.getWorkbook(file);
  }

  protected SpreadSheetDataSource createSpreadsheetDataSource(String sheetName, Set<Label> cells)
    throws BiffException, IOException, WriteException, MappingMasterException
  {
    Workbook workbook = createWorkbook(sheetName, cells);
    return new SpreadSheetDataSource(workbook);
  }

  protected MMExpressionNode parseExpression(String expression) throws ParseException
  {
    MappingMasterParser parser = new MappingMasterParser(new ByteArrayInputStream(expression.getBytes()));
    SimpleNode simpleNode = parser.expression();
    ExpressionNode expressionNode = new ExpressionNode((ASTExpression)simpleNode);

    return expressionNode.getMMExpressionNode();
  }

  protected Optional<? extends TextRendering> createTextRendering(String sheetName, Set<Label> cells,
    SpreadsheetLocation currentLocation, String expression)
    throws WriteException, BiffException, MappingMasterException, IOException, ParseException
  {
    SpreadSheetDataSource dataSource = createSpreadsheetDataSource(sheetName, cells);

    dataSource.setCurrentLocation(currentLocation);

    TextRenderer renderer = new TextRenderer(dataSource);
    MMExpressionNode mmExpressionNode = parseExpression(expression);

    return renderer.renderMMExpression(mmExpressionNode);
  }

  protected Optional<? extends OWLAPIRendering> createOWLAPIRendering(OWLOntology ontology, String sheetName,
    Set<Label> cells, SpreadsheetLocation currentLocation, String expression)
    throws WriteException, BiffException, MappingMasterException, IOException, ParseException
  {
    SpreadSheetDataSource dataSource = createSpreadsheetDataSource(sheetName, cells);

    dataSource.setCurrentLocation(currentLocation);

    OWLAPICoreRenderer renderer = new OWLAPICoreRenderer(ontology, dataSource);
    MMExpressionNode mmExpressionNode = parseExpression(expression);

    return renderer.renderMMExpression(mmExpressionNode);
  }

  /**
   *
   * @param content Content of the cell
   * @param columnNumber 1-based column number
   * @param rowNumber 1-based row number
   * @return A cell
   */
  protected Label createCell(String content, int columnNumber, int rowNumber)
  {
    return new Label(columnNumber - 1, rowNumber -1, content); // JXL is 0-based
  }

  protected Set<Label> createCells(Label... cells)
  {
    Set<Label> cellSet = new HashSet<>();
    for (Label cell : cells) {
      cellSet.add(cell);
    }
    return cellSet;
  }
}