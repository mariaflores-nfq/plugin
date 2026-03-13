// Copyright 2000-2022 JetBrains s.r.o. and other contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package icons;

import com.intellij.openapi.util.IconLoader;
import com.intellij.util.IconUtil;
import com.intellij.util.ui.JBUI;

import javax.swing.*;

public class AtiIcons {

    public static final Icon ATI_default_icon = IconLoader.getIcon("/icons/ati_16.svg", AtiIcons.class);

    public static final Icon ATI_SCH_FILE_ICON = IconLoader.getIcon("/icons/ati_sch.svg", AtiIcons.class);

    public static final Icon ATI_BATCH_FILE_ICON = IconLoader.getIcon("/icons/ati_batch.svg", AtiIcons.class);

    public static final Icon CALENDAR_ICON = IconLoader.getIcon("/icons/calendarIcon.gif", AtiIcons.class);

    public static final Icon TRASH_ICON = IconLoader.getIcon("/icons/delete.svg", AtiIcons.class);

    public static final Icon GLOBAL_CONNECT_TOOL_ICON = IconLoader.getIcon("/icons/global_connect_tool.svg", AtiIcons.class);

    public static final Icon CREATE_ETL_STEP_ICON = IconLoader.getIcon("/icons/etl_step.svg", AtiIcons.class);

    public static final Icon CREATE_INCLUSIVE_GATEWAY_ICON = IconLoader.getIcon("/icons/inclusive_gateway.svg", AtiIcons.class);

    public static final Icon CREATE_EXCLUSIVE_GATEWAY_ICON = IconLoader.getIcon("/icons/exclusive_gateway.svg", AtiIcons.class);

    public static final Icon END_EVENT_ICON = IconLoader.getIcon("/icons/end_event.svg", AtiIcons.class);

    public static final Icon HAND_TOOL_ICON = IconLoader.getIcon("/icons/hand_tool.svg", AtiIcons.class);

    public static final Icon LASSO_TOOL_ICON = IconLoader.getIcon("/icons/lasso_tool.svg", AtiIcons.class);

    public static final Icon ADD_ICON = IconLoader.getIcon("/icons/plus.svg", AtiIcons.class);

    public static final Icon WRENCH_CONFIGURATION_ICON = IconLoader.getIcon("/icons/wrench_configuration.svg", AtiIcons.class);

    public static final Icon CREATE_BATCH_STEP_ICON = IconLoader.getIcon("/icons/batch_step.svg", AtiIcons.class);

    public static final Icon CREATE_LOOP_GATEWAY_ICON = IconLoader.getIcon("/icons/loop_gateway.svg", AtiIcons.class);

    public static final Icon CREATE_REMOVE_SPACE_TOOL_ICON = IconLoader.getIcon("/icons/create_remove_space_tool.svg", AtiIcons.class);

    public static final Icon CREATE_PARALLEL_GATEWAY_ICON = IconLoader.getIcon("/icons/parallel_gateway.svg", AtiIcons.class);

    public static final Icon START_EVENT_ICON = IconLoader.getIcon("/icons/start_event.svg", AtiIcons.class);

    public static final Icon REMOVE_ICON = IconLoader.getIcon("/icons/remove.svg", AtiIcons.class);

    public static final Icon BATCH_ICON = IconLoader.getIcon("/icons/batch_icon.svg", AtiIcons.class);

    public static final Icon ETL_ICON = IconLoader.getIcon("/icons/etl_icon.svg", AtiIcons.class);

    public static final Icon OUTPUT_ICON = IconLoader.getIcon("/icons/output.svg", AtiIcons.class);

    public static final Icon INPUT_ICON = IconLoader.getIcon("/icons/input.svg", AtiIcons.class);

    public static final Icon SUBWORKFLOW_ICON = IconLoader.getIcon("/icons/subWorkflowBox.svg", AtiIcons.class);

    public static final Icon FILTER_ICON = IconLoader.getIcon("/icons/filterBox.svg", AtiIcons.class);

    public static final Icon ENRICHER_ICON = IconLoader.getIcon("/icons/enricherBox.svg", AtiIcons.class);

    public static final Icon ROUTER_ICON = IconLoader.getIcon("/icons/routerBox.svg", AtiIcons.class);

    public static final Icon SPLITTER_ICON = IconLoader.getIcon("/icons/splitterBox.svg", AtiIcons.class);

    public static final Icon AGGREGATOR_ICON = IconLoader.getIcon("/icons/agregatorBox.svg", AtiIcons.class);

    public static final Icon DISCARD_EDGE_ICON = IconLoader.getIcon("/icons/discard node.svg", AtiIcons.class);

    public static final Icon QUEUE_EDGE_ICON = IconLoader.getIcon("/icons/queue edge.svg", AtiIcons.class);

    public static final Icon SUBWORKFLOW_GRAPH_ICON = IconLoader.getIcon("/icons/subWorkflow.svg", AtiIcons.class);

    public static final Icon FILTER_GRAPH_ICON = IconLoader.getIcon("/icons/Funnel.svg", AtiIcons.class);

    public static final Icon ENRICHER_GRAPH_ICON = IconLoader.getIcon("/icons/Enricher.svg", AtiIcons.class);

    public static final Icon ROUTER_GRAPH_ICON = IconLoader.getIcon("/icons/Router.svg", AtiIcons.class);

    public static final Icon SPLITTER_GRAPH_ICON = IconLoader.getIcon("/icons/Splitter.svg", AtiIcons.class);

    public static final Icon AGGREGATOR_GRAPH_ICON = IconLoader.getIcon("/icons/Agregator.svg", AtiIcons.class);

    public static Icon getScaledIcon(Icon icon, int size) {
        if (icon == null) return null;
        float scale = JBUI.pixScale((float) size / icon.getIconWidth());
        return IconUtil.scale(icon, null, scale);
    }
}