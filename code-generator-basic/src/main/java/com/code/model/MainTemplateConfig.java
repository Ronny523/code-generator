package com.code.model;

import lombok.Data;

/**
 * 静态模板配置
 */
@Data
public class MainTemplateConfig {

    // 在代码开头增加作者 @Author 注释（增加代码）
    // 修改程序输出的信息提示（替换代码）
    // 将循环读取输入改为单次读取（可选代码）

    /**
     * 作者（字符串，填充值）
     */
    private String author = "coder";

    /**
     * 输出信息
     */
    private String outputText = "输出结果";

    /**
     * 是否循环（开关）
     */
    private boolean loop;
}
