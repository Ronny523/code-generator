package com.code.maker.meta;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.code.maker.meta.enums.FileGenerateTypeEnum;
import com.code.maker.meta.enums.FileTypeEnum;
import com.code.maker.meta.enums.ModelTypeEnum;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;

public class MetaValidator {

    public static void doValidAndFill(Meta meta) {
        // 基础信息校验 和 默认值
        validAndFillMetaRoot(meta);

        // fileConfig 校验 和 默认值
        validAndFillFileConfig(meta);

        // modelConfig 校验 和 默认值
        validAndFillModelConfig(meta);

    }

    private static void validAndFillModelConfig(Meta meta) {
        Meta.ModelConfigDTO modelConfig = meta.getModelConfig();
        if (modelConfig == null) {
            return;
        }
        List<Meta.ModelConfigDTO.ModelInfo> modelInfoList = modelConfig.getModels();
        if (CollUtil.isEmpty(modelInfoList)) {
            return;
        }
        for (Meta.ModelConfigDTO.ModelInfo modelInfo : modelInfoList) {
            // 输出路径默认值
            String fieldName = modelInfo.getFieldName();
            if (StrUtil.isBlank(fieldName)) {
                throw new MetaException("未填写 fieldName");
            }

            String modelInfoType = modelInfo.getType();
            if (StrUtil.isEmpty(modelInfoType)) {
                modelInfo.setType(ModelTypeEnum.STRING.getValue());
            }
        }
    }

    private static void validAndFillFileConfig(Meta meta) {
        Meta.FileConfigDTO fileConfig = meta.getFileConfig();
        if (fileConfig == null) {
           return;
        }
        // sourceRootPath 必填
        String sourceRootPath = fileConfig.getSourceRootPath();
        if (StrUtil.isBlank(sourceRootPath)) {
            throw new MetaException("未填写 sourceRootPath");
        }

        // inputRootPath: .source + sourceRootPath 的最后一个层级路径
        String inputRootPath = fileConfig.getInputRootPath();
        String defaultInputRootPath = ".source/" +
                FileUtil.getLastPathEle(Paths.get(sourceRootPath)).getFileName().toString();
        if (StrUtil.isEmpty(inputRootPath)) {
            fileConfig.setInputRootPath(defaultInputRootPath);
        }

        // outputRootPath: 默认为当前路径下的 generated 路径
        String outputRootPath = fileConfig.getOutputRootPath();
        String defaultOutputRootPath = "generated";
        if (StrUtil.isEmpty(outputRootPath)) {
            fileConfig.setOutputRootPath(defaultOutputRootPath);
        }

        String fileConfigType = fileConfig.getType();
        String defaultType = FileTypeEnum.DIR.getValue();
        if (StrUtil.isEmpty(fileConfigType)) {
            fileConfig.setType(defaultType);
        }

        List<Meta.FileConfigDTO.FileInfo> fileInfoList = fileConfig.getFiles();
        if (CollUtil.isEmpty(fileInfoList)) {
            return;
        }
        for (Meta.FileConfigDTO.FileInfo fileInfo : fileInfoList) {
            // inputPath 必填
            String inputPath = fileInfo.getInputPath();
            if (StrUtil.isBlank(inputPath)) {
                throw new MetaException("未填写：inputPath");
            }

            // outputPath: 默认等于 inputPath
            String outputPath = fileInfo.getOutputPath();
            if (StrUtil.isEmpty(outputPath)) {
                fileInfo.setOutputPath(inputPath);
            }

            // type: 默认 inputPath 有文件后缀（比如 .java）默认为file，否则为dir
            String type = fileInfo.getType();
            if (StrUtil.isBlank(type)) {
                if (StrUtil.isBlank(FileUtil.getSuffix(inputPath))) {
                    fileInfo.setType(FileTypeEnum.DIR.getValue());
                } else {
                    fileInfo.setType(FileTypeEnum.FILE.getValue());
                }
            }

            // generateType： 文件结尾不为ftl，generateType 为 static
            String generateType = fileInfo.getGenerateType();
            if (StrUtil.isBlank(generateType)) {
                if (inputPath.endsWith(".ftl")) {
                    fileInfo.setGenerateType(FileGenerateTypeEnum.DYNAMIC.getValue());
                } else {
                    fileInfo.setGenerateType(FileGenerateTypeEnum.STATIC.getValue());
                }
            }
        }
    }

    private static void validAndFillMetaRoot(Meta meta) {

        String name = StrUtil.blankToDefault(meta.getName(), "my-generator");
        String description = StrUtil.emptyToDefault(meta.getDescription(), "我的模板代码生成器");
        String author = StrUtil.emptyToDefault(meta.getAuthor(), "coder");
        String basePackage = StrUtil.blankToDefault(meta.getBasePackage(), "com.code");
        String version = StrUtil.emptyToDefault(meta.getVersion(), "1.0");
        String createTime = StrUtil.emptyToDefault(meta.getCreateTime(), DateUtil.now());

        meta.setName(name);
        meta.setDescription(description);
        meta.setAuthor(author);
        meta.setBasePackage(basePackage);
        meta.setVersion(version);
        meta.setCreateTime(createTime);
    }
}
