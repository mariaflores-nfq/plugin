package com.bbva.gkxj.atiframework.filetype.scheduler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.report.ProcessingMessage;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;

public class SchFileAnnotator implements Annotator {

    private static JsonSchema schema;

    static {
        try {
            InputStream schemaStream = SchFileAnnotator.class
                    .getResourceAsStream("/schemas/sch-schema.json");
            JsonNode schemaNode = JsonLoader.fromResource("/schemas/sch-schema.json");
            JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
            schema = factory.getJsonSchema(schemaNode);
        } catch (Exception e) {
            // Log error
        }
    }

    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        PsiFile file = element.getContainingFile();

        if (file == null || !file.getName().endsWith(".sch")) {
            return;
        }

        // Solo validar en el elemento raíz
        if (element != file) {
            return;
        }

        try {
            String content = file.getText();
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(content);

            ProcessingReport report = schema.validate(jsonNode);

            if (!report.isSuccess()) {
                for (ProcessingMessage message : report) {
                    String pointer = message.asJson().get("instance").get("pointer").asText();
                    String errorMsg = message.getMessage();

                    // Buscar la posición en el documento
                    Document document = file.getViewProvider().getDocument();
                    if (document != null) {
                        int line = findLineForPointer(content, pointer);
                        if (line >= 0 && line < document.getLineCount()) {
                            int start = document.getLineStartOffset(line);
                            int end = document.getLineEndOffset(line);

                            holder.newAnnotation(HighlightSeverity.ERROR, errorMsg)
                                    .range(new TextRange(start, end))
                                    .create();
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Si no es JSON válido, mostrar error general
            holder.newAnnotation(HighlightSeverity.ERROR, "Invalid JSON: " + e.getMessage())
                    .range(element.getTextRange())
                    .create();
        }
    }

    private int findLineForPointer(String content, String pointer) {
        // Lógica para encontrar la línea del error basándose en el JSON pointer
        // Esto es simplificado, puedes mejorarlo
        String[] parts = pointer.split("/");
        if (parts.length > 1) {
            String searchKey = "\"" + parts[1] + "\"";
            String[] lines = content.split("\n");
            for (int i = 0; i < lines.length; i++) {
                if (lines[i].contains(searchKey)) {
                    return i;
                }
            }
        }
        return 0;
    }
}