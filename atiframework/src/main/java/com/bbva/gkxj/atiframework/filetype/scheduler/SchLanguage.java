// Copyright 2000-2022 JetBrains s.r.o. and other contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.bbva.gkxj.atiframework.filetype.scheduler;

import com.intellij.lang.Language;

public class SchLanguage extends Language {

    public static final SchLanguage INSTANCE = new SchLanguage();

    private SchLanguage() {
        super("ATI Scheduler");
    }

}
