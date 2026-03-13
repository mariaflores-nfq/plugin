// Copyright 2000-2022 JetBrains s.r.o. and other contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.bbva.gkxj.atiframework.filetype.batch;

import com.intellij.lang.Language;

public class BatchLanguage extends Language {

    public static final BatchLanguage INSTANCE = new BatchLanguage();

    private BatchLanguage() {
        super("ATI Batch");
    }

}
