package com.code.maker.generator.file;

import com.code.maker.model.DataModel;
import freemarker.template.TemplateException;

import java.io.File;
import java.io.IOException;

public class FileGenerator {

    /**
     * 生成
     *
     * @param model 数据模型
     * @throws TemplateException
     * @throws IOException
     */
    public static void doGenerate(Object model) throws TemplateException, IOException {
//        String projectPath = System.getProperty("user.dir");
//        // 整个项目的根路径
//        File parentFile = new File(projectPath).getParentFile();
//        // 输入路径
//        String inputPath = new File(parentFile, "code-generator-demo-projects/acm-template").getAbsolutePath();
//        String outputPath = projectPath;
//        // 生成静态文件
//        StaticFileGenerator.copyFilesByhutool(inputPath, outputPath);
//        // 生成动态文件
//        String inputDynamicFilePath = projectPath + File.separator + "src/main/resources/templates/MainTemplate.java.ftl";
//        String outputDynamicFilePath = outputPath + File.separator + "acm-template/src/com/yupi/acm/MainTemplate.java";
//        DynamicFileGenerator.doGenerate(inputDynamicFilePath, outputDynamicFilePath, model);


        String inputRootPath = "E:\\code\\code-generator\\code-generator-demo-projects\\acm-template-pro";
        String outputRootPath = "E:\\code\\code-generator\\acm-template-pro";

        String inputPath;
        String outputPath;

        inputPath = new File(inputRootPath, "src/com/yupi/acm/MainTemplate.java.ftl").getAbsolutePath();
        outputPath = new File(outputRootPath, "src/com/yupi/acm/MainTemplate.java").getAbsolutePath();
        DynamicFileGenerator.doGenerate(inputPath, outputPath, model);

        inputPath = new File(inputRootPath, ".gitignore").getAbsolutePath();
        outputPath = new File(outputRootPath, ".gitignore").getAbsolutePath();
        StaticFileGenerator.copyFilesByhutool(inputPath, outputPath);

        inputPath = new File(inputRootPath, "README.md").getAbsolutePath();
        outputPath = new File(outputRootPath, "README.md").getAbsolutePath();
        StaticFileGenerator.copyFilesByhutool(inputPath, outputPath);
    }

    public static void main(String[] args) throws TemplateException, IOException {
        DataModel dataModel = new DataModel();
        dataModel.setAuthor("coder");
        dataModel.setLoop(false);
        dataModel.setOutputText("求和结果：");
        doGenerate(dataModel);
    }
}
