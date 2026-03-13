package com.bbva.gkxj.atiframework.filetype.scheduler.editor.panels;

import com.bbva.gkxj.atiframework.filetype.scheduler.utils.ActionPanel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

/**
 * Panel principal para la gestión de la sección "Action" (Batch y Queries) del Scheduler.
 *
 * Extiende {@link ActionPanel} y proporciona la clave raíz JSON utilizada
 * para serializar/deserializar la sección de errores de tipo "batch".
 */
public class SchedulerActionPanel extends ActionPanel {

    /**
     * Crea una instancia del Action on Error del scheduler.
     *
     * @param project el proyecto de IntelliJ asociado al panel
     * @param file el archivo virtual que representa el recurso abierto
     */
    public SchedulerActionPanel(@NotNull Project project, @NotNull VirtualFile file) {
        super(project, file);
    }

    /**
     * Obtiene la clave raíz JSON para este panel.
     *
     * @return la clave raíz JSON utilizada por el panel ("batch")
     */
    @Override
    protected String getJsonRootKey() {
        return "batch";
    }
}