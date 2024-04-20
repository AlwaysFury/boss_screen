package com.boss.bossscreen.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 逻辑删除
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateStatusDTO {


    /**
     * id列表
     */
    @NotNull(message = "id不能为空")
    private List<Integer> idList;

    /**
     * 状态值
     */
    @NotNull(message = "状态值不能为空")
    private Integer status;

}
