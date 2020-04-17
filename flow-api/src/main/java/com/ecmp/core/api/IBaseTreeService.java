package com.ecmp.core.api;

import com.ecmp.core.entity.BaseEntity;
import com.ecmp.vo.OperateResult;
import io.swagger.annotations.ApiOperation;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import java.util.List;

/**
 * <strong>实现功能:</strong>
 * <p>树形业务实体的API服务接口</p>
 *
 * @param <T> BaseEntity的子类
 * @author 王锦光(wangj)
 * @version 1.0.1 2017-06-21 14:23
 */
public interface IBaseTreeService<T extends BaseEntity> extends IBaseEntityService<T> {
    /**
     * 移动一个节点
     *
     * @param nodeId         节点Id
     * @param targetParentId 目标父节点Id
     * @return 操作状态
     */
    @POST
    @Path("move")
    @ApiOperation(value = "移动节点", notes = "移动一个节点")
    OperateResult move(@QueryParam("nodeId") String nodeId, @QueryParam("targetParentId") String targetParentId);

    /**
     * 获取所有根节点
     *
     * @return 根节点清单
     */
    @GET
    @Path("getAllRootNode")
    @ApiOperation(value = "获取所有根节点", notes = "获取所有根节点")
    List<T> getAllRootNode();

    /**
     * 获取一个节点的树
     *
     * @param nodeId 节点Id
     * @return 节点树
     */
    @GET
    @Path("getTree")
    @ApiOperation(value = "获取一个节点的树", notes = "获取一个节点的树")
    T getTree(@QueryParam("nodeId") String nodeId);

    /**
     * 获取一个节点的所有子节点
     *
     * @param nodeId      节点Id
     * @param includeSelf 是否包含本节点
     * @return 子节点清单
     */
    @GET
    @Path("getChildrenNodes")
    @ApiOperation(value = "获取一个节点的所有子节点", notes = "获取一个节点的所有子节点,可以包含本节点")
    List<T> getChildrenNodes(@QueryParam("nodeId") String nodeId, @QueryParam("includeSelf") boolean includeSelf);

    /**
     * 获取一个节点的所有父节点
     *
     * @param nodeId      节点Id
     * @param includeSelf 是否包含本节点
     * @return 父节点清单
     */
    @GET
    @Path("getParentNodes")
    @ApiOperation(value = "获取一个节点的所有父节点", notes = "获取一个节点的所有父节点,可以包含本节点")
    List<T> getParentNodes(@QueryParam("nodeId") String nodeId, @QueryParam("includeSelf") boolean includeSelf);
}
