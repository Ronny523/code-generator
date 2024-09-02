package com.code.maker.generator.main;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ClassPathResource;
import cn.hutool.core.util.StrUtil;
import com.code.maker.generator.JarGenerator;
import com.code.maker.generator.ScriptGenerator;
import com.code.maker.generator.file.DynamicFileGenerator;
import com.code.maker.meta.Meta;
import com.code.maker.meta.MetaManager;
import freemarker.template.TemplateException;

import java.io.File;
import java.io.IOException;

public class MainGenerator extends GenerateTemplate{

    @Override
    protected void buildDist(String outputPath, String sourceCopyDestPath, String jarPath, String shellOutputFilePath) {
        System.out.println("不用输出dist");
    }


}
