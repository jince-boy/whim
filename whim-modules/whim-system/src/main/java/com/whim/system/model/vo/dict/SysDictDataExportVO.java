package com.whim.system.model.vo.dict;

import com.whim.excel.annotation.ExcelDictFormat;
import com.whim.excel.convert.ExcelMappingConverter;
import com.whim.system.model.entity.SysDictData;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.apache.fesod.sheet.annotation.ExcelIgnoreUnannotated;
import org.apache.fesod.sheet.annotation.ExcelProperty;
import org.apache.fesod.sheet.annotation.format.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * @author Jince
 * @date 2026/10/25
 * @description 字典数据导出列，保留键值文本并转换回显样式。
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = SysDictData.class, convertGenerate = false)
public class SysDictDataExportVO {
    /** 长整型主键由 Excel 模块按文本写入，防止精度丢失。 */
    @ExcelProperty(value = "字典数据ID", index = 0)
    private Long id;
    @ExcelProperty(value = "字典类型", index = 1)
    private String type;
    @ExcelProperty(value = "字典标签", index = 2)
    private String label;
    /** 使用字符串保留前导零和长数字字典值。 */
    @ExcelProperty(value = "字典键值", index = 3)
    private String value;
    @ExcelProperty(value = "回显样式", index = 4, converter = ExcelMappingConverter.class)
    @ExcelDictFormat(readConverterExp = "default=默认,primary=主要,info=信息,success=成功,warning=警告,error=错误", dropdown = false)
    private String listClass;
    @ExcelProperty(value = "排序", index = 5)
    private Integer sort;
    @ExcelProperty(value = "备注", index = 6)
    private String remark;
    @ExcelProperty(value = "创建时间", index = 7)
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    @ExcelProperty(value = "更新时间", index = 8)
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
