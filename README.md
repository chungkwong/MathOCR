# MathOCR : A printed scientific document recognition system

**Warning: MathOCR is still in pre-alpha stage, recognition result may not be good enough for practical purpose.**

MathOCR is a printed scientific document recognition system written in pure Java.
MathOCR has the functionality of image preprocessing, layout analysis and character recognition,
especially the ability to recognize mathematical expression.
MathOCR can work do not depends on other OCR software.

MathOCR is released under the terms of GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

## Technique summary

1. For each input image(page):
	1. Convert input image into gray-scale image
	2. Optionally apply filter(s):
		- Mean value filter
		- Median value filter
	3. Convert gray-scale image into binarized image using one of:
		- Predefined threhold value
		- Global threhold value by Otsu method
		- Adaptive threhold by Sauvola method
	4. Optionally apply filter(s)：
		- kFill
		- Invert
		- Dilation
		- Erosion
	5. Detect and fix skew:
		- Piecewise painting method
		- Piecewise covering method
		- Projection
		- Crossing number
		- Hough transform
		- Cross-corrlation
		- Nearest neighbors clustering
	6. Connected component analysis
	7. Optionally remove component that is too small or too close to page border
	8. Page segmentation:
		- Recursive XY cut
	9. Reading order sort
	10. Classify each block into text, image or table
	11. For each text block
		1. Split it into text line:
			- Projection
		2. Recognize each line:
			- Built-in OCR engine:
				1. Split text line into character：
					- Projection
				2. Recognize each character:
					- SVM
					- MSE
				3. Structural analysis
			- External OCR engine
				- Tesseract
				- GOCR
				- Ocrad
				- Baidu cloud
		3. Classify line into paragraph, heading, etc.:
			- Alignment inside block
2. Merge the recognition result of the pages
3. Export to specified output format, for example：
	- Plain text
	- HTML+MathML
	- LaTeX

## History

### Working on (Starting from 2018-07)

After three year, MathOCR is being largely rewritten:
- Maven is used as build tool
- Better modulity: layout, segmentation, output format can be pluged in
- A SVM recognition algorithm based on directional feature is provided
- HTML+MathML output is supported

### MathOCR 0.0.3 released(2015-05-07)

Major changes:

*   Logical layout analysis functionality is added
*   New structural analysis algorithm for mathematical expression
*   Output format can be LaTeX or HTML
*   New graphics user interface
*   A build-in command line interface
*   Image format PNM is supported

### MathOCR 0.0.2 released(2014-11-29)

Minor changes to improve structural analysis algorithm.

### MathOCR 0.0.1 released(2014-9-29)

This is the first release of MathOCR, features:

*   Input formats: PNG,JPEG,GIF,BMP
*   Output format: LaTeX
*   GUI provided
*   Basic image preprocessing tools
*   Original character recognition system for mathematics symbol
*   Possible to extend symbol set by user
*   Original structural analysis system using bottom-up approach

# MathOCR：一个印刷体科技文档识别系统

**警告：MathOCR仍处于准预览阶段，识别效果对于实用目的而言可能远不足够.**

MathOCR是一个用Java语言编写的印刷体科技文档识别系统。
MathOCR具备基本的图形预处理、版面分析和字符识别能力，特别是能够识别数学公式。
MathOCR不依赖于其它OCR软件。

MathOCR在GNU通用公共许可证版本3或（按你的意愿）更新版本下发布。

## 工作原理

1. 对于每一页：
	1. 把图像灰度化
	2. 可选的图像预处理，目前支持：
		- 均值滤波
		- 中值滤波
	3. 二值化，目前支持
		- 固定阀值
		- Otsu方法
		- Sauvola方法
	4. 可选的图像预处理，目前支持：
		- kFill
		- 反转黑底白字
	5. 检测和修正倾斜，目前支持：
		- 分片填涂方法
		- 分片覆盖方法
		- 投影方法
		- 交错数法
		- 霍夫变换方法
		- 行间相关法
		- 最近邻方法
	6. 连通域分析
	7. 可选地去除边缘或过小的连通域
	8. 版面分割，目前支持：
		- 递归XY切分
	9. 对各块进行排序以模拟阅读顺序
	10. 判定各块分别是文本、图像还是表格
	11. 对于文本块
		1. 把文本块切分为行，日前支持：
			- 投影
		2. 识别各行的内容，目前支持：
			- 内置识别器
				1. 字符切分，目前支持：
					- 连通域分析
				2. 单字符识别，目前支持：
					- SVM
					- 距离
				3. 结构分析（有待完善，如支持数学公式和语言模型）
			- 外部识别器
				- Tesseract
				- GOCR
				- Ocrad
				- 百度云
		3. 把文本行组合为段落、标题等等，目前支持：
			- 块内对齐
2. 合并各页
3. 导出为指定输出格式（有待美化），目前部分支持：
	- 纯文本
	- HTML+MathML
	- LaTeX

## 历史

### 重新上路（2018年7月以来）

在荒废三年之后，MathOCR正在进行大幅度的重写：
- 使用Maven作为构建工具
- 采用更模块化的设计，可方便地插入版面分析算法、字符分割算法和输出格式等等
- 提供基于方向特征的支持向量机字符识别方法
- 支持HTML+MathML输出

### MathOCR 0.0.3 发布（2015-05-07）

这个版本有较大改动，包括：

*   加入文档版面分析功能
*   新的数学公式结构分析算法
*   文档识别结果可输出为LaTeX或HTML格式
*   新的图形用户界面
*   内置命令行界面
*   新增支持图片格式PNM

### MathOCR 0.0.2 发布（2014-11-29）

这个版本主要是对数学公式结构分析算法作出了局部的改进。

### MathOCR 0.0.1 发布（2014-9-29）

这是MathOCR的首个公开发布的版本，它的特性包括：

*   可接受输入格式包括PNG,JPEG,GIF,BMP
*   输出格式为LaTeX
*   提供图形用户界面
*   基本的图形预处理
*   原创的数学符号识别系统
*   用户可自行扩充支持的符号集
*   原创的结构分析系统