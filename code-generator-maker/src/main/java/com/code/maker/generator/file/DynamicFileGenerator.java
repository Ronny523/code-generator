package com.code.maker.generator.file;

import cn.hutool.core.io.FileUtil;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.*;

/**
 * 动态文件生成器
 */
public class DynamicFileGenerator {


    public static void doGenerate(String inputPath, String outputPath, Object model) throws IOException, TemplateException {
        // new 出 Configuration 对象，参数为 FreeMarker 版本号
        Configuration configuration = new Configuration(Configuration.VERSION_2_3_32);

        // 指定模板文件所在的路径
        File templateDir = new File(inputPath).getParentFile();
        configuration.setDirectoryForTemplateLoading(templateDir);

        // 设置模板文件使用的字符集
        configuration.setDefaultEncoding("utf-8");
        configuration.setNumberFormat("0.######");

        // 创建模板对象，加载指定模板
        String templateName = new File(inputPath).getName();
        Template template = configuration.getTemplate(templateName);

        // 如果outputPath文件不存在，则创建目录
        if (!FileUtil.exist(outputPath)) {
            FileUtil.touch(outputPath);
        }

        // FileWrite使用平台默认的字符集
//        Writer out = new FileWriter(outputPath);
        Writer out = new OutputStreamWriter(new FileOutputStream(outputPath), "utf-8");
        template.process(model, out);


        // 生成文件后别忘了关闭哦
        out.close();
    }
}
