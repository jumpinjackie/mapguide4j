package util;

import controllers.MgAbstractController;
import play.mvc.Results.*;
import java.lang.StringBuilder;

import org.osgeo.mapguide.*;

public class MgFeatureSetChunkedResult extends StringChunks {
    
    private MgFeatureReader _reader;
    private MgFeatureService _featSvc;
    private int _limit;

    public MgFeatureSetChunkedResult(MgFeatureService featSvc, MgFeatureReader reader, int maxFeatures) {
        super();
        _reader = reader;
        _featSvc = featSvc;
        _limit = maxFeatures;
    }

    public void onReady(Chunks.Out<String> output) {
        try {
            int read = 0;
            MgFeatureSchemaCollection schemas = new MgFeatureSchemaCollection();
            MgFeatureSchema schema = new MgFeatureSchema("TempSchema", "");
            schemas.Add(schema);
            MgClassDefinitionCollection classes = schema.GetClasses();
            MgClassDefinition clsDef = _reader.GetClassDefinition();
            classes.Add(clsDef);
            MgAgfReaderWriter agfRw = new MgAgfReaderWriter();
            MgWktReaderWriter wktRw = new MgWktReaderWriter();
            StringBuilder xml = new StringBuilder("<FeatureSet>");
            String classXml = _featSvc.SchemaToXml(schemas);
            //HACK: The method includes the xml prolog (the controller will have already put this in)
            //so strip it out
            xml.append(classXml.substring(classXml.indexOf("<xs:schema")));
            xml.append("<Features>");
            output.write(xml.toString());
            while(_reader.ReadNext()) {
                read++;
                //There's a max limit and it's been reached
                if (_limit > 0 && read > _limit) {
                    break;
                }
                xml.setLength(0); //Wipe clean
                xml.append("<Feature>");
                for (int i = 0; i < _reader.GetPropertyCount(); i++) {
                    String name = _reader.GetPropertyName(i);
                    int propType = _reader.GetPropertyType(i);
                    xml.append("<Property>");
                    xml.append("<Name>");
                    xml.append(name);
                    xml.append("</Name>");
                    if (!_reader.IsNull(i)) { //Null values omit the <Value> element
                        xml.append("<Value>");
                        switch(propType) {
                            case MgPropertyType.Boolean:
                                xml.append(_reader.GetBoolean(i));
                                break;
                            case MgPropertyType.Byte:
                                xml.append(_reader.GetByte(i));
                                break;
                            case MgPropertyType.DateTime:
                                MgDateTime dt = _reader.GetDateTime(i);
                                xml.append(dt.ToString());
                                break;
                            case MgPropertyType.Decimal:
                            case MgPropertyType.Double:
                                xml.append(_reader.GetDouble(i));
                                break;
                            case MgPropertyType.Geometry:
                                {
                                    try {
                                        MgByteReader agf = _reader.GetGeometry(i);
                                        MgGeometry geom = agfRw.Read(agf);
                                        xml.append(wktRw.Write(geom));
                                    } catch (MgException ex) {

                                    }
                                }
                                break;
                            case MgPropertyType.Int16:
                                xml.append(_reader.GetInt16(i));
                                break;
                            case MgPropertyType.Int32:
                                xml.append(_reader.GetInt32(i));
                                break;
                            case MgPropertyType.Int64:
                                xml.append(_reader.GetInt64(i));
                                break;
                            case MgPropertyType.Single:
                                xml.append(_reader.GetSingle(i));
                                break;
                            case MgPropertyType.String:
                                xml.append(MgAbstractController.escapeXmlCharacters(_reader.GetString(i)));
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
                _reader.Close();
            }
            catch (Exception e) { }
        }
    }
}