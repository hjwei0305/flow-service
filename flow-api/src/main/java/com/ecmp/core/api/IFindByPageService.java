package com.ecmp.core.api;

import com.ecmp.core.entity.BaseEntity;
import com.ecmp.core.search.PageResult;
import com.ecmp.core.search.Search;
import io.swagger.annotations.ApiOperation;

import javax.ws.rs.POST;
import javax.ws.rs.Path;

/**
 * <strong>实现功能:</strong>
 * <p>分页查询业务实体API服务接口</p>
 *
 * @param <T> BaseEntity的子类
 * @author 王锦光(wangj)
 * @version 1.0.1 2017-06-08 13:03
 */
public interface IFindByPageService<T extends BaseEntity> {
    /**
     * 分页查询业务实体
     *
     * @param search 查询参数
     * @return 分页查询结果
     */
    @POST
    @Path("findByPage")
    @ApiOperation(value = "分页查询业务实体", notes = "分页查询业务实体")
    PageResult<T> findByPage(Search search);
}
