package com.code.maker.template.model;

import lombok.Data;

@Data
public class TemplateMakerOutputConfig {

    // 从未文组的文件中，移除和组内同名的文件
    private boolean removeGroupFilesFromRoot = true;
}
