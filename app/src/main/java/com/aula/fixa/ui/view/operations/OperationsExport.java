package com.aula.fixa.ui.view.operations;

import android.content.Context;
import com.aula.fixa.adapter.operations.OperationsViewModel;
import com.aula.fixa.ui.config.operations.ScreenCatalog;
import org.json.JSONObject;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/** Exportação Open XML real, sem renomear CSV para .xlsx e sem fórmulas externas. */
public final class OperationsExport {
    private static final String NS="http://schemas.openxmlformats.org/spreadsheetml/2006/main";
    private OperationsExport() {}
    private static String esc(String value) { return value.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;").replace("\"","&quot;"); }
    private static void entry(ZipOutputStream zip,String name,String xml)throws Exception {
        zip.putNextEntry(new ZipEntry(name));zip.write(xml.getBytes(StandardCharsets.UTF_8));zip.closeEntry();
    }
    private static String row(int index,String a,String b,String c) {
        StringBuilder row=new StringBuilder("<row r=\"").append(index).append("\">");
        String[] values={a,b,c};
        for(int i=0;i<values.length;i++)row.append("<c r=\"").append((char)('A'+i)).append(index).append("\" t=\"inlineStr\"><is><t xml:space=\"preserve\">").append(esc(values[i])).append("</t></is></c>");
        return row.append("</row>").toString();
    }
    public static void write(Context context,OperationsViewModel model,OutputStream stream)throws Exception {
        StringBuilder sheet=new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?><worksheet xmlns=\"").append(NS).append("\"><cols><col min=\"1\" max=\"1\" width=\"26\" customWidth=\"1\"/><col min=\"2\" max=\"2\" width=\"40\" customWidth=\"1\"/><col min=\"3\" max=\"3\" width=\"35\" customWidth=\"1\"/></cols><sheetData>");
        sheet.append(row(1,"Área","Registro","Informação"));int index=2;
        for(var screen:ScreenCatalog.get(context).all().values()) {
            String name=screen.optString("name");
            if(!screen.optString("role").equals("manager") || !java.util.Arrays.asList("requests","board","occurrences","checklists").contains(name))continue;
            var bindings=screen.optJSONArray("bindings");
            for(int i=0;bindings!=null&&i<bindings.length();i++) {
                JSONObject binding=bindings.getJSONObject(i);
                if(binding.optString("kind").equals("card"))sheet.append(row(index++,name,binding.optString("text"),"Protótipo local"));
            }
        }
        sheet.append("</sheetData></worksheet>");
        try(ZipOutputStream zip=new ZipOutputStream(stream)) {
            entry(zip,"[Content_Types].xml","<?xml version=\"1.0\"?><Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\"><Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/><Default Extension=\"xml\" ContentType=\"application/xml\"/><Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/><Override PartName=\"/xl/worksheets/sheet1.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/></Types>");
            entry(zip,"_rels/.rels","<?xml version=\"1.0\"?><Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\"><Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"xl/workbook.xml\"/></Relationships>");
            entry(zip,"xl/workbook.xml","<?xml version=\"1.0\"?><workbook xmlns=\""+NS+"\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\"><sheets><sheet name=\"FIXA\" sheetId=\"1\" r:id=\"rId1\"/></sheets></workbook>");
            entry(zip,"xl/_rels/workbook.xml.rels","<?xml version=\"1.0\"?><Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\"><Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" Target=\"worksheets/sheet1.xml\"/></Relationships>");
            entry(zip,"xl/worksheets/sheet1.xml",sheet.toString());
        }
    }
}
