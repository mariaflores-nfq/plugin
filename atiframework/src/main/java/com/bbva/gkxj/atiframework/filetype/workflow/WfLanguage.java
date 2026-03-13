package com.bbva.gkxj.atiframework.filetype.workflow;

import com.intellij.lang.Language;

public class WfLanguage extends Language {

    public static final com.bbva.gkxj.atiframework.filetype.workflow.WfLanguage INSTANCE = new com.bbva.gkxj.atiframework.filetype.workflow.WfLanguage();

    private WfLanguage() {
        super("workflow");
    }

}