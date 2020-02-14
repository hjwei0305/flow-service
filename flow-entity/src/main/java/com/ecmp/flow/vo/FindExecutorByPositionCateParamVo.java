package com.ecmp.flow.vo;

import java.io.Serializable;
import java.util.List;

public class FindExecutorByPositionCateParamVo  implements Serializable {

        /**
         * 岗位类别Id清单
         */
        private List<String> postCatIds;

        /**
         * 组织机构Id
         */
        private String orgId;


        public List<String> getPostCatIds() {
            return postCatIds;
        }

        public void setPostCatIds(List<String> postCatIds) {
            this.postCatIds = postCatIds;
        }

        public String getOrgId() {
            return orgId;
        }

        public void setOrgId(String orgId) {
            this.orgId = orgId;
        }
}
