package com.jzo2o.foundations.model.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 服务类型表
 * </p>
 *
 * @author itcast
 * @since 2024-10-30
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("serve_type")
@ApiModel(value="ServeType对象", description="服务类型表")
public class ServeType implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "服务类型id")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @ApiModelProperty(value = "服务类型编码")
    private String code;

    @ApiModelProperty(value = "服务类型名称")
    private String name;

    @ApiModelProperty(value = "服务类型图标")
    private String serveTypeIcon;

    @ApiModelProperty(value = "服务类型图片")
    private String img;

    @ApiModelProperty(value = "排序字段")
    private Integer sortNum;

    @ApiModelProperty(value = "是否启用，0草稿,1禁用，2启用")
    private Integer activeStatus;

    @ApiModelProperty(value = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @ApiModelProperty(value = "创建者")
    private Long createBy;

    @ApiModelProperty(value = "更新者")
    private Long updateBy;


}
