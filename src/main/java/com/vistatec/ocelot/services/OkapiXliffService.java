package com.vistatec.ocelot.services;

import com.vistatec.ocelot.config.ProvenanceConfig;
import com.vistatec.ocelot.segment.Segment;
import com.vistatec.ocelot.segment.XLIFFFactory;
import com.vistatec.ocelot.segment.XLIFFParser;
import com.vistatec.ocelot.segment.XLIFFWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import javax.xml.stream.XMLStreamException;

/**
 * Service for performing Okapi XLIFF operations.
 */
public class OkapiXliffService implements XliffService {

    private XLIFFFactory xliffFactory;
    private XLIFFParser xliffParser;
    private XLIFFWriter segmentWriter;

    private ProvenanceConfig provConfig;

    public OkapiXliffService(ProvenanceConfig provConfig) {
        this.provConfig = provConfig;
    }

    @Override
    public List<Segment> parse(File xliffFile, File detectVersion) throws FileNotFoundException, IOException, XMLStreamException {
        XLIFFParser newParser = xliffFactory.newXLIFFParser(detectVersion);
        List<Segment> xliffSegments = newParser.parse(xliffFile);

        xliffParser = newParser;
        segmentWriter = xliffFactory.newXLIFFWriter(xliffParser, provConfig);
        return xliffSegments;
    }

    @Override
    public void save(File file) throws FileNotFoundException, IOException {
        segmentWriter.save(file);
    }

    @Override
    public String getSourceLang() {
        return xliffParser.getSourceLang();
    }

    @Override
    public String getTargetLang() {
        return xliffParser.getTargetLang();
    }

}
