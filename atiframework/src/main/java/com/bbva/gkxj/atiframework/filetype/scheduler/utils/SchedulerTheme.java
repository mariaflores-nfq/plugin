package com.bbva.gkxj.atiframework.filetype.scheduler.utils;

import com.intellij.ui.JBColor;
import java.awt.*;

public class SchedulerTheme {

    // Singleton
    private SchedulerTheme() {}

    // ---------------------------------------------------------
    // COLORES (Usando JBColor para soporte Dark Mode)
    // ---------------------------------------------------------
    // JBColor(Color Light, Color Dark)

    // Fondo principal
    public static final Color BG_MAIN = new JBColor(
            new Color(245, 247, 250),
            new Color(60, 63, 65)
    );

    // Fondo de tarjeta
    public static final Color BG_CARD = new JBColor(
            Color.WHITE,
            new Color(43, 43, 43)
    );

    // Texto
    public static final Color TEXT_MAIN = new JBColor(
            new Color(55, 65, 81),
            new Color(187, 187, 187)
    );

    public static final Color TEXT_MAIN_2 = new JBColor(
            new Color(102,102,102),
            new Color(187, 187, 187)
    );

    public static final Color TEXT_BLUE_LIGHT = new JBColor(
            new Color(120,120,120),
            new Color(187, 187, 187)
    );

    // Colores corporativos
    public static final Color BBVA_NAVY = new JBColor(
            new Color(0, 68, 129),
            new Color(80, 140, 200) // Versión más clara para que se lea sobre fondo negro
    );

    public static final Color BBVA_BLUE = new JBColor(
            new Color(20, 100, 165),
            new Color(58, 133, 209)
    );

    public static final Color BORDER_SOFT = new JBColor(
            new Color(33, 150, 243),
            new Color(70, 100, 150)
    );

    public static final Color BLUE_FOCUS =  new JBColor(
            new Color(25,118,210),
            new Color(70,100,150)
    );

    // ---------------------------------------------------------
    // FUENTES
    // ---------------------------------------------------------

    public static final Font TITLE_FONT = new Font("Lato", Font.BOLD, 20);

}