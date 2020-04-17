package com.ecmp.core.api;

import com.ecmp.core.entity.BaseEntity;
import io.swagger.annotations.ApiOperation;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.util.List;

/**
 * <strong>实现功能:</strong>
 * <p>获取所有业务实体API服务接口</p>
 *
 * @param <T> BaseEntity的子类
 * @author 王锦光(wangj)
 * @version 1.0.1 2017-06-08 10:42
 */
public interface IFindAllService<T extends BaseEntity> {
    /**
     * 获取所有业务实体
     *
     * @return 业务实体清单
     */
    @GET
    @Path("findAll")
    @ApiOperation(value = "获取所有业务实体", notes = "获取所有业务实体")
    List<T> findAll();

    /**
     * 获取所有未冻结的业务实体
     *
     * @return 业务实体清单
     */
    @GET
    @Path("findAllUnfrozen")
    @ApiOperation(value = "获取所有未冻结业务实体", notes = "获取所有未冻结业务实体(适用与实现了冻结接口的业务实体)")
    List<T> findAllUnfrozen();
}
