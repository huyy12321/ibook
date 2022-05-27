package com.hyy.ibook.service.analysis;

import com.hyy.ibook.entity.Channel;

/**
 * @author huyangyang
 */
public abstract class AnalysisAbstract {

    /**
     * 解析搜索记录
     * @param keyword 关键字
     */
    public abstract void analysisSearch(String keyword);

    /**
     * 解析书本详情/目录
     * @param id 书籍id
     */
    public abstract void analysisList(String id);

    /**
     * 解析章节详情
     * @param id 章节id
     */
    public abstract void analysisInfo(String id);

}
