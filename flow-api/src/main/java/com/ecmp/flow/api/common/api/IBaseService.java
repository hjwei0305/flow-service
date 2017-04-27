package com.ecmp.flow.api.common.api;

import com.ecmp.flow.entity.AppModule;
import com.ecmp.vo.OperateResult;
import com.ecmp.vo.OperateResultWithData;
import io.swagger.annotations.ApiOperation;
import org.springframework.data.domain.Persistable;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：工作流程服务通用基本API接口定义
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/3/31 10:49      谭军(tanjun)               新建
 * <p/>
 * *************************************************************************************************
 */
public interface IBaseService<T extends Persistable<? extends Serializable>, ID extends Serializable>  {

    /**
     * 通过Id获取实体
     * @param id
     * @return 实体
     */
    @GET
    @Path("getById")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "通过Id获取实体",notes = "测试 通过Id获取实体")
    T findOne(ID id);

    /**
     * 获取所有实体
     * @return 实体清单
     */
    @GET
    @Path("getAll")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "获取所有实体",notes = "测试 获取所有实体")
    List<T> findAll();

    /**
     * 保存一个实体
     * @param entity 实体
     * @return 保存后的实体
     */
    @POST
    @Path("save")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "保存实体",notes = "测试 保存实体")
    OperateResultWithData<T> save(T entity);

//    /**
//     * 删除一个实体
//     * @param entity 实体
//     * @return 删除后的返回信息
//     */
//    @DELETE
//    @Path("delete")
//    @Produces(MediaType.APPLICATION_JSON)
//    @Consumes(MediaType.APPLICATION_JSON)
//    @ApiOperation(value = "删除实体",notes = "测试 删除实体")
//    public OperateResult delete(T entity);
//
//    /**
//     * 批量删除实体
//     * @param entities
//     */
//    @DELETE
//    @Path("deletes")
//    @Produces(MediaType.APPLICATION_JSON)
//    @Consumes(MediaType.APPLICATION_JSON)
//    @ApiOperation(value = "删除实体",notes = "测试 删除实体")
//    public void delete(Iterable<T> entities);

    /**
     * 通过ID删除实体
     * @param id
     * @return 删除后的返回信息
     */
    @DELETE
    @Path("deleteById")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "通过ID删除实体",notes = "测试 通过ID删除实体")
    public OperateResult delete(ID id);

    /**
     * 通过ID集合批量删除实体
     * @param ids
     */
    @DELETE
    @Path("deleteByIds")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "通过ID集合删除实体集",notes = "测试 通过ID集合删除实体集")
    public void delete(Collection<ID> ids);
}
