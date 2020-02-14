package com.ecmp.flow.vo;

import java.io.Serializable;
import java.util.List;

public class ExecutorQueryParamVo implements Serializable {

    /**
     * 公司Id清单
     */
    private List<String> corpIds;

    /**
     * 岗位类别Id清单
     */
    private List<String> postCatIds;

    /**
     * 组织机构Id清单
     */
    private List<String> orgIds;

    public List<String> getCorpIds() {
        return corpIds;
    }

    public void setCorpIds(List<String> corpIds) {
        this.corpIds = corpIds;
    }

    public List<String> getPostCatIds() {
        return postCatIds;
    }

    public void setPostCatIds(List<String> postCatIds) {
        this.postCatIds = postCatIds;
    }

    public List<String> getOrgIds() {
        return orgIds;
    }

    public void setOrgIds(List<String> orgIds) {
        this.orgIds = orgIds;
    }
}
