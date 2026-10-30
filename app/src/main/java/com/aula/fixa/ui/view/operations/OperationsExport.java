package com.aula.fixa.ui.view.operations;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/** Exportação Open XML real, sem renomear CSV para .xlsx e sem fórmulas externas. */
public final class OperationsExport {
    private static final String NS="http://schemas.openxmlformats.org/spreadsheetml/2006/main";
    private OperationsExport() {}
    private static String esc(String value) {
        if (value == null) return "";
        return value.replaceAll("[\\u0000-\\u0008\\u000B\\u000C\\u000E-\\u001F]", "")
                .replace("&","&amp;").replace("<","&lt;").replace(">","&gt;").replace("\"","&quot;");
    }
    private static void entry(ZipOutputStream zip,String name,String xml)throws Exception {
        zip.putNextEntry(new ZipEntry(name));zip.write(xml.getBytes(StandardCharsets.UTF_8));zip.closeEntry();
    }
    private static String columnName(int column) {
        StringBuilder name = new StringBuilder();
        while (column > 0) {
            int remainder = (column - 1) % 26;
            name.insert(0, (char) ('A' + remainder));
            column = (column - 1) / 26;
        }
        return name.toString();
    }
    private static String row(int index, List<String> values) {
        StringBuilder row=new StringBuilder("<row r=\"").append(index).append("\">");
        for(int i=0;i<values.size();i++)row.append("<c r=\"").append(columnName(i + 1)).append(index).append("\" t=\"inlineStr\"><is><t xml:space=\"preserve\">").append(esc(values.get(i))).append("</t></is></c>");
        return row.append("</row>").toString();
    }
    public static void write(String type, List<Map<String, String>> data, OutputStream stream)throws Exception {
        List<String> headers = new ArrayList<>();
        if (data != null) {
            LinkedHashSet<String> uniqueHeaders = new LinkedHashSet<>();
            for (Map<String, String> item : data) uniqueHeaders.addAll(item.keySet());
            headers.addAll(uniqueHeaders);
        }
        if (headers.isEmpty()) headers.addAll(headersForEmpty(type));
        StringBuilder sheet=new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?><worksheet xmlns=\"").append(NS).append("\"><sheetData>");
        sheet.append(row(1, headers));
        int index = 2;
        if (data != null) for (Map<String, String> item : data) {
            List<String> values = new ArrayList<>(headers.size());
            for (String header : headers) values.add(item.getOrDefault(header, ""));
            sheet.append(row(index++, values));
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

    private static List<String> headersForEmpty(String type) {
        if ("Solicitações".equals(type)) {
            return List.of("id", "titulo", "descricaoProblema", "categoriaEquipamento", "localUnidade", "descricaoLocal", "status", "usuario", "tipoAcesso", "dataCriacao", "fotos");
        }
        if ("Ordens de serviço".equals(type)) {
            return List.of("id", "titulo", "descricao", "categoriaProblema", "categoriaEquipamento", "prioridade", "dataPrevista", "status", "nomeUsuario", "dataCriacao", "localUnidade", "descricaoLocal", "solicitante");
        }
        return List.of("id", "localUnidadeId", "categoriaEquipamentoId", "titulo", "descricaoOcorrencia", "categoriaProblema", "prioridade", "localUnidade", "descricaoLocal", "dataCriacao", "categoriaEquipamento", "nomeUsuario", "fotos");
    }
}
