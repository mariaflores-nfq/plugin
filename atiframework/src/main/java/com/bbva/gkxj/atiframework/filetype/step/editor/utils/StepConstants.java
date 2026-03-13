package com.bbva.gkxj.atiframework.filetype.step.editor.utils;

import java.awt.*;

/**
 * Clase de utilidad para los ficheros de configuración .step
 */
public class StepConstants {

    public static final String[] ETL_STEP_READER_TYPES = {"","Api Request", "CSV File", "Fixed File", "XML File", "Queries"};
    public static final String[] ETL_STEP_WRITER_TYPES = {"","File", "Api Request", "Mongo Query", "SQL Query"};
    public static final String STEP_CONFIGURATION_PANEL_TITLE = "Step Configuration";
    public static final String STEP_DETAILS_PANEL_TITLE = "Step Details";
    public static final String CARD_EMPTY = "empty";
    public static final String CARD_SPLITTER = "splitter";
    public static final String CARD_PARAMS_EMPTY = "paramsEmpty";
    public static final String CARD_PARAMS_FULL = "paramsFull";
    public static final String[] FILE_TASK_OPERATION_TYPES = {"DELETE","MOVE","COPY","AUTO DELETE","UNZIP","ZIP"
            ,"NOVA TRANSFER","EPSILON UPLOAD","EPSILON DOWNLOAD","EPSILON DELETE"};


    // --- Nuevas constantes centralizadas para queries/steps ---
    public static final String TYPE_SQL = "SQL Query";
    public static final String TYPE_MONGO = "Mongo Query";
    public static final String TYPE_FILE_WATCHER = "File Watcher";
    public static final String TYPE_NOVA = "Nova Transfer Watcher";
    public static final String[] DB_SOURCE_OPTIONS = {"Config", "Data", "Oracle"};

    // --- Nodos JSON por tipo de Step ---
    public static final String JSON_NODE_READER = "reader";
    public static final String JSON_NODE_WRITER = "writer";
    public static final String JSON_NODE_QUERY_LIST = "queryList";
    public static final String JSON_NODE_FILE_TASK = "fileTask";
    public static final String JSON_NODE_JASPER_REPORT = "jasperReport";
    public static final String JSON_NODE_STEP_TYPE = "stepType";
    public static final String TASKLET_QUERY = "TASKLET_QUERY";
    public static final int ACTION_COLUMN_INDEX = 3;

    // --- Estilos ---
    public static final Font FONT_TITLE = new Font("Lato", Font.BOLD, 16);
    public static final Font FONT_SUBTITLE = new Font("Lato", Font.PLAIN, 13);
    public static final Font FONT_LABEL = new Font("Lato", Font.BOLD, 14);
    public static final Font FONT_HEADER_TABLE = new Font("Lato", Font.BOLD, 12);
    public static final Font FONT_SECTION_TITLE = new Font("Lato", Font.PLAIN, 18);

    // --- Colores ---
    public static final Color BG_SIDEBAR = new Color(248, 249, 250);
    public static final Color BG_TABLE_HEADER = new Color(245, 245, 245);
    public static final Color BG_TABLE_SELECTION = new Color(232, 244, 253);
    public static final Color TEXT_GRAY = new Color(100, 100, 100);
    public static final Color BORDER_COLOR = new Color(200, 200, 200);
    public static final Color SCRIPT_HEADER_BG = new Color(244, 244, 244);
    public static final Color BBVA_BLUE = new Color(0, 68, 129);

}