package util;

import controllers.MgAbstractController;
import play.mvc.Results.*;
import java.lang.StringBuilder;

import org.osgeo.mapguide.*;

/**
 * A helper class to output the XML content of a MgFeatureReader as a chunked response
 */
public class MgFeatureSetChunkedResult extends StringChunks {

    private MgFeatureReader _reader;
    private MgFeatureService _featSvc;
    private int _limit;

    private MgCoordinateSystemTransform _transform;

    public MgFeatureSetChunkedResult(MgFeatureService featSvc, MgFeatureReader reader, int maxFeatures) {
        super();
        _reader = reader;
        _featSvc = featSvc;
        _limit = maxFeatures;
    }

    public void setTransform(MgCoordinateSystemTransform transform) {
        _transform = transform;
    }

    public void onReady(Chunks.Out<String> output) {
        try {
            int read = 0;
            MgFeatureSchemaCollection schemas = new MgFeatureSchemaCollection();
            MgFeatureSchema schema = new MgFeatureSchema("TempSchema", "");
            schemas.add(schema);
            MgClassDefinitionCollection classes = schema.getClasses();
            MgClassDefinition clsDef = _reader.getClassDefinition();
            classes.add(clsDef);
            MgAgfReaderWriter agfRw = new MgAgfReaderWriter();
            MgWktReaderWriter wktRw = new MgWktReaderWriter();
            StringBuilder xml = new StringBuilder("<FeatureSet>");
            String classXml = _featSvc.schemaToXml(schemas);
            //HACK: The method includes the xml prolog (the play controller will have already put this in)
            //so strip it out
            xml.append(classXml.substring(classXml.indexOf("<xs:schema")));
            xml.append("<Features>");
            output.write(xml.toString());
            while(_reader.readNext()) {
                read++;
                //There's a max limit and it's been reached
                if (_limit > 0 && read > _limit) {
                    break;
                }
                xml.setLength(0); //Wipe clean
                xml.append("<Feature>");
                for (int i = 0; i < _reader.getPropertyCount(); i++) {
                    String name = _reader.getPropertyName(i);
                    int propType = _reader.getPropertyType(i);
                    xml.append("<Property>");
                    xml.append("<Name>");
                    xml.append(name);
                    xml.append("</Name>");
                    if (!_reader.isNull(i)) { //Null values omit the <Value> element
                        xml.append("<Value>");
                        switch(propType) {
                            case MgPropertyType.Boolean:
                                xml.append(_reader.getBoolean(i));
                                break;
                            case MgPropertyType.Byte:
                                xml.append(_reader.getByte(i));
                                break;
                            case MgPropertyType.DateTime:
                                MgDateTime dt = _reader.getDateTime(i);
                                xml.append(dt.toString());
                                break;
                            case MgPropertyType.Decimal:
                            case MgPropertyType.Double:
                                xml.append(_reader.getDouble(i));
                                break;
                            case MgPropertyType.Geometry:
                                {
                                    try {
                                        MgByteReader agf = _reader.getGeometry(i);
                                        MgGeometry geom = (_transform != null) ? agfRw.read(agf, _transform) : agfRw.read(agf);
                                        xml.append(wktRw.write(geom));
                                    } catch (MgException ex) {
                                    }
                                }
                                break;
                            case MgPropertyType.Int16:
                                xml.append(_reader.getInt16(i));
                                break;
                            case MgPropertyType.Int32:
                                xml.append(_reader.getInt32(i));
                                break;
                            case MgPropertyType.Int64:
                                xml.append(_reader.getInt64(i));
                                break;
                            case MgPropertyType.Single:
                                xml.append(_reader.getSingle(i));
                                break;
                            case MgPropertyType.String:
                                xml.append(MgAbstractController.escapeXmlCharacters(_reader.getString(i)));
                                break;
                        }
                        xml.append("</Value>");
                    }
                    xml.append("</Property>");
                }
                xml.append("</Feature>");
                output.write(xml.toString());
            }
            output.write("</Features></FeatureSet>");
        } catch (MgException ex) {

        } finally {
            output.close();
            try {
                _reader.close();
            }
            catch (Exception e) { }
        }
    }
}