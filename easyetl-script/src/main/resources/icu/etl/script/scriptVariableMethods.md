# print

使用脚本引擎标准输出接口输出变量值

## 语法

```
variableName.print()
```

## 参数

无参数

## 返回值

无



# []

返回字符串指定位置的字符，返回数组指定位置的元素

## 语法

```
variableName[index]
```

## 参数

参数名：index，类型：整数，范围：大于等于零且小于字符串长度或数组长度

## 返回值

字符串或数组元素



# substr

截取字符串或数组

## 语法

```javascript
variableName.substr(start, end)
```

```javascript
variableName.substr(start)
```

## 参数

参数名: start  类型: 整数  范围: 大于等于零 小于字符串长度或数组长度 

参数名: end    类型: 整数  范围: 大于等于零切小于等于 start 参数，截取后的值不包含 end 位置上的值

## 返回值

返回截取后的字符串或数组



# trim

删除字符串或数组中字符串二端的空白字符

## 语法

```javascript
variableName.trim()
```

## 参数

无参数

## 返回值

返回字符串或数组



# ltrim

删除字符串或数组中字符串左边的空白字符

## 语法

```javascript
variableName.ltrim()
```

## 参数

无参数

## 返回值

返回字符串或数组



# rtrim

删除字符串或数组中字符串右边的空白字符

## 语法

```javascript
variableName.rtrim()
```

## 参数

无参数

## 返回值

返回字符串或数组



# length

返回字符串或数组的长度

## 语法

```
variableName.length()
```

## 参数

无参数

## 返回值

```
返回整数
```



# upper

将字符串中的英文字符转为大写字符

## 语法

```
variableName.upper()
```

## 参数

无参数

## 返回值

返回字符串



# lower

将字符串中的英文字符转为小写字符

## 语法

```
variableName.lower()
```

## 参数

无参数

## 返回值

返回字符串



# split

使用分隔符参数与转义字符参数对字符串进行分割

## 语法

```javascript
variableName.split()
```

```javascript
variableName.split(delimiter)
```

```javascript
variableName.split(delimiter, escape)
```

## 参数

无参数时，表示默认使用空白字符串作为分隔符分割字符串 
参数名：delimiter  类型：字符串  范围：不能是空白字符 
参数名：escape     类型：字符串  范围：只能是非空的单字符

## 返回值

分割之后的字符串数组



# getfilename

返回文件名（不包含文件目录）

## 语法

```
stringVariableName.getFilename()
```

## 参数

无参数

## 返回值

文件名字符串



# getfilelineseparator

返回文件中的行间分隔符

## 语法

```
stringVariableName.getFileLineSeparator()
```

## 参数

无参数

## 返回值

文件的行间分隔符



# getfileext

返回文件名中的扩展名

## 语法

```
stringVariableName.getFileExt()
```

## 参数

无参数

## 返回值

文件名扩展名字符串



# getfilenamenoext

返回文件名，但不包含扩展名; 文件扩展名: txt 或 exe 或 zip 等

## 语法

```
stringVariableName.getFilenameNoExt()
```

## 参数

无参数

## 返回值

文件名字符串



# getfilesuffix

返回文件名的后缀

## 语法

```
stringVariableName.getFileSuffix()
```

## 参数

无参数

## 返回值

文件名后缀字符串



# getfilenamenosuffix

返回文件名，但不包含文件名后缀; 文件名后缀: tar.gz 或 txt 或 exe

## 语法

```
stringVariableName.getFilenameNoSuffix()
```

## 参数

无参数

## 返回值

文件名字符串



# getparent

返回文件的父目录的绝对路径

## 语法

```
stringVariableName.getParent()
```

## 参数

无参数

## 返回值

父目录的绝对路径字符串



# deletefile

删除文件或目录

## 语法

```
stringVariableName.deleteFile()
```

## 参数

无参数

## 返回值

true表示删除文件或目录成功



# existsfile

判断文件或目录是否存在

## 语法

```
stringVariableName.existsFile()
```

## 参数

无参数

## 返回值

true表示文件或目录存在



# isfile

判断文件是否存在

## 语法

```
stringVariableName.isFile()
```

## 参数

无参数

## 返回值

true表示文件存在



# isdirectory

判断目录是否存在

## 语法

```
stringVariableName.isDirectory()
```

## 参数

无参数

## 返回值

true表示目录存在



# mkdir

创建目录

## 语法

```
stringVariableName.mkdir()
```

## 参数

无参数

## 返回值

true表示创建目录成功



# touch

创建文件

## 语法

```
stringVariableName.touch()
```

## 参数

无参数

## 返回值

true表示创建文件成功




# ls

显示文件详细信息

## 语法

```
stringVariableName.ls()
```

## 参数

无参数

## 返回值

文件详细信息字符串



# format

将日期变量使用 pattern 格式格式化并输出字符串

将日期字符串变量转为日期并按参数 pattern 格式输出日期信息

## 语法

```javascript
dateVariableName.format('yyyy-MM-dd')
```

```javascript
stringVariableName.format(yyyy-MM-dd)
```

## 参数

参数名：pattern  类型：字符串  范围：日期正则表达式

## 返回值

日期时间字符串



# indexof

搜索字符串参数string 在字符串或字符串数组中首次出现的位置

## 语法

```
variableName.indexOf(string, from) or variableName.indexOf(string)
```

## 参数

参数名: string  类型: 字符串  范围: 必填且不能是null或空字符串 
参数名: from    类型: 整数    范围: 开始搜索的起始位置，选填且大于等于零且小于字符串长度或数组长度

## 返回值

字符串参数首次出现的位置，从0开始，-1表示未找到



# getyear

返回日期的年份

## 语法

```
dateVariableName.getYear()
```

## 参数

无参数

## 返回值

整数



# getmonth

返回日期的月份

## 语法

```
dateVariableName.getmonth()
```

## 参数

无参数

## 返回值

1-12



# getday

返回日期的天数

## 语法

```
dateVariableName.getDay()
```

## 参数

无参数

## 返回值

1-31



# getdays

返回日期从1970年1月1日零点开始直到日期时点的天数

## 语法

```
dateVariableName.getDays()
```

## 参数

无参数

## 返回值

正整数



# gethour

返回日期时间的小时数

## 语法

```
timeVariableName.getHour()
```

## 参数

无参数

## 返回值

0-23



# getminute

返回日期时间的分钟数

## 语法

```
timeVariableName.getMinute()
```

## 参数

无参数

## 返回值

0-59



# getsecond

返回日期时间的秒数

## 语法

```
timeVariableName.getSecond()
```

## 参数

无参数

## 返回值

0-59



# getmillis

返回日期时间的毫秒数

## 语法

```
timeVariableName.getMillis()
```

## 参数

无参数

## 返回值

0-999



# int

将字符串转为整数

## 语法

```
strVariableName.int()
```

## 参数

无参数

## 返回值

整数



# exists

判断数组或集合中是否包含指定变量

## 语法

```javascript
array.exists(variableName)
```

```javascript
collection.exists(variableName)
```

## 参数

变量名

## 返回值

boolean类型