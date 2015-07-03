
package org.mm.parser.node;

import org.mm.parser.ASTShiftSetting;
import org.mm.parser.MappingMasterParserConstants;
import org.mm.parser.ParseException;
import org.mm.parser.ParserUtil;

public class ShiftSettingNode implements MappingMasterParserConstants
{
  private int shiftSetting;
  
  public ShiftSettingNode(ASTShiftSetting node) throws ParseException
  {
  	shiftSetting = node.shiftSetting;
  }
  
  public boolean isNoShift() { return shiftSetting == MM_NO_SHIFT; }
  public boolean isShiftLeft() { return shiftSetting == MM_SHIFT_LEFT; }
  public boolean isShiftRight() { return shiftSetting == MM_SHIFT_RIGHT; }
  public boolean isShiftUp() { return shiftSetting == MM_SHIFT_UP; }
  public boolean isShiftDown() { return shiftSetting == MM_SHIFT_DOWN; }
  
  public int getShiftSetting() { return shiftSetting; }
  
  public String toString() { return ParserUtil.getTokenName(shiftSetting); }
}