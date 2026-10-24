# whim-excel

基于 Apache Fesod `2.1.0-incubating` 的 Excel 框架模块，适用于 JDK 25、Spring Boot 4.1。

模块已加入框架聚合、根依赖管理、`whim-system` 和 `whim-start`。`ExcelAutoConfiguration` 自动注册 `ExcelService`。应用层的 `ExcelDictionaryConfiguration` 将系统字典服务接入 Excel，框架不依赖系统模块、认证实现或持久化实现。

## 能力

| 场景 | 支持方式 |
| --- | --- |
| 普通导出、空白填写模板 | `ExcelBuilder.of(rows, Row.class)` |
| 分页查询与有界批量写入 | `writer(Row.class).pages(provider)` |
| 游标查询与自动推进 | `writer(Row.class).cursor(provider)` |
| 自动拆分 Sheet | `rowsPerSheet(...)` |
| ZIP 分片下载 | `zip(rowsPerFile)`，直接写入 ZIP 条目 |
| 多 Sheet、不同数据模型 | `toStream/toResponse` 的写入会话回调 |
| 导入校验 | Jakarta Validation、校验分组、严格表头检查 |
| 容错导入 | `failFast(false)` 与结构化错误回执 |
| 分批、逐行导入 | `batch(...)`、`listener(...)` |
| 精度保护 | Long、BigInteger、BigDecimal 自动处理 |
| 字典、枚举 | 双向转换、自动下拉、未知值报错 |
| 动态、级联下拉 | Spring 选项提供者、`DropDownOptions` |
| 多级表头、列筛选与排序 | Fesod 注解和构造器配置 |
| 表头样式 | 必填标红、填写批注、冻结表头 |
| 相邻行合并 | `@CellMerge`，支持依赖字段和跨批次合并 |
| 模板填充 | 普通变量、单列表、命名多列表、多 Sheet、横向填充 |
| 扩展 | 自定义转换器、处理器工厂、逐表配置 |

## 定义 Excel 模型

Excel 模型应与持久化 Entity 分离。可按业务需要拆分导入 DTO 与导出 VO。

```java
package com.whim.system.model.vo.excel;

import com.whim.excel.annotation.CellMerge;
import com.whim.excel.annotation.ExcelDictFormat;
import com.whim.excel.annotation.ExcelNotation;
import com.whim.excel.annotation.ExcelRequired;
import com.whim.excel.convert.ExcelMappingConverter;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.apache.fesod.sheet.annotation.ExcelIgnoreUnannotated;
import org.apache.fesod.sheet.annotation.ExcelProperty;

/**
 * @author Jince
 * @date 2026/10/25
 * @description 用户 Excel 数据模型示例。
 */
@Data
@ExcelIgnoreUnannotated
public class UserExcelRow {
    @ExcelProperty("用户编号")
    private Long userId;

    @ExcelProperty("用户名")
    @ExcelRequired
    @ExcelNotation("填写唯一用户名")
    @NotBlank(message = "用户名不能为空")
    private String username;

    @ExcelProperty("部门")
    @CellMerge
    private String department;

    @ExcelProperty(value = "状态", converter = ExcelMappingConverter.class)
    @ExcelDictFormat(readConverterExp = "0=停用,1=启用")
    @NotNull(message = "状态不能为空")
    private Integer status;
}
```

`@ExcelRequired` 负责表头标红，实际导入校验使用 `@NotBlank`、`@NotNull` 等 Jakarta Validation 注解。`@ExcelDictFormat` 和 `@ExcelEnumFormat` 负责描述映射及生成下拉，**双向转换还需在 `@ExcelProperty` 中指定 `ExcelMappingConverter.class`**。

`@ExcelIgnoreUnannotated` 能避免无意导出没有声明的内部字段。日期、图片、数字格式、行高等原生功能可以继续使用 Fesod 注解和转换器。

## 普通导出与模板下载

使用静态门面：

```java
ExcelBuilder.of(rows, UserExcelRow.class).sheetName("用户数据").toResponse(response);
```

需要指定文件名、列或样式时再追加配置：

```java
ExcelBuilder.of(rows, UserExcelRow.class)
        .sheetName("用户数据")
        .filename("用户清单")
        .includeFields(List.of("username", "department", "status"))
        .orderByIncludeColumn(true)
        .columnWidth(24)
        .rowHeight((short) 28, (short) 22)
        .toResponse(response);

// 空列表仍生成表头、批注和下拉，可直接作为填写模板。
ExcelBuilder.of(List.of(), UserExcelRow.class)
        .sheetName("用户导入模板")
        .dropdownRows(5_000)
        .toResponse(response);
```

入口为 `com.whim.excel.utils.ExcelBuilder`，业务代码直接链式调用，无需注入 Excel 服务或手动设置下载响应。框架内部的 `ExcelService` 由自动配置管理，需要依赖注入的场景也可以使用它。

可调用 `toStream(output)`、`toFile(path)` 或 `toBytes()`。`toBytes()` 会保留整个文件，仅用于小文件。`toFile` 会覆盖已存在文件，不自动创建父目录。

## 分批查询、自动分表与 ZIP

字典导出一次查询全部匹配数据，直接传入列表：

```java
List<SysDictDataExportVO> list = dictService.getDictDataExportList(query);
ExcelBuilder.of(list, SysDictDataExportVO.class).sheetName("字典数据").toResponse(response);
```

字典导出不使用分页或游标，也不设置导出总数量上限。模块仍会在超过单个工作表容量时自动分表。

其他业务需要游标查询时，可以使用 `writer(Row.class).cursor(provider)`。`cursor` 首次传入 `null`，后续自动传入上一批最后一条记录，返回空列表后结束；不能返回 `null`。回调只负责查询，不需要管理输出流或保存游标状态。

已有按页查询方法时，使用 `pages`：

```java
ExcelBuilder.writer(UserExcelRow.class)
        .sheetName("用户数据")
        .pages(pageNumber -> userExportService.getUserExportPage(pageNumber, 2_000))
        .batchSize(1_000)
        .rowsPerSheet(100_000)
        .dropdown(false)
        .toResponse(response);

ExcelBuilder.writer(UserExcelRow.class)
        .pages(pageNumber -> userExportService.getUserExportPage(pageNumber, 2_000))
        .zip(50_000)
        .filename("用户数据分片")
        .dropdown(false)
        .toResponse(response);
```

`pages` 的页码从 **1** 开始，每次需要数据时才调用，返回空集合表示结束，不能返回 `null`。分页服务由业务层提供，必须保持稳定排序，并在有并发变更的场景选择合适的快照或游标策略。数据权限也由业务查询负责。

默认最多每张 Sheet 写入 1,000,000 条数据，超出后自动拆表，后续名称追加 `_2`、`_3`。ZIP 中每个 Excel 直接写入压缩流，无整文件字节数组中转；空数据或只有一个分片时仍返回 ZIP，附件类型不会随数据量改变。

`of(完整集合, ...)` 本身不能减少查询时的内存占用，大数据应使用 `cursor`、`pages` 或写入会话。合并区域、下拉数据以及底层文件元数据仍占用内存，复杂报表应合理设置分片大小。

## 多张工作表与自定义配置

```java
ExcelBuilder.writer(UserExcelRow.class).toResponse(response, workbook -> {
    workbook.configureSheet("用户", UserExcelRow.class,
            sheet -> sheet.includeColumnFieldNames(List.of("username", "status")));
    workbook.write("用户", UserExcelRow.class, firstUserBatch);
    workbook.write("用户", UserExcelRow.class, secondUserBatch);
    workbook.write("部门", DepartmentExcelRow.class, departmentRows);
});
```

`workbook.write(rows)` 追加到构造器设置的默认表。按名称写入时，同名表必须使用同一模型；`configureSheet` 只能在该表首次写入前调用。表名和表编号由会话管理，不应通过回调修改。会话只能在回调所在的线程使用，回调结束后即失效。

自定义会话不会自动分表，调用者负责控制单表行数。自动分表只应用于普通集合和分页导出终端。

## 导入、校验与错误回执

```java
try (InputStream input = file.getInputStream()) {
    ExcelResult<UserExcelRow> result = ExcelBuilder.read(input, UserExcelRow.class)
            .failFast(false)
            .maxErrors(100)
            .maxRows(50_000)
            .doRead();

    List<UserExcelRow> validRows = result.getList();
    List<ExcelError> errors = result.getErrors();
    // 业务服务决定：全部通过后写入，或明确允许部分成功。
}
```

- 默认校验表头和字段，遇到第一条错误立即终止。
- `failFast(false)` 允许收集错误，达到 `maxErrors` 后仍会终止，避免错误列表无限增长。
- `ExcelError` 包含工作表名、从 1 开始的行号、可定位时的列号和字段名、中文错误信息，不混入 HTML。
- `ExcelResult` 统计成功及失败的**行数**。字段校验中同一行多个错误会合并为该行的一条描述。
- `ExcelImportException` 继承 `IllegalArgumentException`，沿用现有 HTTP 400 映射；业务代码也可通过 `getResult()` 获取中止前的回执。
- `maxRows` 是整次操作的数据行上限，跨表累计；超限报错，不静默截断。
- `.validate(false)` 关闭字段校验，`.checkHead(false)` 单独关闭表头检查，`.validationGroups(...)` 设置校验分组。
- `headRowNumber` 默认根据模型推断，可用 `.headRowNumber(2)` 显式设置；为零时按列索引读取，不执行标题检查。
- `doReadAll()` 读取所有可见表，隐藏的下拉数据表会跳过。不同业务模型的 Sheet 应分别指定名称或编号读取。
- `doReadSync()` 和 `doReadAllSync()` 也执行完全相同的校验流程；存在任何错误时抛出包含回执的异常，避免只返回成功列表而丢失错误信息。
- 按名称映射时，末级标题必须唯一；同名末级标题应使用 `@ExcelProperty(index = ...)` 明确位置。

默认只读取第一张可见表，可以使用 `sheetName` 或从零开始的 `sheetNo` 指定；同时设置时要求两者匹配。通过 `password` 读取加密文件。

## 分批导入与逐行回调

```java
try (InputStream input = file.getInputStream()) {
    ExcelResult<UserExcelRow> result = ExcelBuilder.read(input, UserExcelRow.class)
            .batch(500, userImportService::addUsers)
            .doRead();
}

try (InputStream input = file.getInputStream()) {
    ExcelBuilder.read(input, UserExcelRow.class)
            .listener((row, context) -> userImportService.addUser(row))
            .doRead();
}
```

两种回调均在转换与校验之后执行。回调模式默认不保留完整成功列表，`getList()` 为空，通过成功和失败计数查看统计。若业务确实需要同时保留数据，可在配置回调后显式调用 `retainRows(true)`。

每张表结束时提交剩余批次，批次不跨 Sheet。业务回调抛异常会终止整个导入，不会被当作普通数据错误吞掉。**本模块不定义数据库事务边界**：批次已提交后，后续错误不会自动撤销之前批次。需要全量原子性时，应在业务服务定义事务，或先完成完整解析校验再进入写入事务。

解析成功不等于数据已入库或事务已提交。权限、唯一性、归属校验和缓存失效继续由业务服务负责。

## 系统字典、枚举与动态选项

系统字典配置：

```java
@ExcelProperty(value = "状态", converter = ExcelMappingConverter.class)
@ExcelDictFormat(dictType = "实际字典类型编码")
private String status;
```

应用层适配器复用 `ISysDictDataService.getDictDataByType` 和已有字典缓存；同次操作内按字典类型加载一次，保持转换与下拉一致，下次操作重新获取，Excel 模块不引入长期字典缓存。

多选字段使用字符串并指定分隔符，关闭单选下拉：

```java
@ExcelProperty(value = "分类", converter = ExcelMappingConverter.class)
@ExcelDictFormat(readConverterExp = "A=类别甲,B=类别乙", separator = "|", dropdown = false)
private String categories;
```

固定表达式通过逗号拆分映射项，通过第一个等号拆分编码与文本；包含复杂标点的标签应使用系统字典。多选编码与标签不能包含所选的多值分隔符。未知编码、未知文本或重复显示文本会报错。

枚举配置：

```java
@ExcelProperty(value = "级别", converter = ExcelMappingConverter.class)
@ExcelEnumFormat(enumClass = UserLevel.class, codeField = "code", textField = "text")
private Integer level;
```

枚举需提供对应属性的 getter，也支持字段本身声明为该枚举类型。

动态下拉：

```java
@ExcelProperty("部门")
@ExcelDynamicOptions(providerClass = DepartmentExcelOptions.class)
private String department;
```

`DepartmentExcelOptions` 是业务层实现并注册的 Spring Bean，实现 `ExcelOptionsProvider#getOptions()` 返回 `List<String>`。同次操作中，同类型提供者只查询一次。动态下拉不会隐式转换部门名称与 ID；需要转换时使用自定义转换器。Excel 数据校验可以被用户绕过，服务端仍需校验业务有效性和权限。

## 级联下拉与合并

```java
Map<String, List<String>> departments = new LinkedHashMap<>();
departments.put("研发中心", List.of("平台组", "应用组"));
departments.put("运营中心", List.of("客户组", "市场组"));

ExcelBuilder.of(rows, OrganizationExcelRow.class)
        .options(List.of(DropDownOptions.linked("department", "team", departments)))
        .dropdownRows(5_000)
        .toResponse(response);
```

普通下拉使用 `DropDownOptions.of("字段名", options)`。按最终导出字段定位，支持列重排。被排除的父字段不生成规则；保留父字段时，级联子字段必须同时导出。级联当前支持两级，同一子列只能配置一次。父标签通过内部命名区域映射，允许中文、数字开头、空格和标点。为符合 Excel 查找行为，同一层级不能出现重复或仅大小写不同的标签。

同一字段的显式选项覆盖注解选项；级联子字段不会再生成普通下拉规则。数据范围从实际表头结束处开始，不把多级表头当作数据。修改父选项后，Excel 不会自动清空原子选项，导入业务应校验父子关系。

```java
@ExcelProperty({"组织信息", "部门"})
@CellMerge(mergeBy = "company")
private String department;
```

调用 `.merge()` 开启。仅相邻、非空、原始值相同且所有依赖字段相同的数据会合并；空白中断分组。无需预排序，但要合并的记录应由业务按分组顺序排列。依赖字段须包含在导出列中。处理器按实际数据行定位，支持无表头、多级表头和跨批次写入。合并后后续行的重复单元格被清空，供重新导入的明细模板应避免合并业务字段。

## 模板填充

模板放在应用的 `src/main/resources/excel` 下：

```java
ExcelBuilder.template("excel/report.xlsx")
        .filename("月度报表")
        .data(Map.of("title", reportTitle, "total", totalAmount))
        .list("users", userRows)
        .list("departments", departmentRows)
        .toResponse(response);
```

占位符约定：

- 普通变量：`{title}`。
- 无前缀列表：`{.username}`，调用 `.data(rows)`，整个列表一次传给填充器。
- 命名列表：`{users.username}`，调用 `.list("users", rows)`。
- 多表：`.sheet(0).list("users", rows).sheet(1).data(summary)`。
- `.multiList(map)` 将集合值当作命名列表，其他键值当作普通变量；`.multiSheet(listOfMaps)` 按顺序应用到模板中的各 Sheet。
- `.horizontal(true)` 设置后续列表横向填充。
- `.forceNewRow(true)` 默认插入新行，适合列表下方还有其他内容的模板；它可能增加内存消耗。大型明细优先使用普通流式导出。
- 不配置任何填充操作时可以输出原模板。

也可传入 Spring `Resource`，例如 `new FileSystemResource(path)`。模板流由模块关闭；输出流由调用者管理。

## 默认配置与扩展

```yaml
whim:
  excel:
    max-import-rows: 100000
    max-errors: 100
    batch-size: 1000
    dropdown-rows: 1000
```

无需添加 YAML 即可使用默认值。构造器上的显式设置优先。

自定义转换器使用 `registerConverter(converter)`；写处理器使用 `registerWriteHandler(CustomHandler::new)`。处理器以工厂形式注册，ZIP 每个工作簿都会取得独立实例；自定义转换器也应避免跨文件保存可变状态。

`Long`、`BigInteger`、`BigDecimal` 超过十五位有效数字时按文本导出，保留原值；在 Excel 中需要运算的金额字段应根据业务精度选择格式或自定义转换器。读取整数时拒绝小数截断与数值溢出。

所有构造器都只能执行一次，不应缓存或跨线程共享。输入流和输出流的所有权始终归调用者；文件与模板方法仅关闭自己打开的流。模块使用 JDK 25 正式版 `ScopedValue` 管理操作内字典快照，不需要开启预览特性。

该模块提供基础能力，没有新增或修改业务 HTTP 接口，因此不涉及 Apifox 契约同步。
