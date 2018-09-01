# MathOCR

_A printed scientific document recognition system_

**Warning: MathOCR is still in pre-alpha stage, recognition result may not be good enough for practical purpose.**

## Introduction

MathOCR is a printed scientific document recognition system written in pure Java, it is released under the terms of GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

MathOCR has the functionality of image preprocessing, layout analysis and character recognition, especially the ability to recognize mathematical expression. MathOCR can work without dependency on external libraries other than the standard Java distribution, however, it can also be used as a front-end to OCR system like [Tesseract](https://code.google.com/p/tesseract-ocr/), [GNU Ocrad](http://www.gnu.org/software/ocrad/ocrad.html) or [GOCR](http://jocr.sourceforge.net/).

MathOCR project is started at March 2014 as a undergraduate research project to develop a printed mathematical formula recognition system in Sun Yat-Sen University, it was first released at September 2014. Later on, it continued development as the project of the undergraduate's thesis of the developer and became a document recognition system.

## Release notes

### MathOCR 0.0.3 released[2015-05-07]

Major changes:

*   Logical layout analysis functionality is added
*   New structural analysis algorithm for mathematical expression
*   Output format can be LaTeX or HTML
*   New graphics user interface
*   A build-in command line interface
*   Image format PNM is supported

### MathOCR 0.0.2 released[2014-11-29]

Minor changes to improve structural analysis algorithm.

### MathOCR 0.0.1 released[2014-9-29]

This is the first release of MathOCR, features:

*   Input formats: PNG,JPEG,GIF,BMP
*   Output format: LaTeX
*   GUI provided
*   Basic image preprocessing tools
*   Original character recognition system for mathematics symbol
*   Possible to extend symbol set by user
*   Original structural analysis system using bottom-up approach

## Technique summary

### Image preprocessing

Standard approaches is used, these are the procedures:

1.  Convert input image into gray-scale image
2.  Convert gray-scale image into binarized image
3.  Apply filter(s) (optional)
4.  Skew detection and correction(optional)

### Layout analysis

These are the procedures:

1.  Connected components analysis based on disjoint-set data structure
2.  Page segmentation based recursive XY-cut
3.  Reading order sort based on topology sort
4.  Text-Graphics classification using components' height
5.  Extract text line using projection
6.  Logical block classification using alignment and OCR result
7.  Paragraph growing using alignment

### Optical character recognition

These are the normal procedures:

1.  Construct initial list of candidates for each glyph
2.  Use a sequence of matchers to filter out some candidates
3.  Template matching based on Hausdorff distance is used to rank the remaining candidates
4.  Combine glyphs to form character

To match special symbols like root sign and big delimiter, template is generated dynamically.

### Optical formula recognition

These are the procedures:

1.  Fix some mis-recognition using the information from other symbols
2.  Construct a initial symbol adjoin graph
3.  Rewrite the symbol adjoin graph using some rules
4.  If the graph cannot be reduced to only one vertex, recognition fail

## Acknowledge

The default data files bundled with MathOCR are derive works of amsfonts by American Mathematical Society. The code used to read PNM files is derive from the JAI library.

In addition, I would like to thank my supervisor Dr. Peixing Li, this program would not be here without his encouragement.



# MathOCR

_一个印刷体科技文档识别系统_

**警告：MathOCR仍处于准预览阶段，识别效果对于实用目的而言可能并不足够.**

## MathOCR简介

MathOCR是一个用Java语言编写的印刷体科技文档识别系统，在GNU通用公共许可证版本3或（按你的意愿）更新版本下发布。

MathOCR具备基本的图形预处理、版面分析和字符识别能力，特别是能够识别数学公式。MathOCR可以不依赖于标准Java库以外的库而独立工作，但也可以作为Tesseract、GNU Ocrad或GOCR等OCR系统的前端。

MathOCR项目在2014年作为中山大学大学生创新训练计划项目《图片中数学公式的自动识别》的副产物而于2014年3月开始开发，同年9月发布首个版本，是少有的作为自由软件的印刷体数学公式识别系统。其后，在2014年12月至2015年4月又作为开发者的本科毕业论文项目加入了文档逻辑版面分析功能，从而扩展为一个印刷体科技文档识别系统。

## 发行注记

### MathOCR 0.0.3 发布[2015-05-07]

这个版本有较大改动，包括：

*   加入文档版面分析功能
*   新的数学公式结构分析算法
*   文档识别结果可输出为LaTeX或HTML格式
*   新的图形用户界面
*   内置命令行界面
*   新增支持图片格式PNM

### MathOCR 0.0.2 发布[2014-11-29]

这个版本主要是对数学公式结构分析算法作出了局部的改进。

### MathOCR 0.0.1 发布[2014-9-29]

这是MathOCR的首个公开发布的版本，它的特性包括：

*   可接受输入格式包括PNG,JPEG,GIF,BMP
*   输出格式为LaTeX
*   提供图形用户界面
*   基本的图形预处理
*   原创的数学符号识别系统
*   用户可自行扩充支持的符号集
*   原创的结构分析系统

## 技术参考

主要技术的描述可参考以下文档：

*   [印刷体科技文档识别技术实践研究](https://github.com/chungkwong/MathOCR/blob/master/doc/MathOCR-report-0.0.3.pdf)描述了MathOCR 0.0.3的设计，这也是开发者的本科毕业论文
*   [图片中印刷体数学公式的自动识别](https://github.com/chungkwong/MathOCR/blob/master/doc/MathOCR-report-0.0.2.pdf)描述了MathOCR 0.0.2的设计
*   [图片中印刷体数学公式的自动识别——MathOCR的设计与实现](https://github.com/chungkwong/MathOCR/blob/master/doc/MathOCR-present-0.0.2.pdf)简单介绍了MathOCR 0.0.2的设计，这也是开发者的创新训练计划答辩用幻灯片

产生MathOCR 0.0.3自带识别数据的训练数据在font目录获取。

## 致谢

MathOCR自带的数据文件为美国数学学会ams­fonts的派生品，而用来读入PNM文件的代码取自JAI库，特此致谢。

此外，还要感谢我的本科毕业论文导师黎培兴老师，正是他的鼓励使这个程序从构想变为现实。
