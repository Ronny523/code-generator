package com.code.maker.model;

import lombok.Data;

/**
 * 静态模板配置
 */
@Data
public class DataModel {

    // 在代码开头增加作者 @Author 注释（增加代码）
    // 修改程序输出的信息提示（替换代码）
    // 将循环读取输入改为单次读取（可选代码）

    /**
     * 是否生成 _gitignore文件
     */
    public boolean needGit = true;

    /**
     * 是否生成循环
     */
    public boolean loop = false;

    /**
     * 核心模板
     */
    public String author = "yupi";

    public String outputText = "sum = ";
}
