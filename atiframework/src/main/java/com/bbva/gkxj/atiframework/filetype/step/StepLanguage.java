package com.bbva.gkxj.atiframework.filetype.step;


import com.intellij.lang.Language;

public class StepLanguage  extends Language {

    public static final StepLanguage INSTANCE = new StepLanguage();

    private StepLanguage() {
        super("ATI Step");
    }

}