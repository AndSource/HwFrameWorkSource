package com.huawei.nb.ai;

import com.huawei.nb.model.aimodel.AiModel;
import java.util.function.Consumer;

final /* synthetic */ class AiModelAttributes$$Lambda$56 implements Consumer {
    private final AiModel arg$1;

    private AiModelAttributes$$Lambda$56(AiModel aiModel) {
        this.arg$1 = aiModel;
    }

    static Consumer get$Lambda(AiModel aiModel) {
        return new AiModelAttributes$$Lambda$56(aiModel);
    }

    public void accept(Object obj) {
        this.arg$1.setDescription((String) obj);
    }
}
