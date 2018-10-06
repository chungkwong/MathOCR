# MathOCR : A printed scientific document recognition system

**Warning: MathOCR is still in pre-alpha stage, recognition result may not be good enough for practical purpose.**

MathOCR is a printed scientific document recognition system written in pure Java.
MathOCR has the functionality of image preprocessing, layout analysis and character recognition,
especially the ability to recognize mathematical expression.
MathOCR can work do not depends on other OCR software.

MathOCR is released under the terms of GNU Affero General Public License
as published by the Free Software Foundation, either version 3 of the License,
or (at your option) any later version.

## Usage

Java 8 or above is required.

### Prebuilt JAR

A prebuilt JAR is available at https://oss.sonatype.org/content/repositories/snapshots/com/github/chungkwong/mathocr/1.0-SNAPSHOT

### Build from source

1. Clone the project: `git clone https://github.com/chungkwong/MathOCR.git`
2. Get into the directory: `cd MathOCR`
3. Edit the file `src/test/resources/com/github/chungkwong/mathocr/text/math_train_set.xml`
    - Ensure that path to the fonts are correct
    - Ensure all the characters needed to be recognized are listed
4. Train the model: `mvn exec:java -Dexec.mainClass="com.github.chungkwong.mathocr.character.ModelBuilder" -Dexec.classpathScope="test"`
5. Link or copy the model to the project:
    - `ln -s ~/.mathocr/default/ src/main/resources/com/github/chungkwong/mathocr/character/default`
    - `ln -s ~/.mathocr/default_small/ src/main/resources/com/github/chungkwong/mathocr/character/default_small`
6. Build the JAR: `mvn package`

### Run

Run the JAR file by double click or something like `java -jar mathocr-1.0-SNAPSHOT.jar`,
then a GUI interface should appear.

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
                    - Connected component
                    - Predictive(For fixed width characters)
                2. Extract features
                    - Aspect ratio
                    - Moments
                    - Density
                    - Gradient
                    - Crossing
				3. Recognize each character:
					- SVM
					- MSE
				4. Structural analysis
                    - Recursive XY cut
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

MathOCR在GNU Affero通用公共许可证版本3或（按你的意愿）更新版本下发布。

## 用法

请确保已安装好Java 8或以上。

### 二进制包

有一个已经构建好的JAR可以下载： https://oss.sonatype.org/content/repositories/snapshots/com/github/chungkwong/mathocr/1.0-SNAPSHOT

### 从源构建

1. 克隆项目： `https://github.com/chungkwong/MathOCR.git`
2. 进入项目目录： `cd MathOCR`
3. 编辑文件 `src/test/resources/com/github/chungkwong/mathocr/text/math_train_set.xml`
    - 确保训练用字体路径正确
    - 确保需要识别的字符都已列出
4. 训练模型： `mvn exec:java -Dexec.mainClass="com.github.chungkwong.mathocr.character.ModelBuilder" -Dexec.classpathScope="test"`
5. 把模型连接或复制到项目:
    - `ln -s ~/.mathocr/default/ src/main/resources/com/github/chungkwong/mathocr/character/default`
    - `ln -s ~/.mathocr/default_small/ src/main/resources/com/github/chungkwong/mathocr/character/default_small`
6. 构建JAR: `mvn package`

### 运行

通过双击或命令如`java -jar mathocr-1.0-SNAPSHOT.jar`运行GUI用户界面。

### 准确程度

日前来说准确程度并不能有太高期望。

- 对于低分辨率图片（包括通常字体大小的屏幕截图）结果可能纯属垃圾。
- 对于噪声比较大的图片结果可能纯属垃圾，轻微光照不均的浅色的背景倒可以接受。
- 目前默认字符切分用连通域分割，基本上不能处理分体字符（如i）或粘连字符，
目前适用于中文的算法虽然能处理这些情况但不适用于数学公式。
- 在InftyCDB1数据集上，不用上下文和统计信息，单字符识别准确率可达92%，
把`S`和`s`等形状雷同字符视为相同的话可达96%。
对于宽度或高度特别小的字符如圆点或竖线特别容易出错。
- 在InftyCDB1数据集上，上下标判定的准确率可达98.57%。
- 在IM2LATEX-100K数据集上，BLEU值仅约为10，但这个低BLEU值很大程度上来源于图片的低分辨率，
在同样这些公式但更大一些的图片可取得超过50的BLEU。

根据阁下的应用场景，可以作以下调整以平衡准确度和时空开销：
- 通过把字符分类器设为一对一的SVM而非默认的一对多LINEAR往往可以提高准确率但模型需要占用
更大空间，识别速度也会明显下降。
- 把待识别的字符集设为以场景容许的字符集以避免识别不出一些字符或识别出不合法的字符。

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
			- 内置识别器（一个用于区分普通字符，另外一个用于句号和逗号等小符号）
				1. 字符切分，目前支持：
					- 连通域分析
					- 投影
					- 预测（只适用于字符等宽的普通行）
				2. 提取字符特征，日前支持：
					- 网格方向特征（轮廓或内部）
					- 宽高比
					- 网格密度
					- 低阶矩
					- 穿线数
				3. 单字符识别，目前支持：
					- SVM（线性或RBF核）
					- 距离
				4. 结构分析（有待完善，如支持语言模型），目前支持：
					- 递归XY切分
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