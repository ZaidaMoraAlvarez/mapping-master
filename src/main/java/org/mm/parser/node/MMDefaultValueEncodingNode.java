
package org.mm.parser.node;

import org.mm.parser.ASTMMDefaultValueEncoding;
import org.mm.parser.MappingMasterParserConstants;
import org.mm.parser.ParseException;

public class MMDefaultValueEncodingNode implements MappingMasterParserConstants
{
	private int encodingType;

	public MMDefaultValueEncodingNode(ASTMMDefaultValueEncoding node) throws ParseException
	{
		encodingType = node.encodingType;
	}

	public int getEncodingType()
	{
		return encodingType;
	}

	public String getEncodingTypeName()
	{
		return tokenImage[encodingType].substring(1, tokenImage[encodingType].length() - 1);
	}

	public String toString()
	{
		return "MM:DefaultValueEncoding " + getEncodingTypeName();
	}
}