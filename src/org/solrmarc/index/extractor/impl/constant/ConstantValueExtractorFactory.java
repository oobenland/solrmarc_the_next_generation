package org.solrmarc.index.extractor.impl.constant;


import java.util.ArrayList;
import java.util.List;

import org.solrmarc.index.extractor.AbstractValueExtractor;
import org.solrmarc.index.extractor.AbstractValueExtractorFactory;
import org.solrmarc.index.utils.StringReader;

public class ConstantValueExtractorFactory extends AbstractValueExtractorFactory
{

    public boolean canHandle(final String solrFieldName, final String mappingConfiguration)
    {
        return mappingConfiguration.startsWith("\"") && mappingConfiguration.endsWith("\"");
    }

    @Override
    public AbstractValueExtractor<?> createExtractor(final String solrFieldName, final StringReader mappingConfiguration)
    {
        final List<String> values = parseMappingConfiguration(mappingConfiguration.readAll());
        if (values.isEmpty())
        {
            return null;
        }
        else
        {
            return new ConstantMultiValueExtractor(values);
        }
    }
    
    @Override
    public AbstractValueExtractor<?> createExtractor(String solrFieldName, String[] parts)
    {
        return new ConstantMultiValueExtractor(parts);
    }


    private List<String> parseMappingConfiguration(final String mappingConfiguration)
    {
        final List<String> values = new ArrayList<>();
        final char[] chars = mappingConfiguration.toCharArray();
        boolean isQuoted = false;
        boolean isEscaped = false;
        StringBuilder buffer = new StringBuilder();
        for (char c : chars)
        {
            switch (c) {
                case '\t':
                case ' ':
                    if (isQuoted)
                    {
                        buffer.append(c);
                    }
                    break;
                case '|':
                    if (isQuoted)
                    {
                        buffer.append(c);
                    }
                    else
                    {
                        final String value = buffer.toString().trim();
                        if (!value.isEmpty())
                        {
                            values.add(value);
                        }
                        buffer = new StringBuilder();
                    }
                    break;
                case '\"':
                    if (isEscaped)
                    {
                        isEscaped = false;
                        buffer.append(c);
                    }
                    else
                    {
                        isQuoted = !isQuoted;
                    }
                    break;
                case '\\':
                    if (isEscaped)
                    {
                        buffer.append(c);
                    }
                    isEscaped = !isEscaped;
                    break;
                default:
                    buffer.append(c);
            }
        }
        if (!buffer.toString().isEmpty())
        {
            values.add(buffer.toString());
        }
        return values;
    }

}
