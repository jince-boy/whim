package com.whim.system.model.vo.dict;

import com.whim.system.model.entity.SysDictType;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.apache.fesod.sheet.annotation.ExcelIgnoreUnannotated;
import org.apache.fesod.sheet.annotation.ExcelProperty;
import org.apache.fesod.sheet.annotation.format.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * @author Jince
 * @date 2026/10/25
 * @description 字典类型导出列，不包含删除标识及操作者信息。
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = SysDictType.class, convertGenerate = false)
public class SysDictTypeExportVO {
    /** 长整型主键由 Excel 模块按文本写入，防止精度丢失。 */
    @ExcelProperty(value = "字典类型ID", index = 0)
    private Long id;
    @ExcelProperty(value = "字典名称", index = 1)
    private String name;
    @ExcelProperty(value = "字典类型", index = 2)
    private String type;
    @ExcelProperty(value = "备注", index = 3)
    private String remark;
    @ExcelProperty(value = "创建时间", index = 4)
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    @ExcelProperty(value = "更新时间", index = 5)
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
