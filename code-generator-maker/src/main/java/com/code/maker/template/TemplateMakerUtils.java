package com.code.maker.template;

import cn.hutool.core.util.StrUtil;
import com.code.maker.meta.Meta;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 模板制作工具类
 */
public class TemplateMakerUtils {

    /**
     * 从未文组的文件中，移除和组内同名的文件
     *
     * @param fileInfoList
     * @return
     */
    public static List<Meta.FileConfigDTO.FileInfo> removeGroupFilesFormRoot(List<Meta.FileConfigDTO.FileInfo> fileInfoList) {

        // 先获取所有分组
        List<Meta.FileConfigDTO.FileInfo> groupFileInfoList = fileInfoList.stream()
                .filter(fileInfo -> StrUtil.isNotBlank(fileInfo.getGroupKey()))
                .collect(Collectors.toList());

        // 获取所有分组内的文件列表（用flatMap展平，为一个流，再收集到一个列表中）
        List<Meta.FileConfigDTO.FileInfo> groupInnerFileInfoList = groupFileInfoList.stream()
                .flatMap(fileInfo -> fileInfo.getFiles().stream())
                .collect(Collectors.toList());

        // 获取所有分组内文件的输入路径集合（用输入路径判断文件是否重复）
        Set<String> fileInputPathSet = groupInnerFileInfoList.stream()
                .map(Meta.FileConfigDTO.FileInfo::getInputPath)
                .collect(Collectors.toSet());

        // 移除所有和集合内重名的外层文件
        return fileInfoList.stream()
                .filter(fileInfo -> !fileInputPathSet.contains(fileInfo.getInputPath()))
                .collect(Collectors.toList());
    }
}
